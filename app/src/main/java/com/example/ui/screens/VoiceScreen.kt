package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ExecutionPlan
import com.example.ai.TaskState
import com.example.ui.components.AiCoreOrb
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCancelled
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateExecuting
import com.example.ui.theme.StateFailed
import com.example.ui.theme.StateListening
import com.example.ui.theme.StatePlanning
import com.example.ui.theme.StateUnderstanding
import com.example.ui.theme.StateVerifying
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VoiceScreen(
    taskState: TaskState,
    isListening: Boolean,
    isSpeaking: Boolean,
    audioRms: Float,
    latestReply: String?,
    currentPlan: ExecutionPlan?,
    onToggleListen: () -> Unit,
    onStopTask: () -> Unit,
    onQuickCommand: (String) -> Unit
) {
    val stateText = when (taskState) {
        TaskState.IDLE -> if (isListening) "Listening..." else "AI CORE READY"
        TaskState.LISTENING -> "Listening..."
        TaskState.UNDERSTANDING -> "Understanding Intent..."
        TaskState.CLARIFYING -> "Clarifying Details..."
        TaskState.CONFIRMING -> "Awaiting Confirmation..."
        TaskState.PLANNING -> "Planning Steps..."
        TaskState.EXECUTING -> "Executing Action..."
        TaskState.VERIFYING -> "Verifying Result..."
        TaskState.PAUSED -> "Task Paused"
        TaskState.COMPLETED -> "Completed"
        TaskState.FAILED -> "Action Failed"
        TaskState.CANCELLED -> "Operation Cancelled"
    }

    val stateColor = when (taskState) {
        TaskState.IDLE -> if (isListening) StateListening else ElectricCyan
        TaskState.LISTENING -> StateListening
        TaskState.UNDERSTANDING -> StateUnderstanding
        TaskState.CLARIFYING -> StateUnderstanding
        TaskState.CONFIRMING -> StateExecuting
        TaskState.PLANNING -> StatePlanning
        TaskState.EXECUTING -> StateExecuting
        TaskState.VERIFYING -> StateVerifying
        TaskState.PAUSED -> StateCancelled
        TaskState.COMPLETED -> StateCompleted
        TaskState.FAILED -> StateFailed
        TaskState.CANCELLED -> StateCancelled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TITONOX",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 6.sp,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "[ AI CORE ]",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp,
                color = NeonBlue
            )
        }

        // Center: Futuristic Orb & Live State
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            AiCoreOrb(
                taskState = taskState,
                isListening = isListening,
                isSpeaking = isSpeaking,
                audioRms = audioRms,
                sizeDp = 220.dp,
                onClick = onToggleListen
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Current State badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(stateColor.copy(alpha = 0.15f))
                    .border(1.dp, stateColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stateText.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = stateColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Spoken prompt / response text
            Text(
                text = latestReply ?: "\"How can I help?\"",
                fontSize = 17.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                maxLines = 3
            )
        }

        // Active multi-step progress bar if running
        AnimatedVisibility(visible = currentPlan != null && taskState == TaskState.EXECUTING) {
            val step = currentPlan?.steps?.getOrNull(currentPlan.currentStepIndex)
            if (step != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, StateExecuting.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STEP ${step.stepNumber}/${currentPlan.steps.size}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = step.action.uppercase(),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Bottom Controls: Microphone Toggle, Stop Button, & Fast Vocal Prompts
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Quick Commands Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Open YouTube",
                    "Read my screen",
                    "Turn flashlight on",
                    "Battery percentage",
                    "Volume down",
                    "Open Instagram",
                    "YouTube kholo",
                    "Who are you?"
                )
                suggestions.forEach { cmd ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { onQuickCommand(cmd) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cmd,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Mic & Cancel Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Mic Orb Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isListening) StateListening else ElectricCyan,
                                    NeonBlue,
                                    CyberBlack
                                )
                            )
                        )
                        .border(2.dp, ElectricCyan, CircleShape)
                        .clickable { onToggleListen() }
                        .testTag("voice_listen_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = CyberBlack,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // If running or listening, show Abort/Stop button
                if (taskState == TaskState.EXECUTING || taskState == TaskState.PLANNING || isListening) {
                    Spacer(modifier = Modifier.width(20.dp))
                    IconButton(
                        onClick = onStopTask,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(StateFailed.copy(alpha = 0.2f))
                            .border(1.5.dp, StateFailed, CircleShape)
                            .testTag("stop_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "Stop Task",
                            tint = StateFailed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
