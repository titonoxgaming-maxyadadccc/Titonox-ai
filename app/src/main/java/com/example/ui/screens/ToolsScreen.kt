package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.TitonoxTokens
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ToolsScreen(
    viewModel: TitonoxViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val ramInfo by viewModel.ramInfo.collectAsState()
    val storageInfo by viewModel.storageInfo.collectAsState()

    var flashlightActive by remember { mutableStateOf(false) }
    var currentVolume by remember { mutableFloatStateOf(70f) }

    // Tool Console State
    var selectedToolName by remember { mutableStateOf("device.flashlight") }
    var toolParamInput by remember { mutableStateOf("{\"enabled\": true}") }
    var toolOutputText by remember { mutableStateOf<String?>(null) }

    val registeredToolList = listOf(
        "device.flashlight",
        "device.volume",
        "device.brightness",
        "device.wifi",
        "device.bluetooth",
        "productivity.add_note",
        "productivity.add_todo",
        "orb.rotate",
        "orb.select",
        "orb.customize"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TitonoxTokens.AccentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Tool Hub",
                            tint = TitonoxTokens.AccentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TITONOX TOOLS & HARDWARE CATALOG",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TitonoxTokens.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Real Android Hardware Controls • Diagnostics • Productivity",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TitonoxTokens.TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. Hardware Telemetry Metrics Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HardwareMetricCard(
                    title = "BATTERY",
                    value = "${batteryInfo.percentage}%",
                    sub = if (batteryInfo.isCharging) "Charging" else "Discharging",
                    icon = Icons.Default.BatteryChargingFull,
                    color = TitonoxTokens.StateSuccess,
                    modifier = Modifier.weight(1f)
                )
                HardwareMetricCard(
                    title = "RAM USAGE",
                    value = "${"%.1f".format(ramInfo.totalRamGb - ramInfo.availRamGb)} / ${"%.1f".format(ramInfo.totalRamGb)} GB",
                    sub = "${ramInfo.usedPercentage}% Active",
                    icon = Icons.Default.Memory,
                    color = TitonoxTokens.AccentPrimary,
                    modifier = Modifier.weight(1f)
                )
                HardwareMetricCard(
                    title = "STORAGE",
                    value = "${"%.1f".format(storageInfo.availStorageGb)} GB",
                    sub = "Free of ${"%.1f".format(storageInfo.totalStorageGb)} GB",
                    icon = Icons.Default.Build,
                    color = TitonoxTokens.AccentSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Quick Device Hardware Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DIRECT HARDWARE ACTUATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Flashlight Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (flashlightActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = null,
                                tint = if (flashlightActive) TitonoxTokens.AccentPrimary else TitonoxTokens.TextSecondary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("CAMERA FLASHLIGHT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                                Text(if (flashlightActive) "Flashlight illumination active" else "Flashlight turned off", fontSize = 10.sp, color = TitonoxTokens.TextSecondary)
                            }
                        }
                        Switch(
                            checked = flashlightActive,
                            onCheckedChange = {
                                flashlightActive = it
                                viewModel.deviceController.toggleFlashlight(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TitonoxTokens.AccentPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Media Volume Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MEDIA VOLUME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                            }
                            Text("${currentVolume.toInt()}%", fontSize = 11.sp, color = TitonoxTokens.AccentPrimary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = currentVolume,
                            onValueChange = {
                                currentVolume = it
                                viewModel.deviceController.setVolumePercentage(it.toInt())
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = TitonoxTokens.AccentPrimary,
                                activeTrackColor = TitonoxTokens.AccentPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Wi-Fi & Bluetooth Settings Launchers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WI-FI SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BLUETOOTH", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Productivity Suite & Orb Studio Quick Access
        item {
            Text(
                text = "ECOSYSTEM SUITE ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuiteLauncherCard(
                    title = "MUSIC PLAYER",
                    subtitle = "Futuristic Player",
                    icon = Icons.Default.MusicNote,
                    color = TitonoxTokens.AccentSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectInterface(TitonoxInterface.PLAYER) }
                )
                SuiteLauncherCard(
                    title = "ORB STUDIO",
                    subtitle = "32+ Themes",
                    icon = Icons.Default.Palette,
                    color = TitonoxTokens.AccentPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectInterface(TitonoxInterface.ORB_STUDIO) }
                )
            }
        }

        // 5. Interactive Structured JSON Tool Execution Console
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STRUCTURED TOOL TESTING CONSOLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentPrimary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(registeredToolList) { tool ->
                            val isSelected = tool == selectedToolName
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) TitonoxTokens.AccentPrimary.copy(alpha = 0.2f) else TitonoxTokens.Surface,
                                border = BorderStroke(1.dp, if (isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle),
                                modifier = Modifier.clickable {
                                    selectedToolName = tool
                                    toolParamInput = when (tool) {
                                        "device.flashlight" -> "{\"enabled\": true}"
                                        "device.volume" -> "{\"level\": 80}"
                                        "device.brightness" -> "{\"level\": 70}"
                                        "productivity.add_note" -> "{\"title\": \"Quick Note\", \"content\": \"TITONOX automated note\"}"
                                        "productivity.add_todo" -> "{\"task\": \"Review system diagnostics\"}"
                                        "orb.rotate" -> "{\"axis\": \"Y\", \"amount\": 45}"
                                        "orb.select" -> "{\"orbId\": \"plasma_sphere\"}"
                                        "orb.customize" -> "{\"color\": \"CYAN\", \"glow\": 0.9}"
                                        else -> "{}"
                                    }
                                }
                            ) {
                                Text(
                                    text = tool,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = toolParamInput,
                        onValueChange = { toolParamInput = it },
                        label = { Text("Parameters (JSON)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TitonoxTokens.AccentPrimary,
                            unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                            focusedTextColor = TitonoxTokens.TextPrimary,
                            unfocusedTextColor = TitonoxTokens.TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val paramsJson = JSONObject(toolParamInput)
                                    val res = viewModel.toolRouter.executeTool(selectedToolName, paramsJson)
                                    toolOutputText = "Result: ${if (res.executed) "Success" else "Failed"} • ${res.output ?: res.error ?: "Executed"}"
                                } catch (e: Exception) {
                                    toolOutputText = "JSON Parsing Error: ${e.localizedMessage}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = TitonoxTokens.Background)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXECUTE STRUCTURED TOOL", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    toolOutputText?.let { out ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TitonoxTokens.Surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = out,
                                fontSize = 11.sp,
                                color = TitonoxTokens.TextPrimary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun HardwareMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = TitonoxTokens.SurfaceElevated,
        border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
            Text(sub, fontSize = 9.sp, color = TitonoxTokens.TextSecondary)
        }
    }
}

@Composable
fun SuiteLauncherCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                Text(subtitle, fontSize = 10.sp, color = TitonoxTokens.TextSecondary)
            }
        }
    }
}
