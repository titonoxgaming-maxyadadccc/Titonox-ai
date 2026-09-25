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
 * Real Anthropic Claude Messages API Provider.
 *
 * Implements native Claude Messages spec (https://api.anthropic.com/v1/messages).
 * Supports Claude 3.5 Sonnet, Haiku, and Opus with vision and streaming.
 */
class AnthropicProvider : AIProvider {

    override val providerType: AIProviderType = AIProviderType.ANTHROPIC
    private val TAG = "AnthropicProvider"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getClient(timeoutSeconds: Int): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxDim = 1024
        val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else 1.0f

        val targetW = (bitmap.width * scale).toInt()
        val targetH = (bitmap.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun buildMessagesJson(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        screenBitmap: Bitmap?
    ): JSONArray {
        val messages = JSONArray()

        for (msg in conversationHistory.takeLast(10)) {
            val obj = JSONObject()
            obj.put("role", if (msg.role == "user") "user" else "assistant")
            obj.put("content", msg.text)
            messages.put(obj)
        }

        val userObj = JSONObject()
        userObj.put("role", "user")

        if (screenBitmap != null) {
            val contentArray = JSONArray()
            val textPart = JSONObject()
            textPart.put("type", "text")
            textPart.put("text", prompt)
            contentArray.put(textPart)

            val imgPart = JSONObject()
            imgPart.put("type", "image")
            val sourceObj = JSONObject()
            sourceObj.put("type", "base64")
            sourceObj.put("media_type", "image/jpeg")
            sourceObj.put("data", bitmapToBase64(screenBitmap))
            imgPart.put("source", sourceObj)
            contentArray.put(imgPart)

            userObj.put("content", contentArray)
        } else {
            userObj.put("content", prompt)
        }

        messages.put(userObj)
        return messages
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
                text = "Sir, Anthropic API Key is not configured. Please enter your key in Models screen.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        val baseUrl = config.baseUrl.ifBlank { "https://api.anthropic.com/v1" }
        val endpoint = baseUrl.removeSuffix("/") + "/messages"
        val model = config.model.ifBlank { "claude-3-5-sonnet-20241022" }

        val systemBuilder = StringBuilder(config.systemInstruction.ifBlank { AIConfig.DEFAULT_SYSTEM_INSTRUCTION })
        if (userMemories.isNotEmpty()) {
            systemBuilder.append("\n\n[USER RECALLED MEMORIES & PREFERENCES]:\n")
            userMemories.forEach { systemBuilder.append("• ").append(it).append("\n") }
        }

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            bodyJson.put("max_tokens", config.maxOutputTokens)
            bodyJson.put("system", systemBuilder.toString())
            bodyJson.put("messages", buildMessagesJson(prompt, conversationHistory, screenBitmap))
            bodyJson.put("temperature", config.temperature.toDouble())
            bodyJson.put("stream", false)

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Anthropic error ($code): $body")
                val cat = when (code) {
                    401 -> ErrorCategory.INVALID_API_KEY
                    403 -> ErrorCategory.PERMISSION_DENIED
                    404 -> ErrorCategory.MODEL_UNAVAILABLE
                    429 -> ErrorCategory.QUOTA_EXCEEDED
                    else -> ErrorCategory.SERVER_ERROR
                }
                return@withContext AIResult(
                    isSuccess = false,
                    text = "Claude request failed ($code): $body",
                    rawResponse = body,
                    errorCategory = cat
                )
            }

            val json = JSONObject(body)
            val contentArray = json.optJSONArray("content")
            val textBuilder = StringBuilder()
            if (contentArray != null) {
                for (i in 0 until contentArray.length()) {
                    val block = contentArray.optJSONObject(i)
                    if (block?.optString("type") == "text") {
                        textBuilder.append(block.optString("text"))
                    }
                }
            }

            AIResult(
                isSuccess = true,
                text = textBuilder.toString(),
                rawResponse = body
            )
        } catch (e: Exception) {
            AIResult(
                isSuccess = false,
                text = e.localizedMessage ?: "Unknown Claude error",
                errorCategory = ErrorCategory.UNKNOWN_ERROR
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
            val msg = "Sir, Anthropic API Key is not configured."
            onChunk(msg)
            return@withContext AIResult(isSuccess = false, text = msg, errorCategory = ErrorCategory.NO_API_KEY)
        }

        val baseUrl = config.baseUrl.ifBlank { "https://api.anthropic.com/v1" }
        val endpoint = baseUrl.removeSuffix("/") + "/messages"
        val model = config.model.ifBlank { "claude-3-5-sonnet-20241022" }

        val systemBuilder = StringBuilder(config.systemInstruction.ifBlank { AIConfig.DEFAULT_SYSTEM_INSTRUCTION })
        if (userMemories.isNotEmpty()) {
            systemBuilder.append("\n\n[USER RECALLED MEMORIES]:\n")
            userMemories.forEach { systemBuilder.append("• ").append(it).append("\n") }
        }

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            bodyJson.put("max_tokens", config.maxOutputTokens)
            bodyJson.put("system", systemBuilder.toString())
            bodyJson.put("messages", buildMessagesJson(prompt, conversationHistory, screenBitmap))
            bodyJson.put("temperature", config.temperature.toDouble())
            bodyJson.put("stream", true)

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val msg = "Claude streaming failed (${response.code})"
                onChunk(msg)
                return@withContext AIResult(isSuccess = false, text = msg, errorCategory = ErrorCategory.SERVER_ERROR)
            }

            val body = response.body ?: return@withContext AIResult(isSuccess = false, text = "Empty response")
            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            val accumulated = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val cur = line?.trim() ?: continue
                if (cur.startsWith("data:")) {
                    val data = cur.removePrefix("data:").trim()
                    try {
                        val json = JSONObject(data)
                        val type = json.optString("type")
                        if (type == "content_block_delta") {
                            val delta = json.optJSONObject("delta")
                            val text = delta?.optString("text")
                            if (!text.isNullOrEmpty()) {
                                accumulated.append(text)
                                onChunk(text)
                            }
                        }
                    } catch (e: Exception) {
                        // ignore malformed
                    }
                }
            }

            AIResult(isSuccess = true, text = accumulated.toString())
        } catch (e: Exception) {
            AIResult(isSuccess = false, text = e.localizedMessage ?: "Stream error")
        }
    }

    override suspend fun testConnection(config: AIConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) {
            return@withContext ConnectionTestResult(
                success = false,
                message = "Anthropic API Key is missing.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        val baseUrl = config.baseUrl.ifBlank { "https://api.anthropic.com/v1" }
        val endpoint = baseUrl.removeSuffix("/") + "/messages"
        val model = config.model.ifBlank { "claude-3-5-haiku-20241022" }
        val startTime = System.currentTimeMillis()

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            bodyJson.put("max_tokens", 1)
            val msgs = JSONArray()
            val msg = JSONObject()
            msg.put("role", "user")
            msg.put("content", "ping")
            msgs.put(msg)
            bodyJson.put("messages", msgs)

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val client = getClient(15)
            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ConnectionTestResult(
                    success = true,
                    message = "Connected to Claude successfully (${latency}ms)",
                    latencyMs = latency,
                    verifiedModel = model
                )
            } else {
                val cat = when (response.code) {
                    401 -> ErrorCategory.INVALID_API_KEY
                    403 -> ErrorCategory.PERMISSION_DENIED
                    404 -> ErrorCategory.MODEL_UNAVAILABLE
                    429 -> ErrorCategory.QUOTA_EXCEEDED
                    else -> ErrorCategory.SERVER_ERROR
                }
                ConnectionTestResult(
                    success = false,
                    message = "Anthropic returned HTTP ${response.code}",
                    errorCategory = cat,
                    latencyMs = latency
                )
            }
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                message = e.localizedMessage ?: "Connection error",
                errorCategory = ErrorCategory.UNKNOWN_ERROR
            )
        }
    }
}
