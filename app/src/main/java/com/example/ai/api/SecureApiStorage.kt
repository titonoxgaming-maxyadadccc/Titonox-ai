package com.example.ai.api

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

/**
 * Secure Storage for AI Configuration and API credentials.
 *
 * Stores the user-configured API key, model selection, temperature, and system instructions.
 * Does not expose the full key in logs or telemetry.
 */
class SecureApiStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("titonox_secure_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL = "gemini_model"
        private const val KEY_TEMPERATURE = "ai_temperature"
        private const val KEY_MAX_TOKENS = "ai_max_tokens"
        private const val KEY_SYSTEM_INSTRUCTION = "ai_system_instruction"
        private const val KEY_TIMEOUT = "ai_timeout"
        private const val KEY_STREAMING = "ai_streaming"

        @Volatile
        private var instance: SecureApiStorage? = null

        fun getInstance(context: Context): SecureApiStorage {
            return instance ?: synchronized(this) {
                instance ?: SecureApiStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Loads the current configuration. If no custom key was stored, falls back
     * to the BuildConfig.GEMINI_API_KEY if available.
     */
    fun loadConfig(): AIConfig {
        val storedKey = prefs.getString(KEY_API_KEY, null)
        val defaultBuildConfigKey = BuildConfig.GEMINI_API_KEY.takeIf {
            it.isNotBlank() && it != "MY_GEMINI_API_KEY"
        } ?: ""

        val effectiveKey = if (!storedKey.isNullOrBlank()) storedKey else defaultBuildConfigKey

        return AIConfig(
            provider = AIProviderType.valueOf(prefs.getString(KEY_PROVIDER, AIProviderType.GEMINI.name) ?: AIProviderType.GEMINI.name),
            apiKey = effectiveKey,
            model = prefs.getString(KEY_MODEL, "gemini-2.0-flash") ?: "gemini-2.0-flash",
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f),
            maxOutputTokens = prefs.getInt(KEY_MAX_TOKENS, 2048),
            systemInstruction = prefs.getString(KEY_SYSTEM_INSTRUCTION, AIConfig.DEFAULT_SYSTEM_INSTRUCTION)
                ?: AIConfig.DEFAULT_SYSTEM_INSTRUCTION,
            timeoutSeconds = prefs.getInt(KEY_TIMEOUT, 30),
            streamingEnabled = prefs.getBoolean(KEY_STREAMING, true)
        )
    }

    /**
     * Saves user configuration changes safely.
     */
    fun saveConfig(config: AIConfig) {
        prefs.edit()
            .putString(KEY_PROVIDER, config.provider.name)
            .putString(KEY_API_KEY, config.apiKey.trim())
            .putString(KEY_MODEL, config.model.trim())
            .putFloat(KEY_TEMPERATURE, config.temperature)
            .putInt(KEY_MAX_TOKENS, config.maxOutputTokens)
            .putString(KEY_SYSTEM_INSTRUCTION, config.systemInstruction)
            .putInt(KEY_TIMEOUT, config.timeoutSeconds)
            .putBoolean(KEY_STREAMING, config.streamingEnabled)
            .apply()
    }

    /**
     * Resets configuration to default baseline.
     */
    fun resetToDefaults() {
        val defaultBuildConfigKey = BuildConfig.GEMINI_API_KEY.takeIf {
            it.isNotBlank() && it != "MY_GEMINI_API_KEY"
        } ?: ""

        saveConfig(
            AIConfig(
                apiKey = defaultBuildConfigKey,
                model = "gemini-2.0-flash",
                temperature = 0.7f,
                maxOutputTokens = 2048,
                systemInstruction = AIConfig.DEFAULT_SYSTEM_INSTRUCTION,
                timeoutSeconds = 30,
                streamingEnabled = true
            )
        )
    }

    /**
     * Returns a masked representation of the key for safe UI display (e.g., AIzaSy...4X9Z).
     */
    fun getMaskedKey(key: String): String {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return "Not configured"
        if (trimmed.length <= 8) return "••••••••"
        val prefix = trimmed.take(6)
        val suffix = trimmed.takeLast(4)
        return "$prefix••••••••$suffix"
    }
}
