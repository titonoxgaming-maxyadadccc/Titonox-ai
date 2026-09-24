package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalendarEventEntity
import com.example.data.NoteEntity
import com.example.data.TodoEntity
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberDarkNavy
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StudioTab {
    NOTEPAD,
    TODOS,
    CALENDAR
}

@Composable
fun TitonoxStudioScreen(
    viewModel: TitonoxViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(StudioTab.NOTEPAD) }
    val notes by viewModel.notes.collectAsState()
    val todos by viewModel.todos.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // AI note summarize / rewrite dialog
    var aiResultTitle by remember { mutableStateOf<String?>(null) }
    var aiResultContent by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CyberBlack,
                        Color(0xFF041426),
                        CyberDarkNavy
                    )
                )
            )
            .testTag("titonox_studio_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        text = "TITONOX STUDIO",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = ElectricCyan
                        )
                    )
                    Text(
                        text = "PRODUCTIVITY & INTELLIGENT CREATION SUITE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonBlue.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    )
                }

                IconButton(
                    onClick = {
                        when (currentTab) {
                            StudioTab.NOTEPAD -> showAddNoteDialog = true
                            StudioTab.TODOS -> showAddTodoDialog = true
                            StudioTab.CALENDAR -> showAddEventDialog = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonBlue)
                        .testTag("studio_add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Item",
                        tint = HoloWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Studio Tabs (Notepad, Todos, Calendar)
            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = DarkNavy.copy(alpha = 0.9f),
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                        color = ElectricCyan
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                StudioTab.values().forEach { tab ->
                    Tab(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        text = {
                            Text(
                                text = tab.name,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentTab == tab) ElectricCyan else HoloWhite.copy(alpha = 0.6f)
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 60.dp)
            ) {
                when (currentTab) {
                    StudioTab.NOTEPAD -> NotepadView(
                        notes = notes,
                        onDelete = { viewModel.deleteNote(it) },
                        onTogglePin = { note ->
                            viewModel.updateNote(note.copy(isPinned = !note.isPinned))
                        },
                        onToggleFavorite = { note ->
                            viewModel.updateNote(note.copy(isFavorite = !note.isFavorite))
                        },
                        onSummarize = { note ->
                            isAiLoading = true
                            aiResultTitle = "AI Note Summary: ${note.title}"
                            viewModel.aiSummarizeNote(note) { summary ->
                                isAiLoading = false
                                aiResultContent = summary
                            }
                        },
                        onRewrite = { note ->
                            isAiLoading = true
                            aiResultTitle = "AI Polished Note: ${note.title}"
                            viewModel.aiRewriteNote(note) { rewrite ->
                                isAiLoading = false
                                aiResultContent = rewrite
                            }
                        }
                    )
                    StudioTab.TODOS -> TodosView(
                        todos = todos,
                        onToggle = { viewModel.toggleTodo(it) },
                        onDelete = { viewModel.deleteTodo(it) }
                    )
                    StudioTab.CALENDAR -> CalendarView(
                        events = calendarEvents,
                        onDelete = { viewModel.deleteCalendarEvent(it) }
                    )
                }
            }
        }

        // Add Note Dialog
        if (showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onConfirm = { title, content, tag ->
                    viewModel.addNote(title, content, tag)
                    showAddNoteDialog = false
                }
            )
        }

        // Add Todo Dialog
        if (showAddTodoDialog) {
            AddTodoDialog(
                onDismiss = { showAddTodoDialog = false },
                onConfirm = { task, priority, category ->
                    viewModel.addTodo(task, priority, category)
                    showAddTodoDialog = false
                }
            )
        }

        // Add Event Dialog
        if (showAddEventDialog) {
            AddCalendarEventDialog(
                onDismiss = { showAddEventDialog = false },
                onConfirm = { title, desc, date, time ->
                    viewModel.addCalendarEvent(title, desc, date, time)
                    showAddEventDialog = false
                }
            )
        }

        // AI Result Viewer Dialog
        if (aiResultContent != null || isAiLoading) {
            AlertDialog(
                onDismissRequest = {
                    aiResultContent = null
                    aiResultTitle = null
                    isAiLoading = false
                },
                title = {
                    Text(
                        text = aiResultTitle ?: "AI Reasoning",
                        style = MaterialTheme.typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    if (isAiLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            CircularProgressIndicator(color = ElectricCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("TITONOX AI analyzing note...", color = HoloWhite)
                        }
                    } else {
                        Text(
                            text = aiResultContent ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite, lineHeight = 20.sp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        aiResultContent = null
                        aiResultTitle = null
                    }) {
                        Text("Done", color = ElectricCyan)
                    }
                },
                containerColor = DarkNavy
            )
        }
    }
}

@Composable
fun NotepadView(
    notes: List<NoteEntity>,
    onDelete: (Long) -> Unit,
    onTogglePin: (NoteEntity) -> Unit,
    onToggleFavorite: (NoteEntity) -> Unit,
    onSummarize: (NoteEntity) -> Unit,
    onRewrite: (NoteEntity) -> Unit
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notes created yet. Tap '+' to create your first high-tech note.",
                style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite.copy(alpha = 0.5f))
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (note.isPinned) Color(0xFF031938) else DarkNavy.copy(alpha = 0.85f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (note.isPinned) ElectricCyan else NeonBlue.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (note.isPinned) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = HoloWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onTogglePin(note) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pin Note",
                                        tint = if (note.isPinned) ElectricCyan else HoloWhite.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleFavorite(note) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (note.isFavorite) Color(0xFFFF5252) else HoloWhite.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDelete(note.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = HoloWhite.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = HoloWhite.copy(alpha = 0.85f),
                                lineHeight = 19.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = note.tag,
                                    style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue, fontSize = 9.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // AI Action Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF021733),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable { onSummarize(note) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Summarize", style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan, fontSize = 10.sp))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF021733),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable { onRewrite(note) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("AI Polish", style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue, fontSize = 10.sp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodosView(
    todos: List<TodoEntity>,
    onToggle: (TodoEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (todos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pending tasks. Tap '+' to organize your priorities.",
                style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite.copy(alpha = 0.5f))
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos) { todo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (todo.isCompleted) StateCompleted.copy(alpha = 0.4f) else NeonBlue.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onToggle(todo) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (todo.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle",
                                tint = if (todo.isCompleted) StateCompleted else ElectricCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = todo.task,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (todo.isCompleted) HoloWhite.copy(alpha = 0.5f) else HoloWhite,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (todo.priority == "Urgent") Color(0xFFFF5252).copy(alpha = 0.2f) else NeonBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = todo.priority,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (todo.priority == "Urgent") Color(0xFFFF5252) else NeonBlue,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DarkNavy
                                ) {
                                    Text(
                                        text = todo.category,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = HoloWhite.copy(alpha = 0.6f),
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDelete(todo.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = HoloWhite.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    events: List<CalendarEventEntity>,
    onDelete: (Long) -> Unit
) {
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "TODAY: $todayDate",
                        style = MaterialTheme.typography.titleSmall.copy(color = HoloWhite, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${events.size} scheduled agendas in calendar",
                        style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No calendar events recorded. Tap '+' to schedule an agenda.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = HoloWhite.copy(alpha = 0.5f))
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkNavy.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = HoloWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (ev.description.isNotBlank()) {
                                    Text(
                                        text = ev.description,
                                        style = MaterialTheme.typography.bodySmall.copy(color = HoloWhite.copy(alpha = 0.7f))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "📅 ${ev.dateString}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan)
                                    )
                                    Text(
                                        text = "⏰ ${ev.timeString}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = NeonBlue)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onDelete(ev.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = HoloWhite.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Work") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Note", style = MaterialTheme.typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag (e.g. Work, Ideas, Cyber)", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, tag)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Save Note", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HoloWhite)
            }
        },
        containerColor = DarkNavy
    )
}

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (task: String, priority: String, category: String) -> Unit
) {
    var task by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var category by remember { mutableStateOf("General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Todo Task", style = MaterialTheme.typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = task,
                    onValueChange = { task = it },
                    label = { Text("Task description", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority (Urgent, High, Normal)", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Work, Personal, System)", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (task.isNotBlank()) {
                        onConfirm(task, priority, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Add Task", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HoloWhite)
            }
        },
        containerColor = DarkNavy
    )
}

@Composable
fun AddCalendarEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, date: String, time: String) -> Unit
) {
    val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var time by remember { mutableStateOf("10:00 AM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Schedule Calendar Agenda", style = MaterialTheme.typography.titleMedium.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Location", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 10:00 AM)", color = HoloWhite.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, date, time)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("Schedule Event", color = CyberBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HoloWhite)
            }
        },
        containerColor = DarkNavy
    )
}
