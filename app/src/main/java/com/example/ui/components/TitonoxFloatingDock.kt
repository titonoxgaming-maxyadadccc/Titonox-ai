package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TitonoxInterface
import com.example.ui.theme.TitonoxTokens

data class DockDestination(
    val destination: TitonoxInterface,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

val TITONOX_NAV_DESTINATIONS = listOf(
    DockDestination(TitonoxInterface.HOME, "HOME", Icons.Default.AutoAwesome, "nav_home"),
    DockDestination(TitonoxInterface.CHAT, "CHAT", Icons.AutoMirrored.Filled.Chat, "nav_chat"),
    DockDestination(TitonoxInterface.AGENT, "AGENT", Icons.Default.SmartToy, "nav_agent"),
    DockDestination(TitonoxInterface.AUTOMATION, "AUTO", Icons.Default.AccessibilityNew, "nav_automation"),
    DockDestination(TitonoxInterface.TOOLS, "TOOLS", Icons.Default.Build, "nav_tools"),
    DockDestination(TitonoxInterface.VISION, "VISION", Icons.Default.CenterFocusStrong, "nav_vision"),
    DockDestination(TitonoxInterface.MEMORY, "MEMORY", Icons.Default.Storage, "nav_memory"),
    DockDestination(TitonoxInterface.MODELS, "MODELS", Icons.Default.Hub, "nav_models"),
    DockDestination(TitonoxInterface.SETTINGS, "SETTINGS", Icons.Default.Settings, "nav_settings")
)

@Composable
fun TitonoxFloatingDock(
    currentInterface: TitonoxInterface,
    onSelectInterface: (TitonoxInterface) -> Unit,
    onOpenSearch: () -> Unit,
    onToggleDevLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll dock to make selected tab visible
    LaunchedEffect(currentInterface) {
        val index = TITONOX_NAV_DESTINATIONS.indexOfFirst { it.destination == currentInterface }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("titonox_floating_dock"),
            shape = TitonoxTokens.RadiusPill,
            color = TitonoxTokens.SurfaceElevated,
            border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
            shadowElevation = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Universal Search Quick Button
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TitonoxTokens.SurfaceHighlight)
                        .testTag("dock_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Universal Search",
                        tint = TitonoxTokens.AccentPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 9 Core Navigation Destinations in smooth scrollable row
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(TITONOX_NAV_DESTINATIONS) { item ->
                        DockInterfaceItem(
                            label = item.label,
                            icon = item.icon,
                            isSelected = currentInterface == item.destination,
                            onClick = { onSelectInterface(item.destination) },
                            testTag = item.testTag
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Telemetry / Dev Logs Toggle
                IconButton(
                    onClick = onToggleDevLogs,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TitonoxTokens.SurfaceHighlight)
                        .testTag("dock_dev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Dev Telemetry Console",
                        tint = TitonoxTokens.TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DockInterfaceItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.93f else 1.0f

    val bgAnim by animateColorAsState(
        targetValue = if (isSelected) TitonoxTokens.AccentPrimary else Color.Transparent,
        animationSpec = tween(220),
        label = "DockBg"
    )
    val contentColor = if (isSelected) TitonoxTokens.Background else TitonoxTokens.TextSecondary

    Surface(
        modifier = Modifier
            .scale(scale)
            .heightIn(min = 40.dp)
            .clip(TitonoxTokens.RadiusPill)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        color = bgAnim,
        shape = TitonoxTokens.RadiusPill
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
