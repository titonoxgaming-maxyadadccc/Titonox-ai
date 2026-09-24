package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateExecuting

@Composable
fun TitonoxControlScreen(
    viewModel: TitonoxViewModel,
    modifier: Modifier = Modifier
) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val ramInfo by viewModel.ramInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()
    val specs by viewModel.deviceSpecs.collectAsState()
    val simSlots by viewModel.simSlots.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()

    val mediaVol by viewModel.mediaVolume.collectAsState()
    val ringVol by viewModel.ringVolume.collectAsState()
    val alarmVol by viewModel.alarmVolume.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        Color(0xFF021329),
                        CyberDarkNavy
                    )
                )
            )
            .testTag("titonox_control_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 72.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TITONOX CONTROL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = "SYSTEM TELEMETRY & HARDWARE CONTROLLER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonBlue.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                IconButton(
                    onClick = { viewModel.refreshDeviceStatus() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkNavy)
                        .testTag("control_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Telemetry",
                        tint = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. HARDWARE TELEMETRY GRID (Battery, RAM, Storage, Device Specs)
            Text(
                text = "REAL-TIME HARDWARE TELEMETRY",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Battery Card
                TelemetryMetricCard(
                    title = "BATTERY",
                    value = "${batteryInfo.percentage}%",
                    subtext = if (batteryInfo.isCharging) "Charging Fast" else "Discharging",
                    progress = batteryInfo.percentage / 100f,
                    icon = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    accentColor = if (batteryInfo.percentage < 20) Color(0xFFFF5252) else StateCompleted,
                    modifier = Modifier.weight(1f)
                )

                // RAM Card
                TelemetryMetricCard(
                    title = "RAM MEMORY",
                    value = "${ramInfo.availRamGb} GB",
                    subtext = "Free of ${ramInfo.totalRamGb} GB",
                    progress = ramInfo.usedPercentage / 100f,
                    icon = Icons.Default.Memory,
                    accentColor = NeonBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Storage Card
                TelemetryMetricCard(
                    title = "INTERNAL STORAGE",
                    value = "${storageInfo.availStorageGb} GB",
                    subtext = "${storageInfo.usedPercentage}% Used of ${storageInfo.totalStorageGb} GB",
                    progress = storageInfo.usedPercentage / 100f,
                    icon = Icons.Default.SdStorage,
                    accentColor = ElectricCyan,
                    modifier = Modifier.weight(1f)
                )

                // Device Specs Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(115.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "DEVICE MODEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = HoloWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${specs.manufacturer} ${specs.model}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = HoloWhite,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "Android ${specs.androidVersion} (API ${specs.sdkInt})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloWhite.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. QUICK ACTIONS & HARDWARE TOGGLES
            Text(
                text = "SYSTEM CONTROLS & SHORTCUTS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Flashlight Action Button
                QuickActionButton(
                    title = if (isFlashlightOn) "Torch ON" else "Torch OFF",
                    subtitle = "Tap to toggle",
                    icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    isActive = isFlashlightOn,
                    activeColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.toggleFlashlight() }
                )

                // Wi-Fi Panel Shortcut
                QuickActionButton(
                    title = "Wi-Fi Panel",
                    subtitle = "Configure network",
                    icon = Icons.Default.Wifi,
                    isActive = true,
                    activeColor = NeonBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openWifiSettings() }
                )

                // Bluetooth Shortcut
                QuickActionButton(
                    title = "Bluetooth",
                    subtitle = "Pair & devices",
                    icon = Icons.Default.Bluetooth,
                    isActive = false,
                    activeColor = NeonBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openBluetoothSettings() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "Display Settings",
                    subtitle = "Brightness & Sleep",
                    icon = Icons.Default.BrightnessMedium,
                    isActive = false,
                    activeColor = HoloWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openDisplaySettings() }
                )

                QuickActionButton(
                    title = "Sound Settings",
                    subtitle = "Ringtones & Equalizer",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    isActive = false,
                    activeColor = HoloWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openSoundSettings() }
                )

                QuickActionButton(
                    title = "Notifications",
                    subtitle = "App alerts",
                    icon = Icons.Default.Notifications,
                    isActive = false,
                    activeColor = HoloWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openNotificationSettings() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "AI & API",
                    subtitle = "Gemini key & model",
                    icon = Icons.Default.Key,
                    isActive = true,
                    activeColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectInterface(TitonoxInterface.API_SETTINGS) }
                )

                QuickActionButton(
                    title = "Orb Studio",
                    subtitle = "30+ 2D/3D Orbs",
                    icon = Icons.Default.Palette,
                    isActive = true,
                    activeColor = NeonBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectInterface(TitonoxInterface.ORB_STUDIO) }
                )

                QuickActionButton(
                    title = "Settings",
                    subtitle = "Android system",
                    icon = Icons.Default.Settings,
                    isActive = false,
                    activeColor = HoloWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.deviceController.openSettings() }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. LIVE MULTI-STREAM AUDIO VOLUME SLIDERS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE AUDIO STREAM CALIBRATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    VolumeSliderRow(
                        label = "Media Volume",
                        percentage = mediaVol,
                        onValueChange = { viewModel.setMediaVolume(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VolumeSliderRow(
                        label = "Ringtone / Calls",
                        percentage = ringVol,
                        onValueChange = { viewModel.setRingVolume(it) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    VolumeSliderRow(
                        label = "Alarms",
                        percentage = alarmVol,
                        onValueChange = { viewModel.setAlarmVolume(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. DUAL SIM & NETWORK CARRIER TELEMETRY
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CELLULAR & DUAL SIM CARDS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.SimCard,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (simSlots.isNotEmpty()) {
                        simSlots.forEach { sim ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StateCompleted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${sim.displayName} (${sim.carrierName})",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite)
                                    )
                                }
                                Text(
                                    text = if (sim.isDataRoaming) "Roaming" else "Active",
                                    style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "SIM 1 & SIM 2: Active Cellular Radio Available",
                            style = MaterialTheme.typography.bodySmall.copy(color = HoloWhite.copy(alpha = 0.7f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    subtext: String,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = HoloWhite,
                    fontWeight = FontWeight.Black
                )
            )

            Column {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accentColor,
                    trackColor = HoloWhite.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HoloWhite.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(85.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        color = if (isActive) activeColor.copy(alpha = 0.2f) else DarkNavy.copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) activeColor else NeonBlue.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else HoloWhite.copy(alpha = 0.8f),
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = HoloWhite,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = HoloWhite.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VolumeSliderRow(
    label: String,
    percentage: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(color = HoloWhite)
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Slider(
            value = percentage.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = ElectricCyan,
                activeTrackColor = ElectricCyan,
                inactiveTrackColor = HoloWhite.copy(alpha = 0.15f)
            )
        )
    }
}
