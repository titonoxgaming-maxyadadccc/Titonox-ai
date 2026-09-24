package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.AccessibilityController
import com.example.ui.TitonoxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    viewModel: TitonoxViewModel,
    onFinishSetup: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityController = remember { AccessibilityController(context) }
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 10

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            currentStep = 3
        }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        currentStep = 5
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TITONOX JARVIS Setup ($currentStep/$totalSteps)",
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Step", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF060B14)
                )
            )
        },
        containerColor = Color(0xFF060B14)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Linear Step Indicator
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF00E5FF),
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> WizardStepWelcome()
                    2 -> WizardStepMicrophone(onGrant = {
                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    })
                    3 -> WizardStepAccessibility(
                        isConfigured = accessibilityController.isServiceConfigured(),
                        onOpenSettings = {
                            accessibilityController.openAccessibilitySettings()
                        }
                    )
                    4 -> WizardStepNotifications(onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            currentStep = 5
                        }
                    })
                    5 -> WizardStepOverlay(onOpenSettings = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    })
                    6 -> WizardStepAiProvider(viewModel = viewModel)
                    7 -> WizardStepTestMic(viewModel = viewModel)
                    8 -> WizardStepTestVoice(viewModel = viewModel)
                    9 -> WizardStepTestApp(context = context)
                    10 -> WizardStepFinish()
                    else -> WizardStepWelcome()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < totalSteps) {
                    TextButton(onClick = { onFinishSetup() }) {
                        Text("Skip Setup", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier.testTag("wizard_next_btn")
                    ) {
                        Text("Next", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF060B14))
                    }
                } else {
                    Button(
                        onClick = {
                            val prefs = context.getSharedPreferences("titonox_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("setup_completed", true).apply()
                            onFinishSetup()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("wizard_finish_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF060B14))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter Command Center", color = Color(0xFF060B14), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepContainer(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF3B82F6))))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun WizardStepWelcome() {
    WizardStepContainer(
        icon = Icons.Default.SmartToy,
        title = "Welcome to TITONOX JARVIS",
        subtitle = "Your high-reliability voice AI assistant with real Android system automation and contextual planning."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("• Zero simulated placeholders — real actions only", color = Color(0xFF38BDF8), fontSize = 12.sp)
            Text("• High-risk authorization gate protects calls & messages", color = Color(0xFF38BDF8), fontSize = 12.sp)
            Text("• Instant emergency stop on \"Ruko\", \"Stop\", \"Cancel\"", color = Color(0xFF38BDF8), fontSize = 12.sp)
        }
    }
}

@Composable
fun WizardStepMicrophone(onGrant: () -> Unit) {
    WizardStepContainer(
        icon = Icons.Default.Mic,
        title = "Microphone Access",
        subtitle = "Required for low-latency hands-free natural voice command listening."
    ) {
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("grant_mic_btn")
        ) {
            Text("Grant Microphone Permission", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepAccessibility(isConfigured: Boolean, onOpenSettings: () -> Unit) {
    WizardStepContainer(
        icon = Icons.Default.Accessibility,
        title = "Accessibility Automation",
        subtitle = "Enables TITONOX to safely read screen elements, click search buttons, and execute multi-step automation."
    ) {
        if (isConfigured) {
            Text("✓ Accessibility Service is Active", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
        } else {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth().testTag("open_accessibility_btn")
            ) {
                Text("Open Accessibility Settings", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WizardStepNotifications(onGrant: () -> Unit) {
    WizardStepContainer(
        icon = Icons.Default.Notifications,
        title = "Notifications Permission",
        subtitle = "Allows TITONOX to post task progress, verification alerts, and background status."
    ) {
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("grant_notif_btn")
        ) {
            Text("Allow Notifications", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepOverlay(onOpenSettings: () -> Unit) {
    WizardStepContainer(
        icon = Icons.Default.Layers,
        title = "Floating Orb Overlay",
        subtitle = "Display the interactive 3D JARVIS orb over any app for instant access."
    ) {
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("open_overlay_btn")
        ) {
            Text("Enable Floating Overlay", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepAiProvider(viewModel: TitonoxViewModel) {
    WizardStepContainer(
        icon = Icons.Default.Psychology,
        title = "AI Intelligence Engine",
        subtitle = "Powered by Google Gemini 2.5 Flash for rapid planning and reasoning."
    ) {
        Text("Active Engine: Google Gemini 2.5 Flash", color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Local Fast Command Engine is always ready offline.", color = Color(0xFF94A3B8), fontSize = 12.sp)
    }
}

@Composable
fun WizardStepTestMic(viewModel: TitonoxViewModel) {
    var testing by remember { mutableStateOf(false) }
    WizardStepContainer(
        icon = Icons.Default.GraphicEq,
        title = "Test Microphone",
        subtitle = "Press test and speak any phrase to verify speech recognition calibration."
    ) {
        Button(
            onClick = {
                testing = !testing
                if (testing) viewModel.voiceManager.startListening() else viewModel.voiceManager.stopListening()
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (testing) Color(0xFFEF4444) else Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("test_mic_btn")
        ) {
            Text(if (testing) "Listening... Tap to Stop" else "Start Microphone Test", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepTestVoice(viewModel: TitonoxViewModel) {
    WizardStepContainer(
        icon = Icons.Default.VolumeUp,
        title = "Test Voice Response",
        subtitle = "Verify audio speech synthesizer output."
    ) {
        Button(
            onClick = {
                viewModel.speakText("All systems operational, Sir. TITONOX JARVIS is ready.")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("test_voice_btn")
        ) {
            Text("Play Voice Response Test", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepTestApp(context: Context) {
    WizardStepContainer(
        icon = Icons.Default.Launch,
        title = "Test App Automation",
        subtitle = "Verify explicit package resolver and launcher."
    ) {
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth().testTag("test_app_btn")
        ) {
            Text("Test Launch Settings App", color = Color(0xFF060B14), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WizardStepFinish() {
    WizardStepContainer(
        icon = Icons.Default.CheckCircle,
        title = "System Setup Complete",
        subtitle = "TITONOX JARVIS is now primed and calibrated for high-precision autonomous operation."
    ) {
        Text("Say \"Hey TITONOX\" or tap the 3D Orb to begin.", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
    }
}
