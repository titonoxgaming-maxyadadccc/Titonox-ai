package com.example.ai.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.ai.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Real Google Gemini API Provider implementation.
 *
 * Supports:
 * - Dynamic configurable models (gemini-2.0-flash, gemini-1.5-flash, gemini-1.5-pro, etc.)
 * - Streaming responses with Server-Sent Events (SSE)
 * - Multimodal screen image inputs
 * - Precise categorized error handling (Quota, Invalid key, Network, Model unavailable)
 * - Real API connection verification
 */
class GeminiProvider : AIProvider {

    override val providerType: AIProviderType = AIProviderType.GEMINI

    private val TAG = "GeminiProvider"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getClient(timeoutSeconds: Int): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
    }

    override suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String>,
        screenBitmap: Bitmap?
    ): AIResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) {
            return@withContext AIResult(
                isSuccess = false,
                text = "Sir, Gemini API key configured nahi hai. Settings me jaakar API key set karein.",
                errorCategory = ErrorCategory.NO_API_KEY,
                errorMessage = "API key is missing"
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=$apiKey"

        try {
            val payload = buildPayload(prompt, conversationHistory, config, userMemories, screenBitmap)
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(request).execute()
            val responseCode = response.code
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorCat = categorizeHttpError(responseCode, responseBody)
                val friendlyMessage = getFriendlyErrorMessage(errorCat, responseCode)
                Log.e(TAG, "Gemini API error ($responseCode): $responseBody")
                return@withContext AIResult(
                    isSuccess = false,
                    text = friendlyMessage,
                    rawResponse = responseBody,
                    errorCategory = errorCat,
                    errorMessage = "HTTP $responseCode"
                )
            }

            val parsedText = extractTextFromGeminiJson(responseBody)
            AIResult(
                isSuccess = true,
                text = parsedText,
                rawResponse = responseBody
            )
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network unavailable", e)
            AIResult(
                isSuccess = false,
                text = "Sir, internet connection unavailable hai. Please check your network.",
                errorCategory = ErrorCategory.NETWORK_UNAVAILABLE,
                errorMessage = e.message
            )
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Connection timeout", e)
            AIResult(
                isSuccess = false,
                text = "Sir, Gemini API request timeout ho gaya. Please try again.",
                errorCategory = ErrorCategory.TIMEOUT,
                errorMessage = e.message
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in Gemini API call", e)
            AIResult(
                isSuccess = false,
                text = "AI Core se connect karne me dikkat aayi. Local assistant ready hai.",
                errorCategory = ErrorCategory.UNKNOWN_ERROR,
                errorMessage = e.message
            )
        }
    }

    override suspend fun streamResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String>,
        screenBitmap: Bitmap?,
        onChunk: suspend (String) -> Unit
    ): AIResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) {
            return@withContext generateResponse(prompt, conversationHistory, config, userMemories, screenBitmap)
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:streamGenerateContent?alt=sse&key=$apiKey"

        try {
            val payload = buildPayload(prompt, conversationHistory, config, userMemories, screenBitmap)
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorCat = categorizeHttpError(response.code, response.body?.string() ?: "")
                return@withContext AIResult(
                    isSuccess = false,
                    text = getFriendlyErrorMessage(errorCat, response.code),
                    errorCategory = errorCat,
                    errorMessage = "HTTP ${response.code}"
                )
            }

            val body = response.body ?: return@withContext generateResponse(prompt, conversationHistory, config, userMemories, screenBitmap)
            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            val fullTextBuilder = StringBuilder()

            var line: String? = reader.readLine()
            while (line != null) {
                if (line.startsWith("data: ")) {
                    val jsonStr = line.removePrefix("data: ").trim()
                    if (jsonStr.isNotBlank() && jsonStr != "[DONE]") {
                        try {
                            val chunkText = extractTextFromGeminiJson(jsonStr)
                            if (chunkText.isNotBlank()) {
                                fullTextBuilder.append(chunkText)
                                onChunk(chunkText)
                            }
                        } catch (ignored: Exception) {}
                    }
                }
                line = reader.readLine()
            }

            val finalOutput = fullTextBuilder.toString().trim()
            if (finalOutput.isNotEmpty()) {
                AIResult(isSuccess = true, text = finalOutput)
            } else {
                generateResponse(prompt, conversationHistory, config, userMemories, screenBitmap)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Streaming failed, falling back to standard generateResponse", e)
            generateResponse(prompt, conversationHistory, config, userMemories, screenBitmap)
        }
    }

    override suspend fun testConnection(config: AIConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                success = false,
                message = "API Key is missing. Please enter your Gemini API key.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        val startTime = System.currentTimeMillis()
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=$apiKey"

        try {
            val rootJson = JSONObject().apply {
                val contents = JSONArray().put(
                    JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", "Respond with exact word 'TITONOX_ONLINE'")))
                    }
                )
                put("contents", contents)
                put("generationConfig", JSONObject().put("maxOutputTokens", 16).put("temperature", 0.1))
            }

            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(15)
            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ConnectionTestResult(
                    success = true,
                    message = "Gemini API connected successfully (${latency}ms).",
                    latencyMs = latency,
                    verifiedModel = config.model
                )
            } else {
                val errorCat = categorizeHttpError(response.code, responseBody)
                ConnectionTestResult(
                    success = false,
                    message = "API connection failed: ${errorCat.displayName} (HTTP ${response.code}).",
                    errorCategory = errorCat,
                    latencyMs = latency
                )
            }
        } catch (e: UnknownHostException) {
            ConnectionTestResult(
                success = false,
                message = "API connection failed: Internet network unavailable.",
                errorCategory = ErrorCategory.NETWORK_UNAVAILABLE
            )
        } catch (e: SocketTimeoutException) {
            ConnectionTestResult(
                success = false,
                message = "API connection failed: Connection timed out.",
                errorCategory = ErrorCategory.TIMEOUT
            )
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                message = "API connection failed: ${e.localizedMessage ?: "Unknown error"}",
                errorCategory = ErrorCategory.UNKNOWN_ERROR
            )
        }
    }

    private fun buildPayload(
        prompt: String,
        history: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String>,
        screenBitmap: Bitmap?
    ): JSONObject {
        val root = JSONObject()

        // System Instruction
        val sysInstructionText = buildString {
            append(config.systemInstruction)
            if (userMemories.isNotEmpty()) {
                append("\n\nUser Profile & Memory Preferences:\n")
                userMemories.forEach { append("- $it\n") }
            }
        }
        root.put(
            "systemInstruction",
            JSONObject().put("parts", JSONArray().put(JSONObject().put("text", sysInstructionText)))
        )

        // Contents
        val contentsArray = JSONArray()

        // Recent conversation history (last 8 turns)
        val recentHistory = history.takeLast(8)
        for (msg in recentHistory) {
            val turn = JSONObject()
            turn.put("role", if (msg.role == "user") "user" else "model")
            turn.put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
            contentsArray.put(turn)
        }

        // Current user message
        val currentTurn = JSONObject().apply {
            put("role", "user")
            val parts = JSONArray().put(JSONObject().put("text", prompt))

            // Multimodal image
            if (screenBitmap != null) {
                val stream = ByteArrayOutputStream()
                screenBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                parts.put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject().put("mimeType", "image/jpeg").put("data", base64)
                    )
                )
            }
            put("parts", parts)
        }
        contentsArray.put(currentTurn)

        root.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", config.temperature.toDouble())
            put("maxOutputTokens", config.maxOutputTokens)
        }
        root.put("generationConfig", genConfig)

        return root
    }

    private fun extractTextFromGeminiJson(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return ""
            val first = candidates.optJSONObject(0) ?: return ""
            val content = first.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                val p = parts.optJSONObject(i)
                val text = p?.optString("text")
                if (!text.isNullOrEmpty()) {
                    sb.append(text)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            ""
        }
    }

    private fun categorizeHttpError(code: Int, body: String): ErrorCategory {
        val lower = body.lowercase()
        return when {
            code == 400 && lower.contains("api_key_invalid") -> ErrorCategory.INVALID_API_KEY
            code == 400 && lower.contains("key") -> ErrorCategory.INVALID_API_KEY
            code == 403 -> ErrorCategory.PERMISSION_DENIED
            code == 404 -> ErrorCategory.MODEL_UNAVAILABLE
            code == 429 || lower.contains("quota") || lower.contains("resource_exhausted") -> ErrorCategory.QUOTA_EXCEEDED
            code in 500..599 -> ErrorCategory.SERVER_ERROR
            else -> ErrorCategory.UNKNOWN_ERROR
        }
    }

    private fun getFriendlyErrorMessage(category: ErrorCategory, code: Int): String {
        return when (category) {
            ErrorCategory.NO_API_KEY -> "Sir, Gemini API key set nahi hai. Settings me jaakar configure karein."
            ErrorCategory.INVALID_API_KEY -> "Sir, configured Gemini API key invalid hai. Kripya check karein."
            ErrorCategory.PERMISSION_DENIED -> "Sir, API key ke paas is model ko access karne ki permission nahi hai."
            ErrorCategory.MODEL_UNAVAILABLE -> "Sir, selected Gemini model available nahi hai. Settings se doosra model choose karein."
            ErrorCategory.QUOTA_EXCEEDED -> "Sir, Gemini API quota available nahi hai. Thodi der me try karein."
            ErrorCategory.SERVER_ERROR -> "Sir, Google Gemini servers me temporary issue hai ($code)."
            ErrorCategory.TIMEOUT -> "Sir, AI server se response aane me timeout ho gaya."
            ErrorCategory.NETWORK_UNAVAILABLE -> "Sir, device offline hai. Internet connect karein."
            else -> "Sir, AI Core communication me issue aaya ($code)."
        }
    }
}
