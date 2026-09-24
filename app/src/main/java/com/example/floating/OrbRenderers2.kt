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
import kotlin.math.cos
import kotlin.math.sin

object OrbRenderers2 {

    // 11. QUANTUM SPHERE: Multi-axis electron probability orbital cloud
    fun DrawScope.drawQuantumSphere(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f

        // Central nucleus
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.3f
            ),
            radius = maxR * 0.3f,
            center = center
        )

        // 3 Orbital Ellipses at 0, 60, 120 degrees
        val angles = listOf(0f, 60f, 120f)
        for ((idx, deg) in angles.withIndex()) {
            rotate(deg + time * (25f + idx * 10f), center) {
                drawOval(
                    color = if (idx == 0) primary else if (idx == 1) secondary else ring,
                    topLeft = Offset(center.x - maxR * pulse, center.y - maxR * 0.35f * pulse),
                    size = Size(maxR * 2f * pulse, maxR * 0.7f * pulse),
                    style = Stroke(width = 2f)
                )

                // Orbiting electron quantum particle
                val eAngle = time * (3f + idx)
                val ex = center.x + cos(eAngle) * maxR * pulse
                val ey = center.y + sin(eAngle) * maxR * 0.35f * pulse
                drawCircle(Color.White, radius = 3.5f * pulse, center = Offset(ex, ey))
                drawCircle(glow.copy(alpha = 0.6f), radius = 6f * pulse, center = Offset(ex, ey))
            }
        }
    }

    // 12. GLASS ORB: Translucent glass bubble with inner floating sphere and specular crescent highlight
    fun DrawScope.drawGlassOrb(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.86f
        val pulse = 1f + audioLevel * 0.3f

        // Translucent glass sphere gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.25f), secondary.copy(alpha = 0.45f), ring.copy(alpha = 0.75f)),
                center = center + Offset(-maxR * 0.2f, -maxR * 0.2f),
                radius = maxR
            ),
            radius = maxR,
            center = center
        )

        // Inner floating levitating core
        val innerCenter = center + Offset(sin(time * 2f) * maxR * 0.15f, cos(time * 1.5f) * maxR * 0.15f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = innerCenter,
                radius = maxR * 0.35f * pulse
            ),
            radius = maxR * 0.35f * pulse,
            center = innerCenter
        )

        // Specular crescent gleam
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0f)),
                start = Offset(center.x - maxR * 0.6f, center.y - maxR * 0.6f),
                end = Offset(center.x - maxR * 0.2f, center.y - maxR * 0.2f)
            ),
            topLeft = Offset(center.x - maxR * 0.6f, center.y - maxR * 0.7f),
            size = Size(maxR * 0.6f, maxR * 0.3f)
        )
    }

    // 13. MECHANICAL CORE: Interlocking steampunk cyber gear cogs rotating with torque
    fun DrawScope.drawMechanicalCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.25f

        // Outer Gear (Clockwise)
        val outerAngle = (time * 45f) % 360f
        rotate(outerAngle, center) {
            drawGear(center, maxR, maxR * 0.82f, 12, ring)
        }

        // Inner Counter Gear (Counter-Clockwise)
        val innerAngle = (-time * 65f) % 360f
        rotate(innerAngle, center) {
            drawGear(center, maxR * 0.58f * pulse, maxR * 0.44f * pulse, 8, primary)
        }

        // Center Axle Bolt
        drawCircle(secondary, radius = maxR * 0.25f, center = center)
        drawCircle(Color.White, radius = maxR * 0.1f * pulse, center = center)
    }

    private fun DrawScope.drawGear(c: Offset, outerR: Float, innerR: Float, teeth: Int, col: Color) {
        val path = Path()
        val step = 360.0 / teeth
        for (i in 0 until teeth) {
            val a1 = Math.toRadians(i * step).toFloat()
            val a2 = Math.toRadians(i * step + step * 0.3).toFloat()
            val a3 = Math.toRadians(i * step + step * 0.5).toFloat()
            val a4 = Math.toRadians((i + 1) * step).toFloat()

            val p1 = c + Offset(cos(a1) * innerR, sin(a1) * innerR)
            val p2 = c + Offset(cos(a2) * outerR, sin(a2) * outerR)
            val p3 = c + Offset(cos(a3) * outerR, sin(a3) * outerR)
            val p4 = c + Offset(cos(a4) * innerR, sin(a4) * innerR)

            if (i == 0) path.moveTo(p1.x, p1.y) else path.lineTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.lineTo(p3.x, p3.y)
            path.lineTo(p4.x, p4.y)
        }
        path.close()
        drawPath(path, col, style = Stroke(width = 2.5f))
    }

    // 14. WAVE CORE: Concentric sinusoidal acoustic ripples propagating outward
    fun DrawScope.drawWaveCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f

        val rippleCount = 5
        for (i in 0 until rippleCount) {
            val progress = ((time * 1.5f + i.toFloat() / rippleCount) % 1f)
            val r = progress * maxR * pulse
            val alpha = (1f - progress).coerceIn(0f, 1f)
            drawCircle(
                color = if (i % 2 == 0) primary.copy(alpha = alpha) else secondary.copy(alpha = alpha),
                radius = r,
                center = center,
                style = Stroke(width = 3f * (1f - progress * 0.5f))
            )
        }

        // Central emitter
        drawCircle(Color.White, radius = maxR * 0.15f * pulse, center = center)
        drawCircle(glow.copy(alpha = 0.6f), radius = maxR * 0.25f, center = center)
    }

    // 15. NEON CUBE: 3D tesseract hypercube rotating in isometric space
    fun DrawScope.drawNeonCube(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.3f

        val rot = (time * 45f) % 360f
        rotate(rot, center) {
            val outerS = maxR * 0.7f * pulse
            val innerS = maxR * 0.35f * pulse

            // Outer square
            val oTL = center + Offset(-outerS, -outerS)
            val oTR = center + Offset(outerS, -outerS)
            val oBR = center + Offset(outerS, outerS)
            val oBL = center + Offset(-outerS, outerS)

            drawRect(primary, topLeft = oTL, size = Size(outerS * 2f, outerS * 2f), style = Stroke(width = 2.5f))

            // Inner square
            val iTL = center + Offset(-innerS, -innerS)
            val iTR = center + Offset(innerS, -innerS)
            val iBR = center + Offset(innerS, innerS)
            val iBL = center + Offset(-innerS, innerS)

            drawRect(secondary, topLeft = iTL, size = Size(innerS * 2f, innerS * 2f), style = Stroke(width = 2f))

            // 4 Corner connector beams
            drawLine(glow, oTL, iTL, strokeWidth = 2f)
            drawLine(glow, oTR, iTR, strokeWidth = 2f)
            drawLine(glow, oBR, iBR, strokeWidth = 2f)
            drawLine(glow, oBL, iBL, strokeWidth = 2f)

            // Central energy node
            drawCircle(Color.White, radius = innerS * 0.4f, center = center)
        }
    }

    // 16. ROTATING RINGS: Triple gyro gimbal rings with attitude indicators
    fun DrawScope.drawRotatingRings(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.3f

        // Ring 1 (Yaw)
        val rot1 = (time * 50f) % 360f
        rotate(rot1, center) {
            drawCircle(primary, radius = maxR * pulse, center = center, style = Stroke(width = 3f))
            drawCircle(Color.White, radius = 4f, center = center + Offset(0f, -maxR * pulse))
        }

        // Ring 2 (Pitch - Oval projection)
        val rot2 = (-time * 70f) % 360f
        rotate(rot2, center) {
            drawOval(
                color = secondary,
                topLeft = Offset(center.x - maxR * 0.75f * pulse, center.y - maxR * 0.35f * pulse),
                size = Size(maxR * 1.5f * pulse, maxR * 0.7f * pulse),
                style = Stroke(width = 2.5f)
            )
        }

        // Ring 3 (Roll - Inner tilted)
        val rot3 = (time * 90f + 45f) % 360f
        rotate(rot3, center) {
            drawOval(
                color = ring,
                topLeft = Offset(center.x - maxR * 0.45f * pulse, center.y - maxR * 0.45f * pulse),
                size = Size(maxR * 0.9f * pulse, maxR * 0.9f * pulse),
                style = Stroke(width = 2f)
            )
        }

        // Gyro core
        drawCircle(Color.White, radius = maxR * 0.12f * pulse, center = center)
    }

    // 17. ENERGY BUBBLE: Buoyant plasma bubbles inside containment field
    fun DrawScope.drawEnergyBubble(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.3f

        // Outer membrane
        drawCircle(glow.copy(alpha = 0.3f), radius = maxR * pulse, center = center, style = Stroke(width = 2f))

        // Rising micro-bubbles
        val bubbleCount = 7
        for (i in 0 until bubbleCount) {
            val progress = ((time * (0.8f + i * 0.2f) + i * 0.3f) % 1f)
            val bx = center.x + sin(time * 3f + i * 2f) * (maxR * 0.6f)
            val by = (center.y + maxR * 0.7f) - progress * (maxR * 1.4f)
            val bRadius = (maxR * (0.08f + (i % 3) * 0.05f)) * (1f - progress * 0.4f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.9f), primary.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(bx, by),
                    radius = bRadius
                ),
                radius = bRadius,
                center = Offset(bx, by)
            )
        }
    }

    // 18. MATRIX CORE: Vertical digital rain glyph streams falling dynamically
    fun DrawScope.drawMatrixCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.3f

        // Boundary ring
        drawCircle(ring.copy(alpha = 0.4f), radius = maxR, center = center, style = Stroke(width = 1.5f))

        // 7 Digital Rain Columns
        val cols = 7
        for (c in 0 until cols) {
            val colX = center.x - maxR * 0.7f + c * (maxR * 1.4f / (cols - 1))
            val speed = 1.2f + (c % 3) * 0.4f
            val dropY = (center.y - maxR * 0.8f) + ((time * speed * maxR) % (maxR * 1.6f))

            // Trail dashes
            for (t in 0..4) {
                val y = dropY - t * 10f
                if (y in (center.y - maxR)..(center.y + maxR)) {
                    val alpha = if (t == 0) 1.0f else (1f - t * 0.22f)
                    val col = if (t == 0) Color.White else primary.copy(alpha = alpha)
                    drawLine(col, Offset(colX, y), Offset(colX, y + 6f), strokeWidth = 2.5f)
                }
            }
        }
    }

    // 19. COSMIC CORE: Nebula cloud with planetary ring orbit & radiant flares
    fun DrawScope.drawCosmicCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f

        // Cosmic central star
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, secondary.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = maxR * 0.5f * pulse
            ),
            radius = maxR * 0.5f * pulse,
            center = center
        )

        // Tilted Saturnian planetary rings
        rotate(28f + sin(time * 0.5f) * 6f, center) {
            drawOval(
                color = ring.copy(alpha = 0.7f),
                topLeft = Offset(center.x - maxR * 1.15f * pulse, center.y - maxR * 0.3f * pulse),
                size = Size(maxR * 2.3f * pulse, maxR * 0.6f * pulse),
                style = Stroke(width = 3.5f)
            )
            drawOval(
                color = secondary.copy(alpha = 0.4f),
                topLeft = Offset(center.x - maxR * 1.3f * pulse, center.y - maxR * 0.4f * pulse),
                size = Size(maxR * 2.6f * pulse, maxR * 0.8f * pulse),
                style = Stroke(width = 1.8f)
            )
        }

        // 4 Radiant diffraction flare spikes
        val flareLen = maxR * (0.8f + audioLevel * 0.5f)
        drawLine(Color.White.copy(alpha = 0.8f), Offset(center.x - flareLen, center.y), Offset(center.x + flareLen, center.y), strokeWidth = 1.8f)
        drawLine(Color.White.copy(alpha = 0.8f), Offset(center.x, center.y - flareLen), Offset(center.x, center.y + flareLen), strokeWidth = 1.8f)
    }

    // 20. MINIMAL DOT: Clean minimalist morphing dot & audio waveform bars
    fun DrawScope.drawMinimalDot(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.85f

        if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
            // Audio Equalizer Bar layout
            val barCount = 5
            val barW = 5f
            val spacing = 7f
            val totalW = barCount * barW + (barCount - 1) * spacing
            val startX = center.x - totalW / 2f

            for (i in 0 until barCount) {
                val hFactor = 0.3f + 0.7f * kotlin.math.abs(sin(time * 6f + i * 1.2f)) * (1f + audioLevel)
                val barH = (maxR * 0.9f * hFactor).coerceAtLeast(10f)
                val bx = startX + i * (barW + spacing)
                drawLine(
                    color = if (i == 2) Color.White else primary,
                    start = Offset(bx + barW / 2f, center.y - barH / 2f),
                    end = Offset(bx + barW / 2f, center.y + barH / 2f),
                    strokeWidth = barW,
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Pulsing minimal dot
            val dotR = maxR * (0.35f + 0.08f * sin(time * 3f))
            drawCircle(glow.copy(alpha = 0.35f), radius = dotR * 1.8f, center = center)
            drawCircle(primary, radius = dotR, center = center)
            drawCircle(Color.White, radius = dotR * 0.4f, center = center)
        }
    }
}
