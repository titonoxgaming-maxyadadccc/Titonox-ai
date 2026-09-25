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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import com.example.ai.api.ErrorCategory
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.api.AIConfig
import com.example.ai.api.AIProviderType
import com.example.ai.api.ApiStatus
import com.example.ai.api.ConnectionTestResult
import com.example.ai.api.ProviderModelCatalogs
import com.example.data.CustomModelEntity
import com.example.ui.TitonoxViewModel
import com.example.ui.theme.TitonoxTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelsScreen(
    viewModel: TitonoxViewModel
) {
    val apiManager = viewModel.apiManager
    val currentConfig by apiManager.config.collectAsState()
    val apiStatus by apiManager.apiStatus.collectAsState()
    val activeProviderName by apiManager.activeProviderName.collectAsState()
    val activeModelName by apiManager.activeModelName.collectAsState()
    val latencyMs by apiManager.lastLatencyMs.collectAsState()
    val customModels by viewModel.customModels.collectAsState()
    val lastAutoDecision by apiManager.lastAutoDecision.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val usageStats by apiManager.usageTracker.usageStats.collectAsState()
    var selectedScreenTab by remember { mutableIntStateOf(0) } // 0 = Engine, 1 = Usage & Cost

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var selectedTabProvider by remember(currentConfig.provider) { mutableStateOf(currentConfig.provider) }

    // State for the currently viewed provider card
    var apiKeyInput by remember(selectedTabProvider) {
        mutableStateOf(apiManager.storage.loadKeyForProvider(selectedTabProvider))
    }
    var showApiKey by remember { mutableStateOf(false) }
    var baseUrlInput by remember(selectedTabProvider) {
        mutableStateOf(apiManager.storage.loadBaseUrlForProvider(selectedTabProvider))
    }
    var selectedModel by remember(selectedTabProvider) {
        mutableStateOf(apiManager.storage.loadModelForProvider(selectedTabProvider))
    }
    var temperature by remember { mutableFloatStateOf(currentConfig.temperature) }
    var maxTokens by remember { mutableIntStateOf(currentConfig.maxOutputTokens) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ConnectionTestResult?>(null) }

    val majorProviders = listOf(
        AIProviderType.AUTO,
        AIProviderType.GEMINI,
        AIProviderType.OPENAI,
        AIProviderType.ANTHROPIC,
        AIProviderType.DEEPSEEK,
        AIProviderType.GROQ,
        AIProviderType.OPENROUTER,
        AIProviderType.MISTRAL,
        AIProviderType.XAI,
        AIProviderType.TOGETHER,
        AIProviderType.CUSTOM
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Banner & Active Status Card
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
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = "AI Models Hub",
                                    tint = TitonoxTokens.AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI PROVIDER & MODEL ENGINE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TitonoxTokens.TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Multi-provider orchestration • Real Handshakes",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TitonoxTokens.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Status Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (apiStatus) {
                                ApiStatus.CONNECTED -> TitonoxTokens.StateSuccess.copy(alpha = 0.15f)
                                ApiStatus.CONNECTING -> TitonoxTokens.StateThinking.copy(alpha = 0.15f)
                                ApiStatus.NOT_CONFIGURED -> TitonoxTokens.StateWarning.copy(alpha = 0.15f)
                                else -> TitonoxTokens.StateError.copy(alpha = 0.15f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when (apiStatus) {
                                    ApiStatus.CONNECTED -> TitonoxTokens.StateSuccess
                                    ApiStatus.CONNECTING -> TitonoxTokens.StateThinking
                                    ApiStatus.NOT_CONFIGURED -> TitonoxTokens.StateWarning
                                    else -> TitonoxTokens.StateError
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (apiStatus) {
                                                ApiStatus.CONNECTED -> TitonoxTokens.StateSuccess
                                                ApiStatus.CONNECTING -> TitonoxTokens.StateThinking
                                                ApiStatus.NOT_CONFIGURED -> TitonoxTokens.StateWarning
                                                else -> TitonoxTokens.StateError
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${apiStatus.label} (${latencyMs}ms)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (apiStatus) {
                                        ApiStatus.CONNECTED -> TitonoxTokens.StateSuccess
                                        ApiStatus.CONNECTING -> TitonoxTokens.StateThinking
                                        ApiStatus.NOT_CONFIGURED -> TitonoxTokens.StateWarning
                                        else -> TitonoxTokens.StateError
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TitonoxTokens.Surface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ACTIVE PROVIDER", fontSize = 9.sp, color = TitonoxTokens.TextTertiary, fontWeight = FontWeight.Bold)
                            Text(activeProviderName, fontSize = 13.sp, color = TitonoxTokens.AccentPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("ACTIVE MODEL", fontSize = 9.sp, color = TitonoxTokens.TextTertiary, fontWeight = FontWeight.Bold)
                            Text(activeModelName, fontSize = 13.sp, color = TitonoxTokens.TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (currentConfig.provider == AIProviderType.AUTO && lastAutoDecision != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TitonoxTokens.AccentSecondary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, TitonoxTokens.AccentSecondary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = TitonoxTokens.AccentSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[AUTO ROUTE]: ${lastAutoDecision?.reason}",
                                    fontSize = 11.sp,
                                    color = TitonoxTokens.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Tabs: Engine vs Usage & Pricing
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedScreenTab == 0) TitonoxTokens.AccentPrimary.copy(alpha = 0.25f) else TitonoxTokens.SurfaceElevated,
                    border = BorderStroke(1.dp, if (selectedScreenTab == 0) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedScreenTab = 0 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = if (selectedScreenTab == 0) TitonoxTokens.AccentPrimary else TitonoxTokens.TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI ENGINE & MODELS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedScreenTab == 0) TitonoxTokens.AccentPrimary else TitonoxTokens.TextPrimary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedScreenTab == 1) TitonoxTokens.AccentSecondary.copy(alpha = 0.25f) else TitonoxTokens.SurfaceElevated,
                    border = BorderStroke(1.dp, if (selectedScreenTab == 1) TitonoxTokens.AccentSecondary else TitonoxTokens.BorderSubtle),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedScreenTab = 1 }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = if (selectedScreenTab == 1) TitonoxTokens.AccentSecondary else TitonoxTokens.TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("USAGE & PRICING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedScreenTab == 1) TitonoxTokens.AccentSecondary else TitonoxTokens.TextPrimary)
                    }
                }
            }
        }

        if (selectedScreenTab == 1) {
            // USAGE & COST DASHBOARD
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("ESTIMATED API COST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                if (usageStats.isCostAvailable && usageStats.estimatedCostUsd != null) {
                                    Text(
                                        text = "$${"%.4f".format(usageStats.estimatedCostUsd)} USD",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TitonoxTokens.StateSuccess
                                    )
                                } else {
                                    Text(
                                        text = "Cost unavailable",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TitonoxTokens.StateWarning
                                    )
                                    Text(
                                        text = "No rates configured. Enter your token rates below to compute estimated cost.",
                                        fontSize = 10.sp,
                                        color = TitonoxTokens.TextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = { apiManager.usageTracker.clearUsage() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear Usage", tint = TitonoxTokens.StateError)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("TOTAL REQUESTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                    Text("${usageStats.totalRequests}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentPrimary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("DAILY USAGE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                    Text("${usageStats.dailyRequests} req", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentSecondary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("MONTHLY USAGE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                    Text("${usageStats.monthlyRequests} req", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("INPUT TOKENS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                    Text("${usageStats.totalInputTokens}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TitonoxTokens.Surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("OUTPUT TOKENS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextTertiary)
                                    Text("${usageStats.totalOutputTokens}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Custom Pricing Configuration Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TitonoxTokens.BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var inPriceStr by remember(usageStats.inputPricePerMillion) {
                        mutableStateOf(if (usageStats.inputPricePerMillion > 0) usageStats.inputPricePerMillion.toString() else "")
                    }
                    var outPriceStr by remember(usageStats.outputPricePerMillion) {
                        mutableStateOf(if (usageStats.outputPricePerMillion > 0) usageStats.outputPricePerMillion.toString() else "")
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CONFIGURE MODEL TOKEN PRICING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentPrimary)
                        Text("Enter your provider's rates to calculate estimated costs locally.", fontSize = 10.sp, color = TitonoxTokens.TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inPriceStr,
                            onValueChange = { inPriceStr = it },
                            label = { Text("Input Price ($ per 1M tokens)") },
                            placeholder = { Text("e.g. 0.15") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = outPriceStr,
                            onValueChange = { outPriceStr = it },
                            label = { Text("Output Price ($ per 1M tokens)") },
                            placeholder = { Text("e.g. 0.60") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val inP = inPriceStr.toDoubleOrNull() ?: 0.0
                                val outP = outPriceStr.toDoubleOrNull() ?: 0.0
                                apiManager.usageTracker.updatePricing(inP, outP)
                                Toast.makeText(context, "Pricing updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE PRICING RATES", fontWeight = FontWeight.Bold, color = TitonoxTokens.Background, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
        item {
            Text(
                text = "SELECT PROVIDER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TitonoxTokens.TextTertiary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(majorProviders) { prov ->
                    val isSelected = selectedTabProvider == prov
                    val isActive = currentConfig.provider == prov
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) TitonoxTokens.AccentPrimary.copy(alpha = 0.2f) else TitonoxTokens.SurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TitonoxTokens.AccentPrimary else if (isActive) TitonoxTokens.StateSuccess.copy(alpha = 0.6f) else TitonoxTokens.BorderSubtle
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedTabProvider = prov
                                apiKeyInput = apiManager.storage.loadKeyForProvider(prov)
                                baseUrlInput = apiManager.storage.loadBaseUrlForProvider(prov)
                                selectedModel = apiManager.storage.loadModelForProvider(prov)
                                testResult = null
                            }
                            .testTag("provider_tab_${prov.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TitonoxTokens.StateSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = prov.shortName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Provider Detailed Configuration Card
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
                        Column {
                            Text(
                                text = selectedTabProvider.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TitonoxTokens.AccentPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Base Endpoint: ${selectedTabProvider.defaultBaseUrl.ifBlank { "Dynamic" }}",
                                fontSize = 10.sp,
                                color = TitonoxTokens.TextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.switchProvider(selectedTabProvider, selectedModel)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentConfig.provider == selectedTabProvider) TitonoxTokens.StateSuccess else TitonoxTokens.AccentPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("set_active_provider_btn")
                        ) {
                            Text(
                                text = if (currentConfig.provider == selectedTabProvider) "ACTIVE" else "SET ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TitonoxTokens.Background
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedTabProvider != AIProviderType.AUTO) {
                        // Masked API Key Display & Actions
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TitonoxTokens.Surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("KEY STATUS & MASK", fontSize = 9.sp, color = TitonoxTokens.TextTertiary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = apiManager.storage.getMaskedKey(apiKeyInput),
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = TitonoxTokens.TextPrimary
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            if (apiKeyInput.isNotBlank()) {
                                                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clip.setPrimaryClip(ClipData.newPlainText("API Key", apiKeyInput))
                                                Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Key", tint = TitonoxTokens.AccentPrimary, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            apiKeyInput = ""
                                            viewModel.updateProviderKey(selectedTabProvider, "")
                                            Toast.makeText(context, "API Key cleared", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Key", tint = TitonoxTokens.StateError, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // API Key input
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("${selectedTabProvider.shortName} API Key") },
                            placeholder = { Text("Enter API key or paste from console") },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle API Key Visibility",
                                        tint = TitonoxTokens.TextSecondary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TitonoxTokens.AccentPrimary,
                                unfocusedBorderColor = TitonoxTokens.BorderSubtle,
                                focusedTextColor = TitonoxTokens.TextPrimary,
                                unfocusedTextColor = TitonoxTokens.TextPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_api_key_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Base URL (if Custom or editable)
                        if (selectedTabProvider == AIProviderType.CUSTOM || selectedTabProvider == AIProviderType.OPENAI) {
                            OutlinedTextField(
                                value = baseUrlInput,
                                onValueChange = { baseUrlInput = it },
                                label = { Text("API Base URL") },
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
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Model Selection
                        Text("SELECT MODEL", fontSize = 10.sp, color = TitonoxTokens.TextTertiary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val availableModels = ProviderModelCatalogs.getModelsForProvider(selectedTabProvider)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableModels) { modelDesc ->
                                val isModelSelected = selectedModel == modelDesc.modelId
                                FilterChip(
                                    selected = isModelSelected,
                                    onClick = { selectedModel = modelDesc.modelId },
                                    label = {
                                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Text(modelDesc.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            if (modelDesc.supportsVision) {
                                                Text("Vision", fontSize = 8.sp, color = TitonoxTokens.AccentPrimary)
                                            }
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TitonoxTokens.AccentPrimary.copy(alpha = 0.25f),
                                        selectedLabelColor = TitonoxTokens.AccentPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Handshake / Test Button & Save Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isTestingConnection = true
                                        viewModel.updateProviderKey(selectedTabProvider, apiKeyInput)
                                        viewModel.updateProviderModel(selectedTabProvider, selectedModel)
                                        if (baseUrlInput.isNotBlank()) {
                                            viewModel.updateProviderBaseUrl(selectedTabProvider, baseUrlInput)
                                        }
                                        testResult = apiManager.testProviderConnection(
                                            selectedTabProvider,
                                            AIConfig(
                                                provider = selectedTabProvider,
                                                apiKey = apiKeyInput,
                                                model = selectedModel,
                                                baseUrl = baseUrlInput
                                            )
                                        )
                                        isTestingConnection = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_handshake_btn")
                            ) {
                                if (isTestingConnection) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TitonoxTokens.AccentPrimary)
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TEST HANDSHAKE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.updateProviderKey(selectedTabProvider, apiKeyInput)
                                    viewModel.updateProviderModel(selectedTabProvider, selectedModel)
                                    if (baseUrlInput.isNotBlank()) {
                                        viewModel.updateProviderBaseUrl(selectedTabProvider, baseUrlInput)
                                    }
                                    viewModel.switchProvider(selectedTabProvider, selectedModel)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_provider_btn")
                            ) {
                                Text("SAVE & ACTIVATE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TitonoxTokens.Background)
                            }
                        }

                        // Test Result Alert Card with Real Statuses
                        testResult?.let { res ->
                            val statusLabel = if (res.success) {
                                "CONNECTED"
                            } else {
                                when (res.errorCategory) {
                                    ErrorCategory.INVALID_API_KEY -> "INVALID KEY"
                                    ErrorCategory.QUOTA_EXCEEDED -> "RATE LIMITED"
                                    ErrorCategory.NETWORK_UNAVAILABLE -> "NETWORK ERROR"
                                    ErrorCategory.MODEL_UNAVAILABLE -> "INVALID MODEL"
                                    ErrorCategory.SERVER_ERROR -> "SERVER ERROR"
                                    else -> "HANDSHAKE ERROR"
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (res.success) TitonoxTokens.StateSuccess.copy(alpha = 0.15f) else TitonoxTokens.StateError.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (res.success) TitonoxTokens.StateSuccess else TitonoxTokens.StateError),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = if (res.success) TitonoxTokens.StateSuccess else TitonoxTokens.StateError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$statusLabel • ${res.latencyMs}ms",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (res.success) TitonoxTokens.StateSuccess else TitonoxTokens.StateError
                                        )
                                    }
                                    Text(
                                        text = res.message,
                                        fontSize = 10.sp,
                                        color = TitonoxTokens.TextPrimary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // AUTO ROUTING EXPLANATION CARD
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TitonoxTokens.Surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("HOW AUTO ROUTING WORKS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TitonoxTokens.AccentPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Screen / Vision Tasks -> Routes to Vision models (Gemini 2.5 Flash / GPT-4o)", fontSize = 11.sp, color = TitonoxTokens.TextSecondary)
                                Text("• Complex Logic & Code -> Routes to Deep Reasoning (DeepSeek / Gemini 3.1 Pro)", fontSize = 11.sp, color = TitonoxTokens.TextSecondary)
                                Text("• Quick Voice & Device Actions -> Routes to Ultra-Fast LPU (Groq Llama 3.3)", fontSize = 11.sp, color = TitonoxTokens.TextSecondary)
                                Text("• Offline Fallback -> Deterministic Local Command Engine", fontSize = 11.sp, color = TitonoxTokens.TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // 4. Custom Models Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED CUSTOM MODELS (${customModels.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitonoxTokens.TextTertiary,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { showAddCustomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentSecondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_custom_model_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD CUSTOM MODEL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (customModels.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TitonoxTokens.SurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom models added yet.",
                            color = TitonoxTokens.TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Tap '+ ADD CUSTOM MODEL' to attach any OpenAI-compatible API, local Ollama, LM Studio, or custom endpoint.",
                            color = TitonoxTokens.TextTertiary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(customModels) { model ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = TitonoxTokens.SurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (model.isSelected) TitonoxTokens.AccentPrimary else TitonoxTokens.BorderSubtle),
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
                                Text(
                                    text = model.modelName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TitonoxTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = TitonoxTokens.SurfaceHighlight
                                ) {
                                    Text(
                                        text = model.providerName,
                                        fontSize = 9.sp,
                                        color = TitonoxTokens.AccentPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Model ID: ${model.modelId} • ${model.apiBaseUrl}",
                                fontSize = 10.sp,
                                color = TitonoxTokens.TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.selectCustomModel(model) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (model.isSelected) TitonoxTokens.StateSuccess else TitonoxTokens.AccentPrimary
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (model.isSelected) "ACTIVE" else "ACTIVATE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { viewModel.deleteCustomModel(model.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Model", tint = TitonoxTokens.StateError)
                            }
                        }
                    }
                }
            }
        }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Modal: Add Custom Model Dialog
    if (showAddCustomDialog) {
        AddCustomModelDialog(
            onDismiss = { showAddCustomDialog = false },
            onSave = { entity ->
                viewModel.addCustomModel(
                    providerName = entity.providerName,
                    modelName = entity.modelName,
                    modelId = entity.modelId,
                    apiBaseUrl = entity.apiBaseUrl,
                    apiKey = entity.apiKey,
                    requestFormat = entity.requestFormat,
                    responseFormat = entity.responseFormat,
                    authHeader = entity.authHeader,
                    temperature = entity.temperature,
                    maxTokens = entity.maxTokens,
                    contextWindow = entity.contextWindow,
                    visionSupport = entity.visionSupport,
                    toolCalling = entity.toolCalling,
                    streaming = entity.streaming
                )
                showAddCustomDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomModelDialog(
    onDismiss: () -> Unit,
    onSave: (CustomModelEntity) -> Unit
) {
    var providerName by remember { mutableStateOf("Custom") }
    var modelName by remember { mutableStateOf("") }
    var modelId by remember { mutableStateOf("") }
    var apiBaseUrl by remember { mutableStateOf("https://api.openai.com/v1") }
    var apiKey by remember { mutableStateOf("") }
    var authHeader by remember { mutableStateOf("Bearer") }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var maxTokens by remember { mutableIntStateOf(2048) }
    var visionSupport by remember { mutableStateOf(false) }
    var toolCalling by remember { mutableStateOf(true) }
    var streaming by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("ADD CUSTOM MODEL", fontWeight = FontWeight.Bold, color = TitonoxTokens.AccentPrimary)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = providerName,
                    onValueChange = { providerName = it },
                    label = { Text("Provider Name (e.g. Ollama, OpenRouter, Self-Hosted)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model Display Name") },
                    placeholder = { Text("e.g. Llama 3 70B Local") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = modelId,
                    onValueChange = { modelId = it },
                    label = { Text("Model ID") },
                    placeholder = { Text("e.g. llama3:70b or gpt-4o-custom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiBaseUrl,
                    onValueChange = { apiBaseUrl = it },
                    label = { Text("API Base URL") },
                    placeholder = { Text("https://example.com/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional for local)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vision Support", fontSize = 12.sp, color = TitonoxTokens.TextPrimary)
                    Switch(
                        checked = visionSupport,
                        onCheckedChange = { visionSupport = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TitonoxTokens.AccentPrimary)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tool Calling", fontSize = 12.sp, color = TitonoxTokens.TextPrimary)
                    Switch(
                        checked = toolCalling,
                        onCheckedChange = { toolCalling = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TitonoxTokens.AccentPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (modelName.isNotBlank() && modelId.isNotBlank()) {
                        onSave(
                            CustomModelEntity(
                                providerName = providerName,
                                modelName = modelName,
                                modelId = modelId,
                                apiBaseUrl = apiBaseUrl,
                                apiKey = apiKey,
                                authHeader = authHeader,
                                temperature = temperature,
                                maxTokens = maxTokens,
                                visionSupport = visionSupport,
                                toolCalling = toolCalling,
                                streaming = streaming
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TitonoxTokens.AccentPrimary)
            ) {
                Text("SAVE MODEL", color = TitonoxTokens.Background, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
