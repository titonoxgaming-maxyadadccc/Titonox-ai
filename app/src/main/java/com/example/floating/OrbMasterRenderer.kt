package com.example.floating

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.floating.Orb2DRenderers.drawAudioWave
import com.example.floating.Orb2DRenderers.drawConcentricScanner
import com.example.floating.Orb2DRenderers.drawDigitalEye
import com.example.floating.Orb2DRenderers.drawDigitalRadar
import com.example.floating.Orb2DRenderers.drawEnergyRing
import com.example.floating.Orb2DRenderers.drawEqualizerCore
import com.example.floating.Orb2DRenderers.drawHexCore
import com.example.floating.Orb2DRenderers.drawHolographicLines
import com.example.floating.Orb2DRenderers.drawInfinityCore
import com.example.floating.Orb2DRenderers.drawNeonCircuit
import com.example.floating.Orb2DRenderers.drawNeuralNetwork
import com.example.floating.Orb2DRenderers.drawParticleMatrix
import com.example.floating.Orb2DRenderers.drawPulseRing
import com.example.floating.Orb2DRenderers.drawStarCore
import com.example.floating.Orb2DRenderers.drawTitonoxDigitalCore
import com.example.floating.Orb3DRenderers.draw3DNeuralBrain
import com.example.floating.Orb3DRenderers.drawAtomicCore
import com.example.floating.Orb3DRenderers.drawBlackHoleCore
import com.example.floating.Orb3DRenderers.drawCrystalCore
import com.example.floating.Orb3DRenderers.drawCyberReactor
import com.example.floating.Orb3DRenderers.drawFireEnergyCore
import com.example.floating.Orb3DRenderers.drawGalaxyCore
import com.example.floating.Orb3DRenderers.drawHolographicGlobe
import com.example.floating.Orb3DRenderers.drawIceCore
import com.example.floating.Orb3DRenderers.drawLiquidEnergySphere
import com.example.floating.Orb3DRenderers.drawMechanicalReactor
import com.example.floating.Orb3DRenderers.drawPlasmaSphere
import com.example.floating.Orb3DRenderers.drawQuantumReactor
import com.example.floating.Orb3DRenderers.drawTitonox3DCore
import com.example.floating.Orb3DRenderers.drawWireframePlanet

