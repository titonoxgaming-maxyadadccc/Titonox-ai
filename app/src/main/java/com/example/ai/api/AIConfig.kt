package com.example.ai.api

/**
 * Supported AI Providers in TITONOX.
 */
enum class AIProviderType(val displayName: String) {
    GEMINI("Google Gemini")
}

/**
 * Supported Gemini Models with capabilities.
 */
data class GeminiModelOption(
    val modelId: String,
    val displayName: String,
    val description: String
)

val SUPPORTED_GEMINI_MODELS = listOf(
    GeminiModelOption(
        modelId = "gemini-2.0-flash",
        displayName = "Gemini 2.0 Flash (Recommended)",
        description = "Next-gen multimodal, low latency, real-time tool calling"
    ),
    GeminiModelOption(
        modelId = "gemini-1.5-flash",
        displayName = "Gemini 1.5 Flash",
        description = "Lightweight, ultra-fast response for everyday voice queries"
    ),
    GeminiModelOption(
        modelId = "gemini-1.5-pro",
        displayName = "Gemini 1.5 Pro",
        description = "Deep reasoning, complex multi-step task planning"
    )
)

/**
 * Centralized AI Configuration.
 */
data class AIConfig(
    val provider: AIProviderType = AIProviderType.GEMINI,
    val apiKey: String = "",
    val model: String = "gemini-2.0-flash",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 2048,
    val systemInstruction: String = DEFAULT_SYSTEM_INSTRUCTION,
    val timeoutSeconds: Int = 30,
    val streamingEnabled: Boolean = true
) {
    companion object {
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "You are TITONOX, an advanced Jarvis-style Android AI assistant.\n" +
            "Created and Owned by: Aditya Yadav\n" +
            "Official YouTube: TITONOXOFFICIAL | Official Instagram: TITONOXOFFICIAL\n" +
            "Tone: Calm, highly intelligent, precise, action-oriented, loyal, futuristic.\n" +
            "Languages: Fluent in English, Hindi, and Hinglish. Reply in the user's spoken language naturally.\n" +
            "When controlling the orb, emit structured tool commands such as:\n" +
            "{\"tool\": \"orb.rotate\", \"parameters\": {\"axis\": \"Y\", \"amount\": 45}}\n" +
            "{\"tool\": \"orb.select\", \"parameters\": {\"orbId\": \"plasma_sphere\"}}\n" +
            "{\"tool\": \"orb.customize\", \"parameters\": {\"color\": \"CYAN\", \"glow\": 0.9}}\n" +
            "Never fabricate action execution. Keep chat responses concise and spoken-friendly."
    }
}
