package com.example.ai.api

/**
 * Authentication mechanism required by the provider endpoint.
 */
enum class AuthType(val displayName: String) {
    BEARER("Bearer Token (Authorization: Bearer <key>)"),
    API_KEY_HEADER("Custom Header (e.g. x-api-key)"),
    QUERY_PARAM("URL Parameter (e.g. ?key=<key>)"),
    BASIC("Basic Auth"),
    NONE("No Authentication")
}

/**
 * Metadata descriptor for an AI model.
 */
data class ModelDescriptor(
    val modelId: String,
    val displayName: String,
    val description: String = "",
    val supportsVision: Boolean = false,
    val supportsTools: Boolean = true,
    val contextWindow: Int = 128000,
    val isRecommended: Boolean = false
)

/**
 * Complete, configurable profile for an AI Provider.
 */
data class ProviderConfig(
    val providerType: AIProviderType,
    val providerName: String,
    val apiBaseUrl: String,
    val apiKey: String = "",
    val authType: AuthType = AuthType.BEARER,
    val authHeaderName: String = "Authorization",
    val modelList: List<ModelDescriptor> = emptyList(),
    val selectedModel: String = "",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val systemPrompt: String = AIConfig.DEFAULT_SYSTEM_INSTRUCTION,
    val streamingEnabled: Boolean = true,
    val visionSupport: Boolean = false,
    val toolCallingSupport: Boolean = true,
    val jsonSupport: Boolean = true,
    val isEnabled: Boolean = true
)

/**
 * Result of dynamic AUTO routing analysis.
 */
data class AutoRoutingDecision(
    val chosenProvider: AIProviderType,
    val chosenModel: String,
    val reason: String,
    val taskCategory: String, // "VISION", "REASONING", "LOW_LATENCY_VOICE", "OFFLINE"
    val confidence: Float = 0.95f
)
