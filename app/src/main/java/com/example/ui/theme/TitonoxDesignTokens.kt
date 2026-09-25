package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TITONOX JARVIS Design Tokens
 * Centralized source of truth for the luxury futuristic AI interface.
 */
object TitonoxTokens {
    // Surface & Depth Palette
    val Background = Color(0xFF050608)
    val Surface = Color(0xFF0B0E14)
    val SurfaceElevated = Color(0xFF11151D)
    val SurfaceCard = Color(0xFF0D1118)
    val SurfaceHighlight = Color(0xFF161C26)

    // Borders & Separators
    val BorderSubtle = Color(0xFF1A222E)
    val BorderActive = Color(0xFF2C394C)
    val BorderGlow = Color(0x3300D2FF)

    // Strategic Accents
    val AccentPrimary = Color(0xFF00D2FF) // Electric Cyan
    val AccentSecondary = Color(0xFF0088FF) // Deep Blue
    val AccentViolet = Color(0xFF7952FF) // Subtle Violet
    val AccentGold = Color(0xFFE5A93C) // Pro / Commercial Badge

    // Typography Colors
    val TextPrimary = Color(0xFFF4F6F8)
    val TextSecondary = Color(0xFF8E9AA8)
    val TextMuted = Color(0xFF5A6675)
    val TextTertiary = Color(0xFF5A6675)

    // State Colors
    val StateIdle = Color(0xFF00D2FF)
    val StateListening = Color(0xFF00D2FF)
    val StateThinking = Color(0xFF7952FF)
    val StateExecuting = Color(0xFF2575FC)
    val StateSuccess = Color(0xFF10B981)
    val StateWarning = Color(0xFFF59E0B)
    val StateError = Color(0xFFEF4444)

    // Corner Radius
    val RadiusPill = RoundedCornerShape(999.dp)
    val RadiusXl = RoundedCornerShape(24.dp)
    val RadiusLg = RoundedCornerShape(18.dp)
    val RadiusMd = RoundedCornerShape(14.dp)
    val RadiusSm = RoundedCornerShape(10.dp)
    val RadiusXs = RoundedCornerShape(6.dp)

    // Motion & Animation Curves
    val LuxuryEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val SpringSnappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
