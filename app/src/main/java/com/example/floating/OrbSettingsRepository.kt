package com.example.floating

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrbSettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("titonox_orb_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<OrbCustomization> = _settings.asStateFlow()

    private val _lastPosX = MutableStateFlow(prefs.getInt("orb_pos_x", 100))
    val lastPosX: StateFlow<Int> = _lastPosX.asStateFlow()

    private val _lastPosY = MutableStateFlow(prefs.getInt("orb_pos_y", 300))
    val lastPosY: StateFlow<Int> = _lastPosY.asStateFlow()

    private val _isPowerOff = MutableStateFlow(prefs.getBoolean("titonox_power_off", false))
    val isPowerOff: StateFlow<Boolean> = _isPowerOff.asStateFlow()

    private fun loadSettings(): OrbCustomization {
        val themeName = prefs.getString("orb_theme", OrbThemeType.TITONOX_3D_CORE.name)
        val theme = OrbThemeType.fromLegacyOrName(themeName)

        val glowName = prefs.getString("orb_glow", GlowPreset.HIGH.name) ?: GlowPreset.HIGH.name
        val glow = try {
            GlowPreset.valueOf(glowName)
        } catch (e: Exception) {
            GlowPreset.HIGH
        }

        val personaName = prefs.getString("voice_persona", VoicePersona.CORE.name) ?: VoicePersona.CORE.name
        val persona = try {
            VoicePersona.valueOf(personaName)
        } catch (e: Exception) {
            VoicePersona.CORE
        }

        return OrbCustomization(
            theme = theme,
            primaryColor = prefs.getLong("orb_color_primary", 0xFF00E5FF),
            secondaryColor = prefs.getLong("orb_color_secondary", 0xFF00B0FF),
            glowColor = prefs.getLong("orb_color_glow", 0xFF80D8FF),
            particleColor = prefs.getLong("orb_color_particle", 0xFFF0F6FC),
            ringColor = prefs.getLong("orb_color_ring", 0xFF2979FF),
            backgroundGlow = prefs.getBoolean("orb_bg_glow", true),
            glowIntensity = prefs.getFloat("orb_glow_intensity", 1.0f),
            animationSpeed = prefs.getFloat("orb_anim_speed", 1.0f),
            glowPreset = glow,
            particleDensity = prefs.getInt("orb_particle_density", 20),
            pulseIntensity = prefs.getFloat("orb_pulse_intensity", 1.0f),
            rotationSpeed = prefs.getFloat("orb_rotation_speed", 1.0f),
            sizeDp = prefs.getInt("orb_size_dp", 70),
            opacity = prefs.getFloat("orb_opacity", 0.95f),
            snapToEdge = prefs.getBoolean("orb_snap_edge", true),
            isAssistantEnabled = prefs.getBoolean("orb_assistant_enabled", true),
            isPowerOff = prefs.getBoolean("titonox_power_off", false),
            voicePersona = persona,
            speechRate = prefs.getFloat("voice_speech_rate", 1.05f),
            speechPitch = prefs.getFloat("voice_speech_pitch", 1.00f),
            speechLanguage = prefs.getString("voice_language", "en-US") ?: "en-US"
        )
    }

    fun updateSettings(newSettings: OrbCustomization) {
        _settings.value = newSettings
        prefs.edit()
            .putString("orb_theme", newSettings.theme.name)
            .putLong("orb_color_primary", newSettings.primaryColor)
            .putLong("orb_color_secondary", newSettings.secondaryColor)
            .putLong("orb_color_glow", newSettings.glowColor)
            .putLong("orb_color_particle", newSettings.particleColor)
            .putLong("orb_color_ring", newSettings.ringColor)
            .putBoolean("orb_bg_glow", newSettings.backgroundGlow)
            .putFloat("orb_glow_intensity", newSettings.glowIntensity)
            .putFloat("orb_anim_speed", newSettings.animationSpeed)
            .putString("orb_glow", newSettings.glowPreset.name)
            .putInt("orb_particle_density", newSettings.particleDensity)
            .putFloat("orb_pulse_intensity", newSettings.pulseIntensity)
            .putFloat("orb_rotation_speed", newSettings.rotationSpeed)
            .putInt("orb_size_dp", newSettings.sizeDp)
            .putFloat("orb_opacity", newSettings.opacity)
            .putBoolean("orb_snap_edge", newSettings.snapToEdge)
            .putBoolean("orb_assistant_enabled", newSettings.isAssistantEnabled)
            .putBoolean("titonox_power_off", newSettings.isPowerOff)
            .putString("voice_persona", newSettings.voicePersona.name)
            .putFloat("voice_speech_rate", newSettings.speechRate)
            .putFloat("voice_speech_pitch", newSettings.speechPitch)
            .putString("voice_language", newSettings.speechLanguage)
            .apply()
    }

    fun savePosition(x: Int, y: Int) {
        _lastPosX.value = x
        _lastPosY.value = y
        prefs.edit()
            .putInt("orb_pos_x", x)
            .putInt("orb_pos_y", y)
            .apply()
    }

    fun setPowerOff(powerOff: Boolean) {
        _isPowerOff.value = powerOff
        _settings.value = _settings.value.copy(isPowerOff = powerOff)
        prefs.edit().putBoolean("titonox_power_off", powerOff).apply()
    }

    fun setAssistantEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(isAssistantEnabled = enabled)
        prefs.edit().putBoolean("orb_assistant_enabled", enabled).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: OrbSettingsRepository? = null

        fun getInstance(context: Context): OrbSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrbSettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
