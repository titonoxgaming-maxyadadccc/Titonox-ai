package com.example.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SoftCyan

@Composable
fun FloatingQuickPanel(
    state: OrbState,
    isListening: Boolean,
    isMuted: Boolean,
    activeTaskText: String?,
    onToggleMic: () -> Unit,
    onToggleMute: () -> Unit,
    onAbortTask: () -> Unit,
    onOpenApp: () -> Unit,
    onOpenOrbStudio: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE60A1128),
                        Color(0xF2001F54)
                    )
                )
            )
            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFF00E676) else ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TITONOX QUICK PANEL",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SoftCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Task Banner
            if (!activeTaskText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3300E5FF))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TASK:",
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeTaskText,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mic Button
                QuickActionButton(
                    icon = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    label = if (isListening) "Listening" else "Mic",
                    isActive = isListening,
                    activeColor = Color(0xFF00E676),
                    onClick = onToggleMic
                )

                // Mute Button
                QuickActionButton(
                    icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    label = if (isMuted) "Unmute" else "Mute",
                    isActive = isMuted,
                    activeColor = Color(0xFFFF5252),
                    onClick = onToggleMute
                )

                // Stop / Abort Button
                QuickActionButton(
                    icon = Icons.Default.Stop,
                    label = "Stop",
                    isActive = false,
                    activeColor = Color(0xFFFF1744),
                    onClick = onAbortTask
                )

                // Open App Button
                QuickActionButton(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    label = "App",
                    isActive = false,
                    activeColor = ElectricCyan,
                    onClick = onOpenApp
                )

                // Orb Studio Shortcut
                QuickActionButton(
                    icon = Icons.Default.Palette,
                    label = "Studio",
                    isActive = false,
                    activeColor = NeonBlue,
                    onClick = onOpenOrbStudio
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.25f) else Color(0x26FFFFFF))
                .border(1.dp, if (isActive) activeColor else ElectricCyan.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = SoftCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
