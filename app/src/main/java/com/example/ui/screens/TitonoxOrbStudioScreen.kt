package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floating.FloatingOrbService
import com.example.floating.GlowPreset
import com.example.floating.OrbCustomization
import com.example.floating.OrbDimension
import com.example.floating.OrbMasterCanvas
import com.example.floating.OrbSettingsRepository
import com.example.floating.OrbState
import com.example.floating.OrbThemeType
import com.example.floating.VoicePersona
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.SoftCyan

enum class ColorTarget(val label: String) {
    PRIMARY("Primary Core Color"),
    SECONDARY("Secondary Accent Color"),
    GLOW("Ambient Glow Color"),
    PARTICLE("Particle Dust Color")
}

@Composable
fun TitonoxOrbStudioScreen(
    viewModel: TitonoxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = remember { OrbSettingsRepository.getInstance(context) }
    val customization by repo.settings.collectAsState()
    val isPowerOff by repo.isPowerOff.collectAsState()

    var simulatedState by remember { mutableStateOf(OrbState.IDLE) }
    var simulatedAudioLevel by remember { mutableFloatStateOf(0.4f) }
    var selectedDimensionTab by remember { mutableStateOf("ALL") } // "2D", "3D", "ALL"

    var colorPickerTarget by remember { mutableStateOf<ColorTarget?>(null) }
    val hasOverlayPermission = Settings.canDrawOverlays(context)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Column {
                Text(
                    text = "TITONOX // ORB STUDIO",
                    color = ElectricCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Core Visual Architecture",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "30 completely unique 2D and 3D geometric AI cores with real-time audio reactivity, custom RGB/HEX color physics, and voice persona calibration.",
                    color = SoftCyan,
                    fontSize = 13.sp
                )
            }
        }

        // 2. Permission / Status Notice
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPowerOff) Color(0x33FF1744) else if (hasOverlayPermission) Color(0x2600E5FF) else Color(0x33FF9100)
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPowerOff) Color(0xFFFF1744) else if (hasOverlayPermission) ElectricCyan else Color(0xFFFF9100)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPowerOff) "TITONOX OFF MODE" else if (hasOverlayPermission) "OVERLAY PERMISSION ACTIVE" else "PERMISSION REQUIRED",
                            color = if (isPowerOff) Color(0xFFFF5252) else if (hasOverlayPermission) ElectricCyan else Color(0xFFFFB74D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (isPowerOff) "Assistant was powered off. Tap restart to re-ignite core." else if (hasOverlayPermission) "Floating orb appears automatically outside TITONOX." else "Grant display over other apps to enable floating orb.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isPowerOff) {
                        Button(
                            onClick = {
                                repo.setPowerOff(false)
                                FloatingOrbService.startService(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = DarkNavyBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restart", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = DarkNavyBg, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Grant", color = DarkNavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Switch(
                            checked = customization.isAssistantEnabled,
                            onCheckedChange = { enabled ->
                                repo.setAssistantEnabled(enabled)
                                if (enabled) FloatingOrbService.startService(context) else FloatingOrbService.stopService(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkNavyBg,
                                checkedTrackColor = ElectricCyan
                            )
                        )
                    }
                }
            }
        }

        // 3. Interactive Hero Stage Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LIVE INTERACTIVE PREVIEW",
                        color = SoftCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Canvas
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(CyberBlack)
                            .border(1.dp, ElectricCyan.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        OrbMasterCanvas(
                            customization = customization,
                            state = simulatedState,
                            audioLevel = simulatedAudioLevel,
                            modifier = Modifier.size(130.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = customization.theme.displayName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${customization.theme.dimension.label} • ${customization.theme.description}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // State Simulator Selector (IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, SUCCESS, ERROR, OFF)
                    Text(text = "Simulate State Response:", color = SoftCyan, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(OrbState.values()) { state ->
                            val isSelected = simulatedState == state
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricCyan else Color(0x26FFFFFF))
                                    .clickable { simulatedState = state }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = state.name,
                                    color = if (isSelected) DarkNavyBg else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Level Simulator Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Simulated Audio Level: ${(simulatedAudioLevel * 100).toInt()}%", color = SoftCyan, fontSize = 11.sp)
                    }
                    Slider(
                        value = simulatedAudioLevel,
                        onValueChange = { simulatedAudioLevel = it },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ElectricCyan,
                            activeTrackColor = ElectricCyan,
                            inactiveTrackColor = Color(0x3300E5FF)
                        )
                    )
                }
            }
        }

        // 4. 30 Unique Orbs Category & Dimension Selector
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORB SELECTOR (30 UNIQUE DESIGNS)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current: ${customization.theme.displayName}",
                        color = ElectricCyan,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dimension Filter Tabs: 2D ORBS (15) vs 3D ORBS (15) vs ALL (30)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("ALL" to "ALL (30)", "2D" to "2D ORBS (15)", "3D" to "3D ORBS (15)")
                    tabs.forEach { (tabKey, label) ->
                        val isSelected = selectedDimensionTab == tabKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan else Color(0x1A00E5FF))
                                .border(1.dp, if (isSelected) ElectricCyan else Color(0x3300E5FF), RoundedCornerShape(12.dp))
                                .clickable { selectedDimensionTab = tabKey }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) DarkNavyBg else SoftCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Filter themes based on selected dimension tab
        val displayedThemes = OrbThemeType.values().filter {
            when (selectedDimensionTab) {
                "2D" -> it.dimension == OrbDimension.TWO_D
                "3D" -> it.dimension == OrbDimension.THREE_D
                else -> true
            }
        }

        items(displayedThemes.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (t in pair) {
                    val isSelected = customization.theme == t
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                repo.updateSettings(customization.copy(theme = t))
                            }
                            .testTag("orb_card_${t.name.lowercase()}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0x4D00E5FF) else DarkNavyCard
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) ElectricCyan else Color(0x3300E5FF)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(CyberBlack),
                                contentAlignment = Alignment.Center
                            ) {
                                OrbMasterCanvas(
                                    customization = customization.copy(theme = t),
                                    state = OrbState.IDLE,
                                    audioLevel = 0.35f,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t.displayName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = t.dimension.label,
                                color = SoftCyan,
                                fontSize = 10.sp
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ACTIVE", color = ElectricCyan, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 5. Complete Orb Customization Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = ElectricCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CUSTOMIZE ORB AESTHETICS",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(
                            onClick = {
                                repo.updateSettings(OrbCustomization(theme = customization.theme))
                            }
                        ) {
                            Text("Reset", color = SoftCyan, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Palettes
                    Text(text = "Color Palettes:", color = SoftCyan, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    val palettes = listOf(
                        "Cyber Cyan" to (0xFF00E5FF to 0xFF00B0FF),
                        "Plasma Violet" to (0xFFD500F9 to 0xFF651FFF),
                        "Matrix Emerald" to (0xFF00E676 to 0xFF00B0FF),
                        "Solar Flame" to (0xFFFFAB00 to 0xFFFF3D00),
                        "Arctic Frost" to (0xFF80D8FF to 0xFFFFFFFF),
                        "Crimson Drive" to (0xFFFF1744 to 0xFFD50000)
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(palettes) { (name, colors) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(colors.first), Color(colors.second))
                                        )
                                    )
                                    .clickable {
                                        repo.updateSettings(
                                            customization.copy(
                                                primaryColor = colors.first,
                                                secondaryColor = colors.second,
                                                glowColor = colors.first,
                                                ringColor = colors.second
                                            )
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = name, color = DarkNavyBg, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4 Interactive Color Selectors (Primary, Secondary, Glow, Particle)
                    Text(text = "Fine Color Controls (HEX / RGB):", color = SoftCyan, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    ColorSettingRow(
                        label = "Primary Color",
                        color = Color(customization.primaryColor),
                        onClick = { colorPickerTarget = ColorTarget.PRIMARY }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorSettingRow(
                        label = "Secondary Color",
                        color = Color(customization.secondaryColor),
                        onClick = { colorPickerTarget = ColorTarget.SECONDARY }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorSettingRow(
                        label = "Glow Color",
                        color = Color(customization.glowColor),
                        onClick = { colorPickerTarget = ColorTarget.GLOW }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorSettingRow(
                        label = "Particle Color",
                        color = Color(customization.particleColor),
                        onClick = { colorPickerTarget = ColorTarget.PARTICLE }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Background Glow Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Background Glow", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Ambient radial illumination behind orb", color = SoftCyan, fontSize = 11.sp)
                        }
                        Switch(
                            checked = customization.backgroundGlow,
                            onCheckedChange = { repo.updateSettings(customization.copy(backgroundGlow = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = DarkNavyBg, checkedTrackColor = ElectricCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Glow Intensity
                    Text(
                        text = "Glow Intensity: ${String.format("%.1fx", customization.glowIntensity)}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.glowIntensity,
                        onValueChange = { repo.updateSettings(customization.copy(glowIntensity = it)) },
                        valueRange = 0.0f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Size
                    Text(
                        text = "Orb Dimensions: ${customization.sizeDp} dp",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.sizeDp.toFloat(),
                        onValueChange = { repo.updateSettings(customization.copy(sizeDp = it.toInt())) },
                        valueRange = 48f..130f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Animation Speed
                    Text(
                        text = "Animation Speed: ${String.format("%.1fx", customization.animationSpeed)}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.animationSpeed,
                        onValueChange = { repo.updateSettings(customization.copy(animationSpeed = it)) },
                        valueRange = 0.3f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Rotation Speed
                    Text(
                        text = "Rotation Velocity: ${String.format("%.1fx", customization.rotationSpeed)}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.rotationSpeed,
                        onValueChange = { repo.updateSettings(customization.copy(rotationSpeed = it)) },
                        valueRange = 0.3f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Particle Density
                    Text(
                        text = "Particle Density: ${customization.particleDensity}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.particleDensity.toFloat(),
                        onValueChange = { repo.updateSettings(customization.copy(particleDensity = it.toInt())) },
                        valueRange = 5f..50f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Pulse Intensity
                    Text(
                        text = "Pulse Strength: ${String.format("%.1fx", customization.pulseIntensity)}",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.pulseIntensity,
                        onValueChange = { repo.updateSettings(customization.copy(pulseIntensity = it)) },
                        valueRange = 0.3f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )

                    // Opacity
                    Text(
                        text = "Opacity: ${(customization.opacity * 100).toInt()}%",
                        color = SoftCyan,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = customization.opacity,
                        onValueChange = { repo.updateSettings(customization.copy(opacity = it)) },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                }
            }
        }

        // 6. Voice Persona Calibration
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavyCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TITONOX VOICE PERSONAS",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    for (p in VoicePersona.values()) {
                        val isSelected = customization.voicePersona == p
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x3300E5FF) else Color(0x1AFFFFFF))
                                .border(1.dp, if (isSelected) ElectricCyan else Color(0x2600E5FF), RoundedCornerShape(12.dp))
                                .clickable {
                                    repo.updateSettings(
                                        customization.copy(
                                            voicePersona = p,
                                            speechPitch = p.defaultPitch,
                                            speechRate = p.defaultRate
                                        )
                                    )
                                    viewModel.voiceManager.setVoicePersona(p)
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.displayName,
                                        color = if (isSelected) ElectricCyan else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = p.description,
                                        color = SoftCyan,
                                        fontSize = 11.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice Testing Button
                    Button(
                        onClick = {
                            viewModel.voiceManager.testVoicePersona(customization.voicePersona)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = DarkNavyBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST ${customization.voicePersona.displayName.uppercase()}",
                            color = DarkNavyBg,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Bottom space to clear floating dock
        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // Color Picker Dialog (RGB & HEX)
    if (colorPickerTarget != null) {
        val target = colorPickerTarget!!
        val currentColorLong = when (target) {
            ColorTarget.PRIMARY -> customization.primaryColor
            ColorTarget.SECONDARY -> customization.secondaryColor
            ColorTarget.GLOW -> customization.glowColor
            ColorTarget.PARTICLE -> customization.particleColor
        }

        ColorPickerDialog(
            title = target.label,
            initialColor = currentColorLong,
            onDismiss = { colorPickerTarget = null },
            onColorChosen = { newColor ->
                when (target) {
                    ColorTarget.PRIMARY -> repo.updateSettings(customization.copy(primaryColor = newColor))
                    ColorTarget.SECONDARY -> repo.updateSettings(customization.copy(secondaryColor = newColor))
                    ColorTarget.GLOW -> repo.updateSettings(customization.copy(glowColor = newColor))
                    ColorTarget.PARTICLE -> repo.updateSettings(customization.copy(particleColor = newColor))
                }
                colorPickerTarget = null
            }
        )
    }
}

@Composable
fun ColorSettingRow(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x1AFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2600E5FF)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("#%06X", (0xFFFFFF and ((color.red * 255).toInt() shl 16 or ((color.green * 255).toInt() shl 8) or (color.blue * 255).toInt()))),
                    color = SoftCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Long,
    onDismiss: () -> Unit,
    onColorChosen: (Long) -> Unit
) {
    val initialR = ((initialColor shr 16) and 0xFF).toInt()
    val initialG = ((initialColor shr 8) and 0xFF).toInt()
    val initialB = (initialColor and 0xFF).toInt()

    var r by remember { mutableIntStateOf(initialR) }
    var g by remember { mutableIntStateOf(initialG) }
    var b by remember { mutableIntStateOf(initialB) }
    var hexText by remember { mutableStateOf(String.format("%02X%02X%02X", r, g, b)) }

    val activeColorLong = (0xFF000000L or ((r.toLong() and 0xFF) shl 16) or ((g.toLong() and 0xFF) shl 8) or (b.toLong() and 0xFF))
    val activeColor = Color(activeColorLong)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // Color Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeColor)
                        .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${String.format("%02X%02X%02X", r, g, b)}",
                        color = if (r + g + b > 380) Color.Black else Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HEX Text Field
                OutlinedTextField(
                    value = hexText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }.take(6)
                        hexText = clean
                        if (clean.length == 6) {
                            try {
                                val parsed = clean.toLong(16)
                                r = ((parsed shr 16) and 0xFF).toInt()
                                g = ((parsed shr 8) and 0xFF).toInt()
                                b = (parsed and 0xFF).toInt()
                            } catch (ignored: Exception) {}
                        }
                    },
                    label = { Text("HEX Code (#RRGGBB)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = SoftCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RGB Sliders
                Text(text = "Red: $r", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = r.toFloat(),
                    onValueChange = {
                        r = it.toInt()
                        hexText = String.format("%02X%02X%02X", r, g, b)
                    },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252))
                )

                Text(text = "Green: $g", color = Color(0xFF69F0AE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = g.toFloat(),
                    onValueChange = {
                        g = it.toInt()
                        hexText = String.format("%02X%02X%02X", r, g, b)
                    },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF69F0AE), activeTrackColor = Color(0xFF69F0AE))
                )

                Text(text = "Blue: $b", color = Color(0xFF40C4FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = b.toFloat(),
                    onValueChange = {
                        b = it.toInt()
                        hexText = String.format("%02X%02X%02X", r, g, b)
                    },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF40C4FF), activeTrackColor = Color(0xFF40C4FF))
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorChosen(activeColorLong) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Apply Color", color = DarkNavyBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SoftCyan)
            }
        },
        containerColor = DarkNavyCard
    )
}
