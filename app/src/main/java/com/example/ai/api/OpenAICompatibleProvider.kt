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
 * Universal OpenAI-Compatible API Provider.
 *
 * Fully powers:
 * - OpenAI (https://api.openai.com/v1)
 * - OpenRouter (https://openrouter.ai/api/v1)
 * - Groq (https://api.groq.com/openai/v1)
 * - DeepSeek (https://api.deepseek.com)
 * - Mistral (https://api.mistral.ai/v1)
 * - xAI / Grok (https://api.x.ai/v1)
 * - Together AI (https://api.together.xyz/v1)
 * - Custom OpenAI-compatible endpoints (e.g. Local LLM / Ollama / LM Studio)
 */
class OpenAICompatibleProvider(
    override val providerType: AIProviderType
) : AIProvider {

    private val TAG = "OpenAICompatProvider"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getClient(timeoutSeconds: Int): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
    }

    private fun resolveEndpoint(baseUrl: String): String {
        val clean = baseUrl.trim().removeSuffix("/")
        return if (clean.endsWith("/chat/completions")) {
            clean
        } else if (clean.endsWith("/v1")) {
            "$clean/chat/completions"
        } else {
            "$clean/chat/completions"
        }
    }

    private fun buildMessagesJson(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String>,
        screenBitmap: Bitmap?
    ): JSONArray {
        val messages = JSONArray()

        // 1. System Prompt with User Memories
        val systemPromptBuilder = StringBuilder(config.systemInstruction.ifBlank { AIConfig.DEFAULT_SYSTEM_INSTRUCTION })
        if (userMemories.isNotEmpty()) {
            systemPromptBuilder.append("\n\n[USER RECALLED MEMORIES & PREFERENCES]:\n")
            userMemories.forEach { systemPromptBuilder.append("• ").append(it).append("\n") }
        }
        val sysObj = JSONObject()
        sysObj.put("role", "system")
        sysObj.put("content", systemPromptBuilder.toString())
        messages.put(sysObj)

        // 2. Conversation History
        for (msg in conversationHistory.takeLast(10)) {
            val historyObj = JSONObject()
            historyObj.put("role", if (msg.role == "user") "user" else "assistant")
            historyObj.put("content", msg.text)
            messages.put(historyObj)
        }

        // 3. Current User Message (Multimodal if screenBitmap provided)
        val userObj = JSONObject()
        userObj.put("role", "user")

        if (screenBitmap != null) {
            val contentArray = JSONArray()
            val textPart = JSONObject()
            textPart.put("type", "text")
            textPart.put("text", prompt)
            contentArray.put(textPart)

            val base64Img = bitmapToBase64(screenBitmap)
            val imgPart = JSONObject()
            imgPart.put("type", "image_url")
            val imgUrlObj = JSONObject()
            imgUrlObj.put("url", "data:image/jpeg;base64,$base64Img")
            imgPart.put("image_url", imgUrlObj)
            contentArray.put(imgPart)

            userObj.put("content", contentArray)
        } else {
            userObj.put("content", prompt)
        }

        messages.put(userObj)
        return messages
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Downscale to max 1024 to optimize network transit
        val maxDim = 1024
        val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
        } else 1.0f

        val targetW = (bitmap.width * scale).toInt()
        val targetH = (bitmap.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String>,
        screenBitmap: Bitmap?
    ): AIResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank() && providerType != AIProviderType.CUSTOM) {
            return@withContext AIResult(
                isSuccess = false,
                text = "Sir, ${providerType.displayName} API Key is not configured. Please configure it in Models screen.",
                errorCategory = ErrorCategory.NO_API_KEY,
                errorMessage = "API Key missing"
            )
        }

        val baseUrl = config.baseUrl.ifBlank { providerType.defaultBaseUrl }
        val endpoint = resolveEndpoint(baseUrl)
        val model = config.model.ifBlank { providerType.defaultModel }

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            bodyJson.put("messages", buildMessagesJson(prompt, conversationHistory, config, userMemories, screenBitmap))
            bodyJson.put("temperature", config.temperature.toDouble())
            bodyJson.put("max_tokens", config.maxOutputTokens)
            bodyJson.put("stream", false)

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))

            if (apiKey.isNotBlank()) {
                requestBuilder.header(config.authHeaderName.ifBlank { "Authorization" }, "Bearer $apiKey")
            }

            // OpenRouter site branding headers
            if (providerType == AIProviderType.OPENROUTER) {
                requestBuilder.header("HTTP-Referer", "https://titonox.ai")
                requestBuilder.header("X-Title", "TITONOX JARVIS")
            }

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(requestBuilder.build()).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorCat = categorizeHttpError(code, body)
                Log.e(TAG, "${providerType.displayName} error ($code): $body")
                return@withContext AIResult(
                    isSuccess = false,
                    text = "Request failed ($code): ${extractErrorMessage(body)}",
                    rawResponse = body,
                    errorCategory = errorCat,
                    errorMessage = "HTTP $code"
                )
            }

            val parsedJson = JSONObject(body)
            val choices = parsedJson.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val messageObj = firstChoice?.optJSONObject("message")
            val content = messageObj?.optString("content") ?: ""

            AIResult(
                isSuccess = true,
                text = content,
                rawResponse = body
            )
        } catch (e: UnknownHostException) {
            AIResult(
                isSuccess = false,
                text = "Network connection unavailable. Please check your internet connectivity.",
                errorCategory = ErrorCategory.NETWORK_UNAVAILABLE,
                errorMessage = e.message
            )
        } catch (e: SocketTimeoutException) {
            AIResult(
                isSuccess = false,
                text = "The AI provider timed out. You may increase the timeout limit in Settings.",
                errorCategory = ErrorCategory.TIMEOUT,
                errorMessage = e.message
            )
        } catch (e: Exception) {
            AIResult(
                isSuccess = false,
                text = "An unexpected error occurred: ${e.localizedMessage ?: "Unknown"}",
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
        if (apiKey.isBlank() && providerType != AIProviderType.CUSTOM) {
            val msg = "Sir, ${providerType.displayName} API Key is not configured."
            onChunk(msg)
            return@withContext AIResult(
                isSuccess = false,
                text = msg,
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        val baseUrl = config.baseUrl.ifBlank { providerType.defaultBaseUrl }
        val endpoint = resolveEndpoint(baseUrl)
        val model = config.model.ifBlank { providerType.defaultModel }

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            bodyJson.put("messages", buildMessagesJson(prompt, conversationHistory, config, userMemories, screenBitmap))
            bodyJson.put("temperature", config.temperature.toDouble())
            bodyJson.put("max_tokens", config.maxOutputTokens)
            bodyJson.put("stream", true)

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))

            if (apiKey.isNotBlank()) {
                requestBuilder.header(config.authHeaderName.ifBlank { "Authorization" }, "Bearer $apiKey")
            }

            if (providerType == AIProviderType.OPENROUTER) {
                requestBuilder.header("HTTP-Referer", "https://titonox.ai")
                requestBuilder.header("X-Title", "TITONOX JARVIS")
            }

            val client = getClient(config.timeoutSeconds)
            val response = client.newCall(requestBuilder.build()).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                val errorCat = categorizeHttpError(response.code, errorBody)
                val friendly = "Stream failed (${response.code}): ${extractErrorMessage(errorBody)}"
                onChunk(friendly)
                return@withContext AIResult(
                    isSuccess = false,
                    text = friendly,
                    rawResponse = errorBody,
                    errorCategory = errorCat
                )
            }

            val responseBody = response.body ?: return@withContext AIResult(
                isSuccess = false,
                text = "Empty response received",
                errorCategory = ErrorCategory.SERVER_ERROR
            )

            val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
            val accumulatedText = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (currentLine.isEmpty() || currentLine.startsWith(":")) continue

                if (currentLine.startsWith("data:")) {
                    val dataContent = currentLine.removePrefix("data:").trim()
                    if (dataContent == "[DONE]") break

                    try {
                        val chunkJson = JSONObject(dataContent)
                        val choices = chunkJson.optJSONArray("choices")
                        val firstChoice = choices?.optJSONObject(0)
                        val delta = firstChoice?.optJSONObject("delta")
                        val content = delta?.optString("content")

                        if (!content.isNullOrEmpty()) {
                            accumulatedText.append(content)
                            onChunk(content)
                        }
                    } catch (e: Exception) {
                        // ignore malformed lines
                    }
                }
            }

            AIResult(
                isSuccess = true,
                text = accumulatedText.toString()
            )
        } catch (e: Exception) {
            AIResult(
                isSuccess = false,
                text = e.localizedMessage ?: "Stream error",
                errorCategory = ErrorCategory.UNKNOWN_ERROR
            )
        }
    }

    override suspend fun testConnection(config: AIConfig): ConnectionTestResult = withContext(Dispatchers.IO) {
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank() && providerType != AIProviderType.CUSTOM) {
            return@withContext ConnectionTestResult(
                success = false,
                message = "${providerType.displayName} API Key is not set.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        val baseUrl = config.baseUrl.ifBlank { providerType.defaultBaseUrl }
        val endpoint = resolveEndpoint(baseUrl)
        val model = config.model.ifBlank { providerType.defaultModel }
        val startTime = System.currentTimeMillis()

        try {
            val bodyJson = JSONObject()
            bodyJson.put("model", model)
            val messages = JSONArray()
            val userMsg = JSONObject()
            userMsg.put("role", "user")
            userMsg.put("content", "ping")
            messages.put(userMsg)
            bodyJson.put("messages", messages)
            bodyJson.put("max_tokens", 1)

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(bodyJson.toString().toRequestBody(jsonMediaType))

            if (apiKey.isNotBlank()) {
                requestBuilder.header(config.authHeaderName.ifBlank { "Authorization" }, "Bearer $apiKey")
            }

            val client = getClient(15)
            val response = client.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ConnectionTestResult(
                    success = true,
                    message = "Connected successfully (${latency}ms)",
                    latencyMs = latency,
                    verifiedModel = model
                )
            } else {
                val errorCat = categorizeHttpError(response.code, body)
                ConnectionTestResult(
                    success = false,
                    message = "${extractErrorMessage(body)} (HTTP ${response.code})",
                    errorCategory = errorCat,
                    latencyMs = latency
                )
            }
        } catch (e: UnknownHostException) {
            ConnectionTestResult(
                success = false,
                message = "Host not reachable (${e.message})",
                errorCategory = ErrorCategory.NETWORK_UNAVAILABLE
            )
        } catch (e: SocketTimeoutException) {
            ConnectionTestResult(
                success = false,
                message = "Connection timed out after 15s",
                errorCategory = ErrorCategory.TIMEOUT
            )
        } catch (e: Exception) {
            ConnectionTestResult(
                success = false,
                message = e.localizedMessage ?: "Connection error",
                errorCategory = ErrorCategory.UNKNOWN_ERROR
            )
        }
    }

    private fun categorizeHttpError(statusCode: Int, body: String): ErrorCategory {
        return when (statusCode) {
            401 -> ErrorCategory.INVALID_API_KEY
            403 -> ErrorCategory.PERMISSION_DENIED
            404 -> ErrorCategory.MODEL_UNAVAILABLE
            429 -> ErrorCategory.QUOTA_EXCEEDED
            500, 502, 503 -> ErrorCategory.SERVER_ERROR
            else -> ErrorCategory.UNKNOWN_ERROR
        }
    }

    private fun extractErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val err = json.get("error")
                if (err is JSONObject) {
                    err.optString("message", "API Error")
                } else {
                    err.toString()
                }
            } else {
                json.optString("message", "API Request Failed")
            }
        } catch (e: Exception) {
            if (body.length > 100) body.take(100) + "..." else body
        }
    }
}
