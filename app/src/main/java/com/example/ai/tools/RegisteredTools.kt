package com.example.ai.tools

import org.json.JSONObject

enum class ActionRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class ToolParamType {
    STRING,
    NUMBER,
    BOOLEAN,
    OBJECT
}

enum class ToolRetryPolicy {
    NO_RETRY,
    RETRY_ONCE_IF_SAFE
}

sealed class ParameterValidationResult {
    object Valid : ParameterValidationResult()
    data class Invalid(val reason: String) : ParameterValidationResult()
}

data class RegisteredTool(
    val name: String,
    val description: String,
    val riskLevel: ActionRiskLevel = ActionRiskLevel.LOW,
    val requiredParameters: List<String> = emptyList(),
    val parameterTypes: Map<String, ToolParamType> = emptyMap(),
    val requiredPermission: String? = null,
    val timeoutMs: Long = 5000L,
    val retryPolicy: ToolRetryPolicy = ToolRetryPolicy.NO_RETRY,
    val customValidator: ((JSONObject) -> ParameterValidationResult)? = null
)

/**
 * Single Source of Truth for Tool Definitions, Risk Levels, and Strict Parameter Validation.
 */
object RegisteredTools {

    const val TOOL_ORB_ROTATE = "orb.rotate"
    const val TOOL_ORB_SELECT = "orb.select"
    const val TOOL_ORB_CUSTOMIZE = "orb.customize"
    const val TOOL_ORB_RESET = "orb.reset"
    const val TOOL_OPEN_APP = "openApp"
    const val TOOL_SEARCH_WEB = "searchWeb"
    const val TOOL_CONTROL_DEVICE = "controlDevice"
    const val TOOL_CREATE_NOTE = "createNote"
    const val TOOL_PLAY_MUSIC = "playMusic"
    const val TOOL_CALL_CONTACT = "callContact"
    const val TOOL_SEND_WHATSAPP = "sendWhatsApp"
    const val TOOL_READ_SCREEN = "readScreen"
    const val TOOL_NAVIGATE = "navigate"

