package com.example.ai.api

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

/**
 * Secure Storage for Multi-Provider AI Configuration and API credentials.
 *
 * Safely persists:
 * - Active provider selection
 * - Per-provider API keys and custom models
 * - Per-provider base URLs and headers
 * - Temperature, max tokens, and system instructions
 */
class SecureApiStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("titonox_secure_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_PROVIDER = "active_ai_provider"
        private const val KEY_TEMPERATURE = "ai_temperature"
        private const val KEY_MAX_TOKENS = "ai_max_tokens"
        private const val KEY_SYSTEM_INSTRUCTION = "ai_system_instruction"
        private const val KEY_TIMEOUT = "ai_timeout"
        private const val KEY_STREAMING = "ai_streaming"

        private const val PREFIX_API_KEY = "api_key_"
        private const val PREFIX_MODEL = "model_"
        private const val PREFIX_BASE_URL = "base_url_"
        private const val PREFIX_AUTH_HEADER = "auth_header_"

        @Volatile
        private var instance: SecureApiStorage? = null

        fun getInstance(context: Context): SecureApiStorage {
            return instance ?: synchronized(this) {
                instance ?: SecureApiStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    fun loadActiveProvider(): AIProviderType {
        val raw = prefs.getString(KEY_ACTIVE_PROVIDER, AIProviderType.GEMINI.name) ?: AIProviderType.GEMINI.name
        return try {
            AIProviderType.valueOf(raw)
        } catch (e: Exception) {
            AIProviderType.GEMINI
        }
    }

    fun saveActiveProvider(provider: AIProviderType) {
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, provider.name).apply()
    }

    fun loadKeyForProvider(providerType: AIProviderType): String {
        val storedKey = prefs.getString(PREFIX_API_KEY + providerType.name, null)
        if (!storedKey.isNullOrBlank()) return storedKey

        // Fallback for Gemini to BuildConfig if available
        if (providerType == AIProviderType.GEMINI) {
            val defaultKey = BuildConfig.GEMINI_API_KEY.takeIf {
                it.isNotBlank() && it != "MY_GEMINI_API_KEY"
            } ?: ""
            if (defaultKey.isNotBlank()) return defaultKey
        }
        return ""
    }

    fun saveKeyForProvider(providerType: AIProviderType, key: String) {
        prefs.edit().putString(PREFIX_API_KEY + providerType.name, key.trim()).apply()
    }

    fun loadModelForProvider(providerType: AIProviderType): String {
        val stored = prefs.getString(PREFIX_MODEL + providerType.name, null)
        return if (!stored.isNullOrBlank()) stored else providerType.defaultModel
    }

    fun saveModelForProvider(providerType: AIProviderType, model: String) {
        prefs.edit().putString(PREFIX_MODEL + providerType.name, model.trim()).apply()
    }

    fun loadBaseUrlForProvider(providerType: AIProviderType): String {
        val stored = prefs.getString(PREFIX_BASE_URL + providerType.name, null)
        return if (!stored.isNullOrBlank()) stored else providerType.defaultBaseUrl
    }

    fun saveBaseUrlForProvider(providerType: AIProviderType, url: String) {
        prefs.edit().putString(PREFIX_BASE_URL + providerType.name, url.trim()).apply()
    }

    fun loadAuthHeaderForProvider(providerType: AIProviderType): String {
        return prefs.getString(PREFIX_AUTH_HEADER + providerType.name, "Authorization") ?: "Authorization"
    }

    fun saveAuthHeaderForProvider(providerType: AIProviderType, header: String) {
        prefs.edit().putString(PREFIX_AUTH_HEADER + providerType.name, header.trim()).apply()
    }

    /**
     * Loads the active consolidated AI configuration.
     */
    fun loadConfig(): AIConfig {
        val provider = loadActiveProvider()
        val apiKey = loadKeyForProvider(provider)
        val model = loadModelForProvider(provider)
        val baseUrl = loadBaseUrlForProvider(provider)
        val authHeader = loadAuthHeaderForProvider(provider)

        return AIConfig(
            provider = provider,
            apiKey = apiKey,
            model = model,
            baseUrl = baseUrl,
            authHeaderName = authHeader,
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f),
            maxOutputTokens = prefs.getInt(KEY_MAX_TOKENS, 2048),
            systemInstruction = prefs.getString(KEY_SYSTEM_INSTRUCTION, AIConfig.DEFAULT_SYSTEM_INSTRUCTION)
                ?: AIConfig.DEFAULT_SYSTEM_INSTRUCTION,
            timeoutSeconds = prefs.getInt(KEY_TIMEOUT, 30),
            streamingEnabled = prefs.getBoolean(KEY_STREAMING, true),
            isAutoRouting = provider == AIProviderType.AUTO
        )
    }

    /**
     * Saves user configuration changes safely.
     */
    fun saveConfig(config: AIConfig) {
        saveActiveProvider(config.provider)
        saveKeyForProvider(config.provider, config.apiKey)
        saveModelForProvider(config.provider, config.model)
        saveBaseUrlForProvider(config.provider, config.baseUrl)
        saveAuthHeaderForProvider(config.provider, config.authHeaderName)

        prefs.edit()
            .putFloat(KEY_TEMPERATURE, config.temperature)
            .putInt(KEY_MAX_TOKENS, config.maxOutputTokens)
            .putString(KEY_SYSTEM_INSTRUCTION, config.systemInstruction)
            .putInt(KEY_TIMEOUT, config.timeoutSeconds)
            .putBoolean(KEY_STREAMING, config.streamingEnabled)
            .apply()
    }

    fun resetToDefaults() {
        val defaultBuildConfigKey = BuildConfig.GEMINI_API_KEY.takeIf {
            it.isNotBlank() && it != "MY_GEMINI_API_KEY"
        } ?: ""

        saveActiveProvider(AIProviderType.GEMINI)
        saveKeyForProvider(AIProviderType.GEMINI, defaultBuildConfigKey)
        saveModelForProvider(AIProviderType.GEMINI, "gemini-2.5-flash")
        saveBaseUrlForProvider(AIProviderType.GEMINI, AIProviderType.GEMINI.defaultBaseUrl)

        prefs.edit()
            .putFloat(KEY_TEMPERATURE, 0.7f)
            .putInt(KEY_MAX_TOKENS, 2048)
            .putString(KEY_SYSTEM_INSTRUCTION, AIConfig.DEFAULT_SYSTEM_INSTRUCTION)
            .putInt(KEY_TIMEOUT, 30)
            .putBoolean(KEY_STREAMING, true)
            .apply()
    }

    fun getMaskedKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return "Not configured"
        if (trimmed.length <= 8) return "••••••••"
        val prefix = trimmed.take(5)
        val suffix = trimmed.takeLast(4)
        return "$prefix••••••••$suffix"
    }
}
