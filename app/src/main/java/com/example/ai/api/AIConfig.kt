package com.example.ai.api

/**
 * All supported AI Providers in TITONOX JARVIS.
 */
enum class AIProviderType(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val shortName: String
) {
    AUTO("AUTO (Dynamic Routing)", "", "auto", "AUTO"),
    GEMINI("Google Gemini", "https://generativelanguage.googleapis.com", "gemini-2.5-flash", "Gemini"),
    OPENAI("OpenAI", "https://api.openai.com/v1", "gpt-4o", "OpenAI"),
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1", "anthropic/claude-3.5-sonnet", "OpenRouter"),
    GROQ("Groq", "https://api.groq.com/openai/v1", "llama-3.3-70b-versatile", "Groq"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1", "claude-3-5-sonnet-20241022", "Claude"),
    DEEPSEEK("DeepSeek", "https://api.deepseek.com", "deepseek-chat", "DeepSeek"),
    MISTRAL("Mistral AI", "https://api.mistral.ai/v1", "mistral-large-latest", "Mistral"),
    XAI("xAI / Grok", "https://api.x.ai/v1", "grok-2-latest", "xAI"),
    TOGETHER("Together AI", "https://api.together.xyz/v1", "meta-llama/Llama-3.3-70B-Instruct-Turbo", "Together"),
    HUGGINGFACE("Hugging Face", "https://api-inference.huggingface.co/models", "meta-llama/Meta-Llama-3-8B-Instruct", "HF"),
    COHERE("Cohere", "https://api.cohere.ai/v1", "command-r-plus", "Cohere"),
    CUSTOM("Custom Endpoint", "https://api.openai.com/v1", "custom-model", "Custom")
}

/**
 * Pre-defined model choices per provider for instant setup.
 */
object ProviderModelCatalogs {

    val GEMINI_MODELS = listOf(
        ModelDescriptor("gemini-2.5-flash", "Gemini 2.5 Flash", "Latest recommended multimodal, fast response", supportsVision = true, supportsTools = true, isRecommended = true),
        ModelDescriptor("gemini-3.5-flash", "Gemini 3.5 Flash", "Next-gen preview flash model", supportsVision = true, supportsTools = true),
        ModelDescriptor("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep logical reasoning & complex task planning", supportsVision = true, supportsTools = true),
        ModelDescriptor("gemini-2.5-flash-image", "Gemini 2.5 Flash Image", "Optimized for visual comprehension & OCR", supportsVision = true, supportsTools = true),
        ModelDescriptor("gemini-2.0-flash", "Gemini 2.0 Flash", "Real-time low latency multi-tool calling", supportsVision = true, supportsTools = true)
    )

    val OPENAI_MODELS = listOf(
        ModelDescriptor("gpt-4o", "GPT-4o", "Flagship omni model, high vision intelligence", supportsVision = true, supportsTools = true, isRecommended = true),
        ModelDescriptor("gpt-4o-mini", "GPT-4o Mini", "Lightweight, ultra-fast for voice and everyday commands", supportsVision = true, supportsTools = true),
        ModelDescriptor("o1", "o1 Reasoning", "Maximum STEM, math, and code deduction", supportsVision = true, supportsTools = false),
        ModelDescriptor("o3-mini", "o3-mini", "Fast advanced reasoning model", supportsVision = false, supportsTools = true),
        ModelDescriptor("gpt-4-turbo", "GPT-4 Turbo", "Proven production workhorse", supportsVision = true, supportsTools = true)
    )

    val OPENROUTER_MODELS = listOf(
        ModelDescriptor("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet", "Industry-leading reasoning & coding", supportsVision = true, supportsTools = true, isRecommended = true),
        ModelDescriptor("openai/gpt-4o", "OpenAI GPT-4o", "Fast multi-modal router endpoint", supportsVision = true, supportsTools = true),
        ModelDescriptor("deepseek/deepseek-r1", "DeepSeek R1", "Open-weights reasoning breakthrough", supportsVision = false, supportsTools = true),
        ModelDescriptor("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "High-performance open weights", supportsVision = false, supportsTools = true),
        ModelDescriptor("google/gemini-2.5-flash", "Gemini 2.5 Flash via OR", "Fast cloud multimodal fallback", supportsVision = true, supportsTools = true)
    )

    val GROQ_MODELS = listOf(
        ModelDescriptor("llama-3.3-70b-versatile", "Llama 3.3 70B Versatile", "Blazing fast inference (<100ms voice speed)", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("llama-3.1-8b-instant", "Llama 3.1 8B Instant", "Instantaneous 800+ tokens/sec", supportsVision = false, supportsTools = true),
        ModelDescriptor("deepseek-r1-distill-llama-70b", "DeepSeek R1 Distill 70B", "Reasoning distilled on Groq LPU speed", supportsVision = false, supportsTools = true),
        ModelDescriptor("mixtral-8x7b-32768", "Mixtral 8x7B", "MoE high throughput", supportsVision = false, supportsTools = true)
    )

    val ANTHROPIC_MODELS = listOf(
        ModelDescriptor("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet (Latest)", "Supreme comprehension, computer use & tools", supportsVision = true, supportsTools = true, isRecommended = true),
        ModelDescriptor("claude-3-5-haiku-20241022", "Claude 3.5 Haiku", "Fast, responsive assistant for voice commands", supportsVision = true, supportsTools = true),
        ModelDescriptor("claude-3-opus-20240229", "Claude 3 Opus", "Deep philosophical and creative synthesis", supportsVision = true, supportsTools = true)
    )

    val DEEPSEEK_MODELS = listOf(
        ModelDescriptor("deepseek-chat", "DeepSeek V3 (Chat)", "Strong conversational and tool instruction", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("deepseek-reasoner", "DeepSeek R1 (Reasoner)", "Chain-of-thought mathematical reasoning", supportsVision = false, supportsTools = false)
    )

    val MISTRAL_MODELS = listOf(
        ModelDescriptor("mistral-large-latest", "Mistral Large", "Top-tier flagship reasoning & multilingual", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("pixtral-12b-2409", "Pixtral 12B Vision", "Multimodal image & document reasoning", supportsVision = true, supportsTools = true),
        ModelDescriptor("mistral-small-latest", "Mistral Small", "Cost-effective, rapid latency", supportsVision = false, supportsTools = true)
    )

    val XAI_MODELS = listOf(
        ModelDescriptor("grok-2-latest", "Grok 2", "Unfiltered knowledge base & real-time reasoning", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("grok-2-vision-latest", "Grok 2 Vision", "Multimodal vision & screen reasoning", supportsVision = true, supportsTools = true)
    )

    val TOGETHER_MODELS = listOf(
        ModelDescriptor("meta-llama/Llama-3.3-70B-Instruct-Turbo", "Llama 3.3 70B Turbo", "Fast serverless inference", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("deepseek-ai/DeepSeek-R1", "DeepSeek R1", "Open reasoning powerhouse", supportsVision = false, supportsTools = false)
    )

    val COHERE_MODELS = listOf(
        ModelDescriptor("command-r-plus", "Command R+", "Enterprise RAG & multi-step tool execution", supportsVision = false, supportsTools = true, isRecommended = true),
        ModelDescriptor("command-r", "Command R", "Fast multilingual reasoning", supportsVision = false, supportsTools = true)
    )

    fun getModelsForProvider(providerType: AIProviderType): List<ModelDescriptor> {
        return when (providerType) {
            AIProviderType.GEMINI -> GEMINI_MODELS
            AIProviderType.OPENAI -> OPENAI_MODELS
            AIProviderType.OPENROUTER -> OPENROUTER_MODELS
            AIProviderType.GROQ -> GROQ_MODELS
            AIProviderType.ANTHROPIC -> ANTHROPIC_MODELS
            AIProviderType.DEEPSEEK -> DEEPSEEK_MODELS
            AIProviderType.MISTRAL -> MISTRAL_MODELS
            AIProviderType.XAI -> XAI_MODELS
            AIProviderType.TOGETHER -> TOGETHER_MODELS
            AIProviderType.COHERE -> COHERE_MODELS
            AIProviderType.HUGGINGFACE -> listOf(
                ModelDescriptor("meta-llama/Meta-Llama-3-8B-Instruct", "Llama 3 8B Instruct", "HF Inference API", supportsVision = false, supportsTools = true)
            )
            AIProviderType.CUSTOM, AIProviderType.AUTO -> listOf(
                ModelDescriptor("custom-model", "Custom Model", "User-specified custom endpoint", supportsVision = true, supportsTools = true)
            )
        }
    }
}

/**
 * Backward compatibility alias for GeminiModelOption.
 */
data class GeminiModelOption(
    val modelId: String,
    val displayName: String,
    val description: String
)

val SUPPORTED_GEMINI_MODELS = ProviderModelCatalogs.GEMINI_MODELS.map {
    GeminiModelOption(it.modelId, it.displayName, it.description)
}

/**
 * Centralized AI Configuration.
 */
data class AIConfig(
    val provider: AIProviderType = AIProviderType.GEMINI,
    val apiKey: String = "",
    val model: String = "gemini-2.5-flash",
    val baseUrl: String = "",
    val authHeaderName: String = "Authorization",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 2048,
    val systemInstruction: String = DEFAULT_SYSTEM_INSTRUCTION,
    val timeoutSeconds: Int = 30,
    val streamingEnabled: Boolean = true,
    val isAutoRouting: Boolean = false
) {
    companion object {
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "You are TITONOX, an advanced Jarvis-style Android AI assistant.\n" +
            "Created and Owned by: Aditya Yadav\n" +
            "Official YouTube: TITONOXOFFICIAL | Official Instagram: TITONOXOFFICIAL\n" +
            "Tone: Calm, highly intelligent, precise, action-oriented, loyal, futuristic.\n" +
            "Languages: Fluent in English, Hindi, and Hinglish. Reply in the user's spoken language naturally.\n" +
            "When executing actions, emit structured tool commands such as:\n" +
            "{\"tool\": \"device.flashlight\", \"parameters\": {\"enabled\": true}}\n" +
            "{\"tool\": \"device.volume\", \"parameters\": {\"level\": 80}}\n" +
            "{\"tool\": \"productivity.add_note\", \"parameters\": {\"title\": \"Meeting\", \"content\": \"Call Aditya at 5pm\"}}\n" +
            "{\"tool\": \"orb.rotate\", \"parameters\": {\"axis\": \"Y\", \"amount\": 45}}\n" +
            "{\"tool\": \"orb.select\", \"parameters\": {\"orbId\": \"plasma_sphere\"}}\n" +
            "{\"tool\": \"orb.customize\", \"parameters\": {\"color\": \"CYAN\", \"glow\": 0.9}}\n" +
            "Never fabricate action execution. Keep chat responses concise and spoken-friendly."
    }
}
