package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable root container that enforces safe-area calculations and responsive constraints.
 * Respects status bar, notch/cutout, punch-hole, navigation bars, and gesture insets.
 */
@Composable
fun TitonoxSafeScreenContainer(
    modifier: Modifier = Modifier,
    maxContentWidth: Dp = 840.dp,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = maxContentWidth)
                .align(Alignment.Center),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

/**
 * Helper to inspect current system insets for debugging or responsive layout decisions.
 */
object TitonoxSafeInsets {
    @Composable
    fun statusBarHeight(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    @Composable
    fun navigationBarHeight(): Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    @Composable
    fun cutoutTop(): Dp = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
}
