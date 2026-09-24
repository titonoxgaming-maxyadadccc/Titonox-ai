package com.example.ai.api

import android.graphics.Bitmap
import com.example.ai.ChatMessage

/**
 * Result of testing connection to the AI provider.
 */
data class ConnectionTestResult(
    val success: Boolean,
    val message: String,
    val errorCategory: ErrorCategory? = null,
    val latencyMs: Long = 0L,
    val verifiedModel: String? = null
)

enum class ErrorCategory(val displayName: String) {
    NO_API_KEY("No API Key Provided"),
    INVALID_API_KEY("Invalid API Key"),
    PERMISSION_DENIED("Permission Denied"),
    MODEL_UNAVAILABLE("Model Unavailable"),
    QUOTA_EXCEEDED("Quota Exceeded"),
    NETWORK_UNAVAILABLE("Network Unavailable"),
    TIMEOUT("Request Timeout"),
    SERVER_ERROR("AI Server Error"),
    UNKNOWN_ERROR("Unexpected Error")
}

/**
 * Result returned by AI Provider text generation.
 */
data class AIResult(
    val isSuccess: Boolean,
    val text: String,
    val rawResponse: String? = null,
    val errorCategory: ErrorCategory? = null,
    val errorMessage: String? = null
)

/**
 * Universal AI Provider Interface for TITONOX.
 */
interface AIProvider {
    val providerType: AIProviderType

    /**
     * Executes a standard single-turn or multi-turn request.
     */
    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null
    ): AIResult

    /**
     * Executes a streaming request, notifying chunks progressively.
     */
    suspend fun streamResponse(
        prompt: String,
        conversationHistory: List<ChatMessage>,
        config: AIConfig,
        userMemories: List<String> = emptyList(),
        screenBitmap: Bitmap? = null,
        onChunk: suspend (String) -> Unit
    ): AIResult

    /**
     * Performs a real API handshake test with the configured credentials.
     */
    suspend fun testConnection(config: AIConfig): ConnectionTestResult
}