    val ALL_TOOLS: Map<String, RegisteredTool> = listOf(
        RegisteredTool(
            name = TOOL_ORB_ROTATE,
            description = "Rotates the TITONOX 3D orb along X, Y, or Z axis",
            riskLevel = ActionRiskLevel.LOW,
            requiredParameters = listOf("axis", "amount"),
            parameterTypes = mapOf(
                "axis" to ToolParamType.STRING,
                "amount" to ToolParamType.NUMBER
            ),
            timeoutMs = 2000L,
            retryPolicy = ToolRetryPolicy.RETRY_ONCE_IF_SAFE,
            customValidator = { params ->
                val axis = params.optString("axis", "").uppercase()
                if (axis !in listOf("X", "Y", "Z")) {
                    ParameterValidationResult.Invalid("Axis must be one of X, Y, or Z")
                } else {
                    val amount = params.optDouble("amount", Double.NaN)
                    if (amount.isNaN() || amount < -360.0 || amount > 360.0) {
                        ParameterValidationResult.Invalid("Rotation amount must be between -360 and +360 degrees")
                    } else {
                        ParameterValidationResult.Valid
                    }
                }
            }
        ),
        RegisteredTool(
            name = TOOL_ORB_SELECT,
            description = "Changes the active orb design by ID or theme name",
            riskLevel = ActionRiskLevel.LOW,
            requiredParameters = listOf("orbId"),
            parameterTypes = mapOf(
                "orbId" to ToolParamType.STRING,
                "orbType" to ToolParamType.STRING
            ),
            timeoutMs = 2000L
        ),
        RegisteredTool(
            name = TOOL_ORB_CUSTOMIZE,
            description = "Customizes orb visual parameters (color, glow, speed)",
            riskLevel = ActionRiskLevel.LOW,
            parameterTypes = mapOf(
                "color" to ToolParamType.STRING,
                "glow" to ToolParamType.NUMBER,
                "speed" to ToolParamType.NUMBER
            ),
            customValidator = { params ->
                if (params.has("glow")) {
                    val glow = params.optDouble("glow", -1.0)
                    if (glow < 0.0 || glow > 100.0) {
                        return@RegisteredTool ParameterValidationResult.Invalid("Glow intensity must be between 0 and 100")
                    }
                }
                if (params.has("speed")) {
                    val speed = params.optDouble("speed", -1.0)
                    if (speed < 0.1 || speed > 10.0) {
                        return@RegisteredTool ParameterValidationResult.Invalid("Animation speed must be between 0.1 and 10.0")
                    }
                }
                ParameterValidationResult.Valid
            }
        ),
        RegisteredTool(
            name = TOOL_ORB_RESET,
            description = "Resets orb orientation and theme to defaults",
            riskLevel = ActionRiskLevel.LOW,
            timeoutMs = 1500L
        ),
        RegisteredTool(
            name = TOOL_OPEN_APP,
            description = "Opens an installed Android application",
            riskLevel = ActionRiskLevel.LOW,
            requiredParameters = listOf("appName"),
            parameterTypes = mapOf("appName" to ToolParamType.STRING),
            timeoutMs = 4000L,
            retryPolicy = ToolRetryPolicy.RETRY_ONCE_IF_SAFE,
            customValidator = { params ->
                val app = params.optString("appName", "").trim()
                if (app.isEmpty()) {
                    ParameterValidationResult.Invalid("appName cannot be empty")
                } else if (app.length > 80) {
                    ParameterValidationResult.Invalid("appName is too long")
                } else {
                    ParameterValidationResult.Valid
                }
            }
        ),
        RegisteredTool(
            name = TOOL_SEARCH_WEB,
            description = "Performs a web or YouTube search",
            riskLevel = ActionRiskLevel.MEDIUM,
            requiredParameters = listOf("query"),
            parameterTypes = mapOf(
                "query" to ToolParamType.STRING,
                "target" to ToolParamType.STRING
            ),
            timeoutMs = 5000L,
            customValidator = { params ->
                val query = params.optString("query", "").trim()
                if (query.isEmpty()) {
                    ParameterValidationResult.Invalid("Search query cannot be empty")
                } else if (query.length > 200) {
                    ParameterValidationResult.Invalid("Search query exceeds 200 characters")
                } else {
                    ParameterValidationResult.Valid
                }
            }
        ),
        RegisteredTool(
            name = TOOL_CONTROL_DEVICE,
            description = "Adjusts device setting (e.g. flashlight, volume, wifi, settings)",
            riskLevel = ActionRiskLevel.MEDIUM,
            requiredParameters = listOf("setting", "action"),
            parameterTypes = mapOf(
                "setting" to ToolParamType.STRING,
                "action" to ToolParamType.STRING,
                "value" to ToolParamType.NUMBER
            ),
            customValidator = { params ->
                val setting = params.optString("setting", "").lowercase()
                if (setting in listOf("volume", "media_volume", "ring_volume")) {
                    val value = params.optInt("value", -1)
                    if (value !in 0..100) {
                        return@RegisteredTool ParameterValidationResult.Invalid("Volume percentage must be between 0 and 100")
                    }
                }
                ParameterValidationResult.Valid
            }
        ),
        RegisteredTool(
            name = TOOL_CREATE_NOTE,
            description = "Creates a persistent note in TITONOX Studio",
            riskLevel = ActionRiskLevel.MEDIUM,
            requiredParameters = listOf("content"),
            parameterTypes = mapOf(
                "title" to ToolParamType.STRING,
                "content" to ToolParamType.STRING
            ),
            customValidator = { params ->
                val content = params.optString("content", "").trim()
                if (content.isEmpty()) {
                    ParameterValidationResult.Invalid("Note content cannot be empty")
                } else if (content.length > 1000) {
                    ParameterValidationResult.Invalid("Note content exceeds maximum 1000 characters")
                } else {
                    ParameterValidationResult.Valid
                }
            }
        ),
        RegisteredTool(
            name = TOOL_PLAY_MUSIC,
            description = "Plays or pauses music tracks",
            riskLevel = ActionRiskLevel.LOW,
            parameterTypes = mapOf(
                "track" to ToolParamType.STRING,
                "action" to ToolParamType.STRING
            )
        ),
        RegisteredTool(
            name = TOOL_CALL_CONTACT,
            description = "Places a telephone call to a verified contact",
            riskLevel = ActionRiskLevel.HIGH,
            requiredParameters = listOf("contactName"),
            parameterTypes = mapOf(
                "contactName" to ToolParamType.STRING,
                "phoneNumber" to ToolParamType.STRING
            ),
            requiredPermission = "android.permission.CALL_PHONE",
            timeoutMs = 6000L,
            retryPolicy = ToolRetryPolicy.NO_RETRY
        ),
        RegisteredTool(
            name = TOOL_SEND_WHATSAPP,
            description = "Sends a WhatsApp message to a specific contact",
            riskLevel = ActionRiskLevel.HIGH,
            requiredParameters = listOf("contactName", "message"),
            parameterTypes = mapOf(
                "contactName" to ToolParamType.STRING,
                "message" to ToolParamType.STRING
            ),
            timeoutMs = 8000L,
            retryPolicy = ToolRetryPolicy.NO_RETRY,
            customValidator = { params ->
                val msg = params.optString("message", "").trim()
                if (msg.isEmpty()) {
                    ParameterValidationResult.Invalid("Message content cannot be empty")
                } else if (msg.length > 500) {
                    ParameterValidationResult.Invalid("Message length exceeds 500 characters")
                } else {
                    ParameterValidationResult.Valid
                }
            }
        ),
        RegisteredTool(
            name = TOOL_READ_SCREEN,
            description = "Takes a privacy-sanitized snapshot of active screen elements",
            riskLevel = ActionRiskLevel.LOW,
            timeoutMs = 3000L
        ),
        RegisteredTool(
            name = TOOL_NAVIGATE,
            description = "Navigates system UI (back, home, notifications, recents)",
            riskLevel = ActionRiskLevel.LOW,
            requiredParameters = listOf("action"),
            parameterTypes = mapOf("action" to ToolParamType.STRING)
        )
    ).associateBy { it.name }

