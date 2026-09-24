package com.example.floating

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

/**
 * Centralized OrbController.
 *
 * Controls the TITONOX orb for:
 * - Real Touch gestures (finger drag, swipe, fling, tap, double tap, long press)
 * - Voice orb commands (routed through OrbIntentRouter)
 * - AI structured tool actions (e.g. orb.rotate, orb.select, orb.customize)
 * - UI buttons & sliders
 *
 * Guaranteed unified architecture: Touch, Voice, AI, and UI all call this controller.
 */
class OrbController private constructor(private val context: Context) {

    private val TAG = "OrbController"
    private val scope = CoroutineScope(Dispatchers.Main)
    val settingsRepository = OrbSettingsRepository.getInstance(context)

    // Current settings flow from persistent repository
    val settings: StateFlow<OrbCustomization> = settingsRepository.settings

    // Real 3D Rotation State in degrees
    private val _rotationX = MutableStateFlow(0f)
    val rotationX: StateFlow<Float> = _rotationX.asStateFlow()

    private val _rotationY = MutableStateFlow(0f)
    val rotationY: StateFlow<Float> = _rotationY.asStateFlow()

    private val _rotationZ = MutableStateFlow(0f)
    val rotationZ: StateFlow<Float> = _rotationZ.asStateFlow()

    // Runtime state & audio reactive amplitude
    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    val activeTheme: OrbThemeType get() = settings.value.theme
    val activeDimension: OrbDimension get() = settings.value.theme.dimension

