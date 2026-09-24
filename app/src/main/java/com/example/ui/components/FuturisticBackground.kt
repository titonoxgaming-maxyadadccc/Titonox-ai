package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavyDark
import kotlin.math.cos
import kotlin.math.sin

/**
 * Luxury Intelligent Background for TITONOX:
 * - Near-black / deep graphite foundation
 * - Subtle ambient radial illumination around the AI energy core
 * - Ultra low-density micro particles with elegant organic drift
 * - Controlled vignette depth (does not compete with orb or text)
 */
@Composable
fun FuturisticBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ambient_drift")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Pre-calculate deterministic pseudo-random particle seed coordinates
    val particleSeeds = remember {
        List(20) { index ->
            val relX = (index * 47 % 100) / 100f
            val relY = (index * 61 % 100) / 100f
            val radius = 1.2f + (index % 3) * 0.8f
            val speedFactor = 0.5f + (index % 5) * 0.3f
            Triple(Offset(relX, relY), radius, speedFactor)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Deep space graphite gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        Color(0xFF090D14),
                        CyberBlack
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // 2. Soft, restrained ambient radial glow centered behind the orb area (upper-center)
            val orbCenterY = height * 0.32f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x1800D2FF), // Very soft cyan glow center
                        Color(0x0A0066FF), // Subtle deep blue dissipation
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, orbCenterY),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(width * 0.5f, orbCenterY)
            )

            // 3. Subtle ambient micro-particles (low-density, calm drift)
            for (i in particleSeeds.indices) {
                val (base, radius, speed) = particleSeeds[i]
                val offsetX = sin(phase * speed + i.toFloat()) * 18f
                val offsetY = cos(phase * speed * 0.8f + i.toFloat()) * 22f

                val px = (base.x * width + offsetX).coerceIn(0f, width)
                val py = (base.y * height + offsetY).coerceIn(0f, height)

                // Soft alpha pulse
                val alpha = (0.15f + 0.12f * sin(phase * speed * 1.5f + i)).coerceIn(0.05f, 0.35f)
                val particleColor = if (i % 3 == 0) Color(0xFF00D2FF) else Color(0xFF7952FF)

                drawCircle(
                    color = particleColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(px, py)
                )
            }

            // 4. Subtle corner edge vignette for cinematic depth
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x40000000)
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.95f
                )
            )
        }
        content()
    }
}

