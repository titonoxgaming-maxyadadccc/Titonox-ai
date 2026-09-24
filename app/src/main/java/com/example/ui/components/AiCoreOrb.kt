package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ai.TaskState
import com.example.floating.OrbController
import com.example.floating.OrbCustomization
import com.example.floating.OrbMasterCanvas
import com.example.floating.OrbState
import com.example.floating.orbTouchGestures
import com.example.ui.theme.TitonoxTokens
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium AI Core Orb for TITONOX:
 * Layered visual system:
 * 1. Ambient bloom and inner energy light
 * 2. Dynamic outer concentric energy ring with scientific ticks
 * 3. Core energy sphere rendered through OrbMasterCanvas (2D/3D models)
 * 4. State-dependent behavioral animations (Calm breath in READY, audio reactive pulse in LISTENING,
 *    subtle rotating layers in THINKING, active directional energy in EXECUTING, controlled expansion in SUCCESS)
 * 5. Natural drag rotation with momentum and velocity inertia.
 */
@Composable
fun AiCoreOrb(
    taskState: TaskState,
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRms: Float,
    sizeDp: Dp = 220.dp,
    customization: OrbCustomization? = null,
    onLongPress: () -> Unit = {},
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val controller = remember { OrbController.getInstance(context) }
    val repoCustomization by controller.settings.collectAsState()
    val activeCustomization = customization ?: repoCustomization

    val rotX by controller.rotationX.collectAsState()
    val rotY by controller.rotationY.collectAsState()
    val rotZ by controller.rotationZ.collectAsState()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "press_scale"
    )

    // Map TaskState + Voice state to the standard OrbStates
    val orbState = when {
        taskState == TaskState.FAILED -> OrbState.ERROR
        taskState == TaskState.COMPLETED -> OrbState.SUCCESS
        taskState == TaskState.EXECUTING || taskState == TaskState.VERIFYING -> OrbState.EXECUTING
        taskState == TaskState.UNDERSTANDING || taskState == TaskState.PLANNING || taskState == TaskState.CLARIFYING || taskState == TaskState.CONFIRMING -> OrbState.THINKING
        isListening -> OrbState.LISTENING
        isSpeaking -> OrbState.SPEAKING
        else -> OrbState.IDLE
    }

    // Audio level normalized from RMS (-2.0 to 12.0)
    val normalizedAudioLevel = if (isListening || isSpeaking) {
        ((audioRms + 2f) / 14f).coerceIn(0.15f, 1.0f)
    } else {
        0.1f
    }

    // State Color mapping
    val stateAccentColor = when (orbState) {
        OrbState.ERROR -> TitonoxTokens.StateError
        OrbState.SUCCESS -> TitonoxTokens.StateSuccess
        OrbState.EXECUTING -> TitonoxTokens.StateExecuting
        OrbState.THINKING -> TitonoxTokens.StateThinking
        OrbState.LISTENING -> TitonoxTokens.StateListening
        OrbState.SPEAKING -> TitonoxTokens.AccentPrimary
        OrbState.IDLE, OrbState.OFF -> TitonoxTokens.StateIdle
    }

    // Infinite transitions for calm breathing & gentle outer ring rotation
    val transition = rememberInfiniteTransition(label = "orb_breath_transition")

    // Slow calm breathing scale for READY state (6s period)
    val breathingScale by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Subtle outer ring slow rotation
    val ringRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (orbState == OrbState.THINKING) 8000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Dynamic scale combining breath, audio reactivity, and touch press
    val effectiveScale = when (orbState) {
        OrbState.LISTENING -> 1.0f + (normalizedAudioLevel * 0.08f)
        OrbState.SPEAKING -> 1.0f + (normalizedAudioLevel * 0.06f)
        OrbState.EXECUTING -> 1.02f
        else -> breathingScale
    } * pressScale

    Box(
        modifier = Modifier
            .size(sizeDp)
            .scale(effectiveScale)
            .testTag("ai_core_orb")
            .orbTouchGestures(
                controller = controller,
                onTap = onClick,
                onLongPress = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Layer: Ambient Bloom Canvas & Outer Precision Ring
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.width * 0.44f

            // Ambient soft bloom behind orb
            val bloomRadius = size.width * 0.48f
            val bloomAlpha = when (orbState) {
                OrbState.LISTENING -> 0.25f + (normalizedAudioLevel * 0.15f)
                OrbState.THINKING -> 0.20f
                OrbState.EXECUTING -> 0.24f
                OrbState.ERROR -> 0.28f
                else -> 0.14f
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stateAccentColor.copy(alpha = bloomAlpha),
                        stateAccentColor.copy(alpha = bloomAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = bloomRadius
                ),
                radius = bloomRadius,
                center = center
            )

            // Dynamic outer reticle ring with micro-precision indicators
            val ringRadius = size.width * 0.465f
            drawCircle(
                color = stateAccentColor.copy(alpha = 0.18f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1f)
            )

            // Dynamic rotating segment arcs
            val radAngle = Math.toRadians(ringRotation.toDouble()).toFloat()
            val arcSpan = if (orbState == OrbState.THINKING) 80f else 45f

            drawArc(
                color = stateAccentColor.copy(alpha = 0.45f),
                startAngle = ringRotation,
                sweepAngle = arcSpan,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = androidx.compose.ui.geometry.Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )

            drawArc(
                color = stateAccentColor.copy(alpha = 0.45f),
                startAngle = ringRotation + 180f,
                sweepAngle = arcSpan,
                useCenter = false,
                topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                size = androidx.compose.ui.geometry.Size(ringRadius * 2f, ringRadius * 2f),
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )

            // 4 Micro tick marks on the ring
            for (i in 0 until 4) {
                val angle = radAngle + (i * Math.PI / 2.0).toFloat()
                val innerX = center.x + cos(angle) * (ringRadius - 4f)
                val innerY = center.y + sin(angle) * (ringRadius - 4f)
                val outerX = center.x + cos(angle) * (ringRadius + 4f)
                val outerY = center.y + sin(angle) * (ringRadius + 4f)

                drawLine(
                    color = stateAccentColor.copy(alpha = 0.5f),
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = 1.5f
                )
            }
        }

        // 2. Layer: Core Energy Sphere & Visualizer Canvas (All 30 2D/3D renderers)
        OrbMasterCanvas(
            customization = activeCustomization,
            state = orbState,
            audioLevel = normalizedAudioLevel,
            rotationX = rotX,
            rotationY = rotY,
            rotationZ = rotZ,
            modifier = Modifier.size(sizeDp * 0.88f)
        )
    }
}

