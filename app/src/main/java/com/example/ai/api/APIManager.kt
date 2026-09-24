package com.example.ai.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.ai.ChatMessage
import com.example.ai.FastLocalCommandEngine
import com.example.ai.tools.ToolRouter
import com.example.floating.OrbController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ApiStatus(val label: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    OFFLINE("Offline"),
    ERROR("Error"),
    NOT_CONFIGURED("Not Configured")
}

/**
 * Central APIManager for TITONOX.
 *
 * Coordinates:
 * - AI Provider selection (Gemini)
 * - Safe API Key storage
 * - Live Connection Health Monitoring
 * - Structured Tool Execution Pipeline
 * - Real Fallbacks to Local Intelligence
 */
class APIManager private constructor(private val context: Context) {

    private val TAG = "APIManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val storage = SecureApiStorage.getInstance(context)

    private val _config = MutableStateFlow(storage.loadConfig())
    val config: StateFlow<AIConfig> = _config.asStateFlow()

    private val _apiStatus = MutableStateFlow(
        if (_config.value.apiKey.isNotBlank()) ApiStatus.OFFLINE else ApiStatus.NOT_CONFIGURED
    )
    val apiStatus: StateFlow<ApiStatus> = _apiStatus.asStateFlow()

    private val geminiProvider = GeminiProvider()
    private val toolRouter = ToolRouter(context = context)

    init {
        // Initial health check in background if key is present
        if (_config.value.apiKey.isNotBlank()) {
            scope.launch {
                verifyActiveConnection()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: APIManager? = null

        fun getInstance(context: Context): APIManager {
            return instance ?: synchronized(this) {
                instance ?: APIManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun updateConfig(newConfig: AIConfig) {
        _config.value = newConfig
        storage.saveConfig(newConfig)
        if (newConfig.apiKey.isBlank()) {
            _apiStatus.value = ApiStatus.NOT_CONFIGURED
        } else {
            scope.launch {
                verifyActiveConnection()
            }
        }
    }

    suspend fun verifyActiveConnection(): ConnectionTestResult {
        val cfg = _config.value
        if (cfg.apiKey.isBlank()) {
            _apiStatus.value = ApiStatus.NOT_CONFIGURED
            return ConnectionTestResult(
                success = false,
                message = "API key is not configured.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        _apiStatus.value = ApiStatus.CONNECTING
        val result = geminiProvider.testConnection(cfg)
        _apiStatus.value = if (result.success) ApiStatus.CONNECTED else {
            if (result.errorCategory == ErrorCategory.NETWORK_UNAVAILABLE) ApiStatus.OFFLINE else ApiStatus.ERROR
        }
        return result
    }

    /**
     * Complete AI chat pipeline with structured tool handling:
     * Prompt -> Gemini Provider -> Response Parser -> Tool Router -> Spoken Result
     */
    suspend fun processUserQuery(
        prompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null,
        onActionExecuted: ((String) -> Unit)? = null
    ): String {
        val currentCfg = _config.value

        // Execute API request
        val result = geminiProvider.generateResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            config = currentCfg,
            userMemories = userMemories,
            screenBitmap = screenBitmap
        )

        if (!result.isSuccess) {
            if (result.errorCategory == ErrorCategory.NO_API_KEY) {
                _apiStatus.value = ApiStatus.NOT_CONFIGURED
            } else if (result.errorCategory == ErrorCategory.NETWORK_UNAVAILABLE) {
                _apiStatus.value = ApiStatus.OFFLINE
            } else {
                _apiStatus.value = ApiStatus.ERROR
            }
            return result.text
        }

        _apiStatus.value = ApiStatus.CONNECTED

        // Parse structured tool calls from model output
        val parsed = AIResponseParser.parse(result.text)

        // Execute any recognized structured tools safely
        if (parsed.toolCalls.isNotEmpty()) {
            for (call in parsed.toolCalls) {
                Log.d(TAG, "Executing structured tool: ${call.toolName} with params: ${call.parameters}")
                val execResult = toolRouter.executeTool(call.toolName, call.parameters)
                onActionExecuted?.invoke(execResult.output ?: execResult.toolName)
            }
        }

        return parsed.userFacingText
    }

    /**
     * Streaming user query execution.
     */
    suspend fun streamUserQuery(
        prompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null,
        onChunk: suspend (String) -> Unit,
        onActionExecuted: ((String) -> Unit)? = null
    ): String {
        val currentCfg = _config.value

        val fullTextBuilder = StringBuilder()
        val result = geminiProvider.streamResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            config = currentCfg,
            userMemories = userMemories,
            screenBitmap = screenBitmap,
            onChunk = { chunk ->
                fullTextBuilder.append(chunk)
                onChunk(chunk)
            }
        )

        val textToProcess = if (result.isSuccess && result.text.isNotBlank()) result.text else fullTextBuilder.toString()
        val parsed = AIResponseParser.parse(textToProcess)

        if (parsed.toolCalls.isNotEmpty()) {
            for (call in parsed.toolCalls) {
                val execResult = toolRouter.executeTool(call.toolName, call.parameters)
                onActionExecuted?.invoke(execResult.output ?: execResult.toolName)
            }
        }

        return parsed.userFacingText
    }
}
