package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.TitonoxTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemoryScreen(
    viewModel: TitonoxViewModel
) {
    val memories by viewModel.memories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "UserPreference", "Identity", "Habit", "Fact", "Custom")

    val filteredMemories = memories.filter { mem ->
        val matchesCategory = selectedCategory == "All" || mem.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                mem.key.contains(searchQuery, ignoreCase = true) ||
                mem.value.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Card
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
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Memory Hub",
                                    tint = TitonoxTokens.AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LONG-TERM AI MEMORY",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Persistent Room DB • Injected into active context",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TitonoxTokens.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_memory_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = TitonoxTokens.Background)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD MEMORY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.Background)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TitonoxTokens.Surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Saved memories are automatically retrieved and injected into the AI system prompt across all providers (Gemini, OpenAI, Claude, DeepSeek, Groq).",
                            fontSize = 11.sp,
                            color = TitonoxTokens.TextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // 2. Search & Category Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search memories...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TitonoxTokens.TextSecondary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TitonoxTokens.AccentPrimary,
                    unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                    focusedTextColor = TitonoxTokens.TextPrimary,
                    unfocusedTextColor = TitonoxTokens.TextPrimary
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TitonoxTokens.AccentPrimary.copy(alpha = 0.25f),
                            selectedLabelColor = TitonoxTokens.AccentPrimary
                        )
                    )
                }
            }
        }

        // 3. Memory Items List
        if (filteredMemories.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TitonoxTokens.SurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (memories.isEmpty()) "No memories saved yet." else "No memories match your filter.",
                            color = TitonoxTokens.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Add preferences like 'Favorite Music: Synthwave' or 'User Name: Aditya' to personalize your JARVIS OS.",
                            color = TitonoxTokens.TextTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredMemories) { mem ->
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(mem.timestamp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TitonoxTokens.AccentPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = mem.category,
                                        fontSize = 9.sp,
                                        color = TitonoxTokens.AccentPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = mem.key,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TitonoxTokens.TextPrimary
                                )
                            }
                            Text(
                                text = mem.value,
                                fontSize = 12.sp,
                                color = TitonoxTokens.TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Saved $dateStr",
                                fontSize = 9.sp,
                                color = TitonoxTokens.TextTertiary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteMemory(mem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Memory",
                                tint = TitonoxTokens.StateError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showAddDialog) {
        var key by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("UserPreference") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("ADD MEMORY ITEM", fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentPrimary)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("Fact / Key (e.g. Creator Name, Favorite Coffee)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Memory Content / Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (UserPreference, Identity, Fact)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (key.isNotBlank() && value.isNotBlank()) {
                            viewModel.addMemory(key, value, category)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary)
                ) {
                    Text("SAVE MEMORY", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
