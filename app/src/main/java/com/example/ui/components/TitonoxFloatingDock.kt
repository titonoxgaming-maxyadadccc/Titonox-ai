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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun TitonoxFloatingDock(
    currentInterface: TitonoxInterface,
    onSelectInterface: (TitonoxInterface) -> Unit,
    onOpenSearch: () -> Unit,
    onToggleDevLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .testTag("titonox_floating_dock"),
            shape = TitonoxTokens.RadiusPill,
            color = TitonoxTokens.SurfaceElevated,
            border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                // 6 Core Interface Navigation Pills
                DockInterfaceItem(
                    label = "CORE",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = currentInterface == TitonoxInterface.CORE,
                    onClick = { onSelectInterface(TitonoxInterface.CORE) },
                    testTag = "nav_core"
                )

                DockInterfaceItem(
                    label = "CONTROL",
                    icon = Icons.Default.Tune,
                    isSelected = currentInterface == TitonoxInterface.CONTROL,
                    onClick = { onSelectInterface(TitonoxInterface.CONTROL) },
                    testTag = "nav_control"
                )

                DockInterfaceItem(
                    label = "VISION",
                    icon = Icons.Default.CenterFocusStrong,
                    isSelected = currentInterface == TitonoxInterface.VISION,
                    onClick = { onSelectInterface(TitonoxInterface.VISION) },
                    testTag = "nav_vision"
                )

                DockInterfaceItem(
                    label = "STUDIO",
                    icon = Icons.Default.EditCalendar,
                    isSelected = currentInterface == TitonoxInterface.STUDIO,
                    onClick = { onSelectInterface(TitonoxInterface.STUDIO) },
                    testTag = "nav_studio"
                )

                DockInterfaceItem(
                    label = "PLAYER",
                    icon = Icons.Default.MusicNote,
                    isSelected = currentInterface == TitonoxInterface.PLAYER,
                    onClick = { onSelectInterface(TitonoxInterface.PLAYER) },
                    testTag = "nav_player"
                )

                DockInterfaceItem(
                    label = "ORB",
                    icon = Icons.Default.Palette,
                    isSelected = currentInterface == TitonoxInterface.ORB_STUDIO,
                    onClick = { onSelectInterface(TitonoxInterface.ORB_STUDIO) },
                    testTag = "nav_orb_studio"
                )

                // Dev Telemetry Console Toggle Button
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
                        contentDescription = "Dev Console",
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
    val scale = if (isPressed) 0.94f else 1.0f

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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

