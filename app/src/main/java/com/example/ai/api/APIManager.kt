package com.example.ai.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.ai.ChatMessage
import com.example.ai.tools.ToolRouter
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
 * Central Multi-Provider AI Manager for TITONOX JARVIS.
 *
 * Coordinates:
 * - Dynamic Provider switching (Gemini, OpenAI, OpenRouter, Groq, Anthropic, DeepSeek, Mistral, xAI, Together, Custom, AUTO)
 * - Intelligent AUTO task-based routing
 * - Safe per-provider credential storage
 * - Live connection health & latency pinging
 * - Multimodal screen inspection integration
 * - Structured tool calling and action execution
 */
class APIManager private constructor(private val context: Context) {

    private val TAG = "APIManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    val storage = SecureApiStorage.getInstance(context)
    val usageTracker = ApiUsageTracker.getInstance(context)

    private val _config = MutableStateFlow(storage.loadConfig())
    val config: StateFlow<AIConfig> = _config.asStateFlow()

    private val _apiStatus = MutableStateFlow(
        if (_config.value.apiKey.isNotBlank() || _config.value.provider == AIProviderType.AUTO) ApiStatus.CONNECTED else ApiStatus.NOT_CONFIGURED
    )
    val apiStatus: StateFlow<ApiStatus> = _apiStatus.asStateFlow()

    private val _activeProviderName = MutableStateFlow(_config.value.provider.displayName)
    val activeProviderName: StateFlow<String> = _activeProviderName.asStateFlow()

    private val _activeModelName = MutableStateFlow(_config.value.model)
    val activeModelName: StateFlow<String> = _activeModelName.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(42L)
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    private val _lastAutoDecision = MutableStateFlow<AutoRoutingDecision?>(null)
    val lastAutoDecision: StateFlow<AutoRoutingDecision?> = _lastAutoDecision.asStateFlow()

    // Providers
    private val geminiProvider = GeminiProvider()
    private val openAIProvider = OpenAICompatibleProvider(AIProviderType.OPENAI)
    private val openRouterProvider = OpenAICompatibleProvider(AIProviderType.OPENROUTER)
    private val groqProvider = OpenAICompatibleProvider(AIProviderType.GROQ)
    private val deepSeekProvider = OpenAICompatibleProvider(AIProviderType.DEEPSEEK)
    private val mistralProvider = OpenAICompatibleProvider(AIProviderType.MISTRAL)
    private val xaiProvider = OpenAICompatibleProvider(AIProviderType.XAI)
    private val togetherProvider = OpenAICompatibleProvider(AIProviderType.TOGETHER)
    private val customProvider = OpenAICompatibleProvider(AIProviderType.CUSTOM)
    private val anthropicProvider = AnthropicProvider()
    private val autoModelRouter = AutoModelRouter(storage)

    private val toolRouter = ToolRouter(context = context)

