package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChatMessage
import com.example.ai.TaskState
import com.example.ui.TitonoxInterface
import com.example.ui.theme.TitonoxTokens

/**
 * Luxury Surface: A dark, sleek container with controlled borders and depth.
 */
@Composable
fun LuxurySurface(
    modifier: Modifier = Modifier,
    shape: Shape = TitonoxTokens.RadiusLg,
    backgroundColor: Color = TitonoxTokens.Surface,
    borderColor: Color = TitonoxTokens.BorderSubtle,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed && onClick != null) 0.985f else 1.0f

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        content()
    }
}

/**
 * AI State Badge: Visualizes READY, LISTENING, THINKING, EXECUTING, COMPLETED, ERROR.
 */
@Composable
fun AIStatusBadge(
    state: TaskState,
    isListening: Boolean,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (label, stateColor) = when {
        isListening -> "LISTENING" to TitonoxTokens.StateListening
        isSpeaking -> "SPEAKING" to TitonoxTokens.AccentPrimary
        state == TaskState.PLANNING || state == TaskState.UNDERSTANDING -> "THINKING" to TitonoxTokens.StateThinking
        state == TaskState.EXECUTING || state == TaskState.VERIFYING -> "EXECUTING" to TitonoxTokens.StateExecuting
        state == TaskState.COMPLETED -> "COMPLETED" to TitonoxTokens.StateSuccess
        state == TaskState.FAILED -> "ERROR" to TitonoxTokens.StateError
        else -> "READY" to TitonoxTokens.StateIdle
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LuxurySurface(
        modifier = modifier.testTag("ai_status_badge"),
        shape = TitonoxTokens.RadiusPill,
        backgroundColor = stateColor.copy(alpha = 0.12f),
        borderColor = stateColor.copy(alpha = 0.35f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stateColor.copy(alpha = if (isListening || state == TaskState.EXECUTING) alphaPulse else 1f))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = stateColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

/**
 * Emergency STOP Button: Instant cancellation trigger with high tactile feedback.
 */
@Composable
fun EmergencyStopButton(
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.94f else 1.0f

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onEmergencyStop
            )
            .testTag("emergency_stop_button"),
        shape = TitonoxTokens.RadiusPill,
        color = TitonoxTokens.StateError.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, TitonoxTokens.StateError.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 5.dp else 7.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(TitonoxTokens.StateError)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "STOP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TitonoxTokens.StateError,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )
            )
        }
    }
}

/**
 * Task Execution Progress HUD: Clean 5-stage progress indicator.
 */
