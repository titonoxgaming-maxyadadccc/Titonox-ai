package com.example.floating

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.util.Locale

/**
 * Result returned by the OrbIntentRouter when processing a command.
 */
data class OrbActionResult(
    val handled: Boolean,
    val intentName: String? = null,
    val feedbackMessage: String? = null
)

/**
 * Real Voice / Natural Language Action Router for TITONOX Orb.
 *
 * Routes spoken or typed commands directly to the centralized OrbController:
 * VOICE / TEXT -> Intent Parser -> Orb Action -> OrbController -> Real Execution
 *
 * Fully supports Hindi, Hinglish, and English commands:
 * - "Orb ghumao", "Orb rotate karo", "Rotate the orb"
 * - "Orb ko right ghumao", "Rotate right", "Orb ko daayein ghumao"
 * - "Orb ko left ghumao", "Rotate left", "Orb ko baayein ghumao"
 * - "Orb ko upar ghumao", "Rotate up"
 * - "Orb ko neeche ghumao", "Rotate down"
 * - "Orb reset karo", "Reset orb"
 * - "Agla orb", "Next orb", "Orb change karo"
 * - "Pichhla orb", "Previous orb"
 * - "3D orb lagao", "Switch to 3D"
 * - "2D orb lagao", "Switch to 2D"
 * - "Random orb", "Koi bhi orb lagao"
 * - "Plasma sphere lagao", "Digital eye lagao", "Quantum reactor lagao", etc.
 * - "2D number 5", "3D number 10"
 * - "Orb ko blue karo", "Orb ko cyan karo", "Change orb color to red"
 * - "Orb ki speed badhao", "Speed kam karo"
 * - "Orb ki glow badhao", "Orb ki brightness kam karo"
 */
