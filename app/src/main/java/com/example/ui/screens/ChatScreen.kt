package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ai.api.AIProviderType
import com.example.ui.TitonoxInterface
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.StateCompleted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TitonoxTokens

@Composable
fun ChatScreen(
    viewModel: TitonoxViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val activeProvider by viewModel.apiManager.activeProviderName.collectAsState()
    val activeModel by viewModel.apiManager.activeModelName.collectAsState()
    val context = LocalContext.current

    val quickModels = listOf(
        AIProviderType.AUTO,
        AIProviderType.GEMINI,
        AIProviderType.OPENAI,
        AIProviderType.ANTHROPIC,
        AIProviderType.DEEPSEEK,
        AIProviderType.GROQ
    )

    ChatScreenContent(
        messages = messages,
        isListening = isListening,
        activeProvider = activeProvider,
        activeModel = activeModel,
        onSendMessage = { viewModel.processUserInput(it) },
        onSpeakMessage = { viewModel.speakText(it) },
        onToggleListen = { viewModel.toggleListening() },
        onSelectProvider = { viewModel.switchProvider(it) },
        onOpenModels = { viewModel.selectInterface(TitonoxInterface.MODELS) },
        onCopyText = { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("TITONOX AI", text))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isListening: Boolean,
    onSendMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onToggleListen: () -> Unit
) {
    ChatScreenContent(
        messages = messages,
        isListening = isListening,
        activeProvider = "Google Gemini",
        activeModel = "gemini-2.5-flash",
        onSendMessage = onSendMessage,
        onSpeakMessage = onSpeakMessage,
        onToggleListen = onToggleListen,
        onSelectProvider = {},
        onOpenModels = {},
        onCopyText = {}
    )
}

@Composable
fun ChatScreenContent(
    messages: List<ChatMessage>,
    isListening: Boolean,
    activeProvider: String,
    activeModel: String,
    onSendMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onToggleListen: () -> Unit,
    onSelectProvider: (AIProviderType) -> Unit,
    onOpenModels: () -> Unit,
    onCopyText: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        // Top Provider & Model Indicator Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TitonoxTokens.SurfaceElevated,
            border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenModels)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Switch Model",
                        tint = TitonoxTokens.AccentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = activeProvider,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TitonoxTokens.TextPrimary
                        )
                        Text(
                            text = activeModel,
                            fontSize = 9.sp,
                            color = TitonoxTokens.TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TitonoxTokens.AccentPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.clickable(onClick = onOpenModels)
                ) {
                    Text(
                        text = "CHANGE MODEL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TitonoxTokens.AccentPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Message list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                )
                            )
                            .background(if (isUser) CyberNavySurface else CyberCardSurface)
                            .border(
                                1.dp,
                                if (isUser) NeonBlue.copy(alpha = 0.5f) else CyberGlassBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUser) "COMMANDER" else "TITONOX",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isUser) NeonBlue else ElectricCyan,
                                    letterSpacing = 1.sp
                                )

                                if (msg.actionBadge != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ElectricCyan.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = msg.actionBadge,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = StateCompleted,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )

                            if (!isUser) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { onCopyText(msg.text) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onSpeakMessage(msg.text) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Read aloud",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleListen,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isListening) ElectricCyan else CyberCardSurface)
                    .border(1.dp, ElectricCyan, CircleShape)
                    .testTag("chat_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (isListening) CyberBlack else ElectricCyan
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input"),
                placeholder = {
                    Text(
                        text = "Command TITONOX...",
                        fontSize = 13.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CyberGlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ElectricCyan
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = CyberBlack
                )
            }
        }
    }
}