@Composable
fun TaskProgressHUD(
    state: TaskState,
    currentStepDesc: String? = null,
    totalSteps: Int = 0,
    currentStepIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val stages = listOf("UNDERSTAND", "PLAN", "EXECUTE", "VERIFY", "DONE")
    val activeIndex = when (state) {
        TaskState.UNDERSTANDING -> 0
        TaskState.PLANNING -> 1
        TaskState.EXECUTING -> 2
        TaskState.VERIFYING -> 3
        TaskState.COMPLETED -> 4
        else -> -1
    }

    if (activeIndex >= 0) {
        LuxurySurface(
            modifier = modifier.fillMaxWidth(),
            shape = TitonoxTokens.RadiusMd,
            backgroundColor = TitonoxTokens.SurfaceElevated,
            borderColor = TitonoxTokens.BorderActive
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TASK PIPELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TitonoxTokens.AccentPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    if (totalSteps > 0) {
                        Text(
                            text = "STEP ${currentStepIndex + 1}/$totalSteps",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TitonoxTokens.TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    stages.forEachIndexed { idx, stageName ->
                        val isDone = idx < activeIndex
                        val isCurrent = idx == activeIndex
                        val color = when {
                            isDone -> TitonoxTokens.StateSuccess
                            isCurrent -> TitonoxTokens.AccentPrimary
                            else -> TitonoxTokens.TextMuted.copy(alpha = 0.4f)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = stageName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = color,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        if (idx < stages.size - 1) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.dp)
                                    .padding(horizontal = 4.dp)
                                    .background(if (idx < activeIndex) TitonoxTokens.StateSuccess else TitonoxTokens.BorderSubtle)
                            )
                        }
                    }
                }

                if (!currentStepDesc.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentStepDesc,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TitonoxTokens.TextPrimary,
                            lineHeight = 16.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Chat Message View: Custom luxury message container for TITONOX.
 */
@Composable
fun LuxuryChatMessage(
    message: ChatMessage,
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role.equals("user", ignoreCase = true)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI Assistant Message
            LuxurySurface(
                modifier = Modifier.fillMaxWidth(0.92f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                backgroundColor = TitonoxTokens.SurfaceElevated,
                borderColor = TitonoxTokens.BorderSubtle
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TitonoxTokens.AccentPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TITONOX AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TitonoxTokens.AccentPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("TITONOX Message", message.text))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = TitonoxTokens.TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            IconButton(
                                onClick = { onSpeak(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = TitonoxTokens.TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TitonoxTokens.TextPrimary,
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        } else {
            // User Query Message
            LuxurySurface(
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                backgroundColor = Color(0xFF141A24),
                borderColor = Color(0xFF222C3D)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TitonoxTokens.TextPrimary,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 19.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Expandable AI Command Center Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandCenterSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSelectInterface: (TitonoxInterface) -> Unit,
    onOpenSetupWizard: () -> Unit,
    onOpenSocial: (String) -> Unit
) {
    if (isOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = TitonoxTokens.SurfaceElevated,
            contentColor = TitonoxTokens.TextPrimary,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMAND CENTER",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TitonoxTokens.AccentPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "QUICK SUBSYSTEM CONTROL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TitonoxTokens.TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TitonoxTokens.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Grid (2 columns)
                val items = listOf(
                    CommandCenterAction("Core Hub", "AI Reactor & Assistant", Icons.Default.AutoAwesome, TitonoxInterface.CORE),
                    CommandCenterAction("Device Hardware", "Volumes, Battery, Specs", Icons.Default.PhoneAndroid, TitonoxInterface.CONTROL),
                    CommandCenterAction("Vision AI", "Camera & OCR Analysis", Icons.Default.CameraAlt, TitonoxInterface.VISION),
                    CommandCenterAction("Productivity", "Notes, Calendar & Tasks", Icons.Default.EditCalendar, TitonoxInterface.STUDIO),
                    CommandCenterAction("Audio Player", "Music & Playlists", Icons.Default.MusicNote, TitonoxInterface.PLAYER),
                    CommandCenterAction("Orb Studio", "30 2D/3D Core Themes", Icons.Default.Palette, TitonoxInterface.ORB_STUDIO),
                    CommandCenterAction("AI Engine Config", "API Keys & Models", Icons.Default.Tune, TitonoxInterface.API_SETTINGS)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in items.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CommandCenterButton(
                                item = items[i],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDismiss()
                                    onSelectInterface(items[i].targetInterface)
                                }
                            )

                            if (i + 1 < items.size) {
                                CommandCenterButton(
                                    item = items[i + 1],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        onDismiss()
                                        onSelectInterface(items[i + 1].targetInterface)
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Setup Wizard & Social Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LuxurySurface(
                        modifier = Modifier.weight(1f),
                        shape = TitonoxTokens.RadiusMd,
                        backgroundColor = TitonoxTokens.Surface,
                        borderColor = TitonoxTokens.BorderSubtle,
                        onClick = {
                            onDismiss()
                            onOpenSetupWizard()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Wizard", style = MaterialTheme.typography.labelMedium.copy(color = TitonoxTokens.TextPrimary))
                        }
                    }

                    LuxurySurface(
                        modifier = Modifier.weight(1f),
                        shape = TitonoxTokens.RadiusMd,
                        backgroundColor = TitonoxTokens.Surface,
                        borderColor = TitonoxTokens.BorderSubtle,
                        onClick = {
                            onOpenSocial("https://youtube.com/@TITONOXOFFICIAL")
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = TitonoxTokens.AccentViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Official Channel", style = MaterialTheme.typography.labelMedium.copy(color = TitonoxTokens.TextPrimary))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class CommandCenterAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val targetInterface: TitonoxInterface
)

@Composable
private fun CommandCenterButton(
    item: CommandCenterAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LuxurySurface(
        modifier = modifier,
        shape = TitonoxTokens.RadiusMd,
        backgroundColor = TitonoxTokens.Surface,
        borderColor = TitonoxTokens.BorderSubtle,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(TitonoxTokens.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = TitonoxTokens.AccentPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TitonoxTokens.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TitonoxTokens.TextSecondary,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
