package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.StopScreenShare
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.TaskState
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateExecuting
import com.example.ui.theme.StateListening

@Composable
fun TitonoxVisionScreen(
    viewModel: TitonoxViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val taskState by viewModel.taskState.collectAsState()
    val isCapturing by viewModel.screenCaptureManager.isScreenCaptureActive.collectAsState()
    val screenSummary by viewModel.screenSummary.collectAsState()
    val visionAnalysis by viewModel.visionAnalysisResult.collectAsState()
    val latestBitmap = viewModel.screenCaptureManager.latestBitmap

    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserAnim"
    )

    // Screen Capture MediaProjection launcher
    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.screenCaptureManager.startCapture(result.resultCode, result.data!!)
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        Color(0xFF030D1E),
                        CyberDarkNavy
                    )
                )
            )
            .testTag("titonox_vision_screen")
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
                        text = "TITONOX VISION",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = "NEURAL HUD • SCREEN OPTICS & OCR INSPECTOR",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonBlue.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkNavy.copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isCapturing) StateCompleted else Color(0xFFFF9800))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCapturing) "STREAM ACTIVE" else "STANDBY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. CYBER VIEWPORT / HUD SCANNER
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF010814)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (latestBitmap != null) {
                        Image(
                            bitmap = latestBitmap.asImageBitmap(),
                            contentDescription = "Screen Capture Viewport",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Futuristic Radar Grid
                        Text(
                            text = if (isCapturing) "ACQUIRING SCREEN FRAME..." else "START SCREEN SHARING TO ACTIVATE LIVE OPTICS",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonBlue.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    // Cyber HUD Overlay Lines and Scanning Laser
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Corner Target Reticles
                        val reticleLen = 24.dp.toPx()
                        val pad = 12.dp.toPx()

                        // Top-Left
                        drawLine(ElectricCyan, Offset(pad, pad), Offset(pad + reticleLen, pad), 2f)
                        drawLine(ElectricCyan, Offset(pad, pad), Offset(pad, pad + reticleLen), 2f)

                        // Top-Right
                        drawLine(ElectricCyan, Offset(w - pad, pad), Offset(w - pad - reticleLen, pad), 2f)
                        drawLine(ElectricCyan, Offset(w - pad, pad), Offset(w - pad, pad + reticleLen), 2f)

                        // Bottom-Left
                        drawLine(ElectricCyan, Offset(pad, h - pad), Offset(pad + reticleLen, h - pad), 2f)
                        drawLine(ElectricCyan, Offset(pad, h - pad), Offset(pad, h - pad - reticleLen), 2f)

                        // Bottom-Right
                        drawLine(ElectricCyan, Offset(w - pad, h - pad), Offset(w - pad - reticleLen, h - pad), 2f)
                        drawLine(ElectricCyan, Offset(w - pad, h - pad), Offset(w - pad, h - pad - reticleLen), 2f)

                        // Animated Sweeping Laser Line
                        val currentY = h * laserYRatio
                        drawLine(
                            color = ElectricCyan.copy(alpha = 0.85f),
                            start = Offset(0f, currentY),
                            end = Offset(w, currentY),
                            strokeWidth = 2.5f
                        )
                    }

                    // Center HUD Status Tag
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkNavy.copy(alpha = 0.8f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "TITONOX VISION v2.4",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (isCapturing) {
                            viewModel.screenCaptureManager.stopCapture()
                        } else {
                            val mgr = context.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                            projectionLauncher.launch(mgr.createScreenCaptureIntent())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCapturing) Color(0xFFC62828) else NeonBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isCapturing) Icons.AutoMirrored.Filled.StopScreenShare else Icons.AutoMirrored.Filled.ScreenShare,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isCapturing) "Stop Sharing" else "Start Sharing")
                }

                Button(
                    onClick = { viewModel.analyzeScreenVision() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = CyberBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Analyze Vision", color = CyberBlack, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Accessibility Screen Read Button
            Button(
                onClick = { viewModel.readCurrentScreen() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Scan Active Screen UI Tree (Buttons & Text)", color = HoloWhite)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. VISION REASONING ANALYSIS RESULT
            if (visionAnalysis != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "GEMINI MULTIMODAL VISION REASONING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = visionAnalysis ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = HoloWhite,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. UI ELEMENT TREE & ACCESSIBILITY INSPECTOR
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
                            text = "ACCESSIBILITY UI TREE & ACTION DISPATCH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (screenSummary != null) {
                        val summary = screenSummary!!
                        Text(
                            text = "Target App: ${summary.packageName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HoloWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DETECTED INTERACTIVE BUTTONS (${summary.buttonLabels.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue, fontSize = 10.sp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        summary.buttonLabels.forEach { label ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.accessibilityController.clickElement(label)
                                    },
                                color = Color(0xFF031633),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall.copy(color = HoloWhite)
                                    )
                                    Text(
                                        text = "DISPATCH CLICK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ElectricCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "DETECTED TEXT CONTENT (${summary.textElements.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        summary.textElements.take(8).forEach { txt ->
                            Text(
                                text = "• $txt",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = HoloWhite.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Tap 'Scan Active Screen UI Tree' to inspect all clickable buttons, input fields, and texts on whatever application is currently visible.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HoloWhite.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
