package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ExecutionPlan
import com.example.ai.StepStatus
import com.example.ai.TaskState
import com.example.data.NoteEntity
import com.example.data.TodoEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCancelled
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.StateExecuting
import com.example.ui.theme.StateFailed
import com.example.ui.theme.StateVerifying
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TasksScreen(
    currentPlan: ExecutionPlan?,
    taskState: TaskState,
    notes: List<NoteEntity>,
    todos: List<TodoEntity>,
    onCancelTask: () -> Unit,
    onExecuteTemplate: (String) -> Unit,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (TodoEntity) -> Unit,
    onDeleteTodo: (Long) -> Unit
) {
    var newTodoText by remember { mutableStateOf("") }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Active Multi-Step Execution Engine
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
                        Column {
                            Text(
                                text = "TASK EXECUTION ENGINE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                                color = ElectricCyan
                            )
                            Text(
                                text = "Status: ${taskState.name}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when (taskState) {
                                    TaskState.EXECUTING -> StateExecuting
                                    TaskState.VERIFYING -> StateVerifying
                                    TaskState.COMPLETED -> StateCompleted
                                    TaskState.FAILED -> StateFailed
                                    TaskState.CANCELLED -> StateCancelled
                                    else -> TextSecondary
                                }
                            )
                        }

                        if (taskState == TaskState.EXECUTING || taskState == TaskState.PLANNING) {
                            Button(
                                onClick = onCancelTask,
                                colors = ButtonDefaults.buttonColors(containerColor = StateFailed),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("cancel_multi_step_button")
                            ) {
                                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("STOP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentPlan != null && currentPlan.steps.isNotEmpty()) {
                        Text(
                            text = "Query: \"${currentPlan.userQuery}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentPlan.steps.forEach { step ->
                                val stepColor = when (step.status) {
                                    StepStatus.PENDING -> TextSecondary.copy(alpha = 0.5f)
                                    StepStatus.EXECUTING -> StateExecuting
                                    StepStatus.VERIFYING -> StateVerifying
                                    StepStatus.SUCCESS -> StateCompleted
                                    StepStatus.FAILED -> StateFailed
                                    StepStatus.SKIPPED -> Color.Gray
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(stepColor.copy(alpha = 0.1f))
                                        .border(1.dp, stepColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(stepColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${step.stepNumber}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = stepColor,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = step.description,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        if (step.verificationResult != null) {
                                            Text(
                                                text = step.verificationResult!!,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = stepColor
                                            )
                                        }
                                    }

                                    Text(
                                        text = step.status.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = stepColor
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No active multi-step task running. Speak a multi-step instruction or test a template below.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section 2: Automation Templates
        item {
            Text(
                text = "AUTOMATION TEMPLATES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = NeonBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            val templates = listOf(
                "Open YouTube, search Minecraft survival and play first video",
                "Turn flashlight on, set volume to 50 percent, and check battery",
                "Read my screen and explain what you see"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                templates.forEach { tmpl ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberGlassBorder, RoundedCornerShape(10.dp))
                            .clickable { onExecuteTemplate(tmpl) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tmpl,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Todos & Task Management
        item {
            Text(
                text = "TODOS & ACTION ITEMS (${todos.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                color = NeonBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Add Todo Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTodoText,
                    onValueChange = { newTodoText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add new task...", fontSize = 12.sp, color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = CyberGlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTodoText.isNotBlank()) {
                            onAddTodo(newTodoText)
                            newTodoText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ADD", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                todos.forEach { todo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberGlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = todo.isCompleted,
                            onCheckedChange = { onToggleTodo(todo) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = StateCompleted,
                                uncheckedColor = ElectricCyan
                            )
                        )
                        Text(
                            text = todo.task,
                            fontSize = 13.sp,
                            color = if (todo.isCompleted) TextSecondary else TextPrimary,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteTodo(todo.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section 4: Productivity Notes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOICE NOTES (${notes.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = NeonBlue
                )
                Text(
                    text = if (showAddNoteDialog) "CLOSE" else "+ NEW NOTE",
                    fontSize = 11.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAddNoteDialog = !showAddNoteDialog }
                )
            }

            if (showAddNoteDialog) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Note Title", fontSize = 12.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CyberGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Note Content...", fontSize = 12.sp, color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = CyberGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 4
                    )
                    Button(
                        onClick = {
                            if (newNoteContent.isNotBlank()) {
                                onAddNote(newNoteTitle.ifBlank { "Voice Note" }, newNoteContent)
                                newNoteTitle = ""
                                newNoteContent = ""
                                showAddNoteDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE NOTE", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notes.forEach { note ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberGlassBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                                IconButton(
                                    onClick = { onDeleteNote(note.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.content,
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