    private val _audioLevel = MutableStateFlow(0.1f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private var inertiaJob: Job? = null

    // ------------------------------------------------------------------------
    // 3D Rotation & Touch Drag Methods
    // ------------------------------------------------------------------------

    /**
     * Called continuously during a finger drag on the 3D orb.
     * Horizontal finger movement -> rotates around Y axis.
     * Vertical finger movement -> rotates around X axis.
     */
    fun onDragDelta(deltaX: Float, deltaY: Float) {
        inertiaJob?.cancel()
        // Sensitivity factor: 1 pixel drag = ~0.65 degrees rotation
        val sensitivity = 0.65f
        val newY = (_rotationY.value + deltaX * sensitivity) % 360f
        val newX = (_rotationX.value - deltaY * sensitivity).coerceIn(-85f, 85f)
        _rotationY.value = newY
        _rotationX.value = newX
    }

    /**
     * Fast swipe release: initiates inertial rotational glide with smooth deceleration.
     */
    fun onFling(velocityX: Float, velocityY: Float) {
        inertiaJob?.cancel()
        if (abs(velocityX) < 100f && abs(velocityY) < 100f) return

        inertiaJob = scope.launch {
            var vx = (velocityX * 0.012f).coerceIn(-25f, 25f)
            var vy = (velocityY * 0.012f).coerceIn(-25f, 25f)
            val friction = 0.92f

            while (abs(vx) > 0.15f || abs(vy) > 0.15f) {
                val newY = (_rotationY.value + vx) % 360f
                val newX = (_rotationX.value - vy).coerceIn(-85f, 85f)
                _rotationY.value = newY
                _rotationX.value = newX
                vx *= friction
                vy *= friction
                delay(16) // ~60 FPS
            }
        }
    }

    fun rotateX(amountDegrees: Float) {
        inertiaJob?.cancel()
        _rotationX.value = (_rotationX.value + amountDegrees).coerceIn(-85f, 85f)
    }

    fun rotateY(amountDegrees: Float) {
        inertiaJob?.cancel()
        _rotationY.value = (_rotationY.value + amountDegrees) % 360f
    }

    fun rotateZ(amountDegrees: Float) {
        inertiaJob?.cancel()
        _rotationZ.value = (_rotationZ.value + amountDegrees) % 360f
    }

    fun setRotation(x: Float, y: Float, z: Float) {
        inertiaJob?.cancel()
        _rotationX.value = x.coerceIn(-85f, 85f)
        _rotationY.value = y % 360f
        _rotationZ.value = z % 360f
    }

    /**
     * Smoothly or immediately resets orb orientation back to default (0, 0, 0).
     */
    fun resetRotation(smooth: Boolean = true) {
        inertiaJob?.cancel()
        if (!smooth) {
            _rotationX.value = 0f
            _rotationY.value = 0f
            _rotationZ.value = 0f
            return
        }
        inertiaJob = scope.launch {
            val startX = _rotationX.value
            val startY = _rotationY.value
            val startZ = _rotationZ.value
            val frames = 18
            for (i in 1..frames) {
                val progress = i.toFloat() / frames
                // Ease-out cubic
                val factor = 1f - (1f - progress) * (1f - progress) * (1f - progress)
                _rotationX.value = startX * (1f - factor)
                _rotationY.value = startY * (1f - factor)
                _rotationZ.value = startZ * (1f - factor)
                delay(16)
            }
            _rotationX.value = 0f
            _rotationY.value = 0f
            _rotationZ.value = 0f
        }
    }

    // ------------------------------------------------------------------------
    // Orb Selection & Dimensional Navigation
    // ------------------------------------------------------------------------

    fun selectOrb(theme: OrbThemeType) {
        val current = settings.value
        if (current.theme != theme) {
            settingsRepository.updateSettings(current.copy(theme = theme))
            // Reset rotation when switching to a fresh 3D orb
            resetRotation(smooth = false)
        }
    }

    fun nextOrb() {
        val all = OrbThemeType.values()
        val current = settings.value.theme
        val currentDim = current.dimension
        val dimOrbs = all.filter { it.dimension == currentDim }
        val currentIndex = dimOrbs.indexOf(current)
        val nextTheme = if (currentIndex != -1 && currentIndex < dimOrbs.size - 1) {
            dimOrbs[currentIndex + 1]
        } else {
            dimOrbs.first()
        }
        selectOrb(nextTheme)
    }

    fun previousOrb() {
        val all = OrbThemeType.values()
        val current = settings.value.theme
        val currentDim = current.dimension
        val dimOrbs = all.filter { it.dimension == currentDim }
        val currentIndex = dimOrbs.indexOf(current)
        val prevTheme = if (currentIndex > 0) {
            dimOrbs[currentIndex - 1]
        } else {
            dimOrbs.last()
        }
        selectOrb(prevTheme)
    }

    fun setOrbDimension(dimension: OrbDimension) {
        val current = settings.value.theme
        if (current.dimension != dimension) {
            val firstOfDim = OrbThemeType.values().firstOrNull { it.dimension == dimension }
            if (firstOfDim != null) {
                selectOrb(firstOfDim)
            }
        }
    }

    fun setOrbMode(mode: OrbDimension) = setOrbDimension(mode)

    fun randomOrb() {
        val all = OrbThemeType.values()
        val randomTheme = all.random()
        selectOrb(randomTheme)
    }

    /**
     * Resolves natural language orb selection names (e.g. "plasma sphere", "digital eye",
     * "2d number 5", "3d number 10", "quantum reactor", etc.)
     */
    fun selectOrbByName(query: String): Boolean {
        val clean = query.trim().lowercase(Locale.ROOT)
            .replace("lagao", "")
            .replace("karo", "")
            .replace("set", "")
            .replace("apply", "")
            .replace("orb", "")
            .trim()

        // Match 2D number X or 3D number X
        val numMatch2D = Regex("(?:2d|two d)\\s*(?:number|no|#)?\\s*(\\d+)").find(clean)
        if (numMatch2D != null) {
            val num = numMatch2D.groupValues[1].toIntOrNull() ?: 1
            val twoDOrbs = OrbThemeType.values().filter { it.dimension == OrbDimension.TWO_D }
            val index = (num - 1).coerceIn(0, twoDOrbs.size - 1)
            selectOrb(twoDOrbs[index])
            return true
        }

        val numMatch3D = Regex("(?:3d|three d)\\s*(?:number|no|#)?\\s*(\\d+)").find(clean)
        if (numMatch3D != null) {
            val num = numMatch3D.groupValues[1].toIntOrNull() ?: 1
            val threeDOrbs = OrbThemeType.values().filter { it.dimension == OrbDimension.THREE_D }
            val index = (num - 1).coerceIn(0, threeDOrbs.size - 1)
            selectOrb(threeDOrbs[index])
            return true
        }

        // Match specific theme names
        val all = OrbThemeType.values()
        val exactMatch = all.find { it.displayName.lowercase(Locale.ROOT) == clean || it.name.lowercase(Locale.ROOT) == clean.replace(" ", "_") }
        if (exactMatch != null) {
            selectOrb(exactMatch)
            return true
        }

        val partialMatch = all.find {
            clean.contains(it.displayName.lowercase(Locale.ROOT)) ||
                    it.displayName.lowercase(Locale.ROOT).contains(clean)
        }
        if (partialMatch != null) {
            selectOrb(partialMatch)
            return true
        }

        return false
    }

    // ------------------------------------------------------------------------
    // Customization & Visual Calibrations
    // ------------------------------------------------------------------------

    fun setPrimaryColor(colorLong: Long) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(primaryColor = colorLong))
    }

    fun setPrimaryColor(color: Color) {
        setPrimaryColor(color.toArgb().toLong())
    }

    fun setSecondaryColor(colorLong: Long) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(secondaryColor = colorLong))
    }

    fun setSecondaryColor(color: Color) {
        setSecondaryColor(color.toArgb().toLong())
    }

    fun setGlowIntensity(value: Float) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(glowIntensity = value.coerceIn(0.1f, 2.5f)))
    }

    fun increaseIntensity(delta: Float = 0.25f) {
        val current = settings.value
        val newGlow = (current.glowIntensity + delta).coerceIn(0.2f, 2.5f)
        val newPulse = (current.pulseIntensity + delta).coerceIn(0.3f, 2.5f)
        settingsRepository.updateSettings(current.copy(glowIntensity = newGlow, pulseIntensity = newPulse))
    }

    fun decreaseIntensity(delta: Float = 0.25f) {
        val current = settings.value
        val newGlow = (current.glowIntensity - delta).coerceIn(0.2f, 2.5f)
        val newPulse = (current.pulseIntensity - delta).coerceIn(0.3f, 2.5f)
        settingsRepository.updateSettings(current.copy(glowIntensity = newGlow, pulseIntensity = newPulse))
    }

    fun setAnimationSpeed(value: Float) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(animationSpeed = value.coerceIn(0.3f, 3.0f)))
    }

    fun setParticleDensity(value: Int) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(particleDensity = value.coerceIn(5, 50)))
    }

    fun setScale(valueDp: Int) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(sizeDp = valueDp.coerceIn(48, 140)))
    }

    fun setOpacity(value: Float) {
        val current = settings.value
        settingsRepository.updateSettings(current.copy(opacity = value.coerceIn(0.2f, 1.0f)))
    }

    fun setState(state: OrbState) {
        _orbState.value = state
    }

    fun setAudioLevel(level: Float) {
        _audioLevel.value = level.coerceIn(0f, 1f)
    }

    fun toggleAnimationMode() {
        val current = settings.value
        val newSpeed = if (current.animationSpeed > 1.5f) 0.8f else 2.0f
        setAnimationSpeed(newSpeed)
    }

    companion object {
        @Volatile
        private var INSTANCE: OrbController? = null

        fun getInstance(context: Context): OrbController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OrbController(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
