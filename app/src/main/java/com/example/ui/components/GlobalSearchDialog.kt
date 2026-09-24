package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UniversalSearchResult
import com.example.ui.TitonoxInterface
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HoloWhite
import com.example.ui.theme.NeonBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(
    query: String,
    results: List<UniversalSearchResult>,
    onQueryChanged: (String) -> Unit,
    onResultSelected: (UniversalSearchResult) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("global_search_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkNavy,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TITONOX UNIVERSAL SEARCH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = HoloWhite)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    placeholder = {
                        Text("Search notes, tasks, events, songs...", color = HoloWhite.copy(alpha = 0.4f))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("global_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NeonBlue.copy(alpha = 0.4f),
                        focusedTextColor = HoloWhite,
                        unfocusedTextColor = HoloWhite
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (results.isEmpty() && query.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching results found.", color = HoloWhite.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(results) { item ->
                            val icon = when (item.type) {
                                "NOTE" -> Icons.AutoMirrored.Filled.Notes
                                "TODO" -> Icons.Default.TaskAlt
                                "CALENDAR" -> Icons.Default.Event
                                else -> Icons.Default.MusicNote
                            }
                            val badgeColor = when (item.type) {
                                "NOTE" -> ElectricCyan
                                "TODO" -> NeonBlue
                                "CALENDAR" -> Color(0xFFFFB300)
                                else -> Color(0xFF00E676)
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onResultSelected(item) },
                                color = Color(0xFF031633),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonBlue.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = HoloWhite,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        if (item.snippet.isNotBlank()) {
                                            Text(
                                                text = item.snippet,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = HoloWhite.copy(alpha = 0.6f)
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = badgeColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = item.type,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = badgeColor,
                                                fontSize = 9.sp
                                            ),
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
}
