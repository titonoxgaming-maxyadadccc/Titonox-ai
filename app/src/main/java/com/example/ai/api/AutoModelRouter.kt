package com.example.ai.api

import android.graphics.Bitmap
import android.util.Log
import java.util.Locale

/**
 * Intelligent Dynamic Model Router for TITONOX JARVIS.
 *
 * Automatically assigns user tasks to the optimal provider & model:
 * 1. Vision Tasks -> Vision-Capable model (Gemini 2.5 Flash / GPT-4o / Claude 3.5 Sonnet)
 * 2. Complex Reasoning -> High-Deduction model (Gemini 3.1 Pro / GPT-4o / DeepSeek R1 / Claude 3.5)
 * 3. Quick Device Actions & Spoken Voice -> Ultra-Low Latency model (Groq Llama 3.3 / Gemini 2.5 Flash / GPT-4o-mini)
 * 4. Offline / Basic -> Local Fast Command Engine
 */
class AutoModelRouter(
    private val secureStorage: SecureApiStorage
) {
    private val TAG = "AutoModelRouter"

    fun decideOptimalModel(
        prompt: String,
        screenBitmap: Bitmap?
    ): AutoRoutingDecision {
        val lower = prompt.lowercase(Locale.ROOT)

        // 1. Vision Check
        val isVisionTask = screenBitmap != null ||
                lower.contains("look at") ||
                lower.contains("screen") ||
                lower.contains("screenshot") ||
                lower.contains("image") ||
                lower.contains("what do you see") ||
                lower.contains("read this text") ||
                lower.contains("scan")

        if (isVisionTask) {
            val geminiKey = secureStorage.loadKeyForProvider(AIProviderType.GEMINI)
            val openAiKey = secureStorage.loadKeyForProvider(AIProviderType.OPENAI)
            val anthropicKey = secureStorage.loadKeyForProvider(AIProviderType.ANTHROPIC)
            val openRouterKey = secureStorage.loadKeyForProvider(AIProviderType.OPENROUTER)

            return when {
                geminiKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.GEMINI,
                    chosenModel = "gemini-2.5-flash",
                    reason = "Selected Gemini 2.5 Flash for native high-resolution screen vision and OCR.",
                    taskCategory = "VISION"
                )
                openAiKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.OPENAI,
                    chosenModel = "gpt-4o",
                    reason = "Selected OpenAI GPT-4o for multimodal vision analysis.",
                    taskCategory = "VISION"
                )
                anthropicKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.ANTHROPIC,
                    chosenModel = "claude-3-5-sonnet-20241022",
                    reason = "Selected Claude 3.5 Sonnet for detailed document and image understanding.",
                    taskCategory = "VISION"
                )
                openRouterKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.OPENROUTER,
                    chosenModel = "anthropic/claude-3.5-sonnet",
                    reason = "Selected OpenRouter multimodal endpoint for vision processing.",
                    taskCategory = "VISION"
                )
                else -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.GEMINI,
                    chosenModel = "gemini-2.5-flash",
                    reason = "Defaulting to Gemini for vision task (configure API key in Models screen).",
                    taskCategory = "VISION"
                )
            }
        }

        // 2. Complex Reasoning Check
        val isReasoningTask = lower.contains("plan") ||
                lower.contains("code") ||
                lower.contains("analyze") ||
                lower.contains("calculate") ||
                lower.contains("strategy") ||
                lower.contains("compare") ||
                lower.contains("multi-step") ||
                lower.contains("deeply") ||
                prompt.length > 250

        if (isReasoningTask) {
            val deepSeekKey = secureStorage.loadKeyForProvider(AIProviderType.DEEPSEEK)
            val anthropicKey = secureStorage.loadKeyForProvider(AIProviderType.ANTHROPIC)
            val openAiKey = secureStorage.loadKeyForProvider(AIProviderType.OPENAI)
            val geminiKey = secureStorage.loadKeyForProvider(AIProviderType.GEMINI)

            return when {
                deepSeekKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.DEEPSEEK,
                    chosenModel = "deepseek-chat",
                    reason = "Routed to DeepSeek for chain-of-thought advanced reasoning.",
                    taskCategory = "REASONING"
                )
                anthropicKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.ANTHROPIC,
                    chosenModel = "claude-3-5-sonnet-20241022",
                    reason = "Routed to Claude 3.5 Sonnet for deep analytical deduction.",
                    taskCategory = "REASONING"
                )
                openAiKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.OPENAI,
                    chosenModel = "gpt-4o",
                    reason = "Routed to OpenAI GPT-4o for complex logic planning.",
                    taskCategory = "REASONING"
                )
                geminiKey.isNotBlank() -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.GEMINI,
                    chosenModel = "gemini-3.1-pro-preview",
                    reason = "Routed to Gemini 3.1 Pro for deep structured task planning.",
                    taskCategory = "REASONING"
                )
                else -> AutoRoutingDecision(
                    chosenProvider = AIProviderType.GEMINI,
                    chosenModel = "gemini-2.5-flash",
                    reason = "Defaulting to Gemini 2.5 Flash for reasoning.",
                    taskCategory = "REASONING"
                )
            }
        }

        // 3. Quick Voice & Device Action Check (Low Latency)
        val groqKey = secureStorage.loadKeyForProvider(AIProviderType.GROQ)
        val geminiKey = secureStorage.loadKeyForProvider(AIProviderType.GEMINI)
        val openAiKey = secureStorage.loadKeyForProvider(AIProviderType.OPENAI)

        return when {
            groqKey.isNotBlank() -> AutoRoutingDecision(
                chosenProvider = AIProviderType.GROQ,
                chosenModel = "llama-3.3-70b-versatile",
                reason = "Routed to Groq LPU for instantaneous <100ms voice command execution.",
                taskCategory = "LOW_LATENCY_VOICE"
            )
            geminiKey.isNotBlank() -> AutoRoutingDecision(
                chosenProvider = AIProviderType.GEMINI,
                chosenModel = "gemini-2.5-flash",
                reason = "Routed to Gemini 2.5 Flash for high-speed voice response.",
                taskCategory = "LOW_LATENCY_VOICE"
            )
            openAiKey.isNotBlank() -> AutoRoutingDecision(
                chosenProvider = AIProviderType.OPENAI,
                chosenModel = "gpt-4o-mini",
                reason = "Routed to GPT-4o Mini for fast low-latency execution.",
                taskCategory = "LOW_LATENCY_VOICE"
            )
            else -> AutoRoutingDecision(
                chosenProvider = AIProviderType.GEMINI,
                chosenModel = "gemini-2.5-flash",
                reason = "Defaulting to Gemini Flash for rapid response.",
                taskCategory = "LOW_LATENCY_VOICE"
            )
        }
    }
}
