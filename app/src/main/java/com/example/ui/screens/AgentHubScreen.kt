package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.StepStatus
import com.example.ai.TaskState
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.TitonoxTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AgentHubScreen(
    viewModel: TitonoxViewModel
) {
    var goalInput by remember { mutableStateOf("") }
    val currentPlan by viewModel.currentPlan.collectAsState()
    val taskState by viewModel.taskState.collectAsState()
    val activeConfirmation by viewModel.activeConfirmation.collectAsState()
    val recentTasks by viewModel.recentTasks.collectAsState()

    val quickGoals = listOf(
        "Open YouTube and search Android Compose",
        "Check battery level and report diagnostics",
        "Take screenshot and analyze active UI",
        "Set volume to 75% and test audio feedback"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Mission Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TitonoxTokens.AccentPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "Agent Engine",
                                    tint = TitonoxTokens.AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AUTONOMOUS AGENT ENGINE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Multi-Step Planner • Gate Verifier • Safe Retry",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TitonoxTokens.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // State pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (taskState) {
                                TaskState.EXECUTING -> TitonoxTokens.StateExecuting.copy(alpha = 0.2f)
                                TaskState.COMPLETED -> TitonoxTokens.StateSuccess.copy(alpha = 0.2f)
                                TaskState.FAILED -> TitonoxTokens.StateError.copy(alpha = 0.2f)
                                TaskState.CONFIRMING, TaskState.CLARIFYING -> TitonoxTokens.StateThinking.copy(alpha = 0.2f)
                                else -> TitonoxTokens.SurfaceHighlight
                            },
                            border = BorderStroke(
                                1.dp,
                                when (taskState) {
                                    TaskState.EXECUTING -> TitonoxTokens.StateExecuting
                                    TaskState.COMPLETED -> TitonoxTokens.StateSuccess
                                    TaskState.FAILED -> TitonoxTokens.StateError
                                    else -> TitonoxTokens.BorderSubtle
                                }
                            )
                        ) {
                            Text(
                                text = taskState.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (taskState) {
                                    TaskState.EXECUTING -> TitonoxTokens.StateExecuting
                                    TaskState.COMPLETED -> TitonoxTokens.StateSuccess
                                    TaskState.FAILED -> TitonoxTokens.StateError
                                    else -> TitonoxTokens.TextPrimary
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Goal Input
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("What objective should TITONOX achieve?") },
                        placeholder = { Text("e.g. Open YouTube and search lo-fi beats") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (goalInput.isNotBlank()) {
                                        viewModel.processUserInput(goalInput)
                                        goalInput = ""
                                    }
                                },
                                modifier = Modifier.testTag("submit_agent_goal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit Goal",
                                    tint = TitonoxTokens.AccentPrimary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TitonoxTokens.AccentPrimary,
                            unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                            focusedTextColor = TitonoxTokens.TextPrimary,
                            unfocusedTextColor = TitonoxTokens.TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("agent_goal_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickGoals) { g ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                                modifier = Modifier.clickable {
                                    goalInput = g
                                }
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 10.sp,
                                    color = TitonoxTokens.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Execution Plan Card
        currentPlan?.let { plan ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.AccentPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.taskTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TitonoxTokens.AccentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Target App: ${plan.targetApp} • Step ${plan.currentStepIndex + 1} of ${plan.steps.size}",
                                    fontSize = 11.sp,
                                    color = TitonoxTokens.TextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (taskState == TaskState.EXECUTING) {
                                    IconButton(onClick = { viewModel.pauseTask() }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = TitonoxTokens.StateThinking)
                                    }
                                } else if (taskState == TaskState.PAUSED) {
                                    IconButton(onClick = { viewModel.resumeTask() }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = TitonoxTokens.StateSuccess)
                                    }
                                }
                                IconButton(onClick = { viewModel.retryFailedStep() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Retry Step", tint = TitonoxTokens.AccentPrimary)
                                }
                                IconButton(onClick = { viewModel.cancelTask() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Stop, contentDescription = "Cancel Task", tint = TitonoxTokens.StateError)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Steps List
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            plan.steps.forEachIndexed { idx, step ->
                                val isCurrent = idx == plan.currentStepIndex
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) TitonoxTokens.AccentPrimary.copy(alpha = 0.15f) else TitonoxTokens.Surface,
                                    border = BorderStroke(1.dp, if (isCurrent) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (step.status) {
                                                        StepStatus.SUCCESS -> TitonoxTokens.StateSuccess.copy(alpha = 0.2f)
                                                        StepStatus.EXECUTING -> TitonoxTokens.StateExecuting.copy(alpha = 0.2f)
                                                        StepStatus.FAILED -> TitonoxTokens.StateError.copy(alpha = 0.2f)
                                                        else -> TitonoxTokens.SurfaceHighlight
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (step.status) {
                                                StepStatus.SUCCESS -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TitonoxTokens.StateSuccess, modifier = Modifier.size(14.dp))
                                                StepStatus.EXECUTING -> CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TitonoxTokens.AccentPrimary)
                                                StepStatus.FAILED -> Icon(Icons.Default.Warning, contentDescription = null, tint = TitonoxTokens.StateError, modifier = Modifier.size(14.dp))
                                                else -> Text("${idx + 1}", fontSize = 10.sp, color = TitonoxTokens.TextSecondary, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${step.action} -> ${step.target}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TitonoxTokens.TextPrimary
                                            )
                                            Text(
                                                text = step.description,
                                                fontSize = 11.sp,
                                                color = TitonoxTokens.TextSecondary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TitonoxTokens.SurfaceHighlight
                                        ) {
                                            Text(
                                                text = step.status.name,
                                                fontSize = 9.sp,
                                                color = TitonoxTokens.TextSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Pending Confirmation Gate Card (if sensitive action waiting for authorization)
        activeConfirmation?.let { confirm ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.StateWarning),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = TitonoxTokens.StateWarning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURITY APPROVAL REQUIRED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TitonoxTokens.StateWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(confirm.actionTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TitonoxTokens.TextPrimary)
                        Text(confirm.actionDetail, fontSize = 11.sp, color = TitonoxTokens.TextSecondary, modifier = Modifier.padding(top = 2.dp))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.submitConfirmationDecision(false) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("DECLINE", color = TitonoxTokens.StateError, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.submitConfirmationDecision(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.StateSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("APPROVE", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. Recent Autonomous Tasks History
        item {
            Text(
                text = "RECENT TASK EXECUTION AUDIT (${recentTasks.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
        }

        if (recentTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TitonoxTokens.SurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No prior tasks logged. Launch a command above to observe agent planning.",
                        fontSize = 11.sp,
                        color = TitonoxTokens.TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentTasks) { task ->
                val dateStr = SimpleDateFormat("HH:mm • MMM d", Locale.getDefault()).format(Date(task.timestamp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.command,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TitonoxTokens.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (task.state == "COMPLETED") TitonoxTokens.StateSuccess.copy(alpha = 0.2f) else TitonoxTokens.SurfaceHighlight
                            ) {
                                Text(
                                    text = task.state,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.state == "COMPLETED") TitonoxTokens.StateSuccess else TitonoxTokens.TextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = task.summary,
                            fontSize = 11.sp,
                            color = TitonoxTokens.TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = dateStr,
                            fontSize = 9.sp,
                            color = TitonoxTokens.TextTertiary,
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
