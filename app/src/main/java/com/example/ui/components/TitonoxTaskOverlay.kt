package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ClarificationPrompt
import com.example.ai.ConfirmationPrompt
import com.example.ai.ContactRecord
import com.example.ai.DisambiguationPrompt
import com.example.ai.ExecutionPlan
import com.example.ai.StepStatus
import com.example.ai.TaskState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TitonoxTaskOverlayHUD(
    plan: ExecutionPlan?,
    taskState: TaskState,
    clarification: ClarificationPrompt?,
    disambiguation: DisambiguationPrompt?,
    confirmation: ConfirmationPrompt?,
    isScreenHeldAwake: Boolean,
    screenAwakeWarning: String?,
    onClarificationAnswer: (String) -> Unit,
    onSelectContact: (ContactRecord) -> Unit,
    onConfirmAction: (Boolean) -> Unit,
    onPauseTask: () -> Unit,
    onResumeTask: () -> Unit,
    onRetryStep: () -> Unit,
    onCancelTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = plan != null && taskState != TaskState.IDLE ||
            clarification != null ||
            disambiguation != null ||
            confirmation != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFF00E5FF))
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            color = Color(0xEE090D16),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header Bar: Task Title, Target App, State badge & Screen Awake Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (taskState) {
                                        TaskState.EXECUTING -> Color(0xFF00E5FF)
                                        TaskState.VERIFYING -> Color(0xFFFFD600)
                                        TaskState.PAUSED -> Color(0xFFFF9100)
                                        TaskState.CLARIFYING -> Color(0xFFFF4081)
                                        TaskState.CONFIRMING -> Color(0xFF00E676)
                                        TaskState.COMPLETED -> Color(0xFF00E676)
                                        TaskState.FAILED -> Color(0xFFFF1744)
                                        else -> Color(0xFF00E5FF)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = plan?.taskTitle ?: "TITONOX ACTIVE TASK",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "App: ${plan?.targetApp ?: "Autonomous Engine"} • ${taskState.name}",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Screen Awake Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isScreenHeldAwake) Color(0xFF00E676) else Color(0xFFFF9100))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isScreenHeldAwake) "AWAKE" else "NORMAL",
                            color = if (isScreenHeldAwake) Color(0xFF00E676) else Color(0xFFFF9100),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onCancelTask,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Task",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Screen Wake warning banner if restricted
                if (screenAwakeWarning != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FF9100))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = screenAwakeWarning,
                            color = Color(0xFFFFCC80),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                // Clarification Prompt Section
                if (clarification != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FF4081))
                            .border(1.dp, Color(0x66FF4081), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF4081),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLARIFICATION REQUIRED",
                                color = Color(0xFFFF4081),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = clarification.question,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (clarification.options.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                clarification.options.forEach { option ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF1E2840))
                                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                            .clickable { onClarificationAnswer(option) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = option,
                                            color = Color(0xFFE0F7FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Disambiguation Prompt Section
                if (disambiguation != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300B0FF))
                            .border(1.dp, Color(0x6600B0FF), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = disambiguation.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        disambiguation.contacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161F30))
                                    .clickable { onSelectContact(contact) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(contact.avatarColor),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = contact.phoneNumber,
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Call",
                                        color = Color(0xFF00E5FF),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Sensitive Action Confirmation Banner
                if (confirmation != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x3300E676))
                            .border(1.dp, Color(0x6600E676), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = confirmation.actionTitle,
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = confirmation.actionDetail,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { onConfirmAction(false) },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text(confirmation.cancelButtonText, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onConfirmAction(true) },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                            ) {
                                Text(confirmation.confirmButtonText, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Steps Progress & Status Checklist
                if (plan != null && plan.steps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    val completedCount = plan.steps.count { it.status == StepStatus.SUCCESS }
                    val progressRatio = (completedCount.toFloat() / plan.steps.size.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0xFF1E2840)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display Current and Recent Steps
                    plan.steps.forEachIndexed { idx, step ->
                        val isCurrent = idx == plan.currentStepIndex
                        val isSuccess = step.status == StepStatus.SUCCESS
                        val isVerifying = step.status == StepStatus.VERIFYING
                        val isFailed = step.status == StepStatus.FAILED

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    isSuccess -> "✓"
                                    isVerifying -> "●"
                                    isCurrent -> "⏳"
                                    isFailed -> "✗"
                                    else -> "○"
                                },
                                color = when {
                                    isSuccess -> Color(0xFF00E676)
                                    isVerifying -> Color(0xFFFFD600)
                                    isCurrent -> Color(0xFF00E5FF)
                                    isFailed -> Color(0xFFFF1744)
                                    else -> Color.Gray
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(20.dp)
                            )

                            Text(
                                text = "${step.stepNumber}. ${step.description}",
                                color = when {
                                    isCurrent -> Color.White
                                    isSuccess -> Color.LightGray.copy(alpha = 0.8f)
                                    isFailed -> Color(0xFFFF8A80)
                                    else -> Color.Gray
                                },
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (step.verificationResult != null && (isCurrent || isFailed)) {
                                Text(
                                    text = if (isFailed) "Failed" else "Verified",
                                    color = if (isFailed) Color(0xFFFF1744) else Color(0xFF00E676),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Task Controls: Pause, Resume, Retry, Stop
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step ${plan.currentStepIndex + 1} of ${plan.steps.size}",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row {
                            if (taskState == TaskState.EXECUTING) {
                                OutlinedButton(
                                    onClick = onPauseTask,
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Pause ⏸", fontSize = 10.sp, color = Color.White)
                                }
                            } else if (taskState == TaskState.PAUSED) {
                                Button(
                                    onClick = onResumeTask,
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (taskState == TaskState.FAILED) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = onRetryStep,
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = onCancelTask,
                                modifier = Modifier.height(28.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Stop ⏹", fontSize = 10.sp, color = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        }
    }
}
