package com.example.floating

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object OrbRenderers3 {

    // 21. RADAR CORE: Sweeping radar phosphor trail with target blips
    fun DrawScope.drawRadarCore(
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

        // Concentric range rings
        drawCircle(ring.copy(alpha = 0.4f), radius = maxR, center = center, style = Stroke(width = 2f))
        drawCircle(ring.copy(alpha = 0.3f), radius = maxR * 0.65f, center = center, style = Stroke(width = 1.5f))
        drawCircle(ring.copy(alpha = 0.25f), radius = maxR * 0.35f, center = center, style = Stroke(width = 1.5f))

        // Cardinal crosshair lines
        drawLine(ring.copy(alpha = 0.3f), Offset(center.x - maxR, center.y), Offset(center.x + maxR, center.y), strokeWidth = 1f)
        drawLine(ring.copy(alpha = 0.3f), Offset(center.x, center.y - maxR), Offset(center.x, center.y + maxR), strokeWidth = 1f)

        // Sweeping beam
        val sweepAngle = (time * 120f) % 360f
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Transparent, primary.copy(alpha = 0.7f)),
                center = center
            ),
            startAngle = sweepAngle - 55f,
            sweepAngle = 55f,
            useCenter = true,
            topLeft = Offset(center.x - maxR, center.y - maxR),
            size = Size(maxR * 2f, maxR * 2f)
        )

        // Detected target blips
        val blip1 = center + Offset(maxR * 0.5f, -maxR * 0.3f)
        drawCircle(Color.White, radius = 3.5f, center = blip1)
        drawCircle(glow.copy(alpha = 0.7f), radius = 7f, center = blip1, style = Stroke(width = 1.5f))
    }

    // 22. PULSING CIRCLE: Harmonic audio shockwaves with frequency ring
    fun DrawScope.drawPulsingCircle(
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
        val pulse = 1f + audioLevel * 0.45f

        // Outer pulsing ring
        drawCircle(primary.copy(alpha = 0.7f), radius = maxR * pulse, center = center, style = Stroke(width = 3.5f))

        // Multiple concentric shockwaves
        for (i in 1..3) {
            val r = maxR * (0.3f + i * 0.22f) * pulse
            val alpha = (0.8f - i * 0.2f).coerceIn(0.2f, 1f)
            drawCircle(secondary.copy(alpha = alpha), radius = r, center = center, style = Stroke(width = 2f))
        }

        // Core dot
        drawCircle(Color.White, radius = maxR * 0.2f * pulse, center = center)
    }

    // 23. CRYSTAL CORE: Faceted quartz polygon with prismatic refractions
    fun DrawScope.drawCrystalCore(
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

        val rot = (time * 30f) % 360f
        rotate(rot, center) {
            val topVertex = center + Offset(0f, -maxR * pulse)
            val bottomVertex = center + Offset(0f, maxR * pulse)
            val leftVertex = center + Offset(-maxR * 0.75f * pulse, 0f)
            val rightVertex = center + Offset(maxR * 0.75f * pulse, 0f)
            val midLeft = center + Offset(-maxR * 0.3f, -maxR * 0.2f)
            val midRight = center + Offset(maxR * 0.3f, maxR * 0.2f)

            // Diamond boundary
            val path = Path().apply {
                moveTo(topVertex.x, topVertex.y)
                lineTo(rightVertex.x, rightVertex.y)
                lineTo(bottomVertex.x, bottomVertex.y)
                lineTo(leftVertex.x, leftVertex.y)
                close()
            }
            drawPath(path, primary.copy(alpha = 0.35f), style = Fill)
            drawPath(path, primary, style = Stroke(width = 2.5f))

            // Internal facet facets
            drawLine(secondary, topVertex, midLeft, strokeWidth = 2f)
            drawLine(secondary, topVertex, midRight, strokeWidth = 2f)
            drawLine(secondary, bottomVertex, midLeft, strokeWidth = 2f)
            drawLine(secondary, bottomVertex, midRight, strokeWidth = 2f)
            drawLine(ring, leftVertex, midLeft, strokeWidth = 2f)
            drawLine(ring, rightVertex, midRight, strokeWidth = 2f)
            drawLine(Color.White, midLeft, midRight, strokeWidth = 2.5f)
        }
    }

    // 24. FLOATING FLAME: Cybernetic plasma fire licking upwards with embers
    fun DrawScope.drawFloatingFlame(
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

        // Flame teardrop tongue
        val path = Path()
        val baseLeft = center + Offset(-maxR * 0.45f * pulse, maxR * 0.6f)
        val baseRight = center + Offset(maxR * 0.45f * pulse, maxR * 0.6f)
        val flameTip = center + Offset(sin(time * 6f) * 12f, -maxR * pulse)

        path.moveTo(baseLeft.x, baseLeft.y)
        path.cubicTo(
            center.x - maxR * 0.8f, center.y,
            center.x - maxR * 0.3f, center.y - maxR * 0.5f,
            flameTip.x, flameTip.y
        )
        path.cubicTo(
            center.x + maxR * 0.3f, center.y - maxR * 0.5f,
            center.x + maxR * 0.8f, center.y,
            baseRight.x, baseRight.y
        )
        path.close()

        drawPath(
            path,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, primary, secondary, Color.Transparent),
                startY = flameTip.y,
                endY = baseLeft.y
            )
        )

        // Rising embers
        for (i in 0..4) {
            val progress = ((time * (1.5f + i * 0.3f) + i * 0.2f) % 1f)
            val ex = center.x + sin(time * 4f + i) * (maxR * 0.4f)
            val ey = (center.y + maxR * 0.3f) - progress * maxR * 1.3f
            drawCircle(Color.White.copy(alpha = 1f - progress), radius = 2.5f, center = Offset(ex, ey))
        }
    }

    // 25. DIGITAL BRAIN: Bilateral cerebral nodes with synaptic bridges
    fun DrawScope.drawDigitalBrain(
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

        // Left hemisphere lobe
        drawOval(
            primary.copy(alpha = 0.5f),
            topLeft = Offset(center.x - maxR * 0.85f * pulse, center.y - maxR * 0.7f * pulse),
            size = Size(maxR * 0.75f * pulse, maxR * 1.4f * pulse),
            style = Stroke(width = 2.5f)
        )

        // Right hemisphere lobe
        drawOval(
            secondary.copy(alpha = 0.5f),
            topLeft = Offset(center.x + maxR * 0.1f * pulse, center.y - maxR * 0.7f * pulse),
            size = Size(maxR * 0.75f * pulse, maxR * 1.4f * pulse),
            style = Stroke(width = 2.5f)
        )

        // Corpus callosum bridge synapses
        for (i in -2..2) {
            val y = center.y + i * (maxR * 0.25f)
            drawLine(Color.White.copy(alpha = 0.7f), Offset(center.x - maxR * 0.3f, y), Offset(center.x + maxR * 0.3f, y), strokeWidth = 2f)
        }

        // Firing cerebral lights
        val firing = (time * 5f).toInt() % 4
        val fireNode = center + Offset(if (firing % 2 == 0) -maxR * 0.4f else maxR * 0.4f, (firing - 1.5f) * maxR * 0.3f)
        drawCircle(Color.White, radius = 4f * pulse, center = fireNode)
    }

    // 26. CIRCUIT CORE: PCB bus traces with traveling packet pulses
    fun DrawScope.drawCircuitCore(
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

        // Central silicon microchip die
        val chipS = maxR * 0.38f * pulse
        drawRect(ring, topLeft = Offset(center.x - chipS, center.y - chipS), size = Size(chipS * 2f, chipS * 2f), style = Stroke(width = 3f))
        drawRect(primary.copy(alpha = 0.4f), topLeft = Offset(center.x - chipS, center.y - chipS), size = Size(chipS * 2f, chipS * 2f), style = Fill)
        drawCircle(Color.White, radius = chipS * 0.3f, center = center)

        // 8 Orthogonal circuit traces extending outwards
        val dirs = listOf(
            Offset(0f, -1f), Offset(1f, 0f), Offset(0f, 1f), Offset(-1f, 0f),
            Offset(0.7f, -0.7f), Offset(0.7f, 0.7f), Offset(-0.7f, 0.7f), Offset(-0.7f, -0.7f)
        )

        for ((idx, d) in dirs.withIndex()) {
            val start = center + d * chipS
            val end = center + d * maxR
            drawLine(secondary, start, end, strokeWidth = 2f)
            drawCircle(ring, radius = 3.5f, center = end)

            // Packet pulse along trace
            val progress = ((time * 2f + idx * 0.25f) % 1f)
            val packet = start + (end - start) * progress
            drawCircle(Color.White, radius = 2.5f, center = packet)
        }
    }

    // 27. 3D WIREFRAME: Rotating icosahedron wireframe with depth shading
    fun DrawScope.drawWireframe3D(
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

        val rot = (time * 40f) % 360f
        rotate(rot, center) {
            val r = maxR * 0.8f * pulse
            // Hexagon wireframe base
            val verts = (0..5).map {
                val a = Math.toRadians((it * 60.0).toDouble()).toFloat()
                center + Offset(cos(a) * r, sin(a) * r)
            }

            for (i in 0..5) {
                drawLine(primary, verts[i], verts[(i + 1) % 6], strokeWidth = 2f)
                drawLine(secondary.copy(alpha = 0.6f), center, verts[i], strokeWidth = 1.5f)
                drawCircle(Color.White, radius = 3f, center = verts[i])
            }
            drawCircle(Color.White, radius = 4f * pulse, center = center)
        }
    }

    // 28. LIGHT TUNNEL: Hyperspace wormhole rings zooming inward
    fun DrawScope.drawLightTunnel(
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

        val ringCount = 6
        for (i in 0 until ringCount) {
            val progress = ((time * 1.5f + i.toFloat() / ringCount) % 1f)
            val r = (1f - progress) * maxR * pulse
            val alpha = progress.coerceIn(0.1f, 1f)
            drawCircle(
                color = if (i % 2 == 0) primary.copy(alpha = alpha) else secondary.copy(alpha = alpha),
                radius = r.coerceAtLeast(2f),
                center = center,
                style = Stroke(width = 2.5f * (1f - progress * 0.4f))
            )
        }
        drawCircle(Color.White, radius = maxR * 0.08f * pulse, center = center)
    }

    // 29. MAGNETIC CORE: Toroidal magnetic flux loops with orbiting ions
    fun DrawScope.drawMagneticCore(
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

        // Central North/South Magnetic Dipole
        drawLine(Color.White, center + Offset(0f, -maxR * 0.4f), center + Offset(0f, maxR * 0.4f), strokeWidth = 4f, cap = StrokeCap.Round)

        // Toroidal magnetic flux lines
        val loops = 4
        for (i in 1..loops) {
            val w = maxR * (i.toFloat() / loops) * pulse
            val h = maxR * 0.9f * pulse
            drawOval(
                color = primary.copy(alpha = 0.4f + 0.15f * i),
                topLeft = Offset(center.x - w, center.y - h / 2f),
                size = Size(w * 2f, h),
                style = Stroke(width = 1.8f)
            )
        }

        // Orbiting ionized charged particle
        val ionA = time * 4f
        val ionX = center.x + cos(ionA) * (maxR * 0.65f)
        val ionY = center.y + sin(ionA) * (maxR * 0.35f)
        drawCircle(Color.White, radius = 4f, center = Offset(ionX, ionY))
        drawCircle(glow.copy(alpha = 0.8f), radius = 8f, center = Offset(ionX, ionY), style = Stroke(width = 1.5f))
    }

    // 30. TITONOX SIGNATURE CORE: Dual runic rings, central hyper-cube & audio needles
    fun DrawScope.drawTitonoxSignature(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.90f
        val pulse = 1f + audioLevel * 0.4f

        // 1. Outer Heavy Titan Ring with 4 Cyber Thruster Fins
        drawCircle(ring, radius = maxR, center = center, style = Stroke(width = 3.5f))
        for (i in 0..3) {
            val a = Math.toRadians((i * 90.0 + 45.0).toDouble()).toFloat()
            val pt = center + Offset(cos(a) * maxR, sin(a) * maxR)
            drawCircle(primary, radius = 5f, center = pt)
            drawLine(primary, pt, pt + Offset(cos(a) * 10f, sin(a) * 10f), strokeWidth = 3f)
        }

        // 2. Rotating Runic Ring (Clockwise)
        val rot1 = (time * 50f) % 360f
        rotate(rot1, center) {
            drawCircle(
                color = secondary.copy(alpha = 0.8f),
                radius = maxR * 0.78f * pulse,
                center = center,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
            // 8 Runic tick teeth
            for (i in 0..7) {
                val a = Math.toRadians((i * 45.0).toDouble()).toFloat()
                val p1 = center + Offset(cos(a) * (maxR * 0.72f * pulse), sin(a) * (maxR * 0.72f * pulse))
                val p2 = center + Offset(cos(a) * (maxR * 0.78f * pulse), sin(a) * (maxR * 0.78f * pulse))
                drawLine(Color.White, p1, p2, strokeWidth = 2.5f)
            }
        }

        // 3. Counter-Rotating Inner Ring (Counter-Clockwise)
        val rot2 = (-time * 75f) % 360f
        rotate(rot2, center) {
            drawArc(
                color = primary,
                startAngle = 0f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - maxR * 0.55f * pulse, center.y - maxR * 0.55f * pulse),
                size = Size(maxR * 1.1f * pulse, maxR * 1.1f * pulse),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }

        // 4. Central Pulsating Hyper-Diamond
        val rotDiamond = (time * 100f) % 360f
        rotate(rotDiamond, center) {
            val diaR = maxR * 0.32f * pulse
            val p = Path().apply {
                moveTo(center.x, center.y - diaR)
                lineTo(center.x + diaR, center.y)
                lineTo(center.x, center.y + diaR)
                lineTo(center.x - diaR, center.y)
                close()
            }
            drawPath(p, primary, style = Stroke(width = 3f))
        }

        // 5. Audio-reactive Core Needle Needles
        val needleCount = 8
        for (i in 0 until needleCount) {
            val a = Math.toRadians((i * (360.0 / needleCount)).toDouble()).toFloat()
            val needleLen = (maxR * 0.2f + audioLevel * maxR * 0.25f)
            val p1 = center + Offset(cos(a) * (maxR * 0.12f), sin(a) * (maxR * 0.12f))
            val p2 = center + Offset(cos(a) * (maxR * 0.12f + needleLen), sin(a) * (maxR * 0.12f + needleLen))
            drawLine(Color.White, p1, p2, strokeWidth = 2f)
        }

        // 6. Blinding Core Singularity
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.22f * pulse
            ),
            radius = maxR * 0.22f * pulse,
            center = center
        )
    }

    // 31. SUPERNOVA CORE: Radiant stellar blast with cosmic prominence loops
    fun DrawScope.drawSupernovaCore(
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
        val pulse = 1f + audioLevel * 0.5f

        // Explosive shockwave corona
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, secondary.copy(alpha = 0.6f), Color.Transparent),
                center = center,
                radius = maxR * pulse
            ),
            radius = maxR * pulse,
            center = center
        )

        // 12 Radiant shockwave rays
        val rays = 12
        for (i in 0 until rays) {
            val a = Math.toRadians((i * (360.0 / rays) + time * 20f).toDouble()).toFloat()
            val rayLen = maxR * (0.6f + 0.35f * sin(time * 8f + i)) * pulse
            drawLine(Color.White, center, center + Offset(cos(a) * rayLen, sin(a) * rayLen), strokeWidth = 2f)
        }
    }

    // 32. SOLAR FLARE: Coronal loops arcing around blazing solar core
    fun DrawScope.drawSolarFlare(
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

        // Blazing solar sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, secondary, ring),
                center = center,
                radius = maxR * 0.6f * pulse
            ),
            radius = maxR * 0.6f * pulse,
            center = center
        )

        // 4 Coronal magnetic plasma loops
        for (i in 0..3) {
            val baseAngle = Math.toRadians((i * 90.0 + time * 30f).toDouble()).toFloat()
            val loopCenter = center + Offset(cos(baseAngle) * (maxR * 0.65f), sin(baseAngle) * (maxR * 0.65f))
            drawCircle(
                color = primary.copy(alpha = 0.7f),
                radius = maxR * 0.25f * (1f + sin(time * 4f + i) * 0.2f) * pulse,
                center = loopCenter,
                style = Stroke(width = 2.5f)
            )
        }
    }
}
