package com.example.ai.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.example.automation.AccessibilityController
import com.example.data.AppDatabase
import com.example.data.TitonoxRepository
import com.example.device.DeviceController
import com.example.floating.OrbController
import com.example.floating.OrbDimension
import com.example.floating.OrbThemeType
import org.json.JSONObject
import java.net.URLEncoder
import java.util.Locale

/**
 * Result of safe tool execution in sandbox.
 */
data class ToolExecutionResult(
    val executed: Boolean,
    val toolName: String,
    val output: String? = null,
    val error: String? = null
)

/**
 * Safe Tool Router & Execution Sandbox.
 *
 * Enforces the strict security rule:
 * Gemini/AI output must NEVER directly execute arbitrary code.
 * All calls must be registered tools with validated parameters.
 */
class ToolRouter(
    private val context: Context,
    private val orbController: OrbController = OrbController.getInstance(context),
    private val deviceController: DeviceController = DeviceController(context),
    private val accessibilityController: AccessibilityController = AccessibilityController(context),
    private val repository: TitonoxRepository = TitonoxRepository(AppDatabase.getDatabase(context))
) {
    private val TAG = "ToolRouter"

    suspend fun executeTool(toolName: String, parameters: JSONObject): ToolExecutionResult {
        if (!RegisteredTools.isRegistered(toolName)) {
            Log.w(TAG, "Tool rejected: '$toolName' is not in the safe RegisteredTools registry.")
            return ToolExecutionResult(
                executed = false,
                toolName = toolName,
                output = "Action '$toolName' was rejected by security sandbox."
            )
        }

        val validation = RegisteredTools.validateParameters(toolName, parameters)
        if (validation is ParameterValidationResult.Invalid) {
            Log.w(TAG, "Tool parameters invalid for '$toolName': ${validation.reason}")
            return ToolExecutionResult(
                executed = false,
                toolName = toolName,
                output = "Validation error: ${validation.reason}"
            )
        }

        return try {
            when (toolName) {
                RegisteredTools.TOOL_ORB_ROTATE -> executeOrbRotate(parameters)
                RegisteredTools.TOOL_ORB_SELECT -> executeOrbSelect(parameters)
                RegisteredTools.TOOL_ORB_CUSTOMIZE -> executeOrbCustomize(parameters)
                RegisteredTools.TOOL_ORB_RESET -> executeOrbReset()
                RegisteredTools.TOOL_OPEN_APP -> executeOpenApp(parameters)
                RegisteredTools.TOOL_SEARCH_WEB -> executeSearchWeb(parameters)
                RegisteredTools.TOOL_CONTROL_DEVICE -> executeControlDevice(parameters)
                RegisteredTools.TOOL_CREATE_NOTE -> executeCreateNote(parameters)
                RegisteredTools.TOOL_CALL_CONTACT -> executeCallContact(parameters)
                RegisteredTools.TOOL_SEND_WHATSAPP -> executeSendWhatsApp(parameters)
                RegisteredTools.TOOL_READ_SCREEN -> executeReadScreen()
                RegisteredTools.TOOL_NAVIGATE -> executeNavigate(parameters)
                else -> ToolExecutionResult(
                    executed = false,
                    toolName = toolName,
                    output = "Tool '$toolName' is registered but has no execution handler."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Execution failure in tool $toolName", e)
            ToolExecutionResult(
                executed = false,
                toolName = toolName,
                error = e.localizedMessage ?: "Execution error"
            )
        }
    }

    private fun executeOrbRotate(params: JSONObject): ToolExecutionResult {
        val axis = params.optString("axis", "Y").uppercase(Locale.ROOT)
        val amount = params.optDouble("amount", 45.0).toFloat()

        when (axis) {
            "X" -> orbController.rotateX(amount)
            "Y" -> orbController.rotateY(amount)
            "Z" -> orbController.rotateZ(amount)
            else -> orbController.rotateY(amount)
        }

        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_ORB_ROTATE,
            output = "Orb rotated $amount degrees around $axis axis."
        )
    }

    private fun executeOrbSelect(params: JSONObject): ToolExecutionResult {
        val orbId = params.optString("orbId", "")
        val orbType = params.optString("orbType", "").uppercase(Locale.ROOT)

        if (orbType == "3D") {
            orbController.setOrbMode(OrbDimension.THREE_D)
        } else if (orbType == "2D") {
            orbController.setOrbMode(OrbDimension.TWO_D)
        }

        if (orbId.isNotBlank()) {
            val matchedTheme = OrbThemeType.values().firstOrNull {
                it.name.equals(orbId, ignoreCase = true) ||
                it.displayName.equals(orbId, ignoreCase = true)
            }
            if (matchedTheme != null) {
                orbController.selectOrb(matchedTheme)
                return ToolExecutionResult(
                    executed = true,
                    toolName = RegisteredTools.TOOL_ORB_SELECT,
                    output = "Selected orb: ${matchedTheme.displayName}."
                )
            } else {
                val found = orbController.selectOrbByName(orbId)
                if (found) {
                    return ToolExecutionResult(
                        executed = true,
                        toolName = RegisteredTools.TOOL_ORB_SELECT,
                        output = "Selected orb matching '$orbId'."
                    )
                }
            }
        }

        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_ORB_SELECT,
            output = "Orb mode updated."
        )
    }

    private fun executeOrbCustomize(params: JSONObject): ToolExecutionResult {
        var actionsCount = 0

        if (params.has("color")) {
            val colorStr = params.optString("color")
            val parsedColor = parseColor(colorStr)
            if (parsedColor != null) {
                orbController.setPrimaryColor(parsedColor)
                actionsCount++
            }
        }

        if (params.has("glow")) {
            val glow = params.optDouble("glow").toFloat()
            orbController.setGlowIntensity(glow)
            actionsCount++
        }

        if (params.has("speed")) {
            val speed = params.optDouble("speed").toFloat()
            orbController.setAnimationSpeed(speed)
            actionsCount++
        }

        if (params.has("density")) {
            val density = params.optInt("density")
            orbController.setParticleDensity(density)
            actionsCount++
        }

        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_ORB_CUSTOMIZE,
            output = "Applied $actionsCount customization adjustments to the orb."
        )
    }

    private fun executeOrbReset(): ToolExecutionResult {
        orbController.resetRotation(smooth = true)
        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_ORB_RESET,
            output = "Orb position and rotation reset to defaults."
        )
    }

    private fun executeOpenApp(params: JSONObject): ToolExecutionResult {
        val appName = params.optString("appName")
        val result = deviceController.launchAppByName(appName)
        return ToolExecutionResult(
            executed = result.success,
            toolName = RegisteredTools.TOOL_OPEN_APP,
            output = if (result.success) "Opened $appName." else "Could not open $appName. Please verify it is installed."
        )
    }

    private fun executeSearchWeb(params: JSONObject): ToolExecutionResult {
        val query = params.optString("query")
        val target = params.optString("target", "").lowercase(Locale.ROOT)
        if (target.contains("youtube")) {
            deviceController.searchYouTube(query)
        } else {
            deviceController.openWebSearch(query)
        }
        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_SEARCH_WEB,
            output = "Searching for '$query'."
        )
    }

    private fun executeControlDevice(params: JSONObject): ToolExecutionResult {
        val setting = params.optString("setting").lowercase(Locale.ROOT)
        val action = params.optString("action").lowercase(Locale.ROOT)

        if (setting.contains("flashlight") || setting.contains("torch")) {
            val turnOn = action.contains("on") || action.contains("enable")
            deviceController.toggleFlashlight(turnOn)
            return ToolExecutionResult(
                executed = true,
                toolName = RegisteredTools.TOOL_CONTROL_DEVICE,
                output = "Flashlight ${if (turnOn) "turned on" else "turned off"}."
            )
        }

        if (setting.contains("volume")) {
            val value = params.optInt("value", 50)
            deviceController.setVolumePercentage(value, android.media.AudioManager.STREAM_MUSIC)
            return ToolExecutionResult(
                executed = true,
                toolName = RegisteredTools.TOOL_CONTROL_DEVICE,
                output = "Media volume set to $value%."
            )
        }

        if (setting.contains("settings")) {
            deviceController.openSettings()
            return ToolExecutionResult(
                executed = true,
                toolName = RegisteredTools.TOOL_CONTROL_DEVICE,
                output = "System settings opened."
            )
        }

        return ToolExecutionResult(
            executed = false,
            toolName = RegisteredTools.TOOL_CONTROL_DEVICE,
            output = "Device control setting '$setting' not supported."
        )
    }

    private suspend fun executeCreateNote(params: JSONObject): ToolExecutionResult {
        val title = params.optString("title", "Quick Note")
        val content = params.optString("content", "")
        repository.saveNote(title = title, content = content)
        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_CREATE_NOTE,
            output = "Note '$title' saved in TITONOX Studio."
        )
    }

    private fun executeCallContact(params: JSONObject): ToolExecutionResult {
        val contactName = params.optString("contactName", "")
        val directPhone = params.optString("phoneNumber", "")

        val phoneToCall = if (directPhone.isNotBlank()) {
            directPhone
        } else {
            val contacts = deviceController.searchContacts(contactName)
            if (contacts.isEmpty()) {
                return ToolExecutionResult(
                    executed = false,
                    toolName = RegisteredTools.TOOL_CALL_CONTACT,
                    output = "No contact found matching '$contactName'."
                )
            }
            contacts.first().phoneNumber
        }

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneToCall")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(dialIntent)

        return ToolExecutionResult(
            executed = true,
            toolName = RegisteredTools.TOOL_CALL_CONTACT,
            output = "Dialer initiated for $contactName ($phoneToCall)."
        )
    }

    private fun executeSendWhatsApp(params: JSONObject): ToolExecutionResult {
        val contactName = params.optString("contactName", "")
        val message = params.optString("message", "")

        val contacts = deviceController.searchContacts(contactName)
        val phone = if (contacts.isNotEmpty()) contacts.first().phoneNumber else ""

        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val encodedText = URLEncoder.encode(message, "UTF-8")

        val intent = if (cleanPhone.isNotEmpty()) {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        return try {
            context.startActivity(intent)
            ToolExecutionResult(
                executed = true,
                toolName = RegisteredTools.TOOL_SEND_WHATSAPP,
                output = "WhatsApp chat initiated for $contactName."
            )
        } catch (e: Exception) {
            ToolExecutionResult(
                executed = false,
                toolName = RegisteredTools.TOOL_SEND_WHATSAPP,
                error = "WhatsApp is not installed on this device."
            )
        }
    }

    private fun executeReadScreen(): ToolExecutionResult {
        val snapshot = accessibilityController.takeStructuredSnapshot()
        return if (snapshot != null) {
            ToolExecutionResult(
                executed = true,
                toolName = RegisteredTools.TOOL_READ_SCREEN,
                output = "Screen inspected: ${snapshot.packageName} (${snapshot.clickableElements.size} interactive elements, ${snapshot.editableFields.size} inputs)."
            )
        } else {
            ToolExecutionResult(
                executed = false,
                toolName = RegisteredTools.TOOL_READ_SCREEN,
                output = "Accessibility service is not active. Please enable in Settings."
            )
        }
    }

    private fun executeNavigate(params: JSONObject): ToolExecutionResult {
        val action = params.optString("action", "").lowercase(Locale.ROOT)
        val success = when (action) {
            "back" -> accessibilityController.pressBack()
            "home" -> accessibilityController.pressHome()
            "notifications" -> accessibilityController.openNotifications()
            "recents" -> accessibilityController.openRecents()
            "quick_settings" -> accessibilityController.openQuickSettings()
            else -> false
        }
        return ToolExecutionResult(
            executed = success,
            toolName = RegisteredTools.TOOL_NAVIGATE,
            output = "Navigation action '$action' executed: $success."
        )
    }

    private fun parseColor(colorStr: String): Color? {
        val clean = colorStr.trim().uppercase(Locale.ROOT)
        return when {
            clean.startsWith("#") -> {
                try {
                    val colorInt = android.graphics.Color.parseColor(clean)
                    Color(colorInt)
                } catch (e: Exception) {
                    null
                }
            }
            clean == "CYAN" || clean.contains("SKY") -> Color(0xFF00E5FF)
            clean == "BLUE" -> Color(0xFF2979FF)
            clean == "PURPLE" || clean == "VIOLET" -> Color(0xFFA855F7)
            clean == "RED" -> Color(0xFFFF3366)
            clean == "GREEN" -> Color(0xFF00E676)
            clean == "AMBER" || clean == "ORANGE" || clean == "GOLD" -> Color(0xFFFFB300)
            clean == "WHITE" -> Color.White
            else -> null
        }
    }
}
