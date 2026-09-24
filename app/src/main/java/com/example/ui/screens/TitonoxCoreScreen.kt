package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ai.TaskState
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.ui.components.AIStatusBadge
import com.example.ui.components.AiCoreOrb
import com.example.ui.components.CommandCenterSheet
import com.example.ui.components.EmergencyStopButton
import com.example.ui.components.LuxuryChatMessage
import com.example.ui.components.LuxurySurface
import com.example.ui.components.TaskProgressHUD
import com.example.ui.theme.TitonoxTokens

/**
 * TITONOX CORE — ULTRA PREMIUM HERO EXPERIENCE
 * Cinematic AI operating system command center:
 * - Top status & telemetry bar
 * - Central 3D/2D layered dynamic energy core
 * - Real-time AI state readout & multi-step execution pipeline
 * - Latest AI answer card with speech & copy actions
 * - Fast gesture chips
 * - Bottom voice/text bar with emergency stop, respecting all WindowInsets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitonoxCoreScreen(
    viewModel: TitonoxViewModel,
    onOpenUrl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val taskState by viewModel.taskState.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val audioRms by viewModel.voiceManager.audioRms.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentPlan by viewModel.taskEngine.currentPlan.collectAsState()
    val licenseInfo by viewModel.licenseInfo.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showCommandCenter by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }
    var inputLicenseKey by remember { mutableStateOf("") }
    var licenseMessage by remember { mutableStateOf<String?>(null) }

    val quickChips = listOf(
        "Open YouTube and search Minecraft",
        "Take a screenshot",
        "Orb ghumao",
        "3D orb lagao",
        "Orb ko blue karo",
        "Open music",
        "Show device controls",
        "Who created you?",
        "Stop everything"
    )

    // Find the latest AI message and user message for the hero view
    val latestAiMessage = messages.lastOrNull { it.role != "user" }
    val latestStepDescription = currentPlan?.steps?.firstOrNull { !it.isCompleted }?.description

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("titonox_core_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP STATUS & BRANDING BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TITONOX Title & Subtitle
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TITONOX",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = TitonoxTokens.TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JARVIS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = TitonoxTokens.AccentPrimary
                            )
                        )
                    }
                    Text(
                        text = "AI AGENT OS • COMMERCIAL ED.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TitonoxTokens.TextMuted,
                            letterSpacing = 1.sp,
                            fontSize = 9.sp
                        )
                    )
                }

                // Controls: License Chip, State Badge, Emergency Stop, Command Center Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Commercial License Badge
                    LuxurySurface(
                        shape = TitonoxTokens.RadiusPill,
                        backgroundColor = if (licenseInfo.status == com.example.license.LicenseStatus.LICENSED) {
                            TitonoxTokens.StateSuccess.copy(alpha = 0.15f)
                        } else {
                            TitonoxTokens.AccentGold.copy(alpha = 0.15f)
                        },
                        borderColor = if (licenseInfo.status == com.example.license.LicenseStatus.LICENSED) {
                            TitonoxTokens.StateSuccess.copy(alpha = 0.4f)
                        } else {
                            TitonoxTokens.AccentGold.copy(alpha = 0.4f)
                        },
                        onClick = { showLicenseDialog = true }
                    ) {
                        Text(
                            text = if (licenseInfo.status == com.example.license.LicenseStatus.LICENSED) "PRO" else "TRIAL ${licenseInfo.trialDaysRemaining}d",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (licenseInfo.status == com.example.license.LicenseStatus.LICENSED) TitonoxTokens.StateSuccess else TitonoxTokens.AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // AI State Badge
                    AIStatusBadge(
                        state = taskState,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        onClick = {
                            if (isListening) viewModel.voiceManager.stopListening()
                            else viewModel.voiceManager.startListening()
                        }
                    )

                    // Emergency Stop Action
                    EmergencyStopButton(
                        compact = true,
                        onEmergencyStop = { viewModel.triggerEmergencyStop() }
                    )

                    // Command Center Hub Button
                    IconButton(
                        onClick = { showCommandCenter = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TitonoxTokens.SurfaceHighlight)
                            .testTag("core_command_center_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Command Center",
                            tint = TitonoxTokens.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. SCROLLABLE HERO BODY AREA (Orb, Status Readout, Response, Task Progress)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Central Premium AI Reactor Orb
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AiCoreOrb(
                        taskState = taskState,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        audioRms = audioRms,
                        sizeDp = 210.dp,
                        onClick = {
                            if (isListening) {
                                viewModel.voiceManager.stopListening()
                            } else {
                                viewModel.voiceManager.startListening()
                            }
                        },
                        onLongPress = {
                            viewModel.selectInterface(TitonoxInterface.ORB_STUDIO)
                        }
                    )
                }

                // AI Status Headline
                val statusHeadline = when {
                    isListening -> "LISTENING TO VOICE..."
                    isSpeaking -> "TITONOX VOCALIZING"
                    taskState == TaskState.PLANNING -> "FORMULATING ACTION PLAN"
                    taskState == TaskState.UNDERSTANDING -> "ANALYZING INTENT..."
                    taskState == TaskState.EXECUTING -> "EXECUTING AUTOMATION..."
                    taskState == TaskState.VERIFYING -> "VERIFYING RESULT..."
                    taskState == TaskState.COMPLETED -> "TASK COMPLETED"
                    taskState == TaskState.FAILED -> "ACTION INTERRUPTED"
                    else -> "SYSTEM READY"
                }

                Text(
                    text = statusHeadline,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = when {
                            isListening -> TitonoxTokens.StateListening
                            taskState == TaskState.EXECUTING -> TitonoxTokens.StateExecuting
                            taskState == TaskState.COMPLETED -> TitonoxTokens.StateSuccess
                            taskState == TaskState.FAILED -> TitonoxTokens.StateError
                            else -> TitonoxTokens.AccentPrimary
                        },
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                )

                // Subtitle readout
                val statusDetail = when {
                    isListening -> "Speak naturally — no commands to memorize"
                    isSpeaking -> "Tap anywhere or say 'Stop' to interrupt"
                    taskState == TaskState.EXECUTING -> latestStepDescription ?: "Interacting with target app..."
                    taskState == TaskState.COMPLETED -> "Ready for your next instruction"
                    taskState == TaskState.FAILED -> "Safe fallback activated"
                    else -> "Tap Orb or Mic to initiate voice command"
                }

                Text(
                    text = statusDetail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TitonoxTokens.TextSecondary,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )

                // Multi-Step Task Pipeline Progress HUD (Shown whenever a multi-step task runs)
                AnimatedVisibility(
                    visible = currentPlan != null && currentPlan?.steps?.isNotEmpty() == true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val steps = currentPlan?.steps ?: emptyList()
                    val total = steps.size
                    val completed = steps.count { it.isCompleted }

                    Box(modifier = Modifier.padding(top = 10.dp)) {
                        TaskProgressHUD(
                            state = taskState,
                            currentStepDesc = latestStepDescription,
                            totalSteps = total,
                            currentStepIndex = completed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Latest AI Response Card (Luxury formatted)
                if (latestAiMessage != null) {
                    LuxuryChatMessage(
                        message = latestAiMessage,
                        onSpeak = { text -> viewModel.voiceManager.speak(text) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Action Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickChips) { chip ->
                        QuickSuggestionChip(
                            text = chip,
                            onClick = { viewModel.processUserInput(chip) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // View Conversation Log Pill Button
                if (messages.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LuxurySurface(
                            shape = TitonoxTokens.RadiusPill,
                            backgroundColor = TitonoxTokens.SurfaceElevated,
                            borderColor = TitonoxTokens.BorderSubtle,
                            onClick = { showHistorySheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Conversation Log",
                                    tint = TitonoxTokens.AccentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CONVERSATION LOG (${messages.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. BOTTOM VOICE & TEXT INPUT BAR
            // Safe insets: adds space for bottom floating dock & system navigation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(bottom = 68.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clean pill input field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                "Command TITONOX via voice or text...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TitonoxTokens.TextMuted,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("core_text_input"),
                        shape = TitonoxTokens.RadiusPill,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TitonoxTokens.AccentPrimary,
                            unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                            focusedTextColor = TitonoxTokens.TextPrimary,
                            unfocusedTextColor = TitonoxTokens.TextPrimary,
                            focusedContainerColor = TitonoxTokens.SurfaceElevated,
                            unfocusedContainerColor = TitonoxTokens.Surface
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (textInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val query = textInput
                                        textInput = ""
                                        viewModel.processUserInput(query)
                                    },
                                    modifier = Modifier.testTag("core_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = TitonoxTokens.AccentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Voice Mic Button (With tactile spring scale)
                    val micInteraction = remember { MutableInteractionSource() }
                    val isMicPressed by micInteraction.collectIsPressedAsState()
                    val micScale = if (isMicPressed) 0.92f else 1.0f

                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(micScale)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = micInteraction,
                                indication = null,
                                onClick = {
                                    if (isListening) {
                                        viewModel.voiceManager.stopListening()
                                    } else {
                                        viewModel.voiceManager.startListening()
                                    }
                                }
                            )
                            .testTag("core_mic_button"),
                        shape = CircleShape,
                        color = if (isListening) TitonoxTokens.StateListening else TitonoxTokens.AccentPrimary,
                        border = BorderStroke(1.dp, if (isListening) TitonoxTokens.StateListening else TitonoxTokens.AccentPrimary)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = TitonoxTokens.Background,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Persistent Emergency Stop Button
                    Surface(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.triggerEmergencyStop() }
                            .testTag("core_emergency_stop_button"),
                        shape = CircleShape,
                        color = TitonoxTokens.StateError.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, TitonoxTokens.StateError)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Emergency Stop",
                                tint = TitonoxTokens.StateError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. COMMAND CENTER BOTTOM SHEET
        CommandCenterSheet(
            isOpen = showCommandCenter,
            onDismiss = { showCommandCenter = false },
            onSelectInterface = { target ->
                showCommandCenter = false
                viewModel.selectInterface(target)
            },
            onOpenSetupWizard = {
                showCommandCenter = false
                viewModel.openSetupWizard()
            },
            onOpenSocial = { url ->
                showCommandCenter = false
                onOpenUrl(url)
            }
        )

        // 5. CONVERSATION LOG MODAL BOTTOM SHEET
        if (showHistorySheet) {
            val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = historySheetState,
                containerColor = TitonoxTokens.SurfaceElevated,
                contentColor = TitonoxTokens.TextPrimary,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CONVERSATION HISTORY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TitonoxTokens.AccentPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "${messages.size} MESSAGES IN ACTIVE SESSION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TitonoxTokens.TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                        IconButton(onClick = { showHistorySheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TitonoxTokens.TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            LuxuryChatMessage(
                                message = msg,
                                onSpeak = { text -> viewModel.voiceManager.speak(text) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // 6. COMMERCIAL EDITION LICENSE DIALOG
        if (showLicenseDialog) {
            AlertDialog(
                onDismissRequest = { showLicenseDialog = false },
                containerColor = TitonoxTokens.SurfaceElevated,
                title = {
                    Text(
                        "TITONOX Commercial License",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TitonoxTokens.AccentPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Edition: ${licenseInfo.tierName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TitonoxTokens.TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Commercial Price: ${licenseInfo.commercialPriceDisplay}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TitonoxTokens.StateSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Hardware Fingerprint: ${licenseInfo.deviceIdHash}...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TitonoxTokens.TextSecondary,
                                fontSize = 11.sp
                            )
                        )

                        if (licenseInfo.status != com.example.license.LicenseStatus.LICENSED) {
                            OutlinedTextField(
                                value = inputLicenseKey,
                                onValueChange = { inputLicenseKey = it },
                                placeholder = { Text("TITONOX-JARVIS-XXXX-XXXX-XXXX", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TitonoxTokens.AccentPrimary,
                                    unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                                    focusedTextColor = TitonoxTokens.TextPrimary,
                                    unfocusedTextColor = TitonoxTokens.TextPrimary
                                )
                            )

                            if (licenseMessage != null) {
                                Text(
                                    text = licenseMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (licenseMessage!!.startsWith("✓")) TitonoxTokens.StateSuccess else TitonoxTokens.StateError
                                    )
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                showLicenseDialog = false
                                viewModel.openSetupWizard()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, TitonoxTokens.AccentPrimary)
                        ) {
                            Text("Re-run Setup Wizard", color = TitonoxTokens.AccentPrimary)
                        }
                    }
                },
                confirmButton = {
                    if (licenseInfo.status != com.example.license.LicenseStatus.LICENSED) {
                        Button(
                            onClick = {
                                val ok = viewModel.licenseManager.activateLicense(inputLicenseKey)
                                if (ok) {
                                    licenseMessage = "✓ Commercial license activated successfully!"
                                } else {
                                    licenseMessage = "Invalid key. Format: TITONOX-JARVIS-XXXX-XXXX-XXXX"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary)
                        ) {
                            Text("Activate", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { showLicenseDialog = false }) {
                            Text("Done", color = TitonoxTokens.AccentPrimary)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLicenseDialog = false }) {
                        Text("Close", color = TitonoxTokens.TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickSuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    LuxurySurface(
        shape = TitonoxTokens.RadiusPill,
        backgroundColor = TitonoxTokens.Surface,
        borderColor = TitonoxTokens.BorderSubtle,
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TitonoxTokens.TextPrimary,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
