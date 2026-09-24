package com.example.automation

import android.content.Context
import android.content.Intent
import android.provider.Settings

class AccessibilityController(private val context: Context) {

    fun isServiceActive(): Boolean {
        return TitonoxAccessibilityService.isRunning()
    }

    fun isServiceConfigured(): Boolean {
        return TitonoxAccessibilityService.isAccessibilitySettingsEnabled(context)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun readCurrentScreen(): ScreenSummary? {
        val service = TitonoxAccessibilityService.instance ?: return null
        return service.readScreen()
    }

    fun takeStructuredSnapshot(): ScreenSnapshot? {
        val service = TitonoxAccessibilityService.instance ?: return null
        return service.takeStructuredSnapshot()
    }

    fun clickElement(target: String): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.findAndClick(target)
    }

    fun clickElementSafe(target: String): ElementClickResult {
        val service = TitonoxAccessibilityService.instance ?: return ElementClickResult.NotFound
        return service.findAndClickSafe(target)
    }

    fun enterText(text: String, hint: String? = null): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.findAndSetText(text, hint)
    }

    fun scroll(forward: Boolean = true): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.scroll(forward)
    }

    fun pressBack(): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.pressBack()
    }

    fun pressHome(): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.pressHome()
    }

    fun openNotifications(): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.openNotifications()
    }

    fun openQuickSettings(): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.openQuickSettings()
    }

    fun openRecents(): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.openRecents()
    }

    fun getCurrentForegroundPackage(): String {
        return TitonoxAccessibilityService.instance?.currentPackage ?: "Unknown"
    }

    fun detectExistingContent(keywordHint: String? = null): String? {
        val service = TitonoxAccessibilityService.instance ?: return null
        return service.detectExistingContent(keywordHint)
    }

    fun verifyElementPresent(targetText: String): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.verifyElementPresent(targetText)
    }

    fun verifyForegroundPackage(expectedPackage: String): Boolean {
        val service = TitonoxAccessibilityService.instance ?: return false
        return service.verifyForegroundPackage(expectedPackage)
    }
}
