package com.example.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object OrbRenderers4 {

    // 1. EQUALIZER CORE (2D): 360-degree radial segmented audio spectrum analyzer
    fun DrawScope.drawEqualizerCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val minDim = size.minDimension
        val baseR = minDim * 0.28f
        val numBars = 36
        val barWidth = 4f

        // Central glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = baseR * 1.6f
            ),
            radius = baseR * 1.6f,
            center = center
        )

        // Inner circular hub
        drawCircle(
            color = ring.copy(alpha = 0.4f),
            radius = baseR,
            center = center,
            style = Stroke(width = 2f)
        )

        // 360 radial audio bars
        for (i in 0 until numBars) {
            val angleDeg = (i * 360f / numBars) + (time * 15f)
            val angleRad = angleDeg * PI.toFloat() / 180f

            // Harmonic frequency simulation with audio reactivity
            val freq = sin(time * 3f + i * 0.5f) * 0.5f + 0.5f
            val reactiveHeight = (minDim * 0.16f) * (0.2f + (audioLevel * 0.8f) + freq * 0.4f)

            val startX = center.x + cos(angleRad) * (baseR + 4f)
            val startY = center.y + sin(angleRad) * (baseR + 4f)
            val endX = center.x + cos(angleRad) * (baseR + 4f + reactiveHeight)
            val endY = center.y + sin(angleRad) * (baseR + 4f + reactiveHeight)

            val barColor = if (i % 2 == 0) primary else secondary
            drawLine(
                color = barColor.copy(alpha = 0.85f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }

        // Central core dot
        drawCircle(
            color = primary,
            radius = 6f + audioLevel * 8f,
            center = center
        )
    }

    // 2. INFINITY CORE (2D): Parametric figure-8 lemniscate energetic flux trajectory
    fun DrawScope.drawInfinityCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val a = size.minDimension * 0.36f
        val pulse = 1f + audioLevel * 0.2f

        // Draw lemniscate curve: r^2 = 2*a^2 * cos(2*theta) -> Bernoulli Lemniscate
        val path = Path()
        val steps = 120
        var first = true

        for (i in 0..steps) {
            val t = (i * 2f * PI.toFloat() / steps)
            // Lemniscate parametric form
            val denom = 1f + sin(t) * sin(t)
            val x = center.x + (a * cos(t) / denom) * pulse
            val y = center.y + (a * sin(t) * cos(t) / denom) * 1.5f * pulse

            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        // Background glow
        drawPath(
            path = path,
            color = glow.copy(alpha = 0.25f),
            style = Stroke(width = 12f, cap = StrokeCap.Round)
        )

        // Main luminous track
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(primary, secondary, ring, primary),
                start = Offset(center.x - a, center.y),
                end = Offset(center.x + a, center.y)
            ),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Traveling flux particle heads along the lemniscate
        val particleCount = 4
        for (p in 0 until particleCount) {
            val pt = (time * 1.8f + p * (2f * PI.toFloat() / particleCount))
            val pDenom = 1f + sin(pt) * sin(pt)
            val px = center.x + (a * cos(pt) / pDenom) * pulse
            val py = center.y + (a * sin(pt) * cos(pt) / pDenom) * 1.5f * pulse

            drawCircle(
                color = Color.White,
                radius = 5f + audioLevel * 4f,
                center = Offset(px, py)
            )
            drawCircle(
                color = primary.copy(alpha = 0.6f),
                radius = 12f + audioLevel * 6f,
                center = Offset(px, py)
            )
        }
    }

    // 3. HOLOGRAPHIC LINES (2D): Raster scanline grid with optical projection interference
    fun DrawScope.drawHolographicLines(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.42f
        val pulse = 1f + audioLevel * 0.15f

        // Outer projection ring
        drawCircle(
            color = ring.copy(alpha = 0.5f),
            radius = radius * pulse,
            center = center,
            style = Stroke(width = 2f)
        )

        // Horizontal scanlines clipped to circle
        val lineCount = 18
        for (i in -lineCount / 2..lineCount / 2) {
            val yOffset = i * (radius * 1.8f / lineCount)
            val currentY = center.y + yOffset

            val distFromCenter = abs(yOffset)
            if (distFromCenter < radius) {
                val halfWidth = sqrt(radius * radius - distFromCenter * distFromCenter) * pulse
                val scanAlpha = (sin(time * 4f + i * 0.4f) * 0.3f + 0.7f).coerceIn(0.1f, 1f)

                drawLine(
                    color = primary.copy(alpha = scanAlpha * 0.8f),
                    start = Offset(center.x - halfWidth, currentY),
                    end = Offset(center.x + halfWidth, currentY),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Vertical tracking sweep line
        val sweepY = center.y + sin(time * 2.5f) * radius * 0.85f
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(center.x - radius * 0.7f, sweepY),
            end = Offset(center.x + radius * 0.7f, sweepY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Central crosshairs
        drawLine(
            color = secondary.copy(alpha = 0.7f),
            start = Offset(center.x - 12f, center.y),
            end = Offset(center.x + 12f, center.y),
            strokeWidth = 1.5f
        )
        drawLine(
            color = secondary.copy(alpha = 0.7f),
            start = Offset(center.x, center.y - 12f),
            end = Offset(center.x, center.y + 12f),
            strokeWidth = 1.5f
        )
    }

    // 4. CONCENTRIC SCANNER (2D): Triple counter-rotating reticle scanner dials with coordinate ticks
    fun DrawScope.drawConcentricScanner(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension * 0.42f
        val pulse = 1f + audioLevel * 0.18f

        // Outer dial - rotates clockwise
        rotate(degrees = time * 25f, pivot = center) {
            drawCircle(
                color = ring.copy(alpha = 0.4f),
                radius = maxR * pulse,
                center = center,
                style = Stroke(width = 1.5f)
            )
            // Ticks on outer dial
            for (deg in 0 until 360 step 30) {
                val rad = deg * PI.toFloat() / 180f
                val r1 = maxR * pulse
                val r2 = r1 - 8f
                drawLine(
                    color = primary,
                    start = Offset(center.x + cos(rad) * r1, center.y + sin(rad) * r1),
                    end = Offset(center.x + cos(rad) * r2, center.y + sin(rad) * r2),
                    strokeWidth = 2f
                )
            }
        }

        // Middle dial - rotates counter-clockwise
        val midR = maxR * 0.68f
        rotate(degrees = -time * 35f, pivot = center) {
            drawArc(
                color = secondary.copy(alpha = 0.8f),
                startAngle = 0f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - midR, center.y - midR),
                size = Size(midR * 2, midR * 2),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
            drawArc(
                color = secondary.copy(alpha = 0.8f),
                startAngle = 180f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - midR, center.y - midR),
                size = Size(midR * 2, midR * 2),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }

        // Inner scanner reticle - rapid scanner pulse
        val inR = maxR * 0.36f
        rotate(degrees = time * 60f, pivot = center) {
            drawCircle(
                color = primary.copy(alpha = 0.6f),
                radius = inR,
                center = center,
                style = Stroke(width = 2f)
            )
            // Target triangle or reticle ticks
            for (i in 0 until 4) {
                val a = i * 90f * PI.toFloat() / 180f
                drawLine(
                    color = glow,
                    start = Offset(center.x + cos(a) * (inR - 6f), center.y + sin(a) * (inR - 6f)),
                    end = Offset(center.x + cos(a) * (inR + 6f), center.y + sin(a) * (inR + 6f)),
                    strokeWidth = 2f
                )
            }
        }

        // Core dot
        drawCircle(color = glow, radius = 5f + audioLevel * 4f, center = center)
    }

    // 5. STAR CORE (2D): Geometric starburst with coronal radiance and rotating flares
    fun DrawScope.drawStarCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension * 0.44f * (1f + audioLevel * 0.25f)
        val innerR = outerR * 0.35f
        val points = 8

        // Radial aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = outerR * 1.3f
            ),
            radius = outerR * 1.3f,
            center = center
        )

        // 8-pointed rotating star
        rotate(degrees = time * 20f, pivot = center) {
            val starPath = Path()
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = (i * PI.toFloat() / points)
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
            }
            starPath.close()

            drawPath(
                path = starPath,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primary, secondary),
                    center = center,
                    radius = outerR
                )
            )
            drawPath(
                path = starPath,
                color = ring,
                style = Stroke(width = 2f)
            )
        }

        // Secondary counter-rotating inner star
        rotate(degrees = -time * 30f, pivot = center) {
            val smallPath = Path()
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) innerR * 0.9f else innerR * 0.4f
                val angle = (i * PI.toFloat() / points)
                val x = center.x + cos(angle) * r
                val y = center.y + sin(angle) * r
                if (i == 0) smallPath.moveTo(x, y) else smallPath.lineTo(x, y)
            }
            smallPath.close()
            drawPath(path = smallPath, color = glow.copy(alpha = 0.8f))
        }
    }

    // 6. ICE CORE (3D): Crystalline cryo-lattice with frost flares and cold prismatic refraction
    fun DrawScope.drawIceCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.38f * (1f + audioLevel * 0.15f)

        // Cold ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F7FA).copy(alpha = 0.5f), Color(0xFF80DEEA).copy(alpha = 0.2f), Color.Transparent),
                center = center,
                radius = r * 1.5f
            ),
            radius = r * 1.5f,
            center = center
        )

        // 3D Hexagonal prism facets
        rotate(degrees = time * 18f, pivot = center) {
            val corners = 6
            val hexPoints = mutableListOf<Offset>()
            for (i in 0 until corners) {
                val a = i * 60f * PI.toFloat() / 180f
                hexPoints.add(Offset(center.x + cos(a) * r, center.y + sin(a) * r * 0.85f))
            }

            // Facet triangles connecting to center
            for (i in 0 until corners) {
                val next = (i + 1) % corners
                val facetPath = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(hexPoints[i].x, hexPoints[i].y)
                    lineTo(hexPoints[next].x, hexPoints[next].y)
                    close()
                }

                val facetColor = if (i % 2 == 0) Color(0xFFB2EBF2) else Color(0xFFE0F7FA)
                drawPath(path = facetPath, color = facetColor.copy(alpha = 0.45f))
                drawPath(path = facetPath, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 1.5f))
            }
        }

        // Floating snowflake frost needles
        val needleCount = 6
        for (i in 0 until needleCount) {
            val a = (i * 60f + time * 12f) * PI.toFloat() / 180f
            val nLen = r * 1.25f
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = center,
                end = Offset(center.x + cos(a) * nLen, center.y + sin(a) * nLen),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}
