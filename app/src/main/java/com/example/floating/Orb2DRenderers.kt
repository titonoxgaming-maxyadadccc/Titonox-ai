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

object Orb2DRenderers {

    // 1. PULSE RING
    fun DrawScope.drawPulseRing(
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

        // Ambient radial background glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.35f * pulse), Color.Transparent),
                center = center,
                radius = maxR * 1.2f
            ),
            radius = maxR * 1.2f,
            center = center
        )

        // Multiple expanding shockwave concentric rings
        val ringCount = 4
        for (i in 0 until ringCount) {
            val progress = ((time * 0.6f + i / ringCount.toFloat()) % 1f)
            val currentR = maxR * progress * pulse
            val alpha = ((1f - progress) * 0.85f).coerceIn(0f, 1f)
            drawCircle(
                color = if (i % 2 == 0) primary.copy(alpha = alpha) else secondary.copy(alpha = alpha),
                radius = currentR,
                center = center,
                style = Stroke(width = (4f * (1f - progress * 0.5f) * pulse).coerceAtLeast(1.5f))
            )
        }

        // Precision radial tick marks
        val tickCount = 24
        for (i in 0 until tickCount) {
            val angle = (i * 360f / tickCount) + time * 20f
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val inner = maxR * 0.75f
            val tickLen = if (i % 4 == 0) 14f + audioLevel * 16f else 7f + audioLevel * 8f
            val start = center + Offset(cos(rad) * inner, sin(rad) * inner)
            val end = center + Offset(cos(rad) * (inner + tickLen), sin(rad) * (inner + tickLen))
            drawLine(
                color = ring.copy(alpha = if (i % 4 == 0) 0.9f else 0.5f),
                start = start,
                end = end,
                strokeWidth = if (i % 4 == 0) 2.5f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        // Central resonant core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary, glow.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = maxR * 0.3f * pulse
            ),
            radius = maxR * 0.3f * pulse,
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = maxR * 0.08f * pulse,
            center = center
        )
    }

    // 2. AUDIO WAVE
    fun DrawScope.drawAudioWave(
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

        // Radial bounding ring
        drawCircle(
            color = ring.copy(alpha = 0.3f),
            radius = maxR,
            center = center,
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)))
        )

        // Triple horizontal/radial fluid sinusoidal waves
        val waveLayers = 3
        for (w in 0 until waveLayers) {
            val path = Path()
            val points = 32
            val waveFreq = 2.5f + w * 1.5f
            val wavePhase = time * (3f + w)
            val waveAmp = (maxR * (0.15f + 0.1f * w) + audioLevel * maxR * 0.3f)

            var first = true
            for (p in 0..points) {
                val fraction = p / points.toFloat()
                val x = center.x - maxR * 0.85f + fraction * (maxR * 1.7f)
                val baseNorm = (fraction - 0.5f) * 2f
                val envelope = 1f - (baseNorm * baseNorm).coerceIn(0f, 1f) // parabolic window
                val y = center.y + sin(fraction * PI.toFloat() * waveFreq + wavePhase) * waveAmp * envelope

                if (first) {
                    path.moveTo(x, y)
                    first = false
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = when (w) {
                    0 -> primary.copy(alpha = 0.9f)
                    1 -> secondary.copy(alpha = 0.75f)
                    else -> glow.copy(alpha = 0.6f)
                },
                style = Stroke(width = 3.5f - w * 0.8f, cap = StrokeCap.Round)
            )
        }

        // Radial harmonic wave ring
        val radialPath = Path()
        val numLobes = 8
        for (i in 0..360 step 6) {
            val rad = Math.toRadians(i.toDouble()).toFloat()
            val lobe = sin(rad * numLobes + time * 4f) * (maxR * 0.12f * pulse)
            val r = maxR * 0.5f + lobe
            val pos = center + Offset(cos(rad) * r, sin(rad) * r)
            if (i == 0) radialPath.moveTo(pos.x, pos.y) else radialPath.lineTo(pos.x, pos.y)
        }
        radialPath.close()

        drawPath(
            path = radialPath,
            color = ring.copy(alpha = 0.7f),
            style = Stroke(width = 2.5f)
        )
    }

    // 3. DIGITAL RADAR
    fun DrawScope.drawDigitalRadar(
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
        val pulse = 1f + audioLevel * 0.2f

        // Radar background screen
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF001A24), Color(0xFF000B11)),
                center = center,
                radius = maxR
            ),
            radius = maxR,
            center = center
        )

        // Concentric range rings
        for (r in 1..4) {
            val ringRadius = maxR * (r / 4f)
            drawCircle(
                color = ring.copy(alpha = 0.35f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            )
        }

        // Crosshairs: Horizontal & Vertical Azimuth lines
        drawLine(
            color = ring.copy(alpha = 0.45f),
            start = Offset(center.x - maxR, center.y),
            end = Offset(center.x + maxR, center.y),
            strokeWidth = 1.5f
        )
        drawLine(
            color = ring.copy(alpha = 0.45f),
            start = Offset(center.x, center.y - maxR),
            end = Offset(center.x, center.y + maxR),
            strokeWidth = 1.5f
        )

        // Sweeping Phosphor Beam (360 degrees sweep)
        val sweepAngle = (time * 80f) % 360f
        rotate(sweepAngle, pivot = center) {
            // Sweeping wedge gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.Transparent,
                        primary.copy(alpha = 0.05f),
                        primary.copy(alpha = 0.45f * pulse),
                        glow.copy(alpha = 0.95f)
                    ),
                    center = center
                ),
                startAngle = -45f,
                sweepAngle = 45f,
                useCenter = true,
                size = Size(maxR * 2, maxR * 2),
                topLeft = center - Offset(maxR, maxR)
            )
            // Leading laser edge line
            drawLine(
                color = Color.White,
                start = center,
                end = center + Offset(maxR, 0f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }

        // Target radar blips (Pings)
        val blipCount = 4
        for (b in 0 until blipCount) {
            val blipAngle = (b * 95f + 30f)
            val blipDist = maxR * (0.35f + b * 0.16f)
            val diffAngle = (sweepAngle - blipAngle + 360f) % 360f
            if (diffAngle in 0f..110f) {
                val blipAlpha = (1f - diffAngle / 110f)
                val blipPos = center + Offset(
                    cos(Math.toRadians(blipAngle.toDouble())).toFloat() * blipDist,
                    sin(Math.toRadians(blipAngle.toDouble())).toFloat() * blipDist
                )
                drawCircle(color = Color(0xFFFF5252).copy(alpha = blipAlpha), radius = 5f + audioLevel * 4f, center = blipPos)
                drawCircle(
                    color = Color(0xFFFF8A80).copy(alpha = blipAlpha * 0.7f),
                    radius = (10f + (1f - blipAlpha) * 12f),
                    center = blipPos,
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // Outer chassis ring
        drawCircle(color = primary, radius = maxR, center = center, style = Stroke(width = 3f))
    }

    // 4. NEON CIRCUIT
    fun DrawScope.drawNeonCircuit(
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

        // Central processor octagonal chip
        val chipSize = maxR * 0.42f * pulse
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.6f), Color(0xFF031628)),
                center = center,
                radius = chipSize
            ),
            topLeft = center - Offset(chipSize / 2f, chipSize / 2f),
            size = Size(chipSize, chipSize)
        )
        drawRect(
            color = primary,
            topLeft = center - Offset(chipSize / 2f, chipSize / 2f),
            size = Size(chipSize, chipSize),
            style = Stroke(width = 2.5f)
        )

        // Circuit Bus Traces radiating outward with 90 & 45 degree orthogonal corners
        val traceCount = 8
        for (i in 0 until traceCount) {
            val angle = i * 45f
            rotate(angle, pivot = center) {
                val path = Path().apply {
                    moveTo(center.x + chipSize / 2f, center.y)
                    lineTo(center.x + chipSize / 2f + maxR * 0.2f, center.y)
                    val turnY = if (i % 2 == 0) maxR * 0.25f else -maxR * 0.25f
                    lineTo(center.x + chipSize / 2f + maxR * 0.45f, center.y + turnY)
                    lineTo(center.x + maxR * 0.95f, center.y + turnY)
                }

                // Base PCB copper/neon track
                drawPath(path = path, color = secondary.copy(alpha = 0.45f), style = Stroke(width = 2.5f))

                // Solder pad at termination
                val padPos = Offset(center.x + maxR * 0.95f, center.y + (if (i % 2 == 0) maxR * 0.25f else -maxR * 0.25f))
                drawCircle(color = glow, radius = 4f, center = padPos)
                drawCircle(color = ring, radius = 7f, center = padPos, style = Stroke(width = 1.5f))

                // Traveling data packet pulse
                val packetProg = ((time * 0.8f + i * 0.125f) % 1f)
                val packetX = center.x + chipSize / 2f + packetProg * (maxR * 0.95f - chipSize / 2f)
                drawCircle(
                    color = Color.White,
                    radius = 3.5f + audioLevel * 3f,
                    center = Offset(packetX, center.y)
                )
            }
        }
    }

    // 5. HEX CORE
    fun DrawScope.drawHexCore(
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

        fun drawHexagon(c: Offset, r: Float, color: Color, strokeWidth: Float, fill: Boolean = false) {
            val path = Path()
            for (i in 0 until 6) {
                val angle = i * 60f - 30f
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val pt = c + Offset(cos(rad) * r, sin(rad) * r)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            if (fill) {
                drawPath(path, color = color, style = Fill)
            } else {
                drawPath(path, color = color, style = Stroke(width = strokeWidth))
            }
        }

        // Multiple concentric rotating hexagon shells
        for (h in 1..4) {
            val r = maxR * (h / 4f) * pulse
            val rot = (if (h % 2 == 0) time * 15f else -time * 15f) + h * 10f
            rotate(rot, pivot = center) {
                drawHexagon(center, r, if (h == 4) ring else secondary.copy(alpha = 0.5f + h * 0.1f), 2f + h * 0.5f)
            }
        }

        // Hexagonal Honeycomb matrix satellites
        val hexSatCount = 6
        val satR = maxR * 0.45f
        for (s in 0 until hexSatCount) {
            val angle = s * 60f + time * 25f
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val satCenter = center + Offset(cos(rad) * satR, sin(rad) * satR)
            val satScale = 0.2f * maxR * (1f + sin(time * 3f + s) * 0.2f)
            drawHexagon(satCenter, satScale, glow.copy(alpha = 0.7f), 1.8f)
        }

        // Central Power Prism Hexagon
        val coreR = maxR * 0.26f * pulse
        drawHexagon(center, coreR, primary.copy(alpha = 0.35f), 0f, fill = true)
        drawHexagon(center, coreR, primary, 3f)
        drawCircle(color = Color.White, radius = 5f + audioLevel * 6f, center = center)
    }

    // 6. PARTICLE MATRIX
    fun DrawScope.drawParticleMatrix(
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

        // Central singularity attractor
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary, glow.copy(alpha = 0.3f), Color.Transparent),
                center = center,
                radius = maxR * 0.4f * pulse
            ),
            radius = maxR * 0.4f * pulse,
            center = center
        )

        // 36 Particle points in gravitational orbit
        val numParticles = 36
        for (i in 0 until numParticles) {
            val speed = 0.5f + (i % 6) * 0.3f
            val orbitRadius = (maxR * (0.2f + (i / numParticles.toFloat()) * 0.75f)) * pulse
            val angle = time * speed + (i * PI.toFloat() * 2f / numParticles)
            val pCenter = center + Offset(cos(angle) * orbitRadius, sin(angle) * orbitRadius * (0.75f + (i % 3) * 0.15f))

            // Velocity trail
            val trailAngle = angle - 0.15f * speed
            val pTrail = center + Offset(cos(trailAngle) * orbitRadius, sin(trailAngle) * orbitRadius * (0.75f + (i % 3) * 0.15f))
            drawLine(
                color = secondary.copy(alpha = 0.4f),
                start = pTrail,
                end = pCenter,
                strokeWidth = 1.5f
            )

            // Spark node
            drawCircle(
                color = if (i % 3 == 0) Color.White else primary,
                radius = (2.5f + (i % 4) * 0.8f + audioLevel * 3f),
                center = pCenter
            )
        }
    }

    // 7. EQUALIZER CORE
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
        val maxR = size.minDimension / 2f * 0.88f
        val bars = 32

        // Inner audio meter ring
        val innerR = maxR * 0.45f
        drawCircle(
            color = ring.copy(alpha = 0.5f),
            radius = innerR,
            center = center,
            style = Stroke(width = 2f)
        )

        // Radial equalizer bars
        for (i in 0 until bars) {
            val angle = i * (360f / bars)
            val rad = Math.toRadians(angle.toDouble()).toFloat()

            // Frequency calculation based on sine harmonics and actual audio level
            val freqMod = sin(i * 0.7f + time * 4f) * 0.5f + 0.5f
            val barLen = (maxR * 0.15f + freqMod * (maxR * 0.35f) + audioLevel * (maxR * 0.4f)).coerceIn(5f, maxR * 0.52f)

            val pStart = center + Offset(cos(rad) * innerR, sin(rad) * innerR)
            val pEnd = center + Offset(cos(rad) * (innerR + barLen), sin(rad) * (innerR + barLen))

            val color = when {
                barLen > maxR * 0.4f -> Color(0xFFFF5252)
                barLen > maxR * 0.25f -> Color(0xFFFFD740)
                else -> primary
            }

            drawLine(
                color = color,
                start = pStart,
                end = pEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }

        // Center decibel readout circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.8f), secondary.copy(alpha = 0.4f)),
                center = center,
                radius = innerR * 0.7f
            ),
            radius = innerR * 0.7f,
            center = center
        )
        drawCircle(color = Color.White, radius = innerR * 0.2f * (1f + audioLevel * 0.5f), center = center)
    }

    // 8. INFINITY CORE
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
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f
        val scale = maxR * 0.75f * pulse

        // Lemniscate of Bernoulli: x = a*cos(t)/(1+sin^2 t), y = a*sin(t)*cos(t)/(1+sin^2 t)
        val path = Path()
        val steps = 100
        for (i in 0..steps) {
            val t = (i / steps.toFloat()) * PI.toFloat() * 2f
            val denom = 1f + sin(t) * sin(t)
            val x = (scale * cos(t)) / denom
            val y = (scale * sin(t) * cos(t)) / denom
            val pt = center + Offset(x, y)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        path.close()

        // Outer glow path
        drawPath(
            path = path,
            color = glow.copy(alpha = 0.4f),
            style = Stroke(width = 12f * pulse, cap = StrokeCap.Round)
        )
        // Main ribbon
        drawPath(
            path = path,
            color = primary,
            style = Stroke(width = 4.5f, cap = StrokeCap.Round)
        )

        // Moving energy nodes along the infinity ribbon
        val nodes = 6
        for (n in 0 until nodes) {
            val t = (time * 1.5f + (n * PI.toFloat() * 2f / nodes)) % (PI.toFloat() * 2f)
            val denom = 1f + sin(t) * sin(t)
            val x = (scale * cos(t)) / denom
            val y = (scale * sin(t) * cos(t)) / denom
            val pt = center + Offset(x, y)

            drawCircle(
                color = Color.White,
                radius = 4.5f + audioLevel * 3f,
                center = pt
            )
        }
    }

    // 9. DIGITAL EYE
    fun DrawScope.drawDigitalEye(
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

        // Outer cyber sclera ring
        drawCircle(color = ring.copy(alpha = 0.6f), radius = maxR, center = center, style = Stroke(width = 2.5f))

        // Reticle ticks
        for (i in 0 until 12) {
            val a = i * 30f
            val rad = Math.toRadians(a.toDouble()).toFloat()
            drawLine(
                color = primary,
                start = center + Offset(cos(rad) * maxR * 0.9f, sin(rad) * maxR * 0.9f),
                end = center + Offset(cos(rad) * maxR, sin(rad) * maxR),
                strokeWidth = 2f
            )
        }

        // Mechanical Iris Aperture Blades (8 blades)
        val numBlades = 8
        val irisRadius = maxR * 0.68f
        val apertureOpen = (0.28f + audioLevel * 0.35f).coerceIn(0.2f, 0.75f)
        val bladeRotation = time * 20f

        rotate(bladeRotation, pivot = center) {
            for (i in 0 until numBlades) {
                val bladeAngle = i * (360f / numBlades)
                val rad = Math.toRadians(bladeAngle.toDouble()).toFloat()
                val p1 = center + Offset(cos(rad) * irisRadius, sin(rad) * irisRadius)
                val nextRad = Math.toRadians((bladeAngle + 45f).toDouble()).toFloat()
                val p2 = center + Offset(cos(nextRad) * (irisRadius * apertureOpen), sin(nextRad) * (irisRadius * apertureOpen))

                drawLine(
                    color = secondary,
                    start = p1,
                    end = p2,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Dilating Cyber Pupil
        val pupilR = maxR * apertureOpen * 0.65f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black, Color(0xFF001F2B)),
                center = center,
                radius = pupilR
            ),
            radius = pupilR,
            center = center
        )

        // Optical Glint and Targeting Crosshair
        drawCircle(
            color = Color(0xFFFF1744).copy(alpha = 0.85f),
            radius = pupilR * 0.35f,
            center = center
        )
        drawLine(
            color = glow.copy(alpha = 0.7f),
            start = Offset(center.x - pupilR * 1.5f, center.y),
            end = Offset(center.x + pupilR * 1.5f, center.y),
            strokeWidth = 1.5f
        )
        drawLine(
            color = glow.copy(alpha = 0.7f),
            start = Offset(center.x, center.y - pupilR * 1.5f),
            end = Offset(center.x, center.y + pupilR * 1.5f),
            strokeWidth = 1.5f
        )
    }

    // 10. ENERGY RING
    fun DrawScope.drawEnergyRing(
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

        // Multi-layered counter-rotating segmented energy arcs
        val layerCount = 3
        for (l in 0 until layerCount) {
            val r = maxR * (0.5f + l * 0.22f) * pulse
            val speed = if (l % 2 == 0) time * 60f else -time * 75f
            rotate(speed, pivot = center) {
                val segments = 4 + l * 2
                for (s in 0 until segments) {
                    val startAngle = s * (360f / segments)
                    drawArc(
                        color = if (s % 2 == 0) primary else secondary,
                        startAngle = startAngle,
                        sweepAngle = (360f / segments) * 0.65f,
                        useCenter = false,
                        topLeft = center - Offset(r, r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = 3.5f + l * 0.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Coronal plasma sparks erupting from ring
        val sparkCount = 12
        for (sp in 0 until sparkCount) {
            val sparkAngle = sp * 30f + time * 45f
            val rad = Math.toRadians(sparkAngle.toDouble()).toFloat()
            val startDist = maxR * 0.85f
            val sparkLen = 10f + audioLevel * 25f + sin(time * 6f + sp) * 8f
            drawLine(
                color = Color.White,
                start = center + Offset(cos(rad) * startDist, sin(rad) * startDist),
                end = center + Offset(cos(rad) * (startDist + sparkLen), sin(rad) * (startDist + sparkLen)),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Central core ball
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, primary, Color.Transparent),
                center = center,
                radius = maxR * 0.3f
            ),
            radius = maxR * 0.3f,
            center = center
        )
    }

    // 11. NEURAL NETWORK
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

        // 12 Synaptic Nodes arranged in neural clusters
        val nodeCount = 12
        val nodePositions = ArrayList<Offset>(nodeCount)
        for (i in 0 until nodeCount) {
            val baseAngle = (i * 360f / nodeCount) + time * 12f
            val rad = Math.toRadians(baseAngle.toDouble()).toFloat()
            val dist = maxR * (0.3f + (i % 3) * 0.28f) * (1f + sin(time * 2f + i) * 0.08f)
            nodePositions.add(center + Offset(cos(rad) * dist, sin(rad) * dist))
        }

        // Axon Dendrite lines connecting neighboring nodes
        for (i in 0 until nodeCount) {
            val next = (i + 1) % nodeCount
            val skip = (i + 3) % nodeCount
            drawLine(
                color = secondary.copy(alpha = 0.45f),
                start = nodePositions[i],
                end = nodePositions[next],
                strokeWidth = 1.5f
            )
            drawLine(
                color = ring.copy(alpha = 0.3f),
                start = nodePositions[i],
                end = nodePositions[skip],
                strokeWidth = 1.2f
            )

            // Firing Action Potential light pulse along axon
            val actionProg = ((time * 1.8f + i * 0.2f) % 1f)
            val sparkPos = nodePositions[i] + (nodePositions[next] - nodePositions[i]) * actionProg
            drawCircle(
                color = Color.White,
                radius = 2.5f + audioLevel * 2.5f,
                center = sparkPos
            )
        }

        // Draw Neuron Soma (Bodies)
        for (i in 0 until nodeCount) {
            val pos = nodePositions[i]
            val firing = sin(time * 4f + i) > 0.4f || audioLevel > 0.3f
            drawCircle(
                color = if (firing) Color.White else primary,
                radius = (4.5f + (i % 3) * 1.5f + (if (firing) 3f else 0f)),
                center = pos
            )
            drawCircle(
                color = glow.copy(alpha = 0.6f),
                radius = 8f + (i % 3) * 2f,
                center = pos,
                style = Stroke(width = 1.5f)
            )
        }

        // Central brain nucleus
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.8f), Color.Transparent),
                center = center,
                radius = maxR * 0.22f * pulse
            ),
            radius = maxR * 0.22f * pulse,
            center = center
        )
    }

    // 12. HOLOGRAPHIC LINES
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
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.3f

        // Bounding circular frame
        drawCircle(color = ring.copy(alpha = 0.4f), radius = maxR, center = center, style = Stroke(width = 2f))

        // Horizontal scanlines with sinusoidal modulation and glitch offsets
        val lineCount = 22
        for (i in 0 until lineCount) {
            val yNorm = (i / (lineCount - 1).toFloat()) * 2f - 1f // -1 to +1
            val y = center.y + yNorm * maxR * 0.9f
            val halfW = kotlin.math.sqrt((maxR * maxR - (y - center.y) * (y - center.y)).coerceAtLeast(0f))

            if (halfW > 5f) {
                val glitch = if ((i + (time * 5).toInt()) % 7 == 0) sin(time * 10f) * 12f else 0f
                val path = Path()
                path.moveTo(center.x - halfW + glitch, y)
                val waveMidY = y + sin(time * 3f + i * 0.5f) * (4f + audioLevel * 8f)
                path.quadraticTo(center.x + glitch, waveMidY, center.x + halfW + glitch, y)

                drawPath(
                    path = path,
                    color = if (i % 2 == 0) primary.copy(alpha = 0.85f) else secondary.copy(alpha = 0.7f),
                    style = Stroke(width = 2.2f)
                )
            }
        }

        // Sweeping vertical laser interrogation bar
        val scanY = center.y + sin(time * 2.5f) * maxR * 0.8f
        val scanW = kotlin.math.sqrt((maxR * maxR - (scanY - center.y) * (scanY - center.y)).coerceAtLeast(0f))
        drawLine(
            color = Color.White,
            start = Offset(center.x - scanW, scanY),
            end = Offset(center.x + scanW, scanY),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }

    // 13. CONCENTRIC SCANNER
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
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.35f

        // Triple vernier dials rotating in alternating directions
        val dialRadii = listOf(maxR * 0.4f, maxR * 0.68f, maxR * 0.92f)
        val dialSpeeds = listOf(time * 40f, -time * 30f, time * 20f)

        for (d in 0 until 3) {
            val r = dialRadii[d] * pulse
            rotate(dialSpeeds[d], pivot = center) {
                // Circle rail
                drawCircle(color = secondary.copy(alpha = 0.5f), radius = r, center = center, style = Stroke(width = 2f))

                // Vernier degree tick marks
                val ticks = 16 + d * 8
                for (t in 0 until ticks) {
                    val angle = t * (360f / ticks)
                    val rad = Math.toRadians(angle.toDouble()).toFloat()
                    val tickLen = if (t % 4 == 0) 10f else 5f
                    drawLine(
                        color = if (t % 4 == 0) primary else ring.copy(alpha = 0.6f),
                        start = center + Offset(cos(rad) * (r - tickLen), sin(rad) * (r - tickLen)),
                        end = center + Offset(cos(rad) * r, sin(rad) * r),
                        strokeWidth = if (t % 4 == 0) 2.5f else 1.5f
                    )
                }
            }
        }

        // Dual diametric laser scanner beams
        val beamAngle = time * 75f
        rotate(beamAngle, pivot = center) {
            drawLine(
                color = Color.White,
                start = center - Offset(maxR * 0.95f, 0f),
                end = center + Offset(maxR * 0.95f, 0f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = primary, radius = 5f + audioLevel * 4f, center = center + Offset(maxR * 0.95f, 0f))
            drawCircle(color = primary, radius = 5f + audioLevel * 4f, center = center - Offset(maxR * 0.95f, 0f))
        }

        // Center hub
        drawCircle(color = primary, radius = maxR * 0.15f, center = center)
        drawCircle(color = Color.White, radius = 4f, center = center)
    }

    // 14. STAR CORE
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
        val maxR = size.minDimension / 2f * 0.88f
        val pulse = 1f + audioLevel * 0.5f

        // Ambient cosmic stellar glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.5f * pulse), Color.Transparent),
                center = center,
                radius = maxR * 1.3f
            ),
            radius = maxR * 1.3f,
            center = center
        )

        // 8-Point Diffraction Star Spikes
        val numPoints = 8
        for (i in 0 until numPoints) {
            val angle = i * 45f + time * 15f
            rotate(angle, pivot = center) {
                val isMajor = (i % 2 == 0)
                val rayLen = (if (isMajor) maxR else maxR * 0.6f) * pulse
                val rayWidth = (if (isMajor) 6f else 3.5f) * pulse

                val path = Path().apply {
                    moveTo(center.x, center.y - rayWidth)
                    lineTo(center.x + rayLen, center.y)
                    lineTo(center.x, center.y + rayWidth)
                    lineTo(center.x - rayLen * 0.2f, center.y)
                    close()
                }
                drawPath(path, color = if (isMajor) primary else secondary, style = Fill)
            }
        }

        // Stellar Diamond Nexus
        val coreR = maxR * 0.25f * pulse
        val corePath = Path().apply {
            moveTo(center.x, center.y - coreR)
            lineTo(center.x + coreR, center.y)
            lineTo(center.x, center.y + coreR)
            lineTo(center.x - coreR, center.y)
            close()
        }
        drawPath(corePath, color = Color.White, style = Fill)
        drawCircle(color = primary, radius = coreR * 0.5f, center = center)
    }

    // 15. TITONOX DIGITAL CORE
    fun DrawScope.drawTitonoxDigitalCore(
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

        // Outer Cyber Runic Ring
        rotate(time * 18f, pivot = center) {
            drawCircle(
                color = primary,
                radius = maxR,
                center = center,
                style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f, 4f, 8f)))
            )
            // Cyber glyph ticks
            for (i in 0 until 16) {
                val a = i * 22.5f
                val rad = Math.toRadians(a.toDouble()).toFloat()
                val p1 = center + Offset(cos(rad) * (maxR - 8f), sin(rad) * (maxR - 8f))
                val p2 = center + Offset(cos(rad) * maxR, sin(rad) * maxR)
                drawLine(color = secondary, start = p1, end = p2, strokeWidth = 2f)
            }
        }

        // Middle Counter-Rotating Gear Ring
        rotate(-time * 24f, pivot = center) {
            drawCircle(
                color = ring,
                radius = maxR * 0.72f,
                center = center,
                style = Stroke(width = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            )
        }

        // Inner Audio Reactive Needle Gauge
        val needleAngle = -135f + audioLevel * 270f + sin(time * 5f) * 10f
        rotate(needleAngle, pivot = center) {
            drawLine(
                color = Color(0xFFFF1744),
                start = center,
                end = center + Offset(maxR * 0.65f, 0f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Central TITONOX Cyber Nexus (Nested glowing concentric shields)
        val coreR = maxR * 0.35f * pulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary, secondary.copy(alpha = 0.5f), Color.Transparent),
                center = center,
                radius = coreR
            ),
            radius = coreR,
            center = center
        )
        drawCircle(color = Color.White, radius = maxR * 0.1f * pulse, center = center)
    }
}
