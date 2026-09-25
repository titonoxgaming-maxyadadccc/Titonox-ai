package com.example.ui.screens

import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.floating.FloatingOrbService
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.TitonoxTokens

@Composable
fun AutomationScreen(
    viewModel: TitonoxViewModel
) {
    val context = LocalContext.current
    val isAccessibilityActive = viewModel.accessibilityController.isServiceActive()
    val isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true

    val devLogs by viewModel.devLogs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Accessibility Service Master Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isAccessibilityActive) TitonoxTokens.StateSuccess else TitonoxTokens.StateWarning),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isAccessibilityActive) TitonoxTokens.StateSuccess.copy(alpha = 0.15f) else TitonoxTokens.StateWarning.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessibilityNew,
                                    contentDescription = "Accessibility",
                                    tint = if (isAccessibilityActive) TitonoxTokens.StateSuccess else TitonoxTokens.StateWarning,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ACCESSIBILITY AUTOMATION ENGINE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = if (isAccessibilityActive) "Active & Inspecting UI nodes" else "Needs system permission to automate",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TitonoxTokens.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.accessibilityController.openAccessibilitySettings() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAccessibilityActive) TitonoxTokens.SurfaceHighlight else TitonoxTokens.StateWarning
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_accessibility_settings_btn")
                        ) {
                            Text(
                                text = if (isAccessibilityActive) "CONFIGURED" else "ENABLE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityActive) TitonoxTokens.TextPrimary else TitonoxTokens.Background
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Overlay Floating Orb Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TitonoxTokens.Surface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FLOATING ORB OUTSIDE APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                            Text(if (isOverlayGranted) "Overlay permission granted" else "Overlay permission required", fontSize = 10.sp, color = TitonoxTokens.TextSecondary)
                        }
                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                } else {
                                    FloatingOrbService.startService(context)
                                }
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(if (isOverlayGranted) "LAUNCH ORB" else "GRANT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Direct Automation Triggers Grid
        item {
            Text(
                text = "DIRECT ACCESSIBILITY ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutomationActionButton(
                        label = "BACK",
                        icon = Icons.Default.ArrowBack,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.pressBack() }
                    )
                    AutomationActionButton(
                        label = "HOME",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.pressHome() }
                    )
                    AutomationActionButton(
                        label = "RECENTS",
                        icon = Icons.Default.Layers,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.pressRecents() }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutomationActionButton(
                        label = "SCROLL DOWN",
                        icon = Icons.Default.ArrowDownward,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.scrollDown() }
                    )
                    AutomationActionButton(
                        label = "SCROLL UP",
                        icon = Icons.Default.ArrowUpward,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.scrollUp() }
                    )
                    AutomationActionButton(
                        label = "LOCK SCREEN",
                        icon = Icons.Default.Lock,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.accessibilityController.lockScreen() }
                    )
                }
            }
        }

        // 3. Automated Smart Protocols
        item {
            Text(
                text = "SMART AUTOMATION PROTOCOLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmartProtocolCard(
                    title = "NIGHT PROTOCOL",
                    description = "Dim brightness to 10%, set media volume to mute, and silence ringers.",
                    icon = Icons.Default.Nightlight,
                    onExecute = {
                        viewModel.processUserInput("Night mode protocol: lower brightness and mute volume")
                    }
                )
                SmartProtocolCard(
                    title = "FOCUS MODE",
                    description = "Silence alerts, close entertainment apps, set volume to 30%, ready work environment.",
                    icon = Icons.Default.VolumeMute,
                    onExecute = {
                        viewModel.processUserInput("Focus mode: silence notifications and lower volume")
                    }
                )
                SmartProtocolCard(
                    title = "BATTERY CONSERVATION",
                    description = "Inspect power state, dim display, disable heavy background routines.",
                    icon = Icons.Default.Power,
                    onExecute = {
                        viewModel.processUserInput("Battery saver protocol: check battery and optimize power")
                    }
                )
            }
        }

        // 4. Live Telemetry & Node Inspection Stream
        item {
            Text(
                text = "REAL-TIME AUTOMATION TELEMETRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
        }

        if (devLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TitonoxTokens.SurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No automation events dispatched yet. Trigger an action above to see real-time execution logs.",
                        fontSize = 11.sp,
                        color = TitonoxTokens.TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(devLogs.take(10)) { log ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TitonoxTokens.SurfaceElevated,
                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${log.intent} • ${log.tool}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TitonoxTokens.AccentPrimary
                            )
                            Text(
                                text = "${log.latencyMs}ms",
                                fontSize = 10.sp,
                                color = TitonoxTokens.AccentSecondary
                            )
                        }
                        Text(
                            text = log.result,
                            fontSize = 10.sp,
                            color = TitonoxTokens.TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
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
fun AutomationActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = TitonoxTokens.SurfaceElevated,
        border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
        }
    }
}

@Composable
fun SmartProtocolCard(
    title: String,
    description: String,
    icon: ImageVector,
    onExecute: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(TitonoxTokens.AccentSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = TitonoxTokens.AccentSecondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                    Text(description, fontSize = 10.sp, color = TitonoxTokens.TextSecondary)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onExecute,
                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("RUN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.Background)
            }
        }
    }
}
