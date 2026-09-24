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
import kotlin.math.sqrt

/**
 * 3D Orb Renderers with Real Cartesian (X, Y, Z) 3D Rotations.
 *
 * Supports finger drag and voice-driven rotation:
 * - manualRotX: Rotation around X axis (vertical pitch)
 * - manualRotY: Rotation around Y axis (horizontal yaw)
 * - manualRotZ: Rotation around Z axis (roll)
 */
object Orb3DRenderers {

    // Helper: 3D to 2D perspective projection
    private data class Vec3(val x: Float, val y: Float, val z: Float) {
        fun rotateY(angle: Float): Vec3 {
            val c = cos(angle)
            val s = sin(angle)
            return Vec3(x * c + z * s, y, -x * s + z * c)
        }
        fun rotateX(angle: Float): Vec3 {
            val c = cos(angle)
            val s = sin(angle)
            return Vec3(x, y * c - z * s, y * s + z * c)
        }
        fun rotateZ(angle: Float): Vec3 {
            val c = cos(angle)
            val s = sin(angle)
            return Vec3(x * c - y * s, x * s + y * c, z)
        }
        fun rotateXYZ(rx: Float, ry: Float, rz: Float): Vec3 {
            var v = this
            if (rx != 0f) v = v.rotateX(rx)
            if (ry != 0f) v = v.rotateY(ry)
            if (rz != 0f) v = v.rotateZ(rz)
            return v
        }
        fun project(center: Offset, fov: Float = 300f): Pair<Offset, Float> {
            val dist = fov / (fov + z).coerceAtLeast(10f)
            return Pair(Offset(center.x + x * dist, center.y + y * dist), dist)
        }
    }

    // 1. PLASMA SPHERE (3D)
    fun DrawScope.drawPlasmaSphere(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        // Volumetric 3D Shading sphere with off-center light source rotated in 3D
        val lightAngle = degY * 0.02f
        val lightOffset = center - Offset(maxR * 0.35f * cos(lightAngle), maxR * 0.35f * (1f + degX * 0.01f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    primary,
                    secondary.copy(alpha = 0.85f),
                    Color(0xFF030D1B)
                ),
                center = lightOffset,
                radius = maxR * 1.1f * pulse
            ),
            radius = maxR * pulse,
            center = center
        )

        // 3D Depth Plasma undulations (latitude bands rotating in perspective)
        for (b in 1..4) {
            val bandY = (b - 2.5f) * (maxR * 0.35f) + (manualRotX * 20f)
            val bandR = sqrt((maxR * maxR - bandY * bandY).coerceAtLeast(0f)) * pulse
            val tiltAngle = 20f + degX * 0.5f
            rotate(tiltAngle + degY, pivot = center) {
                drawOval(
                    color = glow.copy(alpha = 0.4f * (1f + sin(time * 3f + b + manualRotY) * 0.3f)),
                    topLeft = center + Offset(-bandR, bandY - 4f),
                    size = Size(bandR * 2, 8f + audioLevel * 8f)
                )
            }
        }

