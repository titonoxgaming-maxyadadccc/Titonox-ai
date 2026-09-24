package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.ai.api.AIConfig
import com.example.ai.api.SecureApiStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionBadge: String? = null
)

class GeminiClient(private val context: Context? = null) {

    private val TAG = "GeminiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getActiveConfig(): AIConfig {
        if (context != null) {
            return SecureApiStorage.getInstance(context).loadConfig()
        }
        val buildConfigKey = BuildConfig.GEMINI_API_KEY.takeIf { it != "MY_GEMINI_API_KEY" } ?: ""
        return AIConfig(apiKey = buildConfigKey)
    }

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val config = getActiveConfig()
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) {
            return@withContext "TITONOX operates with local intelligence when Gemini API key is not configured. Configure your Gemini API key in TITONOX AI Settings."
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=$apiKey"

        val systemInstructionText = buildString {
            append(config.systemInstruction)
            append("\nOfficial YouTube: TITONOXOFFICIAL\n")
            append("Official Instagram: TITONOXOFFICIAL\n")
            append("Owner and Creator: Aditya Yadav\n\n")
            append("STRICT IDENTITY MANDATE:\n")
            append("- When asked 'Who are you?', answer exactly: 'I am TITONOX, your personal AI assistant.'\n")
            append("- When asked about YouTube: 'My official YouTube channel is TITONOXOFFICIAL.'\n")
            append("- When asked about Instagram: 'My official Instagram account is TITONOXOFFICIAL.'\n")
            append("- When asked about your creator/owner: 'I was created as TITONOX by Aditya Yadav.'\n")
            append("Personality: Calm, intelligent, fast, precise, practical, context-aware, honest, action-oriented.\n")
            append("Languages: Fluent in English, Hindi, and Hinglish. Adapt naturally to user language.\n")
            append("Available tools you can invoke via JSON:\n")
            append("{\"tool\": \"orb.rotate\", \"parameters\": {\"axis\": \"Y\", \"amount\": 45}}\n")
            append("{\"tool\": \"orb.select\", \"parameters\": {\"orbId\": \"plasma_sphere\", \"orbType\": \"3D\"}}\n")
            append("{\"tool\": \"orb.customize\", \"parameters\": {\"color\": \"#00E5FF\", \"glow\": 0.8}}\n")
            append("{\"tool\": \"orb.reset\", \"parameters\": {}}\n")

            if (userMemories.isNotEmpty()) {
                append("\nPersistent User Memories & Preferences:\n")
                userMemories.forEach { append("- $it\n") }
            }
        }

        try {
            val rootJson = JSONObject()

            // System Instruction
            val sysObj = JSONObject()
            val sysPart = JSONObject().put("text", systemInstructionText)
            sysObj.put("parts", JSONArray().put(sysPart))
            rootJson.put("systemInstruction", sysObj)

            // Contents (History + Latest user query)
            val contentsArray = JSONArray()

            // Last 6 conversational turns
            val historyWindow = conversationHistory.takeLast(6)
            for (msg in historyWindow) {
                val turn = JSONObject()
                turn.put("role", if (msg.role == "user") "user" else "model")
                val textPart = JSONObject().put("text", msg.text)
                turn.put("parts", JSONArray().put(textPart))
                contentsArray.put(turn)
            }

            // Current message
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))

            // Screen Bitmap Multimodal Attachment
            if (screenBitmap != null) {
                val stream = ByteArrayOutputStream()
                screenBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                val imagePart = JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64)
                    })
                }
                currentParts.put(imagePart)
            }

            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation Config
            val genConfig = JSONObject().apply {
                put("temperature", config.temperature.toDouble())
                put("maxOutputTokens", config.maxOutputTokens)
            }
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error: HTTP ${response.code} -> $responseBody")
                return@withContext when (response.code) {
                    400 -> "Sir, API key invalid lag rahi hai ya query parameters galat hain."
                    403 -> "Sir, API key permissions access deny hui hai."
                    404 -> "Sir, selected AI model available nahi hai. Settings me change karein."
                    429 -> "Sir, Gemini API quota exhaust ho chuka hai. Kripya thoda wait karein."
                    else -> "TITONOX AI Core communication issue: HTTP ${response.code}"
                }
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val text = part?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        textBuilder.append(text)
                    }
                }
            }

            val result = textBuilder.toString().trim()
            if (result.isNotEmpty()) result else "I have processed your request, sir."
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed", e)
            "Sir, AI Core unavailable hai. Local system active hai."
        }
    }

    suspend fun planTaskSteps(goal: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val config = getActiveConfig()
        val apiKey = config.apiKey.trim()
        if (apiKey.isBlank()) return@withContext emptyList()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/${config.model}:generateContent?key=$apiKey"

        val prompt = """
            You are the autonomous execution planner of TITONOX.
            Convert the following user command into an ordered sequence of concrete Android UI actions.
            User Goal: "$goal"

            Available Actions:
            - "open_app" (param: app name)
            - "click_node" (param: text on button or icon contentDescription)
            - "type_text" (param: text to enter into focused input)
            - "scroll" (param: "down" or "up")
            - "volume" (param: "up" or "down" or "percentage")
            - "flashlight" (param: "on" or "off")
            - "web_search" (param: search query)
            - "create_note" (param: note content)
            - "add_todo" (param: todo task)
            - "speak" (param: spoken progress message)

            Output ONLY a valid JSON array of objects. Each object must have:
            - "action": string
            - "target": string
            - "description": short friendly explanation of step
            Do not include Markdown blocks, only pure JSON array.
        """.trimIndent()

        try {
            val rootJson = JSONObject()
            val part = JSONObject().put("text", prompt)
            val content = JSONObject().put("parts", JSONArray().put(part))
            rootJson.put("contents", JSONArray().put(content))

            val genConfig = JSONObject().put("responseMimeType", "application/json")
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val rawJson = parts?.optJSONObject(0)?.optString("text")?.trim() ?: "[]"

            val jsonArray = JSONArray(rawJson)
            val stepsList = mutableListOf<JSONObject>()
            for (i in 0 until jsonArray.length()) {
                val stepObj = jsonArray.optJSONObject(i)
                if (stepObj != null) stepsList.add(stepObj)
            }
            stepsList
        } catch (e: Exception) {
            Log.e(TAG, "Task planning parse error", e)
            emptyList()
        }
    }
}
