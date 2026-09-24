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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.TextSecondary

@Composable
fun BottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CyberCardSurface.copy(alpha = 0.95f))
                .border(1.dp, CyberGlassBorder, RoundedCornerShape(24.dp))
                .padding(vertical = 6.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItem(
                    label = "Chat",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    isSelected = selectedTab == MainTab.CHAT,
                    onClick = { onTabSelected(MainTab.CHAT) }
                )
                TabItem(
                    label = "Voice",
                    icon = Icons.Default.Mic,
                    isSelected = selectedTab == MainTab.VOICE,
                    onClick = { onTabSelected(MainTab.VOICE) }
                )
                TabItem(
                    label = "Tasks",
                    icon = Icons.Default.TaskAlt,
                    isSelected = selectedTab == MainTab.TASKS,
                    onClick = { onTabSelected(MainTab.TASKS) }
                )
                TabItem(
                    label = "Vision",
                    icon = Icons.Default.RemoveRedEye,
                    isSelected = selectedTab == MainTab.VISION,
                    onClick = { onTabSelected(MainTab.VISION) }
                )
                TabItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == MainTab.SETTINGS,
                    onClick = { onTabSelected(MainTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tag = "tab_${label.lowercase()}"
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else androidx.compose.ui.graphics.Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ElectricCyan else TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = if (isSelected) ElectricCyan else TextSecondary.copy(alpha = 0.7f)
        )
    }
}
