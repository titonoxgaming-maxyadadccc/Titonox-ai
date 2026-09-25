package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MemoryEntity
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.device.BatteryInfo
import com.example.device.SimSlotInfo
import com.example.ui.DevLog
import com.example.ui.components.LuxurySurface
import com.example.ui.theme.TitonoxTokens

private enum class SettingsCategory(val label: String) {
    ALL("ALL"),
    AI("AI & API"),
    AUTOMATION("AUTOMATION"),
    ORB("ORB & STYLE"),
    MEMORY("PRIVACY & MEMORY"),
    TELEMETRY("DEVICE"),
    SYSTEM("DEV & SYSTEM")
}

@Composable
fun SettingsScreen(
    isAccessibilityActive: Boolean,
    batteryInfo: BatteryInfo,
    simSlots: List<SimSlotInfo>,
    memories: List<MemoryEntity>,
    devModeEnabled: Boolean,
    devLogs: List<DevLog>,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenSocial: (String) -> Unit,
    onOpenOrbStudio: () -> Unit = {},
    onOpenApiSettings: () -> Unit = {},
    onSaveMemory: (String, String) -> Unit,
    onDeleteMemory: (Long) -> Unit,
    onClearAllMemories: () -> Unit,
    onToggleDevMode: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.ALL) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .testTag("titonox_settings_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SETTINGS & SYSTEM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TitonoxTokens.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "TITONOX COMMERCIAL ENVIRONMENT CONFIGURATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TitonoxTokens.TextSecondary,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        // Category Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SettingsCategory.values()) { category ->
                val isSelected = selectedCategory == category
                LuxurySurface(
                    shape = TitonoxTokens.RadiusPill,
                    backgroundColor = if (isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.Surface,
                    borderColor = if (isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle,
                    onClick = { selectedCategory = category }
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) TitonoxTokens.Background else TitonoxTokens.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Settings Sections Scrollable Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section: Official Identity & Creator Card
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.SYSTEM) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ASSISTANT IDENTITY",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(TitonoxTokens.RadiusSm)
                                        .background(TitonoxTokens.StateSuccess.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text("VERIFIED COMMERCIAL", style = MaterialTheme.typography.labelSmall.copy(color = TitonoxTokens.StateSuccess, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Product: TITONOX JARVIS AI Assistant", style = MaterialTheme.typography.bodyMedium.copy(color = TitonoxTokens.TextPrimary, fontWeight = FontWeight.SemiBold))
                            Text(text = "Creator & Architect: Aditya Yadav", style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextSecondary))
                            Text(text = "Target Platform: Android Commercial Suite (₹5,000 Class)", style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextMuted, fontSize = 11.sp))

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onOpenSocial("https://www.youtube.com/@TITONOXOFFICIAL") },
                                    colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.Surface),
                                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                                    shape = TitonoxTokens.RadiusMd,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("YouTube Channel", fontSize = 11.sp, color = TitonoxTokens.TextPrimary)
                                }

                                Button(
                                    onClick = { onOpenSocial("https://www.instagram.com/TITONOXOFFICIAL") },
                                    colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.Surface),
                                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                                    shape = TitonoxTokens.RadiusMd,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = TitonoxTokens.AccentViolet, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Instagram", fontSize = 11.sp, color = TitonoxTokens.TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Section: AI & API Configuration (Google Gemini)
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.AI) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI INTELLIGENCE & API",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(TitonoxTokens.RadiusSm)
                                        .background(TitonoxTokens.AccentPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text("GEMINI 2.5", style = MaterialTheme.typography.labelSmall.copy(color = TitonoxTokens.AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Real Google Gemini API provider with tool planning, intent validation, and automated multi-step task generation. Includes temperature and tool permission controls.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TitonoxTokens.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenApiSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                                shape = TitonoxTokens.RadiusMd,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_open_api_config_btn")
                            ) {
                                Text("CONFIGURE GEMINI API & KEYS", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section: Automation & Accessibility
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.AUTOMATION) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AUTOMATION ACCESSIBILITY",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(TitonoxTokens.RadiusSm)
                                        .background(if (isAccessibilityActive) TitonoxTokens.StateSuccess.copy(alpha = 0.15f) else TitonoxTokens.StateError.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isAccessibilityActive) "ENABLED" else "DISABLED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isAccessibilityActive) TitonoxTokens.StateSuccess else TitonoxTokens.StateError,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enables TITONOX to inspect active Android UI elements, input text, tap buttons, scroll views, and verify multi-step automation sequences.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TitonoxTokens.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenAccessibilitySettings,
                                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.Surface),
                                border = BorderStroke(1.dp, TitonoxTokens.AccentPrimary),
                                shape = TitonoxTokens.RadiusMd,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_accessibility_settings_button")
                            ) {
                                Text("ACCESSIBILITY SYSTEM SETTINGS", color = TitonoxTokens.AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section: Orb System (30 Designs)
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.ORB) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = TitonoxTokens.AccentViolet, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI ORB STUDIO (30 THEMES)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentViolet,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(TitonoxTokens.RadiusSm)
                                        .background(TitonoxTokens.AccentViolet.copy(alpha = 0.15f))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text("15 2D • 15 3D", style = MaterialTheme.typography.labelSmall.copy(color = TitonoxTokens.AccentViolet, fontWeight = FontWeight.Bold, fontSize = 9.sp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Customize the visual AI core. Choose from 15 high-tech 2D canvas renderers and 15 interactive 3D math renderers with touch rotation, particle density, and speed control.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TitonoxTokens.TextSecondary,
                                    lineHeight = 16.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenOrbStudio,
                                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentViolet),
                                shape = TitonoxTokens.RadiusMd,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("OPEN ORB STUDIO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section: Long-Term Memory & Personalization
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.MEMORY) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI MEMORY (${memories.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }

                                if (memories.isNotEmpty()) {
                                    Text(
                                        text = "CLEAR ALL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TitonoxTokens.StateError,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.clickable { onClearAllMemories() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Persistent facts and preferences injected into TITONOX prompts for custom personalization.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TitonoxTokens.TextSecondary,
                                    fontSize = 11.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Add Memory Form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newKey,
                                    onValueChange = { newKey = it },
                                    placeholder = { Text("Key", fontSize = 11.sp, color = TitonoxTokens.TextMuted) },
                                    modifier = Modifier.weight(1f),
                                    shape = TitonoxTokens.RadiusSm,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TitonoxTokens.AccentPrimary,
                                        unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                                        focusedTextColor = TitonoxTokens.TextPrimary,
                                        unfocusedTextColor = TitonoxTokens.TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = newValue,
                                    onValueChange = { newValue = it },
                                    placeholder = { Text("Preference", fontSize = 11.sp, color = TitonoxTokens.TextMuted) },
                                    modifier = Modifier.weight(1.5f),
                                    shape = TitonoxTokens.RadiusSm,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TitonoxTokens.AccentPrimary,
                                        unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                                        focusedTextColor = TitonoxTokens.TextPrimary,
                                        unfocusedTextColor = TitonoxTokens.TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                            onSaveMemory(newKey, newValue)
                                            newKey = ""
                                            newValue = ""
                                        }
                                    },
                                    shape = TitonoxTokens.RadiusSm,
                                    colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary)
                                ) {
                                    Text("SAVE", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Memory List
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                memories.forEach { mem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(TitonoxTokens.RadiusSm)
                                            .background(TitonoxTokens.Surface)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = mem.key, style = MaterialTheme.typography.labelSmall.copy(color = TitonoxTokens.AccentPrimary, fontWeight = FontWeight.Bold))
                                            Text(text = mem.value, style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextPrimary))
                                        }
                                        IconButton(
                                            onClick = { onDeleteMemory(mem.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TitonoxTokens.TextMuted, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section: Device Telemetry & Battery
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.TELEMETRY) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DEVICE TELEMETRY",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = TitonoxTokens.AccentPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Battery Power", style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextSecondary))
                                Text(
                                    text = "${batteryInfo.percentage}% (${if (batteryInfo.isCharging) "Charging" else "Discharging"})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            simSlots.forEach { sim ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("SIM Slot ${sim.slotIndex}", style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextSecondary))
                                    Text(
                                        text = "${sim.carrierName} (${sim.displayName})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TitonoxTokens.TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (simSlots.isEmpty()) {
                                Text(
                                    text = "Dual SIM hardware ready / Standard carrier profile",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TitonoxTokens.TextMuted, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // Section: Developer Diagnostics & Live Trace
            if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.SYSTEM) {
                item {
                    LuxurySurface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = TitonoxTokens.RadiusLg,
                        backgroundColor = TitonoxTokens.SurfaceElevated,
                        borderColor = if (devModeEnabled) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BugReport, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "DEVELOPER DIAGNOSTICS",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = TitonoxTokens.AccentPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }

                                Switch(
                                    checked = devModeEnabled,
                                    onCheckedChange = { onToggleDevMode() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TitonoxTokens.Background,
                                        checkedTrackColor = TitonoxTokens.AccentPrimary,
                                        uncheckedThumbColor = TitonoxTokens.TextMuted,
                                        uncheckedTrackColor = TitonoxTokens.Surface
                                    )
                                )
                            }

                            if (devModeEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Real-Time Tool Routing Trace (Intent -> Tool -> Latency):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TitonoxTokens.AccentSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    devLogs.take(8).forEach { log ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(TitonoxTokens.RadiusSm)
                                                .background(TitonoxTokens.Surface)
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${log.intent} -> ${log.tool}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = TitonoxTokens.AccentPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = "${log.latencyMs}ms",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = TitonoxTokens.StateSuccess,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = log.result,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = TitonoxTokens.TextSecondary,
                                                        fontSize = 10.sp
                                                    ),
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: TitonoxViewModel,
    onOpenUrl: (String) -> Unit
) {
    val isAccessibilityActive = viewModel.accessibilityController.isServiceActive()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val simSlots by viewModel.simSlots.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val devModeEnabled by viewModel.devModeEnabled.collectAsState()
    val devLogs by viewModel.devLogs.collectAsState()

    SettingsScreen(
        isAccessibilityActive = isAccessibilityActive,
        batteryInfo = batteryInfo,
        simSlots = simSlots,
        memories = memories,
        devModeEnabled = devModeEnabled,
        devLogs = devLogs,
        onOpenAccessibilitySettings = { viewModel.accessibilityController.openAccessibilitySettings() },
        onOpenSocial = onOpenUrl,
        onOpenOrbStudio = { viewModel.selectInterface(TitonoxInterface.ORB_STUDIO) },
        onOpenApiSettings = { viewModel.selectInterface(TitonoxInterface.MODELS) },
        onSaveMemory = { k, v -> viewModel.addMemory(k, v) },
        onDeleteMemory = { id -> viewModel.deleteMemory(id) },
        onClearAllMemories = { viewModel.clearAllMemories() },
        onToggleDevMode = { viewModel.toggleDevMode() }
    )
}
