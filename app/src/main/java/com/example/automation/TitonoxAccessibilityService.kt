package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

data class UIElementTarget(
    val id: Int,
    val text: String,
    val contentDescription: String?,
    val className: String,
    val isClickable: Boolean,
    val bounds: Rect
)

data class UIInputFieldTarget(
    val id: Int,
    val hint: String?,
    val currentText: String?,
    val isPassword: Boolean
)

data class ScreenSnapshot(
    val packageName: String,
    val activityName: String?,
    val sanitizedTexts: List<String>,
    val clickableElements: List<UIElementTarget>,
    val editableFields: List<UIInputFieldTarget>,
    val isSensitiveScreen: Boolean,
    val totalNodesScanned: Int
)

data class ScreenSummary(
    val packageName: String,
    val textElements: List<String>,
    val buttonLabels: List<String>,
    val inputFields: List<String>,
    val totalNodes: Int
)

sealed class ElementClickResult {
    object Success : ElementClickResult()
    object NotFound : ElementClickResult()
    data class Ambiguous(val matchingLabels: List<String>) : ElementClickResult()
}

class TitonoxAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TitonoxAccessibility"

        @Volatile
        var instance: TitonoxAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null

        fun isAccessibilitySettingsEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${TitonoxAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(expectedServiceName)
        }

        // Privacy Redaction regex patterns
        private val SENSITIVE_PATTERNS = listOf(
            Regex("""\b(?:\d[ -]?){13,16}\b"""),               // Card numbers
            Regex("""\b\d{3,4}\b"""),                           // CVV/PIN (when in suspicious field)
            Regex("""(?i)\b(?:otp|one time password|pin|code)\s*[:=]?\s*\d{4,8}\b""") // OTP
        )

        fun redactSensitiveText(text: String, isPasswordNode: Boolean = false): String {
            if (isPasswordNode) return "[REDACTED_PASSWORD]"
            var redacted = text
            for (p in SENSITIVE_PATTERNS) {
                redacted = p.replace(redacted, "[REDACTED_SENSITIVE]")
            }
            return redacted
        }
    }

    var currentPackage: String = ""
        private set
    var currentActivity: String? = null
        private set

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "TITONOX Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        event.packageName?.let {
            currentPackage = it.toString()
        }
        event.className?.let {
            currentActivity = it.toString()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "TITONOX Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    /**
     * Creates a privacy-sanitized structured snapshot of the screen with bounded nodes and depth.
     */
    fun takeStructuredSnapshot(): ScreenSnapshot {
        val root = rootInActiveWindow ?: return ScreenSnapshot(
            packageName = currentPackage.ifEmpty { "Unknown" },
            activityName = currentActivity,
            sanitizedTexts = emptyList(),
            clickableElements = emptyList(),
            editableFields = emptyList(),
            isSensitiveScreen = false,
            totalNodesScanned = 0
        )

        val texts = mutableListOf<String>()
        val clickable = mutableListOf<UIElementTarget>()
        val editable = mutableListOf<UIInputFieldTarget>()
        var count = 0
        var hasSensitive = false

        val maxDepth = 8
        val maxNodes = 75

        fun traverse(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || count >= maxNodes || depth > maxDepth) return
            count++

            val isPassword = node.isPassword
            if (isPassword) hasSensitive = true

            val rawText = node.text?.toString()?.trim()
            val rawDesc = node.contentDescription?.toString()?.trim()
            val className = node.className?.toString() ?: ""

            val text = if (!rawText.isNullOrEmpty()) redactSensitiveText(rawText, isPassword) else null
            val desc = if (!rawDesc.isNullOrEmpty()) redactSensitiveText(rawDesc, isPassword) else null
            val label = text ?: desc

            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            if (node.isClickable || node.isCheckable) {
                if (!label.isNullOrEmpty()) {
                    clickable.add(
                        UIElementTarget(
                            id = count,
                            text = label,
                            contentDescription = desc,
                            className = className,
                            isClickable = true,
                            bounds = bounds
                        )
                    )
                }
            } else if (!label.isNullOrEmpty()) {
                texts.add(label)
            }

            if (node.isEditable) {
                val hint = node.hintText?.toString()?.let { redactSensitiveText(it, false) }
                editable.add(
                    UIInputFieldTarget(
                        id = count,
                        hint = hint,
                        currentText = text,
                        isPassword = isPassword
                    )
                )
            }

            for (i in 0 until node.childCount) {
                traverse(node.getChild(i), depth + 1)
            }
        }

        try {
            traverse(root, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating snapshot", e)
        }

        return ScreenSnapshot(
            packageName = currentPackage.ifEmpty { root.packageName?.toString() ?: "Unknown" },
            activityName = currentActivity,
            sanitizedTexts = texts.distinct().take(35),
            clickableElements = clickable.distinctBy { it.text }.take(30),
            editableFields = editable.take(10),
            isSensitiveScreen = hasSensitive,
            totalNodesScanned = count
        )
    }

    /**
     * Backward-compatible simplified screen summary for legacy callers.
     */
    fun readScreen(): ScreenSummary {
        val snapshot = takeStructuredSnapshot()
        return ScreenSummary(
            packageName = snapshot.packageName,
            textElements = snapshot.sanitizedTexts,
            buttonLabels = snapshot.clickableElements.map { it.text },
            inputFields = snapshot.editableFields.map { it.hint ?: "Editable Field" },
            totalNodes = snapshot.totalNodesScanned
        )
    }

    /**
     * Safe element clicker. Evaluates matches and safeguards against ambiguity.
     */
    fun findAndClickSafe(targetText: String, preferExact: Boolean = false): ElementClickResult {
        val root = rootInActiveWindow ?: return ElementClickResult.NotFound
        val targetClean = targetText.lowercase().trim()

        val matchingNodes = mutableListOf<AccessibilityNodeInfo>()

        fun search(node: AccessibilityNodeInfo?) {
            if (node == null) return

            val text = node.text?.toString()?.lowercase()?.trim() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase()?.trim() ?: ""

            val exactMatch = text == targetClean || desc == targetClean
            val partialMatch = !preferExact && (text.contains(targetClean) || desc.contains(targetClean))

            if (exactMatch || partialMatch) {
                if (node.isClickable) {
                    matchingNodes.add(node)
                } else if (node.parent?.isClickable == true) {
                    matchingNodes.add(node.parent)
                }
            }

            for (i in 0 until node.childCount) {
                search(node.getChild(i))
            }
        }

        search(root)

        if (matchingNodes.isEmpty()) {
            return ElementClickResult.NotFound
        }

        // If multiple distinct elements matched, check if one is an exact match
        if (matchingNodes.size > 1) {
            val exactNodes = matchingNodes.filter {
                val t = it.text?.toString()?.lowercase()?.trim() ?: ""
                val d = it.contentDescription?.toString()?.lowercase()?.trim() ?: ""
                t == targetClean || d == targetClean
            }
            if (exactNodes.size == 1) {
                val success = exactNodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return if (success) ElementClickResult.Success else ElementClickResult.NotFound
            }

            val labels = matchingNodes.map {
                it.text?.toString() ?: it.contentDescription?.toString() ?: "Unnamed Button"
            }.distinct()

            if (labels.size > 1) {
                return ElementClickResult.Ambiguous(labels)
            }
        }

        val clicked = matchingNodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return if (clicked) ElementClickResult.Success else ElementClickResult.NotFound
    }

    /**
     * Legacy click method returning boolean.
     */
    fun findAndClick(targetText: String): Boolean {
        return when (findAndClickSafe(targetText)) {
            is ElementClickResult.Success -> true
            else -> false
        }
    }

    /**
     * Safe text entry. Verifies target node is editable and resolves correct field.
     */
    fun findAndSetText(query: String, targetFieldHint: String? = null): Boolean {
        val root = rootInActiveWindow ?: return false
        val editableNodes = mutableListOf<AccessibilityNodeInfo>()

        fun collectEditables(node: AccessibilityNodeInfo?) {
            if (node == null) return
            if (node.isEditable) {
                editableNodes.add(node)
            }
            for (i in 0 until node.childCount) {
                collectEditables(node.getChild(i))
            }
        }

        collectEditables(root)
        if (editableNodes.isEmpty()) return false

        val targetField: AccessibilityNodeInfo = if (targetFieldHint.isNullOrBlank()) {
            // Prefer currently focused editable field, otherwise the first
            editableNodes.firstOrNull { it.isFocused } ?: editableNodes.first()
        } else {
            val hintLower = targetFieldHint.lowercase()
            editableNodes.firstOrNull { node ->
                val hint = node.hintText?.toString()?.lowercase() ?: ""
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""
                hint.contains(hintLower) || text.contains(hintLower) || desc.contains(hintLower)
            } ?: editableNodes.first()
        }

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, query)
        }
        targetField.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        return targetField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    fun scroll(forward: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (forward) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }

        var scrolled = false
        fun searchScrollable(node: AccessibilityNodeInfo?): Boolean {
            if (node == null || scrolled) return false
            if (node.isScrollable) {
                val res = node.performAction(action)
                if (res) {
                    scrolled = true
                    return true
                }
            }
            for (i in 0 until node.childCount) {
                if (searchScrollable(node.getChild(i))) return true
            }
            return false
        }

        searchScrollable(root)
        return scrolled
    }

    fun pressBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun pressHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun openQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    fun openRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun detectExistingContent(keywordHint: String? = null): String? {
        val root = rootInActiveWindow ?: return null
        var foundContent: String? = null

        fun inspect(node: AccessibilityNodeInfo?) {
            if (node == null || foundContent != null) return

            val text = node.text?.toString()?.trim()
            val desc = node.contentDescription?.toString()?.trim()
            val candidate = text ?: desc

            if (!candidate.isNullOrBlank() && candidate.length > 2) {
                if (keywordHint != null) {
                    if (candidate.contains(keywordHint, ignoreCase = true)) {
                        foundContent = candidate
                        return
                    }
                } else if (node.isEditable && candidate.isNotEmpty()) {
                    foundContent = candidate
                    return
                }
            }

            for (i in 0 until node.childCount) {
                inspect(node.getChild(i))
            }
        }

        try {
            inspect(root)
        } catch (e: Exception) {
            Log.w(TAG, "Error inspecting existing content", e)
        }
        return foundContent
    }

    fun verifyElementPresent(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val targetLower = targetText.lowercase().trim()
        var exists = false

        fun search(node: AccessibilityNodeInfo?) {
            if (node == null || exists) return
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            if (text.contains(targetLower) || desc.contains(targetLower)) {
                exists = true
                return
            }
            for (i in 0 until node.childCount) {
                search(node.getChild(i))
            }
        }

        try {
            search(root)
        } catch (e: Exception) {
            Log.w(TAG, "Error verifying element", e)
        }
        return exists
    }

    fun verifyForegroundPackage(expectedPackage: String): Boolean {
        val current = currentPackage.lowercase()
        return current.contains(expectedPackage.lowercase())
    }
}