class OrbIntentRouter(
    private val context: Context,
    val controller: OrbController = OrbController.getInstance(context)
) {

    fun handleCommand(input: String): OrbActionResult {
        val query = input.trim().lowercase(Locale.ROOT)
        if (query.isEmpty()) return OrbActionResult(handled = false)

        // 1. ROTATION INTENTS
        if (matchesAny(query, "orb reset karo", "orb ko reset karo", "reset orb", "orb reset", "reset rotation", "orb ki position reset", "rotation reset", "default orientation")) {
            controller.resetRotation(smooth = false)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_RESET_ROTATION",
                feedbackMessage = "Sir, orb rotation reset kar di gayi hai."
            )
        }

        if (matchesAny(query, "orb ko right ghumao", "rotate right", "orb right", "right rotate", "right ghumao", "daayein ghumao")) {
            controller.rotateY(55f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_ROTATE_RIGHT",
                feedbackMessage = "Orb ko right rotate kiya gaya hai, Sir."
            )
        }

        if (matchesAny(query, "orb ko left ghumao", "rotate left", "orb left", "left rotate", "left ghumao", "baayein ghumao")) {
            controller.rotateY(-55f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_ROTATE_LEFT",
                feedbackMessage = "Orb ko left rotate kiya gaya hai, Sir."
            )
        }

        if (matchesAny(query, "orb ko upar ghumao", "rotate up", "orb up", "upar rotate", "upar ghumao", "rotate upwards")) {
            controller.rotateX(40f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_ROTATE_UP",
                feedbackMessage = "Orb ko upar rotate kiya gaya hai, Sir."
            )
        }

        if (matchesAny(query, "orb ko neeche ghumao", "rotate down", "orb down", "neeche rotate", "neeche ghumao", "rotate downwards")) {
            controller.rotateX(-40f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_ROTATE_DOWN",
                feedbackMessage = "Orb ko neeche rotate kiya gaya hai, Sir."
            )
        }

        if (matchesAny(query, "orb ghumao", "orb rotate karo", "rotate the orb", "orb ko spin karo", "spin orb", "rotate orb")) {
            controller.rotateY(75f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_ROTATE",
                feedbackMessage = "Orb ko spin kar diya gaya hai, Sir."
            )
        }

        // 2. ORB SELECTION & NAVIGATION INTENTS
        if (matchesAny(query, "next orb", "agla orb", "orb change karo", "change orb", "doosra orb")) {
            controller.nextOrb()
            val currentName = controller.settings.value.theme.displayName
            return OrbActionResult(
                handled = true,
                intentName = "ORB_NEXT",
                feedbackMessage = "Next orb apply ho gaya hai: $currentName."
            )
        }

        if (matchesAny(query, "previous orb", "pichhla orb", "purana orb", "back orb", "last orb")) {
            controller.previousOrb()
            val currentName = controller.settings.value.theme.displayName
            return OrbActionResult(
                handled = true,
                intentName = "ORB_PREVIOUS",
                feedbackMessage = "Previous orb apply ho gaya hai: $currentName."
            )
        }

        if (matchesAny(query, "random orb", "koi bhi orb", "random orb lagao", "koi bhi orb set karo")) {
            val allThemes = OrbThemeType.values()
            val randomTheme = allThemes.random()
            controller.selectOrb(randomTheme)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_RANDOM",
                feedbackMessage = "Random orb apply kiya gaya: ${randomTheme.displayName}."
            )
        }

        // 3. DIMENSION MODE SWITCHING
        if (matchesAny(query, "3d orb lagao", "3d orb set karo", "switch to 3d", "3d mode", "3d orb")) {
            controller.setOrbMode(OrbDimension.THREE_D)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_MODE_3D",
                feedbackMessage = "3D orb mode activate kar diya gaya hai, Sir."
            )
        }

        if (matchesAny(query, "2d orb lagao", "2d orb set karo", "switch to 2d", "2d mode", "2d orb")) {
            controller.setOrbMode(OrbDimension.TWO_D)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_MODE_2D",
                feedbackMessage = "2D orb mode activate kar diya gaya hai, Sir."
            )
        }

        // 4. SPECIFIC NUMBER SELECTIONS ("2D number 5", "3D number 10")
        val num2DMatch = Regex("""2d\s+(?:number\s+)?(\d+)""").find(query)
        if (num2DMatch != null) {
            val num = num2DMatch.groupValues[1].toIntOrNull() ?: 1
            val twoDThemes = OrbThemeType.values().filter { it.dimension == OrbDimension.TWO_D }
            val index = (num - 1).coerceIn(0, twoDThemes.size - 1)
            val selected = twoDThemes[index]
            controller.selectOrb(selected)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_SELECT_INDEX",
                feedbackMessage = "2D Orb #${num} (${selected.displayName}) select ho gaya hai."
            )
        }

        val num3DMatch = Regex("""3d\s+(?:number\s+)?(\d+)""").find(query)
        if (num3DMatch != null) {
            val num = num3DMatch.groupValues[1].toIntOrNull() ?: 1
            val threeDThemes = OrbThemeType.values().filter { it.dimension == OrbDimension.THREE_D }
            val index = (num - 1).coerceIn(0, threeDThemes.size - 1)
            val selected = threeDThemes[index]
            controller.selectOrb(selected)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_SELECT_INDEX",
                feedbackMessage = "3D Orb #${num} (${selected.displayName}) select ho gaya hai."
            )
        }

        // 5. NAMED ORB SELECTION
        for (theme in OrbThemeType.values()) {
            val cleanTheme = theme.displayName.lowercase(Locale.ROOT)
            val cleanId = theme.name.lowercase(Locale.ROOT).replace("_", " ")
            if (query.contains(cleanTheme) || query.contains(cleanId)) {
                controller.selectOrb(theme)
                return OrbActionResult(
                    handled = true,
                    intentName = "ORB_SELECT_NAMED",
                    feedbackMessage = "${theme.displayName} orb activate ho gaya hai, Sir."
                )
            }
        }

        // 6. COLOR INTENTS
        val colorResult = parseColorIntent(query)
        if (colorResult != null) {
            controller.setPrimaryColor(colorResult.first)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_SET_COLOR",
                feedbackMessage = "Orb ka color ${colorResult.second} kar diya gaya hai."
            )
        }

        // 7. SPEED & GLOW ADJUSTMENTS
        if (matchesAny(query, "speed badhao", "increase speed", "orb ki speed badhao", "speed badha do")) {
            val cur = controller.settings.value.animationSpeed
            controller.setAnimationSpeed(cur + 0.35f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_SPEED_UP",
                feedbackMessage = "Orb animation speed badha di gayi hai."
            )
        }

        if (matchesAny(query, "speed kam karo", "decrease speed", "orb ki speed kam karo", "speed ghatao")) {
            val cur = controller.settings.value.animationSpeed
            controller.setAnimationSpeed(cur - 0.35f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_SPEED_DOWN",
                feedbackMessage = "Orb animation speed kam kar di gayi hai."
            )
        }

        if (matchesAny(query, "glow badhao", "increase glow", "orb ki glow badhao", "brightness badhao")) {
            val cur = controller.settings.value.glowIntensity
            controller.setGlowIntensity(cur + 0.25f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_GLOW_UP",
                feedbackMessage = "Orb glow intensity badha di gayi hai."
            )
        }

        if (matchesAny(query, "glow kam karo", "decrease glow", "orb ki glow kam karo", "brightness kam karo")) {
            val cur = controller.settings.value.glowIntensity
            controller.setGlowIntensity(cur - 0.25f)
            return OrbActionResult(
                handled = true,
                intentName = "ORB_GLOW_DOWN",
                feedbackMessage = "Orb glow intensity kam kar di gayi hai."
            )
        }

        return OrbActionResult(handled = false)
    }

    private fun matchesAny(input: String, vararg candidates: String): Boolean {
        return candidates.any { input.contains(it) }
    }

    private fun parseColorIntent(query: String): Pair<Color, String>? {
        if (!query.contains("color") && !query.contains("karo") && !query.contains("kar do") && !query.contains("blue") && !query.contains("red") && !query.contains("green") && !query.contains("cyan") && !query.contains("purple") && !query.contains("gold") && !query.contains("orange") && !query.contains("white")) {
            return null
        }

        return when {
            query.contains("cyan") || query.contains("sky") -> Pair(Color(0xFF00E5FF), "Cyan")
            query.contains("blue") || query.contains("neela") -> Pair(Color(0xFF2979FF), "Blue")
            query.contains("red") || query.contains("laal") -> Pair(Color(0xFFFF1744), "Red")
            query.contains("green") || query.contains("hara") -> Pair(Color(0xFF00E676), "Green")
            query.contains("purple") || query.contains("baingani") || query.contains("violet") -> Pair(Color(0xFFD500F9), "Purple")
            query.contains("gold") || query.contains("yellow") || query.contains("peela") -> Pair(Color(0xFFFFD600), "Gold")
            query.contains("orange") || query.contains("narangi") -> Pair(Color(0xFFFF6D00), "Orange")
            query.contains("white") || query.contains("safed") -> Pair(Color(0xFFFFFFFF), "White")
            else -> null
        }
    }
}
