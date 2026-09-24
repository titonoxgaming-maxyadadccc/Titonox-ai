package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import com.example.ui.components.TitonoxTaskOverlayHUD
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.floating.FloatingOrbService
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.TitonoxFloatingDock
import com.example.ui.screens.TitonoxApiSettingsScreen
import com.example.ui.screens.TitonoxControlScreen
import com.example.ui.screens.TitonoxCoreScreen
import com.example.ui.screens.TitonoxOrbStudioScreen
import com.example.ui.screens.TitonoxPlayerScreen
import com.example.ui.screens.TitonoxStudioScreen
import com.example.ui.screens.TitonoxVisionScreen
import androidx.compose.material.icons.filled.Key
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.TitonoxTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: TitonoxViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (!recordAudioGranted) {
            Toast.makeText(this, "Microphone permission is recommended for TITONOX voice commands.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkInitialPermissions()

        if (intent.getBooleanExtra("OPEN_ORB_STUDIO", false)) {
            viewModel.selectInterface(TitonoxInterface.ORB_STUDIO)
        }

        setContent {
            TitonoxTheme {
                val isScreenHeldAwake by viewModel.isScreenHeldAwake.collectAsState()
                val taskState by viewModel.taskState.collectAsState()

                LaunchedEffect(isScreenHeldAwake, taskState) {
                    if (isScreenHeldAwake || taskState == com.example.ai.TaskState.EXECUTING) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                TitonoxEcosystemApp(
                    viewModel = viewModel,
                    onOpenUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    private fun checkInitialPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val needsRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(needsRequest.toTypedArray())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("OPEN_ORB_STUDIO", false)) {
            viewModel.selectInterface(TitonoxInterface.ORB_STUDIO)
        }
    }

    override fun onStart() {
        super.onStart()
        // User entered TITONOX app -> Hide floating orb
        FloatingOrbService.setAppForegroundState(true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDeviceStatus()
        FloatingOrbService.setAppForegroundState(true)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // User left TITONOX app -> Show floating orb outside TITONOX
        FloatingOrbService.setAppForegroundState(false)
        if (android.provider.Settings.canDrawOverlays(this)) {
            FloatingOrbService.startService(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitonoxEcosystemApp(
    viewModel: TitonoxViewModel,
    onOpenUrl: (String) -> Unit
) {
    val currentInterface by viewModel.currentInterface.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val devModeEnabled by viewModel.devModeEnabled.collectAsState()
    val devLogs by viewModel.devLogs.collectAsState()

    val isAccessibilityActive = viewModel.accessibilityController.isServiceActive()
    val isMuted by viewModel.voiceManager.isMuted.collectAsState()
    val isScreenCapturing by viewModel.screenCaptureManager.isScreenCaptureActive.collectAsState()

    val currentPlan by viewModel.currentPlan.collectAsState()
    val taskState by viewModel.taskState.collectAsState()
    val activeClarification by viewModel.activeClarification.collectAsState()
    val activeDisambiguation by viewModel.activeDisambiguation.collectAsState()
    val activeConfirmation by viewModel.activeConfirmation.collectAsState()
    val isScreenHeldAwake by viewModel.isScreenHeldAwake.collectAsState()
    val screenAwakeWarning by viewModel.screenAwakeWarning.collectAsState()
    val isSetupWizardOpen by viewModel.isSetupWizardOpen.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = CyberBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // High-Tech Top Status Bar strictly respecting status bar and notch/cutout
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = DarkNavy.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Identity & Channel badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onOpenUrl("https://youtube.com/@TITONOXOFFICIAL")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(StateCompleted)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TITONOX",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = ElectricCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@TITONOXOFFICIAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = HoloWhite.copy(alpha = 0.5f),
                                fontSize = 9.sp
                            )
                        )
                    }

                    // System Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Accessibility Service Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAccessibilityActive) StateCompleted.copy(alpha = 0.2f) else Color(0xFFFF9800).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAccessibilityActive) StateCompleted else Color(0xFFFF9800)
                            ),
                            modifier = Modifier.clickable {
                                viewModel.accessibilityController.openAccessibilitySettings()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessibilityNew,
                                    contentDescription = "Accessibility",
                                    tint = if (isAccessibilityActive) StateCompleted else Color(0xFFFF9800),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAccessibilityActive) "AUTO ON" else "ENABLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isAccessibilityActive) StateCompleted else Color(0xFFFF9800),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Mute Toggle Icon
                        IconButton(
                            onClick = { viewModel.voiceManager.toggleMute() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Mute Voice",
                                tint = if (isMuted) Color(0xFFFF5252) else ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // AI & API Settings Button
                        IconButton(
                            onClick = { viewModel.selectInterface(TitonoxInterface.API_SETTINGS) },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("topbar_api_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "AI & API Settings",
                                tint = if (currentInterface == TitonoxInterface.API_SETTINGS) ElectricCyan else HoloWhite.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Emergency STOP TopBar Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier
                                .clickable { viewModel.triggerEmergencyStop() }
                                .testTag("topbar_emergency_stop_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5252))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "STOP",
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 6.dp)
            ) {
                TitonoxFloatingDock(
                    currentInterface = currentInterface,
                    onSelectInterface = { target -> viewModel.selectInterface(target) },
                    onOpenSearch = { viewModel.openSearch() },
                    onToggleDevLogs = { viewModel.toggleDevMode() }
                )
            }
        }
    ) { paddingValues ->
        com.example.ui.components.FuturisticBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Autonomous Task Status HUD & Interactive Confirmation / Clarification Overlay
                TitonoxTaskOverlayHUD(
                    plan = currentPlan,
                    taskState = taskState,
                    clarification = activeClarification,
                    disambiguation = activeDisambiguation,
                    confirmation = activeConfirmation,
                    isScreenHeldAwake = isScreenHeldAwake,
                    screenAwakeWarning = screenAwakeWarning,
                    onClarificationAnswer = { viewModel.submitClarificationResponse(it) },
                    onSelectContact = { viewModel.selectDisambiguationContact(it) },
                    onConfirmAction = { viewModel.submitConfirmationDecision(it) },
                    onPauseTask = { viewModel.pauseTask() },
                    onResumeTask = { viewModel.resumeTask() },
                    onRetryStep = { viewModel.retryFailedStep() },
                    onCancelTask = { viewModel.cancelTask() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(10f)
                )

                // Dynamic Screen Switching across the 6 interfaces
                Crossfade(
                    targetState = currentInterface,
                    label = "InterfaceCrossfade"
                ) { target ->
                    when (target) {
                        TitonoxInterface.CORE -> {
                            TitonoxCoreScreen(
                                viewModel = viewModel,
                                onOpenUrl = onOpenUrl
                            )
                        }
                    TitonoxInterface.CONTROL -> {
                        TitonoxControlScreen(viewModel = viewModel)
                    }
                    TitonoxInterface.VISION -> {
                        TitonoxVisionScreen(viewModel = viewModel)
                    }
                    TitonoxInterface.STUDIO -> {
                        TitonoxStudioScreen(viewModel = viewModel)
                    }
                    TitonoxInterface.PLAYER -> {
                        TitonoxPlayerScreen(viewModel = viewModel)
                    }
                    TitonoxInterface.ORB_STUDIO -> {
                        TitonoxOrbStudioScreen(viewModel = viewModel)
                    }
                    TitonoxInterface.API_SETTINGS -> {
                        TitonoxApiSettingsScreen(
                            onNavigateBack = { viewModel.selectInterface(TitonoxInterface.CORE) }
                        )
                    }
                }
            }

            // Universal Global Search Dialog
            if (isSearchOpen) {
                GlobalSearchDialog(
                    query = searchQuery,
                    results = searchResults,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onResultSelected = { result ->
                        viewModel.closeSearch()
                        when (result.type) {
                            "SONG" -> viewModel.selectInterface(TitonoxInterface.PLAYER)
                            else -> viewModel.selectInterface(TitonoxInterface.STUDIO)
                        }
                    },
                    onDismiss = { viewModel.closeSearch() }
                )
            }

            // Developer Telemetry Console Bottom Sheet
            if (devModeEnabled) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleDevMode() },
                    sheetState = sheetState,
                    containerColor = DarkNavy,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = ElectricCyan) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TITONOX TELEMETRY & DECISION ENGINE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            IconButton(
                                onClick = { viewModel.toggleDevMode() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = HoloWhite)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (devLogs.isEmpty()) {
                            Text(
                                text = "No execution events logged yet. Speak or type a command to observe real-time decision routing.",
                                style = MaterialTheme.typography.bodySmall.copy(color = HoloWhite.copy(alpha = 0.5f))
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(devLogs) { log ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF02132B),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${log.intent} • ${log.tool}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = ElectricCyan,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                Text(
                                                    text = "${log.latencyMs}ms",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = NeonBlue
                                                    )
                                                )
                                            }
                                            Text(
                                                text = log.result,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = HoloWhite.copy(alpha = 0.8f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // First-Launch Onboarding & Setup Wizard (Can also be re-triggered anytime from Settings)
            androidx.compose.animation.AnimatedVisibility(
                visible = isSetupWizardOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(99f)
            ) {
                com.example.ui.screens.SetupWizardScreen(
                    viewModel = viewModel,
                    onFinishSetup = { viewModel.closeSetupWizard() }
                )
            }
        }
    }
}
}
