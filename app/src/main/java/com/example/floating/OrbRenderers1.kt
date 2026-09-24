package com.example.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object OrbRenderers1 {

    // 1. PLASMA CORE: Organic multi-blob fluid with shifting radial gradients and electric arcs
    fun DrawScope.drawPlasmaCore(
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

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.45f * pulse), Color.Transparent),
                center = center,
                radius = maxR * 1.25f
            ),
            radius = maxR * 1.25f,
            center = center
        )

        // Multiple fluid plasma blobs rotating at different speeds
        val blobCount = 4
        for (i in 0 until blobCount) {
            val angle = time * 1.5f + (i * PI.toFloat() * 2f / blobCount)
            val dist = maxR * 0.35f * (1f + sin(time * 2f + i) * 0.2f)
            val blobCenter = center + Offset(cos(angle) * dist, sin(angle) * dist)
            val blobRadius = maxR * (0.35f + 0.08f * sin(time * 3f + i)) * pulse

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(if (i % 2 == 0) primary else secondary, Color.Transparent),
                    center = blobCenter,
                    radius = blobRadius
                ),
                radius = blobRadius,
                center = blobCenter
            )
        }

        // Electric plasma discharge arcs
        if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
            val arcPoints = 6
            val path = Path()
            for (p in 0 until arcPoints) {
                val a = time * 3f + (p * PI.toFloat() * 2f / arcPoints)
                val jitter = sin(time * 12f + p) * 8f * pulse
                val r = maxR * (0.6f + jitter / maxR)
                val pt = center + Offset(cos(a) * r, sin(a) * r)
                if (p == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            drawPath(path, primary.copy(alpha = 0.8f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }

        // Core nucleus
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.4f * pulse
            ),
            radius = maxR * 0.4f * pulse,
            center = center
        )
    }

    // 2. DIGITAL RING: Segmented cyber tick ring with angular velocity markers
    fun DrawScope.drawDigitalRing(
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

        // Outer segmented tick circle
        val tickCount = 28
        val rot1 = (time * 40f) % 360f
        rotate(rot1, center) {
            for (i in 0 until tickCount) {
                val angle = i * (360f / tickCount)
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val innerR = maxR * 0.82f
                val outerR = maxR * (if (i % 4 == 0) 1.0f else 0.92f)
                val p1 = center + Offset(cos(rad) * innerR, sin(rad) * innerR)
                val p2 = center + Offset(cos(rad) * outerR, sin(rad) * outerR)
                val color = if (i % 4 == 0) primary else secondary.copy(alpha = 0.6f)
                drawLine(color, p1, p2, strokeWidth = if (i % 4 == 0) 3.5f else 1.8f)
            }
        }

        // Counter-rotating inner status arc
        val rot2 = (-time * 65f) % 360f
        rotate(rot2, center) {
            drawArc(
                color = primary,
                startAngle = 0f,
                sweepAngle = 120f + audioLevel * 80f,
                useCenter = false,
                topLeft = Offset(center.x - maxR * 0.65f, center.y - maxR * 0.65f),
                size = Size(maxR * 1.3f, maxR * 1.3f),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            drawArc(
                color = ring,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - maxR * 0.65f, center.y - maxR * 0.65f),
                size = Size(maxR * 1.3f, maxR * 1.3f),
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }

        // Inner digital core with pulsing concentric ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.7f), Color.Transparent),
                center = center,
                radius = maxR * 0.4f * pulse
            ),
            radius = maxR * 0.4f * pulse,
            center = center
        )
        drawCircle(primary, radius = maxR * 0.2f * pulse, center = center, style = Stroke(width = 2.5f))
        drawCircle(Color.White, radius = maxR * 0.08f * pulse, center = center)
    }

    // 3. HOLOGRAPHIC SPHERE: 3D latitude & longitude wireframe rings tilted on perspective axes
    fun DrawScope.drawHolographicSphere(
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
        val pulse = 1f + audioLevel * 0.25f

        // Outer horizon circle
        drawCircle(glow.copy(alpha = 0.3f), radius = maxR * pulse, center = center, style = Stroke(width = 1.5f))

        // Latitude ellipses
        val latCount = 5
        for (i in 1..latCount) {
            val yOffset = (i - (latCount + 1) / 2f) / ((latCount + 1) / 2f) * maxR * 0.75f
            val rAtY = kotlin.math.sqrt((maxR * maxR - yOffset * yOffset).coerceAtLeast(0f)) * pulse
            val heightScale = 0.35f + 0.1f * sin(time * 2f + i)
            drawOval(
                color = primary.copy(alpha = 0.5f + 0.2f * (1f - kotlin.math.abs(yOffset) / maxR)),
                topLeft = Offset(center.x - rAtY, center.y + yOffset - rAtY * heightScale),
                size = Size(rAtY * 2f, rAtY * 2f * heightScale),
                style = Stroke(width = 1.8f)
            )
        }

        // Rotating Longitude meridian ellipses
        val lonCount = 4
        for (i in 0 until lonCount) {
            val rotAngle = time * 50f + i * (180f / lonCount)
            val widthFactor = kotlin.math.abs(cos(Math.toRadians(rotAngle.toDouble()).toFloat()))
            drawOval(
                color = secondary.copy(alpha = 0.6f * widthFactor.coerceIn(0.2f, 1f)),
                topLeft = Offset(center.x - maxR * widthFactor * pulse, center.y - maxR * pulse),
                size = Size(maxR * 2f * widthFactor * pulse, maxR * 2f * pulse),
                style = Stroke(width = 2f)
            )
        }

        // Horizontal scan lines effect
        val scanY = (center.y - maxR) + ((time * 70f) % (maxR * 2f))
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(center.x - maxR * 0.7f, scanY),
            end = Offset(center.x + maxR * 0.7f, scanY),
            strokeWidth = 2f
        )
    }

    // 4. ENERGY REACTOR: Arc reactor core with triple rotating flux triangles
    fun DrawScope.drawEnergyReactor(
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

        // Outer heavy segmented reactor ring
        drawCircle(ring, radius = maxR, center = center, style = Stroke(width = 4f))
        val segments = 8
        for (i in 0 until segments) {
            val a = Math.toRadians((i * (360.0 / segments)).toDouble()).toFloat()
            val pt = center + Offset(cos(a) * maxR, sin(a) * maxR)
            drawCircle(primary, radius = 4f, center = pt)
        }

        // Rotating Flux Triangle 1 (Clockwise)
        val rot1 = (time * 80f) % 360f
        rotate(rot1, center) {
            val path = Path()
            for (i in 0..2) {
                val a = Math.toRadians((i * 120.0).toDouble()).toFloat()
                val r = maxR * 0.65f * pulse
                val p = center + Offset(cos(a) * r, sin(a) * r)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, primary, style = Stroke(width = 3f, cap = StrokeCap.Round))
        }

        // Rotating Flux Triangle 2 (Counter-Clockwise)
        val rot2 = (-time * 60f + 60f) % 360f
        rotate(rot2, center) {
            val path = Path()
            for (i in 0..2) {
                val a = Math.toRadians((i * 120.0).toDouble()).toFloat()
                val r = maxR * 0.55f
                val p = center + Offset(cos(a) * r, sin(a) * r)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, secondary, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }

        // Central Supercharged Singularity
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.35f * pulse
            ),
            radius = maxR * 0.35f * pulse,
            center = center
        )
    }

    // 5. AI EYE: Sentient cyber iris with aperture blades, pupil scanning crosshair, and tracking ring
    fun DrawScope.drawAiEye(
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

        // Outer sclera cyber ring
        drawCircle(ring, radius = maxR, center = center, style = Stroke(width = 3f))

        // Aperture blades rotating
        val bladeCount = 6
        val bladeRot = (time * 30f) % 360f
        rotate(bladeRot, center) {
            for (i in 0 until bladeCount) {
                val a = Math.toRadians((i * (360.0 / bladeCount)).toDouble()).toFloat()
                val p1 = center + Offset(cos(a) * (maxR * 0.4f), sin(a) * (maxR * 0.4f))
                val p2 = center + Offset(cos(a + 0.8f) * maxR, sin(a + 0.8f) * maxR)
                drawLine(secondary.copy(alpha = 0.7f), p1, p2, strokeWidth = 2.5f)
            }
        }

        // Dilating Iris & Pupil
        val pupilRadius = maxR * (0.28f + 0.12f * sin(time * 3f)) * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary, secondary, Color.Black),
                center = center,
                radius = maxR * 0.5f
            ),
            radius = maxR * 0.5f,
            center = center
        )

        // Pupil center
        drawCircle(Color.Black, radius = pupilRadius, center = center)

        // Targeting HUD Crosshairs
        val crossLen = pupilRadius * 1.5f
        drawLine(Color.White, Offset(center.x - crossLen, center.y), Offset(center.x + crossLen, center.y), strokeWidth = 1.5f)
        drawLine(Color.White, Offset(center.x, center.y - crossLen), Offset(center.x, center.y + crossLen), strokeWidth = 1.5f)
        drawCircle(primary, radius = pupilRadius * 0.4f, center = center)
    }

    // 6. PARTICLE GALAXY: Swirling logarithmic cosmic particle spiral around singularity
    fun DrawScope.drawParticleGalaxy(
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

        // Central Event Horizon
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.3f
            ),
            radius = maxR * 0.3f,
            center = center
        )

        // 3 Spiral Arms of Stars
        val arms = 3
        val particlesPerArm = 16
        for (arm in 0 until arms) {
            val armOffset = arm * (2f * PI.toFloat() / arms)
            for (p in 1..particlesPerArm) {
                val frac = p.toFloat() / particlesPerArm
                val r = frac * maxR * pulse
                val theta = time * 2.5f + armOffset + (frac * 4f)
                val pos = center + Offset(cos(theta) * r, sin(theta) * r)
                val pSize = (1.5f + (1f - frac) * 3f) * (1f + audioLevel * 0.5f)
                val pColor = if (p % 2 == 0) primary else secondary
                drawCircle(pColor.copy(alpha = (1f - frac * 0.4f)), radius = pSize, center = pos)
            }
        }
    }

    // 7. CYBER CORE: Hexagonal shields with pulsating central data nexus
    fun DrawScope.drawCyberCore(
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

        // Outer Hexagon
        val rotHex = (time * 35f) % 360f
        rotate(rotHex, center) {
            val path = Path()
            for (i in 0..5) {
                val a = Math.toRadians((i * 60.0).toDouble()).toFloat()
                val pt = center + Offset(cos(a) * maxR, sin(a) * maxR)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            drawPath(path, ring, style = Stroke(width = 3.5f))
        }

        // Inner Inverted Hexagon
        rotate(-rotHex * 1.4f, center) {
            val path = Path()
            for (i in 0..5) {
                val a = Math.toRadians((i * 60.0 + 30.0).toDouble()).toFloat()
                val pt = center + Offset(cos(a) * maxR * 0.65f * pulse, sin(a) * maxR * 0.65f * pulse)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            drawPath(path, primary, style = Stroke(width = 2.5f))
        }

        // Pulsing Data Nexus Box in center
        val boxR = maxR * 0.28f * pulse
        drawRect(
            color = secondary,
            topLeft = Offset(center.x - boxR, center.y - boxR),
            size = Size(boxR * 2f, boxR * 2f),
            style = Stroke(width = 2f)
        )
        drawCircle(Color.White, radius = boxR * 0.4f, center = center)
    }

    // 8. LIQUID ORB: Organic fluid droplet with chromatic surface tension waves
    fun DrawScope.drawLiquidOrb(
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
        val pulse = 1f + audioLevel * 0.35f

        // Wobbly liquid perimeter
        val path = Path()
        val numPoints = 24
        for (i in 0 until numPoints) {
            val angle = i * (2f * PI.toFloat() / numPoints)
            val wobble = sin(angle * 3f + time * 4f) * 6f + cos(angle * 5f - time * 3f) * 4f
            val r = (maxR + wobble * pulse) * pulse
            val pt = center + Offset(cos(angle) * r, sin(angle) * r)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()

        drawPath(
            path,
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.85f), secondary.copy(alpha = 0.6f), ring.copy(alpha = 0.9f)),
                center = center + Offset(-maxR * 0.2f, -maxR * 0.2f),
                radius = maxR * 1.1f
            )
        )

        // Gloss Specular Highlight
        drawOval(
            color = Color.White.copy(alpha = 0.75f),
            topLeft = Offset(center.x - maxR * 0.5f, center.y - maxR * 0.65f),
            size = Size(maxR * 0.45f, maxR * 0.25f)
        )
    }

    // 9. HEX REACTOR: Honeycomb matrix with radiating pulse waves
    fun DrawScope.drawHexReactor(
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

        // Central Hex Cell
        drawHexagon(center, maxR * 0.32f * pulse, primary, true)

        // 6 Surrounding Hex Cells
        for (i in 0..5) {
            val a = Math.toRadians((i * 60.0).toDouble()).toFloat()
            val dist = maxR * 0.6f
            val c = center + Offset(cos(a) * dist, sin(a) * dist)
            val waveAlpha = (0.3f + 0.6f * sin(time * 3f + i)).coerceIn(0.2f, 1f)
            drawHexagon(c, maxR * 0.25f, secondary.copy(alpha = waveAlpha), false)
        }

        // Outer Containment Ring with Dashes
        drawCircle(
            color = ring,
            radius = maxR,
            center = center,
            style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
        )
    }

    private fun DrawScope.drawHexagon(c: Offset, r: Float, col: Color, fill: Boolean) {
        val path = Path()
        for (i in 0..5) {
            val a = Math.toRadians((i * 60.0).toDouble()).toFloat()
            val pt = c + Offset(cos(a) * r, sin(a) * r)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()
        if (fill) {
            drawPath(path, col, style = Fill)
        } else {
            drawPath(path, col, style = Stroke(width = 2f))
        }
    }

    // 10. NEURAL NETWORK: Synaptic nodes with glowing firing axon bridges
    fun DrawScope.drawNeuralNetwork(
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

        // 7 Synaptic Nodes
        val nodes = listOf(
            center,
            center + Offset(-maxR * 0.6f, -maxR * 0.4f),
            center + Offset(maxR * 0.5f, -maxR * 0.5f),
            center + Offset(maxR * 0.7f, maxR * 0.2f),
            center + Offset(-maxR * 0.5f, maxR * 0.6f),
            center + Offset(0f, -maxR * 0.75f),
            center + Offset(maxR * 0.2f, maxR * 0.7f)
        )

        // Axon connections
        val connections = listOf(
            0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5, 0 to 6,
            1 to 5, 2 to 5, 2 to 3, 3 to 6, 4 to 6
        )

        for ((idx, pair) in connections.withIndex()) {
            val p1 = nodes[pair.first]
            val p2 = nodes[pair.second]
            drawLine(ring.copy(alpha = 0.45f), p1, p2, strokeWidth = 1.8f)

            // Firing action potential packet moving along axon
            val packetProgress = ((time * 2f + idx * 0.3f) % 1f)
            val packetPos = p1 + (p2 - p1) * packetProgress
            drawCircle(Color.White, radius = 2.5f * pulse, center = packetPos)
        }

        // Draw Nodes
        for ((idx, node) in nodes.withIndex()) {
            val nodeR = (if (idx == 0) maxR * 0.16f else maxR * 0.1f) * pulse
            drawCircle(glow.copy(alpha = 0.5f), radius = nodeR * 1.5f, center = node)
            drawCircle(if (idx == 0) primary else secondary, radius = nodeR, center = node)
        }
    }
}