@Composable
fun OrbMasterCanvas(
    customization: OrbCustomization,
    state: OrbState,
    audioLevel: Float,
    countdownSeconds: Int? = null,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    rotationZ: Float = 0f,
    modifier: Modifier = Modifier
) {
    val speedMultiplier = customization.animationSpeed.coerceIn(0.2f, 3.0f)
    val durationMs = (10000 / speedMultiplier).toInt().coerceAtLeast(1000)

    val transition = rememberInfiniteTransition(label = "orb_rotation")
    val timeProgression by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Convert degrees to radians for 3D renderers
    val radX = Math.toRadians(rotationX.toDouble()).toFloat()
    val radY = Math.toRadians(rotationY.toDouble()).toFloat()
    val radZ = Math.toRadians(rotationZ.toDouble()).toFloat()

    // Speaking vocal modulation when TTS is active
    val vocalModulation = if (state == OrbState.SPEAKING) {
        0.35f + kotlin.math.sin(timeProgression * 12f) * 0.25f
    } else {
        0f
    }

    val effectiveAudioLevel = (audioLevel + vocalModulation).coerceIn(0f, 1f)
    val effectiveTime = timeProgression * customization.rotationSpeed

    // Compute active color palette with state overrides
    val (primary, secondary, glow, ring) = when (state) {
        OrbState.ERROR -> Quad(
            Color(0xFFFF1744),
            Color(0xFFFF5252),
            Color(0xFFFF8A80),
            Color(0xFFD50000)
        )
        OrbState.SUCCESS -> Quad(
            Color(0xFF00E676),
            Color(0xFF69F0AE),
            Color(0xFFB9F6CA),
            Color(0xFF00C853)
        )
        OrbState.THINKING -> Quad(
            Color(0xFF7C4DFF),
            Color(0xFFB388FF),
            Color(0xFFEA80FC),
            Color(0xFF651FFF)
        )
        OrbState.EXECUTING -> Quad(
            Color(0xFFFFAB00),
            Color(0xFFFFD740),
            Color(0xFFFFE57F),
            Color(0xFFFF6D00)
        )
        OrbState.OFF -> Quad(
            Color(0xFF37474F),
            Color(0xFF263238),
            Color(0xFF455A64),
            Color(0xFF1C2529)
        )
        else -> Quad(
            customization.primaryColorCompose.copy(alpha = customization.opacity),
            customization.secondaryColorCompose.copy(alpha = customization.opacity),
            customization.glowColorCompose.copy(alpha = customization.opacity),
            customization.ringColorCompose.copy(alpha = customization.opacity)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (state == OrbState.OFF) {
                // Dim quiescent off state circle
                drawCircle(Color(0xFF1E2630), radius = size.minDimension * 0.42f, center = center)
                drawCircle(Color(0xFF37474F), radius = size.minDimension * 0.42f, center = center, style = Stroke(width = 2f))
                return@Canvas
            }

            // Optional background ambient glow if enabled
            if (customization.backgroundGlow) {
                val glowR = size.minDimension * 0.48f * customization.glowIntensity
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = 0.25f * customization.glowPreset.multiplier), Color.Transparent),
                        center = center,
                        radius = glowR
                    ),
                    radius = glowR,
                    center = center
                )
            }

            // Render Selected Orb Theme (30 completely distinct 2D & 3D designs)
            when (customization.theme) {
                // === 15 UNIQUE 2D ORBS ===
                OrbThemeType.PULSE_RING -> drawPulseRing(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.AUDIO_WAVE -> drawAudioWave(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.DIGITAL_RADAR -> drawDigitalRadar(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.NEON_CIRCUIT -> drawNeonCircuit(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.HEX_CORE -> drawHexCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.PARTICLE_MATRIX -> drawParticleMatrix(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.EQUALIZER_CORE -> drawEqualizerCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.INFINITY_CORE -> drawInfinityCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.DIGITAL_EYE -> drawDigitalEye(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.ENERGY_RING -> drawEnergyRing(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.NEURAL_NETWORK -> drawNeuralNetwork(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.HOLOGRAPHIC_LINES -> drawHolographicLines(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.CONCENTRIC_SCANNER -> drawConcentricScanner(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.STAR_CORE -> drawStarCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)
                OrbThemeType.TITONOX_DIGITAL_CORE -> drawTitonoxDigitalCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring)

                // === 15 UNIQUE 3D ORBS ===
                OrbThemeType.PLASMA_SPHERE -> drawPlasmaSphere(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.CRYSTAL_CORE -> drawCrystalCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.QUANTUM_REACTOR -> drawQuantumReactor(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.MECHANICAL_REACTOR -> drawMechanicalReactor(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.HOLOGRAPHIC_GLOBE -> drawHolographicGlobe(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.GALAXY_CORE -> drawGalaxyCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.LIQUID_ENERGY_SPHERE -> drawLiquidEnergySphere(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.THREE_D_NEURAL_BRAIN -> draw3DNeuralBrain(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.ATOMIC_CORE -> drawAtomicCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.CYBER_REACTOR -> drawCyberReactor(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.BLACK_HOLE_CORE -> drawBlackHoleCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.ICE_CORE -> drawIceCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.FIRE_ENERGY_CORE -> drawFireEnergyCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.WIREFRAME_PLANET -> drawWireframePlanet(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
                OrbThemeType.TITONOX_3D_CORE -> drawTitonox3DCore(effectiveTime, effectiveAudioLevel, state, primary, secondary, glow, ring, radX, radY, radZ)
            }

            // Power off countdown ring overlay
            if (countdownSeconds != null && countdownSeconds > 0) {
                val sweep = (4 - countdownSeconds) / 4f * 360f
                drawArc(
                    color = Color(0xFFFF1744),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }

        // Countdown Text Overlay (4, 3, 2, 1)
        if (countdownSeconds != null && countdownSeconds > 0) {
            Text(
                text = "$countdownSeconds",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
