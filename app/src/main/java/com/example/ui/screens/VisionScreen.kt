package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.automation.ScreenSummary
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateFailed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VisionScreen(
    isScreenCaptureActive: Boolean,
    isAccessibilityActive: Boolean,
    screenSummary: ScreenSummary?,
    visionAnalysis: String?,
    onStartScreenShare: () -> Unit,
    onStopScreenShare: () -> Unit,
    onReadScreenTree: () -> Unit,
    onAnalyzeScreenVision: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Screen Sharing Controls & Security
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberCardSurface)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SCREEN VISION CONTROLS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = ElectricCyan
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isScreenCaptureActive) StateCompleted else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isScreenCaptureActive) "STREAMING" else "IDLE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isScreenCaptureActive) StateCompleted else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "TITONOX uses official Android MediaProjection screen capture. Explicit permission is required. Screen capture is never active in secret.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isScreenCaptureActive) {
                            Button(
                                onClick = onStartScreenShare,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("start_screen_share_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ScreenShare, contentDescription = null, tint = CyberBlack, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("START SCREEN SHARING", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = onStopScreenShare,
                                colors = ButtonDefaults.buttonColors(containerColor = StateFailed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("stop_screen_share_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.StopScreenShare, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("STOP SCREEN SHARING", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Hybrid Inspection Actions
        item {
            Text(
                text = "HYBRID ANALYSIS TOOLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = NeonBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Accessibility Tree
                Button(
                    onClick = onReadScreenTree,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                    border = BorderStroke(1.dp, NeonBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FindInPage, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("READ TREE", color = NeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Gemini Vision AI
                Button(
                    onClick = onAnalyzeScreenVision,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNavySurface),
                    border = BorderStroke(1.dp, ElectricCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI VISION", color = ElectricCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 3: Live Accessibility Hierarchy Summary
        if (screenSummary != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, CyberGlassBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ACCESSIBILITY HIERARCHY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NeonBlue
                            )
                            Text(
                                text = "${screenSummary.totalNodes} Nodes",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foreground App: ${screenSummary.packageName}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detected Buttons (${screenSummary.buttonLabels.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (screenSummary.buttonLabels.isNotEmpty()) screenSummary.buttonLabels.joinToString(" • ") else "No clickable buttons found",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Visible Text Elements (${screenSummary.textElements.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (screenSummary.textElements.isNotEmpty()) screenSummary.textElements.take(8).joinToString("\n- ") else "No text found",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section 4: Gemini Multimodal Vision AI Output
        if (visionAnalysis != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "GEMINI MULTIMODAL VISION DECODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = visionAnalysis,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
