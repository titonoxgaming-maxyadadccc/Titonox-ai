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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ai.api.AIConfig
import com.example.ai.api.APIManager
import com.example.ai.api.AIProviderType
import com.example.ai.api.ApiStatus
import com.example.ai.api.ConnectionTestResult
import com.example.ai.api.SUPPORTED_GEMINI_MODELS
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitonoxApiSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val apiManager = remember { APIManager.getInstance(context) }
    val currentConfig by apiManager.config.collectAsState()
    val apiStatus by apiManager.apiStatus.collectAsState()

    val scope = rememberCoroutineScope()

    var apiKeyInput by remember(currentConfig.apiKey) { mutableStateOf(currentConfig.apiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember(currentConfig.model) { mutableStateOf(currentConfig.model) }
    var temperature by remember(currentConfig.temperature) { mutableFloatStateOf(currentConfig.temperature) }
    var maxTokens by remember(currentConfig.maxOutputTokens) { mutableIntStateOf(currentConfig.maxOutputTokens) }
    var systemInstructionInput by remember(currentConfig.systemInstruction) { mutableStateOf(currentConfig.systemInstruction) }
    var streamingEnabled by remember(currentConfig.streamingEnabled) { mutableStateOf(currentConfig.streamingEnabled) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ConnectionTestResult?>(null) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI & API Configuration", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Configure Google Gemini & Real AI Core", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("api_settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF030D1B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF020914)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07182E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = when (apiStatus) {
                            ApiStatus.CONNECTED -> Color(0xFF00E676)
                            ApiStatus.CONNECTING -> Color(0xFFFFD600)
                            ApiStatus.NOT_CONFIGURED -> Color(0xFFFF9100)
                            ApiStatus.OFFLINE -> Color(0xFFB0BEC5)
                            ApiStatus.ERROR -> Color(0xFFFF1744)
                        }
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("API Status", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                text = apiStatus.label,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTestingConnection = true
                                testResult = null
                                saveSuccessMessage = null
                                val testConfig = AIConfig(
                                    apiKey = apiKeyInput.trim(),
                                    model = selectedModel,
                                    temperature = temperature,
                                    maxOutputTokens = maxTokens,
                                    systemInstruction = systemInstructionInput,
                                    streamingEnabled = streamingEnabled
                                )
                                testResult = apiManager.verifyActiveConnection()
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection && apiKeyInput.isNotBlank(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        modifier = Modifier.testTag("quick_test_connection_btn")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF00E5FF),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Test Result Banner
            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { res ->
                    val bgColor = if (res.success) Color(0xFF0B2E1E) else Color(0xFF2E0F14)
                    val borderColor = if (res.success) Color(0xFF00E676) else Color(0xFFFF1744)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = borderColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (res.success) "Verification Passed" else "Connection Failed",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = res.message,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Save Confirmation Banner
            AnimatedVisibility(visible = saveSuccessMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF09291D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = saveSuccessMessage ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. AI Provider Section
            Text("AI PROVIDER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07182E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Google Gemini API", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text("Primary provider for reasoning, tools & voice", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            // 2. API Key Configuration
            Text("API CREDENTIALS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07182E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gemini API Key", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text("Keys are stored securely and never transmitted elsewhere.", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input"),
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        placeholder = { Text("AIzaSy...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF00E5FF))
                        },
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    // Helper: Load from BuildConfig if available
                    val buildConfigKey = BuildConfig.GEMINI_API_KEY
                    if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY" && apiKeyInput != buildConfigKey) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { apiKeyInput = buildConfigKey },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Use API Key from AI Studio Secrets", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Model Selector
            Text("MODEL SELECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SUPPORTED_GEMINI_MODELS.forEach { modelOpt ->
                    val isSelected = selectedModel == modelOpt.modelId
                    val borderColor = if (isSelected) Color(0xFF00E5FF) else Color.Transparent
                    val bgColor = if (isSelected) Color(0xFF0D2845) else Color(0xFF07182E)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bgColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedModel = modelOpt.modelId }
                            .testTag("model_opt_${modelOpt.modelId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = modelOpt.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = modelOpt.description,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Generation Hyperparameters
            Text("HYPERPARAMETERS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07182E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Temperature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Temperature", fontSize = 13.sp, color = Color.White)
                        Text(String.format("%.2f", temperature), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..1.5f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Max Tokens Chips
                    Text("Max Output Tokens", fontSize = 13.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(512, 1024, 2048, 4096).forEach { tokens ->
                            FilterChip(
                                selected = maxTokens == tokens,
                                onClick = { maxTokens = tokens },
                                label = { Text("$tokens") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    selectedLabelColor = Color.Black,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Streaming Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Streaming Responses", fontSize = 13.sp, color = Color.White)
                            Text("Output stream generated tokens progressively", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = streamingEnabled,
                            onCheckedChange = { streamingEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                        )
                    }
                }
            }

            // 5. System Instruction
            Text("SYSTEM INSTRUCTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07182E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = systemInstructionInput,
                        onValueChange = { systemInstructionInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("system_instruction_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        apiKeyInput = BuildConfig.GEMINI_API_KEY.takeIf { it != "MY_GEMINI_API_KEY" } ?: ""
                        selectedModel = "gemini-2.0-flash"
                        temperature = 0.7f
                        maxTokens = 2048
                        systemInstructionInput = AIConfig.DEFAULT_SYSTEM_INSTRUCTION
                        streamingEnabled = true
                        testResult = null
                        saveSuccessMessage = "Reset to defaults."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_api_defaults_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val newConfig = AIConfig(
                            apiKey = apiKeyInput.trim(),
                            model = selectedModel,
                            temperature = temperature,
                            maxOutputTokens = maxTokens,
                            systemInstruction = systemInstructionInput,
                            streamingEnabled = streamingEnabled
                        )
                        apiManager.updateConfig(newConfig)
                        saveSuccessMessage = "Configuration saved successfully!"
                        testResult = null
                    },
                    modifier = Modifier
                        .weight(2f)
                        .testTag("save_api_settings_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
                ) {
                    Text("Save Configuration", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