    fun isRegistered(toolName: String): Boolean = ALL_TOOLS.containsKey(toolName)

    fun getTool(toolName: String): RegisteredTool? = ALL_TOOLS[toolName]

    /**
     * Strict parameter validation according to schema, required fields, and bounds.
     */
    fun validateParameters(toolName: String, params: JSONObject): ParameterValidationResult {
        val tool = getTool(toolName) ?: return ParameterValidationResult.Invalid("Unregistered tool: $toolName")

        // 1. Check required parameters
        for (req in tool.requiredParameters) {
            if (!params.has(req) || params.isNull(req)) {
                return ParameterValidationResult.Invalid("Missing required parameter: $req")
            }
            if (params.optString(req, "").trim().isEmpty()) {
                return ParameterValidationResult.Invalid("Required parameter cannot be empty: $req")
            }
        }

        // 2. Check parameter types and unknown parameters
        val keys = params.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val expectedType = tool.parameterTypes[key]
            if (expectedType != null) {
                when (expectedType) {
                    ToolParamType.STRING -> {
                        if (params.optString(key, null) == null) {
                            return ParameterValidationResult.Invalid("Parameter '$key' must be a String")
                        }
                    }
                    ToolParamType.NUMBER -> {
                        val num = params.optDouble(key, Double.NaN)
                        if (num.isNaN()) {
                            return ParameterValidationResult.Invalid("Parameter '$key' must be a Number")
                        }
                    }
                    ToolParamType.BOOLEAN -> {
                        if (!params.has(key)) {
                            return ParameterValidationResult.Invalid("Parameter '$key' must be a Boolean")
                        }
                    }
                    ToolParamType.OBJECT -> {
                        if (params.optJSONObject(key) == null) {
                            return ParameterValidationResult.Invalid("Parameter '$key' must be an Object")
                        }
                    }
                }
            }
        }

        // 3. Check custom boundary constraints
        val custom = tool.customValidator
        if (custom != null) {
            return custom(params)
        }

        return ParameterValidationResult.Valid
    }
}
