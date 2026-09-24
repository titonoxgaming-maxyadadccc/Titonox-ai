package com.example.ai.api

import org.json.JSONObject
import java.util.Locale

data class ParsedToolCall(
    val toolName: String,
    val parameters: JSONObject,
    val rawJson: String
)

data class ParsedAIResponse(
    val userFacingText: String,
    val toolCalls: List<ParsedToolCall>,
    val actionBadge: String? = null
)

/**
 * Parses Gemini outputs into clean displayable/spoken text and structured tool actions.
 */
object AIResponseParser {

    fun parse(rawResponse: String): ParsedAIResponse {
        val trimmed = rawResponse.trim()
        val toolCalls = ArrayList<ParsedToolCall>()
        var cleanedText = trimmed

        // 1. First check markdown code fences: ```json ... ``` or ``` ... ```
        val codeBlockRegex = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""")
        val matches = codeBlockRegex.findAll(trimmed).toList()
        for (m in matches) {
            val blockContent = m.groupValues[1].trim()
            if (blockContent.startsWith("{") && blockContent.endsWith("}")) {
                try {
                    val json = JSONObject(blockContent)
                    if (json.has("tool")) {
                        val toolName = json.getString("tool")
                        val params = json.optJSONObject("parameters") ?: JSONObject()
                        toolCalls.add(ParsedToolCall(toolName, params, m.value))
                    }
                } catch (e: Exception) {
                    // Not valid JSON
                }
            }
        }

        // 2. Also find any raw JSON objects containing "tool":
        if (toolCalls.isEmpty()) {
            var startIndex = 0
            while (startIndex < trimmed.length) {
                val openBrace = trimmed.indexOf('{', startIndex)
                if (openBrace == -1) break
                var depth = 0
                var closeBrace = -1
                for (i in openBrace until trimmed.length) {
                    if (trimmed[i] == '{') depth++
                    else if (trimmed[i] == '}') {
                        depth--
                        if (depth == 0) {
                            closeBrace = i
                            break
                        }
                    }
                }
                if (closeBrace != -1) {
                    val candidate = trimmed.substring(openBrace, closeBrace + 1)
                    try {
                        val json = JSONObject(candidate)
                        if (json.has("tool")) {
                            val toolName = json.getString("tool")
                            val params = json.optJSONObject("parameters") ?: JSONObject()
                            toolCalls.add(ParsedToolCall(toolName, params, candidate))
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                    startIndex = closeBrace + 1
                } else {
                    break
                }
            }
        }

        // Clean out JSON blocks from user-facing text
        for (call in toolCalls) {
            cleanedText = cleanedText.replace(call.rawJson, "").trim()
        }
        cleanedText = cleanedText.replace("```json", "").replace("```", "").trim()

        val actionBadge = if (toolCalls.isNotEmpty()) {
            toolCalls.first().toolName.uppercase(Locale.ROOT).replace(".", "_")
        } else null

        val finalUserFacing = if (cleanedText.isNotEmpty()) cleanedText else "Executing requested action."

        return ParsedAIResponse(
            userFacingText = finalUserFacing,
            toolCalls = toolCalls,
            actionBadge = actionBadge
        )
    }
}