        // Specular glint
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.Transparent),
                center = lightOffset,
                radius = maxR * 0.25f
            ),
            radius = maxR * 0.25f,
            center = lightOffset
        )
    }

    // 2. CRYSTAL CORE (3D)
    fun DrawScope.drawCrystalCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val s = maxR * 0.75f * pulse

        // 3D Octahedron 6 vertices
        val vTop = Vec3(0f, -s, 0f)
        val vBottom = Vec3(0f, s, 0f)
        val vRight = Vec3(s, 0f, 0f)
        val vFront = Vec3(0f, 0f, s)
        val vLeft = Vec3(-s, 0f, 0f)
        val vBack = Vec3(0f, 0f, -s)

        val rotX = time * 0.8f + manualRotX
        val rotY = time * 1.2f + manualRotY
        val rotZ = manualRotZ

        val verts = listOf(vTop, vBottom, vRight, vFront, vLeft, vBack).map {
            it.rotateXYZ(rotX, rotY, rotZ).project(center)
        }

        val faces = listOf(
            listOf(0, 3, 2),
            listOf(0, 4, 3),
            listOf(0, 5, 4),
            listOf(0, 2, 5),
            listOf(1, 2, 3),
            listOf(1, 3, 4),
            listOf(1, 4, 5),
            listOf(1, 5, 2)
        )

        faces.forEachIndexed { idx, face ->
            val p0 = verts[face[0]].first
            val p1 = verts[face[1]].first
            val p2 = verts[face[2]].first

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }

            val faceColor = if (idx % 2 == 0) primary.copy(alpha = 0.45f) else secondary.copy(alpha = 0.35f)
            drawPath(path, color = faceColor, style = Fill)
            drawPath(path, color = Color.White.copy(alpha = 0.8f), style = Stroke(width = 2f))
        }

        drawCircle(color = glow, radius = 5f + audioLevel * 6f, center = verts[0].first)
    }

    // 3. QUANTUM REACTOR (3D)
    fun DrawScope.drawQuantumReactor(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        // Central glowing fusion core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, secondary.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = maxR * 0.35f * pulse
            ),
            radius = maxR * 0.35f * pulse,
            center = center
        )

        // 3 Tilted 3D Magnetic Tokamak Rings with real orientation
        val angles = listOf(0f, 60f, 120f)
        for (i in 0 until 3) {
            val a = angles[i]
            rotate(a + time * 15f + degY, pivot = center) {
                val rx = maxR * 0.9f * pulse
                val ry = maxR * 0.3f * pulse * (1f + sin(manualRotX) * 0.3f)
                drawOval(
                    color = if (i == 0) primary else if (i == 1) secondary else ring,
                    topLeft = center - Offset(rx, ry),
                    size = Size(rx * 2, ry * 2),
                    style = Stroke(width = 3f)
                )

                // Orbiting quantum electron charge
                val orbAngle = time * (3f + i) + manualRotY
                val ex = cos(orbAngle) * rx
                val ey = sin(orbAngle) * ry
                drawCircle(color = Color.White, radius = 4.5f + audioLevel * 3f, center = center + Offset(ex, ey))
            }
        }
    }

    // 4. MECHANICAL REACTOR (3D)
    fun DrawScope.drawMechanicalReactor(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        fun draw3DGear(c: Offset, radius: Float, teeth: Int, rot: Float, color: Color, tilt: Float = 0f) {
            rotate(tilt + degX, pivot = c) {
                rotate(rot + degY, pivot = c) {
                    val path = Path()
                    for (i in 0 until teeth * 2) {
                        val angle = i * (360f / (teeth * 2))
                        val rad = Math.toRadians(angle.toDouble()).toFloat()
                        val r = if (i % 2 == 0) radius else radius * 0.82f
                        val pt = c + Offset(cos(rad) * r, sin(rad) * r * 0.7f)
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    path.close()
                    drawPath(path, color = color.copy(alpha = 0.45f), style = Fill)
                    drawPath(path, color = color, style = Stroke(width = 2.5f))
                    drawCircle(color = Color(0xFF031628), radius = radius * 0.35f, center = c)
                    drawCircle(color = color, radius = radius * 0.35f, center = c, style = Stroke(width = 2f))
                }
            }
        }

        draw3DGear(center - Offset(maxR * 0.25f, 0f), maxR * 0.55f, 14, time * 35f, primary, tilt = 15f)
        draw3DGear(center + Offset(maxR * 0.35f, maxR * 0.2f), maxR * 0.45f, 10, -time * 49f, secondary, tilt = -25f)
        drawCircle(color = Color.White, radius = 6f + audioLevel * 6f, center = center)
    }

    // 5. HOLOGRAPHIC GLOBE (3D)
    fun DrawScope.drawHolographicGlobe(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f
        val globeR = maxR * 0.8f * pulse
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        drawCircle(color = ring.copy(alpha = 0.4f), radius = globeR, center = center, style = Stroke(width = 2f))

        // 3D Latitude Parallels
        val latCount = 5
        for (i in 1..latCount) {
            val yNorm = (i / (latCount + 1).toFloat()) * 2f - 1f
            val y = center.y + yNorm * globeR + (manualRotX * 15f)
            val rx = sqrt((globeR * globeR - (y - center.y) * (y - center.y)).coerceAtLeast(0f))
            drawOval(
                color = secondary.copy(alpha = 0.45f),
                topLeft = Offset(center.x - rx, y - rx * 0.25f),
                size = Size(rx * 2, rx * 0.5f),
                style = Stroke(width = 1.5f)
            )
        }

        // 3D Longitude Meridians
        val longCount = 4
        for (i in 0 until longCount) {
            val angle = time * 20f + i * (180f / longCount) + degY
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val rx = globeR * absCos(rad)
            drawOval(
                color = primary.copy(alpha = 0.65f),
                topLeft = Offset(center.x - rx, center.y - globeR),
                size = Size(rx * 2, globeR * 2),
                style = Stroke(width = 1.8f)
            )
        }

        // Orbiting satellite ring
        rotate(time * 30f + degX + degY, pivot = center) {
            drawOval(
                color = glow.copy(alpha = 0.8f),
                topLeft = center - Offset(maxR, maxR * 0.35f),
                size = Size(maxR * 2, maxR * 0.7f),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            )
            drawCircle(color = Color.White, radius = 5f + audioLevel * 4f, center = center + Offset(maxR, 0f))
        }
    }

    private fun absCos(v: Float): Float = kotlin.math.abs(cos(v)).coerceAtLeast(0.05f)

    // 6. GALAXY CORE (3D)
    fun DrawScope.drawGalaxyCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        rotate(-25f + degY, pivot = center) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primary, secondary.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = maxR * 0.35f * pulse
                ),
                radius = maxR * 0.35f * pulse,
                center = center
            )

            val arms = 2
            val starsPerArm = 28
            for (a in 0 until arms) {
                val armOffset = a * PI.toFloat()
                for (s in 0 until starsPerArm) {
                    val fraction = s / starsPerArm.toFloat()
                    val r = maxR * fraction * pulse
                    val theta = fraction * 4f + time * 0.8f + armOffset
                    val x = center.x + cos(theta) * r
                    val y = center.y + sin(theta) * r * (0.45f + sin(manualRotX) * 0.25f)

                    val starColor = if (fraction < 0.35f) Color.White else if (a == 0) primary else secondary
                    drawCircle(
                        color = starColor.copy(alpha = (1f - fraction * 0.5f)),
                        radius = (2f + (1f - fraction) * 3f + audioLevel * 2f),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }

    // 7. LIQUID ENERGY SPHERE (3D)
    fun DrawScope.drawLiquidEnergySphere(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        val path = Path()
        val points = 60
        for (i in 0..points) {
            val rad = (i / points.toFloat()) * PI.toFloat() * 2f
            val wave = sin(rad * 4f + time * 3f + manualRotY) * (maxR * 0.08f * pulse) +
                       cos(rad * 7f - time * 2f + manualRotX) * (maxR * 0.04f * pulse)
            val r = maxR * 0.72f * pulse + wave
            val pt = center + Offset(cos(rad) * r, sin(rad) * r)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()

        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    primary,
                    secondary.copy(alpha = 0.8f),
                    Color(0xFF031628)
                ),
                center = center - Offset(maxR * 0.25f * cos(manualRotY), maxR * 0.25f * (1f + manualRotX * 0.5f)),
                radius = maxR * 0.9f
            ),
            style = Fill
        )
        drawPath(path, color = glow, style = Stroke(width = 2.5f))

        drawOval(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = center - Offset(maxR * 0.45f, maxR * 0.5f) + Offset(degY * 0.4f, degX * 0.4f),
            size = Size(maxR * 0.25f, maxR * 0.12f)
        )
    }

    // 8. 3D NEURAL BRAIN
    fun DrawScope.draw3DNeuralBrain(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f

        val hemispheres = listOf(-1f, 1f)
        hemispheres.forEach { side ->
            val hCenter = center + Offset(side * maxR * 0.35f, 0f)
            val rotY = time * 0.8f + manualRotY
            val rotX = manualRotX
            val rotZ = manualRotZ

            val nodes = 8
            val pts = ArrayList<Offset>(nodes)
            for (i in 0 until nodes) {
                val v = Vec3(
                    x = (cos(i * 1.0f) * maxR * 0.28f),
                    y = (sin(i * 1.0f) * maxR * 0.45f),
                    z = (sin(i * 2.0f) * maxR * 0.2f)
                ).rotateXYZ(rotX, rotY, rotZ).project(hCenter).first
                pts.add(v)
            }

            for (i in 0 until nodes) {
                val next = (i + 1) % nodes
                drawLine(
                    color = if (side < 0) primary.copy(alpha = 0.5f) else secondary.copy(alpha = 0.5f),
                    start = pts[i],
                    end = pts[next],
                    strokeWidth = 1.8f
                )
                val spark = (time * 2f + i) % 1f
                val spPos = pts[i] + (pts[next] - pts[i]) * spark
                drawCircle(color = Color.White, radius = 2.5f + audioLevel * 2f, center = spPos)
            }

            pts.forEach { pt ->
                drawCircle(color = glow, radius = 4f, center = pt)
            }
        }

        drawLine(
            color = Color.White,
            start = center - Offset(maxR * 0.25f, 0f),
            end = center + Offset(maxR * 0.25f, 0f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    // 9. ATOMIC CORE (3D)
    fun DrawScope.drawAtomicCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        val numProtons = 5
        for (p in 0 until numProtons) {
            val pa = p * (2 * PI.toFloat() / numProtons) + time + manualRotY
            val pr = maxR * 0.08f
            drawCircle(
                color = if (p % 2 == 0) primary else secondary,
                radius = maxR * 0.12f * pulse,
                center = center + Offset(cos(pa) * pr, sin(pa) * pr)
            )
        }
        drawCircle(color = Color.White, radius = maxR * 0.08f, center = center)

        val planes = listOf(
            Triple(0f, maxR * 0.85f, maxR * 0.35f),
            Triple(60f, maxR * 0.85f, maxR * 0.35f),
            Triple(120f, maxR * 0.85f, maxR * 0.35f)
        )

        planes.forEachIndexed { idx, (tilt, rx, ry) ->
            rotate(tilt + time * 12f + degY, pivot = center) {
                drawOval(
                    color = ring.copy(alpha = 0.5f),
                    topLeft = center - Offset(rx * pulse, ry * pulse * (1f + sin(manualRotX) * 0.4f)),
                    size = Size(rx * 2 * pulse, ry * 2 * pulse * (1f + sin(manualRotX) * 0.4f)),
                    style = Stroke(width = 2.2f)
                )

                val eAngle = time * (3f + idx * 1.2f) + manualRotY
                val ex = cos(eAngle) * rx * pulse
                val ey = sin(eAngle) * ry * pulse * (1f + sin(manualRotX) * 0.4f)
                drawCircle(color = Color.White, radius = 4.5f + audioLevel * 3f, center = center + Offset(ex, ey))
            }
        }
    }

    // 10. CYBER REACTOR (3D Tesseract / Hypercube)
    fun DrawScope.drawCyberReactor(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val s = maxR * 0.5f * pulse

        val v = listOf(
            Vec3(-s, -s, -s), Vec3(s, -s, -s), Vec3(s, s, -s), Vec3(-s, s, -s),
            Vec3(-s, -s, s), Vec3(s, -s, s), Vec3(s, s, s), Vec3(-s, s, s)
        )

        val rotX = time * 0.9f + manualRotX
        val rotY = time * 1.3f + manualRotY
        val rotZ = manualRotZ
        val proj = v.map { it.rotateXYZ(rotX, rotY, rotZ).project(center) }

        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )

        edges.forEach { (a, b) ->
            drawLine(
                color = primary,
                start = proj[a].first,
                end = proj[b].first,
                strokeWidth = 2.5f
            )
        }

        proj.forEach { (pos, depth) ->
            drawCircle(color = Color.White, radius = 3.5f * depth, center = pos)
        }
        drawCircle(color = glow.copy(alpha = 0.8f), radius = maxR * 0.18f * pulse, center = center)
    }

    // 11. BLACK HOLE CORE (3D)
    fun DrawScope.drawBlackHoleCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        rotate(-20f + degY, pivot = center) {
            val diskRx = maxR * pulse
            val diskRy = maxR * 0.32f * pulse * (1f + sin(manualRotX) * 0.4f)

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFFFF9100), Color(0xFFFF1744), Color.Transparent),
                    center = center,
                    radius = diskRx
                ),
                topLeft = center - Offset(diskRx, diskRy),
                size = Size(diskRx * 2, diskRy * 2)
            )

            drawArc(
                color = Color(0xFFFFD740),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = center - Offset(diskRx * 0.7f, diskRy * 2f),
                size = Size(diskRx * 1.4f, diskRy * 4f),
                style = Stroke(width = 3.5f)
            )
        }

        val eventHorizonR = maxR * 0.38f
        drawCircle(color = Color.Black, radius = eventHorizonR, center = center)
        drawCircle(color = Color.White, radius = eventHorizonR, center = center, style = Stroke(width = 3f))

        val jetLen = maxR * 0.85f * pulse
        val jetOffset = Offset(sin(manualRotY) * jetLen * 0.3f, -jetLen * cos(manualRotX))
        drawLine(
            color = primary,
            start = center,
            end = center + jetOffset,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = primary,
            start = center,
            end = center - jetOffset,
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    // 12. ICE CORE (3D)
    fun DrawScope.drawIceCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.4f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F7FA).copy(alpha = 0.5f * pulse), Color.Transparent),
                center = center,
                radius = maxR * 1.2f
            ),
            radius = maxR * 1.2f,
            center = center
        )

        val branches = 6
        rotate(time * 15f + degY, pivot = center) {
            for (b in 0 until branches) {
                val angle = b * 60f
                rotate(angle, pivot = center) {
                    val branchLen = maxR * 0.85f * pulse
                    drawLine(
                        color = Color.White,
                        start = center,
                        end = center + Offset(branchLen, 0f),
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                    for (n in 1..3) {
                        val nx = branchLen * (n / 4f)
                        val needleLen = 14f * (4 - n) * 0.35f
                        drawLine(
                            color = primary,
                            start = center + Offset(nx, 0f),
                            end = center + Offset(nx + needleLen * 0.7f, needleLen * 0.7f),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = primary,
                            start = center + Offset(nx, 0f),
                            end = center + Offset(nx + needleLen * 0.7f, -needleLen * 0.7f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }

        drawCircle(color = Color.White, radius = maxR * 0.15f * pulse, center = center)
    }

    // 13. FIRE ENERGY CORE (3D)
    fun DrawScope.drawFireEnergyCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.45f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()

        val numFlames = 18
        for (i in 0 until numFlames) {
            val prog = (time * 1.2f + i / numFlames.toFloat()) % 1f
            val y = center.y + (0.5f - prog) * (maxR * 1.4f) + (degX * 0.5f)
            val helixAngle = prog * PI.toFloat() * 6f + manualRotY
            val r = (1f - prog) * maxR * 0.6f * pulse
            val x = center.x + cos(helixAngle) * r

            val flameColor = when {
                prog < 0.3f -> Color.White
                prog < 0.6f -> Color(0xFFFFD54F)
                prog < 0.85f -> Color(0xFFFF5722)
                else -> Color(0xFFD50000)
            }

            drawCircle(
                color = flameColor.copy(alpha = (1f - prog).coerceIn(0f, 1f)),
                radius = (maxR * 0.12f * (1f - prog * 0.7f) + audioLevel * 4f),
                center = Offset(x, y)
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFFFAB00), Color.Transparent),
                center = center + Offset(0f, maxR * 0.2f),
                radius = maxR * 0.4f
            ),
            radius = maxR * 0.4f,
            center = center + Offset(0f, maxR * 0.2f)
        )
    }

    // 14. WIREFRAME PLANET (3D)
    fun DrawScope.drawWireframePlanet(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f
        val r = maxR * 0.75f * pulse

        val phi = 1.6180339887f * r * 0.6f
        val s = r * 0.6f

        val icosaVerts = listOf(
            Vec3(-s, phi, 0f), Vec3(s, phi, 0f), Vec3(-s, -phi, 0f), Vec3(s, -phi, 0f),
            Vec3(0f, -s, phi), Vec3(0f, s, phi), Vec3(0f, -s, -phi), Vec3(0f, s, -phi),
            Vec3(phi, 0f, -s), Vec3(phi, 0f, s), Vec3(-phi, 0f, -s), Vec3(-phi, 0f, s)
        )

        val rotX = time * 0.7f + manualRotX
        val rotY = time * 1.1f + manualRotY
        val rotZ = manualRotZ
        val proj = icosaVerts.map { it.rotateXYZ(rotX, rotY, rotZ).project(center) }

        for (i in proj.indices) {
            for (j in i + 1 until proj.size) {
                val dist = (proj[i].first - proj[j].first).getDistance()
                if (dist < r * 1.25f) {
                    val alpha = (1f - dist / (r * 1.25f)).coerceIn(0.2f, 0.9f)
                    drawLine(
                        color = primary.copy(alpha = alpha),
                        start = proj[i].first,
                        end = proj[j].first,
                        strokeWidth = 2f
                    )
                }
            }
        }

        proj.forEach { (pos, depth) ->
            drawCircle(color = Color.White, radius = 3.5f * depth, center = pos)
        }
    }

    // 15. TITONOX 3D CORE (Flagship 3D AI Core)
    fun DrawScope.drawTitonox3DCore(
        time: Float,
        audioLevel: Float,
        state: OrbState,
        primary: Color,
        secondary: Color,
        glow: Color,
        ring: Color,
        manualRotX: Float = 0f,
        manualRotY: Float = 0f,
        manualRotZ: Float = 0f
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.5f
        val degX = Math.toDegrees(manualRotX.toDouble()).toFloat()
        val degY = Math.toDegrees(manualRotY.toDouble()).toFloat()
        val degZ = Math.toDegrees(manualRotZ.toDouble()).toFloat()

        // Triple 3D Gyroscope Gimbal Rings rotating independently + manual user rotation
        rotate(time * 25f + degX, pivot = center) {
            drawOval(
                color = primary,
                topLeft = center - Offset(maxR * 0.95f, maxR * 0.35f * pulse),
                size = Size(maxR * 1.9f, maxR * 0.7f * pulse),
                style = Stroke(width = 3.5f)
            )
        }

        rotate(-time * 35f + 60f + degY, pivot = center) {
            drawOval(
                color = secondary,
                topLeft = center - Offset(maxR * 0.82f, maxR * 0.4f * pulse),
                size = Size(maxR * 1.64f, maxR * 0.8f * pulse),
                style = Stroke(width = 3f)
            )
        }

        rotate(time * 45f + 120f + degZ, pivot = center) {
            drawOval(
                color = ring,
                topLeft = center - Offset(maxR * 0.68f, maxR * 0.45f * pulse),
                size = Size(maxR * 1.36f, maxR * 0.9f * pulse),
                style = Stroke(width = 2.5f)
            )
        }

        val cubeS = maxR * 0.35f * pulse
        val rotCube = time * 2f
        rotate(rotCube * 30f + degY, pivot = center) {
            val path = Path().apply {
                moveTo(center.x, center.y - cubeS)
                lineTo(center.x + cubeS, center.y)
                lineTo(center.x, center.y + cubeS)
                lineTo(center.x - cubeS, center.y)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, primary, secondary.copy(alpha = 0.5f)),
                    center = center,
                    radius = cubeS
                ),
                style = Fill
            )
            drawPath(path, color = Color.White, style = Stroke(width = 2.5f))
        }

        drawCircle(color = Color.White, radius = maxR * 0.12f * pulse, center = center)
    }
}
