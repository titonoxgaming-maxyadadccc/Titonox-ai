package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateFailed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopBarStatus(
    isAccessibilityActive: Boolean,
    isScreenCaptureActive: Boolean,
    isMuted: Boolean,
    devModeEnabled: Boolean,
    onAccessibilityClick: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleDevMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isAccessibilityActive) StateCompleted else ElectricCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TITONOX",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CORE v2.5",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }

            // Quick actions (Mute & Dev Mode)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("toggle_mute_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Toggle Mute",
                        tint = if (isMuted) StateFailed else NeonBlue
                    )
                }

                IconButton(
                    onClick = onToggleDevMode,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("toggle_dev_mode_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Toggle Developer Mode",
                        tint = if (devModeEnabled) ElectricCyan else TextSecondary.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Sub-bar for Hardware & Accessibility status pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accessibility Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberNavySurface)
                    .border(1.dp, if (isAccessibilityActive) StateCompleted.copy(alpha = 0.6f) else StateFailed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { onAccessibilityClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("accessibility_status_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isAccessibilityActive) StateCompleted else StateFailed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAccessibilityActive) "ACCESSIBILITY: ACTIVE" else "ACCESSIBILITY: OFF",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isAccessibilityActive) StateCompleted else StateFailed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Screen Vision Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberNavySurface)
                    .border(1.dp, if (isScreenCaptureActive) ElectricCyan.copy(alpha = 0.6f) else CyberGlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("screen_capture_status_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isScreenCaptureActive) ElectricCyan else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScreenCaptureActive) "VISION: LIVE" else "VISION: OFF",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isScreenCaptureActive) ElectricCyan else TextSecondary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