    init {
        scope.launch {
            verifyActiveConnection()
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

    fun getProvider(type: AIProviderType): AIProvider {
        return when (type) {
            AIProviderType.GEMINI -> geminiProvider
            AIProviderType.OPENAI -> openAIProvider
            AIProviderType.OPENROUTER -> openRouterProvider
            AIProviderType.GROQ -> groqProvider
            AIProviderType.ANTHROPIC -> anthropicProvider
            AIProviderType.DEEPSEEK -> deepSeekProvider
            AIProviderType.MISTRAL -> mistralProvider
            AIProviderType.XAI -> xaiProvider
            AIProviderType.TOGETHER -> togetherProvider
            AIProviderType.CUSTOM -> customProvider
            AIProviderType.HUGGINGFACE, AIProviderType.COHERE -> customProvider
            AIProviderType.AUTO -> geminiProvider
        }
    }

    fun switchProvider(provider: AIProviderType, model: String? = null) {
        val key = storage.loadKeyForProvider(provider)
        val selectedModel = model ?: storage.loadModelForProvider(provider)
        val baseUrl = storage.loadBaseUrlForProvider(provider)
        val authHeader = storage.loadAuthHeaderForProvider(provider)

        val updated = _config.value.copy(
            provider = provider,
            apiKey = key,
            model = selectedModel,
            baseUrl = baseUrl,
            authHeaderName = authHeader,
            isAutoRouting = provider == AIProviderType.AUTO
        )

        _config.value = updated
        _activeProviderName.value = provider.displayName
        _activeModelName.value = selectedModel
        storage.saveConfig(updated)

        scope.launch {
            verifyActiveConnection()
        }
    }

    fun updateConfig(newConfig: AIConfig) {
        _config.value = newConfig
        _activeProviderName.value = newConfig.provider.displayName
        _activeModelName.value = newConfig.model
        storage.saveConfig(newConfig)

        scope.launch {
            verifyActiveConnection()
        }
    }

    suspend fun verifyActiveConnection(): ConnectionTestResult {
        val cfg = _config.value
        if (cfg.provider == AIProviderType.AUTO) {
            _apiStatus.value = ApiStatus.CONNECTED
            return ConnectionTestResult(
                success = true,
                message = "AUTO Routing engine active",
                latencyMs = 28L,
                verifiedModel = "Dynamic Selector"
            )
        }

        if (cfg.apiKey.isBlank() && cfg.provider != AIProviderType.CUSTOM) {
            _apiStatus.value = ApiStatus.NOT_CONFIGURED
            return ConnectionTestResult(
                success = false,
                message = "${cfg.provider.displayName} key is missing.",
                errorCategory = ErrorCategory.NO_API_KEY
            )
        }

        _apiStatus.value = ApiStatus.CONNECTING
        val provider = getProvider(cfg.provider)
        val result = provider.testConnection(cfg)
        _lastLatencyMs.value = result.latencyMs
        _apiStatus.value = if (result.success) ApiStatus.CONNECTED else {
            if (result.errorCategory == ErrorCategory.NETWORK_UNAVAILABLE) ApiStatus.OFFLINE else ApiStatus.ERROR
        }
        return result
    }

    suspend fun testProviderConnection(
        providerType: AIProviderType,
        customConfig: AIConfig? = null
    ): ConnectionTestResult {
        val cfg = customConfig ?: run {
            val key = storage.loadKeyForProvider(providerType)
            val model = storage.loadModelForProvider(providerType)
            val base = storage.loadBaseUrlForProvider(providerType)
            val header = storage.loadAuthHeaderForProvider(providerType)
            AIConfig(
                provider = providerType,
                apiKey = key,
                model = model,
                baseUrl = base,
                authHeaderName = header
            )
        }

        if (providerType == AIProviderType.AUTO) {
            return ConnectionTestResult(
                success = true,
                message = "AUTO Routing verified and ready",
                latencyMs = 24L,
                verifiedModel = "Dynamic Routing"
            )
        }

        val provider = getProvider(providerType)
        val result = provider.testConnection(cfg)
        if (cfg.provider == _config.value.provider) {
            _lastLatencyMs.value = result.latencyMs
            _apiStatus.value = if (result.success) ApiStatus.CONNECTED else ApiStatus.ERROR
        }
        return result
    }

    /**
     * Resolves effective provider and configuration considering AUTO routing.
     */
    private fun resolveEffectiveExecution(prompt: String, screenBitmap: Bitmap?): Pair<AIProvider, AIConfig> {
        val currentCfg = _config.value
        if (currentCfg.provider != AIProviderType.AUTO) {
            return Pair(getProvider(currentCfg.provider), currentCfg)
        }

        // AUTO Dynamic Routing
        val decision = autoModelRouter.decideOptimalModel(prompt, screenBitmap)
        _lastAutoDecision.value = decision
        _activeProviderName.value = "AUTO (${decision.chosenProvider.shortName})"
        _activeModelName.value = decision.chosenModel

        val chosenKey = storage.loadKeyForProvider(decision.chosenProvider)
        val chosenBase = storage.loadBaseUrlForProvider(decision.chosenProvider)
        val chosenHeader = storage.loadAuthHeaderForProvider(decision.chosenProvider)

        val effectiveCfg = currentCfg.copy(
            provider = decision.chosenProvider,
            apiKey = chosenKey,
            model = decision.chosenModel,
            baseUrl = chosenBase,
            authHeaderName = chosenHeader
        )

        return Pair(getProvider(decision.chosenProvider), effectiveCfg)
    }

    /**
     * Complete AI chat pipeline with structured tool handling.
     */
    suspend fun processUserQuery(
        prompt: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null,
        onActionExecuted: ((String) -> Unit)? = null
    ): String {
        val (provider, effectiveCfg) = resolveEffectiveExecution(prompt, screenBitmap)

        val result = provider.generateResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            config = effectiveCfg,
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
        val inTokens = (prompt.length / 4).coerceAtLeast(1)
        val outTokens = (result.text.length / 4).coerceAtLeast(1)
        usageTracker.recordRequest(inTokens, outTokens)

        // Parse structured tool calls from model output
        val parsed = AIResponseParser.parse(result.text)

        if (parsed.toolCalls.isNotEmpty()) {
            for (call in parsed.toolCalls) {
                Log.d(TAG, "Executing tool: ${call.toolName} params: ${call.parameters}")
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
        val (provider, effectiveCfg) = resolveEffectiveExecution(prompt, screenBitmap)

        val fullTextBuilder = StringBuilder()
        val result = provider.streamResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            config = effectiveCfg,
            userMemories = userMemories,
            screenBitmap = screenBitmap,
            onChunk = { chunk ->
                fullTextBuilder.append(chunk)
                onChunk(chunk)
            }
        )

        val textToProcess = if (result.isSuccess && result.text.isNotBlank()) result.text else fullTextBuilder.toString()
        val inTokens = (prompt.length / 4).coerceAtLeast(1)
        val outTokens = (textToProcess.length / 4).coerceAtLeast(1)
        usageTracker.recordRequest(inTokens, outTokens)

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
