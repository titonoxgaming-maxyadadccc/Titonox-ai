package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberBlack

/**
 * Reusable Safe-Area Root Layout for TITONOX.
 *
 * Guarantees that neither Android system bars (status bar, display cutout,
 * punch hole, navigation buttons, gesture navigation area) will overlap
 * any TITONOX UI components, headers, or bottom navigation docks.
 */
@Composable
fun TitonoxSafeAreaRoot(
    modifier: Modifier = Modifier,
    backgroundColor: Color = CyberBlack,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        content()
    }
}

/**
 * Modifier extension to apply strict Top Safe-Area insets (Status Bar + Display Cutout/Notch/Punch Hole).
 */
fun Modifier.titonoxTopSafeArea(): Modifier = composed {
    this
        .statusBarsPadding()
        .displayCutoutPadding()
}

/**
 * Modifier extension to apply strict Bottom Safe-Area insets (Gesture Pill / 3-Button Navigation Bar + IME).
 */
fun Modifier.titonoxBottomSafeArea(extraBottomPadding: Dp = 8.dp): Modifier = composed {
    this
        .navigationBarsPadding()
        .imePadding()
        .padding(bottom = extraBottomPadding)
}

/**
 * Responsive Screen Metrics Helper to adapt UI cleanly across small phones,
 * large phones, foldables, and tablets.
 */
data class TitonoxWindowMetrics(
    val maxWidthDp: Dp,
    val maxHeightDp: Dp,
    val isSmallPhone: Boolean,
    val isTabletOrFoldable: Boolean,
    val recommendedOrbSizeDp: Dp
)

@Composable
fun rememberTitonoxWindowMetrics(
    maxWidth: Dp,
    maxHeight: Dp
): TitonoxWindowMetrics {
    val isSmallPhone = maxHeight < 700.dp || maxWidth < 360.dp
    val isTablet = maxWidth > 600.dp
    val recommendedOrbSize = when {
        isSmallPhone -> 160.dp
        isTablet -> 240.dp
        else -> 200.dp
    }
    return TitonoxWindowMetrics(
        maxWidthDp = maxWidth,
        maxHeightDp = maxHeight,
        isSmallPhone = isSmallPhone,
        isTabletOrFoldable = isTablet,
        recommendedOrbSizeDp = recommendedOrbSize
    )
}
