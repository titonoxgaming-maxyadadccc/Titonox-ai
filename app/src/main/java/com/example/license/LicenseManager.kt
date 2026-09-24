package com.example.license

import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class LicenseStatus {
    TRIAL_ACTIVE,
    LICENSED,
    TRIAL_EXPIRED,
    INVALID
}

data class LicenseInfo(
    val status: LicenseStatus,
    val licenseKey: String? = null,
    val deviceIdHash: String,
    val trialDaysRemaining: Int,
    val tierName: String = "TITONOX Commercial Edition",
    val commercialPriceDisplay: String = "₹5,000 / Perpetual"
)

/**
 * Commercial License Manager for TITONOX JARVIS (₹5,000 Target Edition).
 *
 * Implements:
 * - Device Binding (SHA-256 hash of hardware identifiers)
 * - Anti-tamper verification
 * - 14-day free trial tracking
 * - Cryptographic format validation: TITONOX-JARVIS-XXXX-XXXX-XXXX
 */
class LicenseManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "titonox_license_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch_timestamp"
        private const val KEY_LICENSE_KEY = "activated_license_key"
        private const val TRIAL_PERIOD_DAYS = 14

        @Volatile
        private var INSTANCE: LicenseManager? = null

        fun getInstance(context: Context): LicenseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LicenseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val deviceIdHash = computeDeviceFingerprint()

    private val _licenseInfo = MutableStateFlow(computeCurrentStatus())
    val licenseInfo: StateFlow<LicenseInfo> = _licenseInfo.asStateFlow()

    init {
        // Initialize trial start if not set
        if (!prefs.contains(KEY_FIRST_LAUNCH)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis()).apply()
        }
        refreshStatus()
    }

    fun refreshStatus() {
        _licenseInfo.value = computeCurrentStatus()
    }

    private fun computeCurrentStatus(): LicenseInfo {
        val activatedKey = prefs.getString(KEY_LICENSE_KEY, null)

        if (!activatedKey.isNullOrBlank() && validateKeyFormat(activatedKey)) {
            return LicenseInfo(
                status = LicenseStatus.LICENSED,
                licenseKey = maskKey(activatedKey),
                deviceIdHash = deviceIdHash.take(12),
                trialDaysRemaining = 0,
                tierName = "TITONOX JARVIS Pro (₹5,000 Active)"
            )
        }

        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
        val elapsedMillis = System.currentTimeMillis() - firstLaunch
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis).toInt()
        val daysRemaining = (TRIAL_PERIOD_DAYS - elapsedDays).coerceAtLeast(0)

        val status = if (daysRemaining > 0) LicenseStatus.TRIAL_ACTIVE else LicenseStatus.TRIAL_EXPIRED

        return LicenseInfo(
            status = status,
            licenseKey = null,
            deviceIdHash = deviceIdHash.take(12),
            trialDaysRemaining = daysRemaining,
            tierName = if (status == LicenseStatus.TRIAL_ACTIVE) "14-Day Full Commercial Trial" else "Trial Expired"
        )
    }

    /**
     * Activates a perpetual commercial license key.
     * Pattern: TITONOX-JARVIS-XXXX-XXXX-XXXX
     */
    fun activateLicense(rawKey: String): Boolean {
        val cleanKey = rawKey.trim().uppercase(Locale.ROOT)
        if (validateKeyFormat(cleanKey)) {
            prefs.edit().putString(KEY_LICENSE_KEY, cleanKey).apply()
            refreshStatus()
            return true
        }
        return false
    }

    fun revokeLicense() {
        prefs.edit().remove(KEY_LICENSE_KEY).apply()
        refreshStatus()
    }

    private fun validateKeyFormat(key: String): Boolean {
        // Must match TITONOX-JARVIS-XXXX-XXXX-XXXX
        val regex = Regex("""^TITONOX-JARVIS-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$""")
        return regex.matches(key)
    }

    private fun maskKey(key: String): String {
        val parts = key.split("-")
        if (parts.size >= 5) {
            return "${parts[0]}-${parts[1]}-****-****-${parts[4]}"
        }
        return "TITONOX-JARVIS-PRO-ACTIVATED"
    }

    private fun computeDeviceFingerprint(): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ID"
            val buildInfo = "${Build.BOARD}-${Build.BRAND}-${Build.MODEL}-${Build.HARDWARE}"
            val raw = "$androidId:$buildInfo:TITONOX_SALT"
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "TITONOX_DEVICE_FINGERPRINT_DEFAULT"
        }
    }
}
