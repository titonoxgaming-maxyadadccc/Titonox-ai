package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ChatMessage
import com.example.ai.ClarificationPrompt
import com.example.ai.ConfirmationPrompt
import com.example.ai.ContactRecord
import com.example.ai.DisambiguationPrompt
import com.example.ai.ExecutionPlan
import com.example.ai.FastLocalCommandEngine
import com.example.ai.GeminiClient
import com.example.ai.LocalCommandResult
import com.example.ai.TaskEngine
import com.example.ai.TaskState
import com.example.ai.TaskStep
import com.example.automation.AccessibilityController
import com.example.automation.ScreenSummary
import com.example.data.AppDatabase
import com.example.data.CalendarEventEntity
import com.example.data.MemoryEntity
import com.example.data.NoteEntity
import com.example.data.SongEntity
import com.example.data.TitonoxRepository
import com.example.data.TodoEntity
import com.example.data.UniversalSearchResult
import com.example.device.BatteryInfo
import com.example.device.DeviceController
import com.example.device.DeviceSpecs
import com.example.device.RamInfo
import com.example.device.SimSlotInfo
import com.example.device.StorageInfo
import com.example.player.MusicManager
import com.example.player.SongItem
import com.example.productivity.ProductivityManager
import com.example.vision.ScreenCaptureManager
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CHAT,
    VOICE,
    TASKS,
    VISION,
    SETTINGS
}

enum class TitonoxInterface {
    HOME,
    CHAT,
    AGENT,
    AUTOMATION,
    TOOLS,
    VISION,
    MEMORY,
    MODELS,
    SETTINGS,
    PLAYER,
    ORB_STUDIO;

    companion object {
        val CORE = HOME
        val CONTROL = AUTOMATION
        val STUDIO = TOOLS
        val API_SETTINGS = MODELS
    }
}

data class DevLog(
    val timestamp: Long = System.currentTimeMillis(),
    val intent: String,
    val tool: String,
    val state: String,
    val result: String,
    val latencyMs: Long
)

class TitonoxViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = TitonoxRepository(db)
    val deviceController = DeviceController(application)
    val accessibilityController = AccessibilityController(application)
    val productivityManager = ProductivityManager(application)
    val screenCaptureManager = ScreenCaptureManager(application)
    val musicManager = MusicManager(application, repository, viewModelScope)
    val orbController = com.example.floating.OrbController.getInstance(application)
    val orbIntentRouter = com.example.floating.OrbIntentRouter(application, orbController)
    val apiManager = com.example.ai.api.APIManager.getInstance(application)
    private val geminiClient = GeminiClient(application)

    // 5 Major Interfaces State
    private val _currentInterface = MutableStateFlow(TitonoxInterface.CORE)
    val currentInterface: StateFlow<TitonoxInterface> = _currentInterface.asStateFlow()

    // Studio & Productivity Data
    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todos: StateFlow<List<TodoEntity>> = repository.allTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customModels: StateFlow<List<com.example.data.CustomModelEntity>> = repository.allCustomModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTasks: StateFlow<List<com.example.data.TaskHistoryEntity>> = repository.recentTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomModel(
        providerName: String,
        modelName: String,
        modelId: String,
        apiBaseUrl: String,
        apiKey: String,
        requestFormat: String = "OpenAI",
        responseFormat: String = "OpenAI",
        authHeader: String = "Bearer",
        temperature: Float = 0.7f,
        maxTokens: Int = 2048,
        contextWindow: Int = 32768,
        visionSupport: Boolean = false,
        toolCalling: Boolean = true,
        streaming: Boolean = true
    ) {
        viewModelScope.launch {
            repository.saveCustomModel(
                com.example.data.CustomModelEntity(
                    providerName = providerName,
                    modelName = modelName,
                    modelId = modelId,
                    apiBaseUrl = apiBaseUrl,
                    apiKey = apiKey,
                    requestFormat = requestFormat,
                    responseFormat = responseFormat,
                    authHeader = authHeader,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    contextWindow = contextWindow,
                    visionSupport = visionSupport,
                    toolCalling = toolCalling,
                    streaming = streaming
                )
            )
        }
    }

    fun deleteCustomModel(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomModel(id)
        }
    }

    fun selectCustomModel(model: com.example.data.CustomModelEntity) {
        viewModelScope.launch {
            repository.selectCustomModel(model.id)
            apiManager.switchProvider(com.example.ai.api.AIProviderType.CUSTOM, model.modelId)
            apiManager.storage.saveKeyForProvider(com.example.ai.api.AIProviderType.CUSTOM, model.apiKey)
            apiManager.storage.saveBaseUrlForProvider(com.example.ai.api.AIProviderType.CUSTOM, model.apiBaseUrl)
            apiManager.storage.saveAuthHeaderForProvider(com.example.ai.api.AIProviderType.CUSTOM, model.authHeader)
        }
    }

    fun addMemory(key: String, value: String, category: String = "UserPreference") {
        viewModelScope.launch {
            repository.saveMemory(key, value, category)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemories()
        }
    }

    fun switchProvider(provider: com.example.ai.api.AIProviderType, model: String? = null) {
        apiManager.switchProvider(provider, model)
    }

    fun updateProviderKey(provider: com.example.ai.api.AIProviderType, key: String) {
        apiManager.storage.saveKeyForProvider(provider, key)
        if (apiManager.config.value.provider == provider) {
            apiManager.updateConfig(apiManager.config.value.copy(apiKey = key))
        }
    }

    fun updateProviderModel(provider: com.example.ai.api.AIProviderType, model: String) {
        apiManager.storage.saveModelForProvider(provider, model)
        if (apiManager.config.value.provider == provider) {
            apiManager.updateConfig(apiManager.config.value.copy(model = model))
        }
    }

    fun updateProviderBaseUrl(provider: com.example.ai.api.AIProviderType, url: String) {
        apiManager.storage.saveBaseUrlForProvider(provider, url)
        if (apiManager.config.value.provider == provider) {
            apiManager.updateConfig(apiManager.config.value.copy(baseUrl = url))
        }
    }

    // Voice & Tasks
    val orbSettingsRepo = com.example.floating.OrbSettingsRepository.getInstance(application)
    val licenseManager = com.example.license.LicenseManager.getInstance(application)
    val licenseInfo = licenseManager.licenseInfo

    val toolRouter = com.example.ai.tools.ToolRouter(
        context = application,
        orbController = orbController,
        deviceController = deviceController,
        accessibilityController = accessibilityController,
        repository = repository
    )

    val voiceManager = VoiceAssistantManager(
        context = application,
        onVoicePowerOff = {
            orbSettingsRepo.setPowerOff(true)
            com.example.floating.FloatingOrbService.stopService(application)
        },
        onVoiceResult = { spokenText ->
            processUserInput(spokenText)
        }
    )

    val taskEngine = TaskEngine(
        deviceController = deviceController,
        accessibilityController = accessibilityController,
        productivityManager = productivityManager,
        repository = repository,
        onSpeak = { text -> voiceManager.speak(text) }
    )

    val agentController = com.example.ai.agent.AgentController(
        context = application,
        scope = viewModelScope,
        deviceController = deviceController,
        accessibilityController = accessibilityController,
        orbController = orbController,
        orbIntentRouter = orbIntentRouter,
        toolRouter = toolRouter,
        taskEngine = taskEngine,
        repository = repository,
        apiManager = apiManager,
        onSpeak = { text -> voiceManager.speak(text) }
    )

    private val localCommandEngine = FastLocalCommandEngine(
        deviceController = deviceController,
        accessibilityController = accessibilityController,
        productivityManager = productivityManager,
        repository = repository
    )

    private val _taskState = MutableStateFlow(TaskState.IDLE)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

    private val _isSetupWizardOpen = MutableStateFlow(
        !application.getSharedPreferences("titonox_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("setup_completed", false)
    )
    val isSetupWizardOpen: StateFlow<Boolean> = _isSetupWizardOpen.asStateFlow()

    fun openSetupWizard() {
        _isSetupWizardOpen.value = true
    }

    fun closeSetupWizard() {
        _isSetupWizardOpen.value = false
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun toggleListening() {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
        } else {
            voiceManager.startListening()
        }
    }

    fun triggerEmergencyStop() {
        com.example.ai.agent.GlobalTaskCancellationController.cancelAll("User pressed Emergency STOP")
        taskEngine.cancelCurrentTask(userSpoken = false)
        voiceManager.stopSpeaking()
        musicManager.pause()
        _taskState.value = TaskState.CANCELLED
        addMessage("user", "[EMERGENCY STOP]")
        addMessage("model", "All tasks and processes stopped immediately.", "EMERGENCY_STOP")
    }

    val currentPlan: StateFlow<ExecutionPlan?> = taskEngine.currentPlan
    val activeClarification: StateFlow<ClarificationPrompt?> = taskEngine.activeClarification
    val activeDisambiguation: StateFlow<DisambiguationPrompt?> = taskEngine.activeDisambiguation
    val activeConfirmation: StateFlow<ConfirmationPrompt?> = taskEngine.activeConfirmation
    val isScreenHeldAwake: StateFlow<Boolean> = taskEngine.isScreenHeldAwake
    val screenAwakeWarning: StateFlow<String?> = taskEngine.screenAwakeWarning

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = "model",
                text = "TITONOX systems initialized. I am your personal AI operating ecosystem. Voice controls, real device telemetry, vision optics, productivity studio, and music player ready.",
                actionBadge = "TITONOX_ONLINE"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Telemetry & Device State
    private val _batteryInfo = MutableStateFlow(deviceController.getBatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _ramInfo = MutableStateFlow(deviceController.getRamInfo())
    val ramInfo: StateFlow<RamInfo> = _ramInfo.asStateFlow()

    private val _storageInfo = MutableStateFlow(deviceController.getStorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(deviceController.getDeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val _simSlots = MutableStateFlow(deviceController.getSimSlots())
    val simSlots: StateFlow<List<SimSlotInfo>> = _simSlots.asStateFlow()

    private val _mediaVolume = MutableStateFlow(deviceController.getVolumePercentage(AudioManager.STREAM_MUSIC))
    val mediaVolume: StateFlow<Int> = _mediaVolume.asStateFlow()

    private val _ringVolume = MutableStateFlow(deviceController.getVolumePercentage(AudioManager.STREAM_RING))
    val ringVolume: StateFlow<Int> = _ringVolume.asStateFlow()

    private val _alarmVolume = MutableStateFlow(deviceController.getVolumePercentage(AudioManager.STREAM_ALARM))
    val alarmVolume: StateFlow<Int> = _alarmVolume.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(deviceController.isFlashlightOn())
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    // Vision & Automation
    private val _screenSummary = MutableStateFlow<ScreenSummary?>(null)
    val screenSummary: StateFlow<ScreenSummary?> = _screenSummary.asStateFlow()

    private val _visionAnalysisResult = MutableStateFlow<String?>(null)
    val visionAnalysisResult: StateFlow<String?> = _visionAnalysisResult.asStateFlow()

    // Universal Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UniversalSearchResult>>(emptyList())
    val searchResults: StateFlow<List<UniversalSearchResult>> = _searchResults.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    // Dev mode
    private val _devModeEnabled = MutableStateFlow(false)
    val devModeEnabled: StateFlow<Boolean> = _devModeEnabled.asStateFlow()

    private val _devLogs = MutableStateFlow<List<DevLog>>(emptyList())
    val devLogs: StateFlow<List<DevLog>> = _devLogs.asStateFlow()

    init {
        viewModelScope.launch {
            taskEngine.taskState.collect { state ->
                if (state != TaskState.IDLE) {
                    _taskState.value = state
                }
            }
        }
        refreshDeviceStatus()
    }

    fun selectInterface(target: TitonoxInterface) {
        _currentInterface.value = target
        if (target == TitonoxInterface.CONTROL) {
            refreshDeviceStatus()
        }
    }

    fun refreshDeviceStatus() {
        _batteryInfo.value = deviceController.getBatteryInfo()
        _ramInfo.value = deviceController.getRamInfo()
        _storageInfo.value = deviceController.getStorageInfo()
        _simSlots.value = deviceController.getSimSlots()
        _mediaVolume.value = deviceController.getVolumePercentage(AudioManager.STREAM_MUSIC)
        _ringVolume.value = deviceController.getVolumePercentage(AudioManager.STREAM_RING)
        _alarmVolume.value = deviceController.getVolumePercentage(AudioManager.STREAM_ALARM)
        _isFlashlightOn.value = deviceController.isFlashlightOn()
    }

    fun setMediaVolume(percent: Int) {
        _mediaVolume.value = deviceController.setVolumePercentage(percent, AudioManager.STREAM_MUSIC)
    }

    fun setRingVolume(percent: Int) {
        _ringVolume.value = deviceController.setVolumePercentage(percent, AudioManager.STREAM_RING)
    }

    fun setAlarmVolume(percent: Int) {
        _alarmVolume.value = deviceController.setVolumePercentage(percent, AudioManager.STREAM_ALARM)
    }

    fun toggleFlashlight() {
        val newState = deviceController.toggleFlashlight()
        _isFlashlightOn.value = deviceController.isFlashlightOn()
    }

    fun openSearch() {
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        viewModelScope.launch {
            if (q.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = repository.searchUniversal(q)
            }
        }
    }

    fun toggleDevMode() {
        _devModeEnabled.value = !_devModeEnabled.value
    }

    // Direct Voice & Chat Processing with Interface Routing
    fun processUserInput(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val lower = trimmed.lowercase()

        // 1. Direct Interface Switching via Voice
        if (lower.contains("music") || lower.contains("player") || lower.contains("play song") || lower.contains("audio")) {
            selectInterface(TitonoxInterface.PLAYER)
            val reply = "Switched to TITONOX PLAYER. Accessing music library and sound engine."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }
        if (lower.contains("control") || lower.contains("battery") || lower.contains("storage") || lower.contains("telemetry") || lower.contains("device info")) {
            selectInterface(TitonoxInterface.CONTROL)
            val reply = "Switched to TITONOX CONTROL. Android system telemetry and device controls online."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }
        if (lower.contains("vision") || lower.contains("scan screen") || lower.contains("inspect screen") || lower.contains("camera")) {
            selectInterface(TitonoxInterface.VISION)
            val reply = "Switched to TITONOX VISION. Cyber HUD optics and screen inspection active."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }
        if (lower.contains("notes") || lower.contains("todo") || lower.contains("tasks") || lower.contains("calendar") || lower == "studio") {
            selectInterface(TitonoxInterface.STUDIO)
            val reply = "Switched to TITONOX STUDIO. Productivity workspace open."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }
        if (lower.contains("orb") || lower.contains("floating assistant") || lower.contains("customize orb") || lower.contains("orb studio")) {
            selectInterface(TitonoxInterface.ORB_STUDIO)
            val reply = "Switched to TITONOX ORB STUDIO. 32 dynamic floating assistant cores and visual calibrations ready."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }
        if (lower == "core" || lower.contains("command center") || lower.contains("main screen")) {
            selectInterface(TitonoxInterface.CORE)
            val reply = "Returning to TITONOX CORE command center."
            addMessage("user", trimmed)
            addMessage("model", reply, "INTERFACE_SWITCH")
            voiceManager.speak(reply)
            return
        }

        // 2. Identity Check (MANDATORY)
        if (lower.contains("who are you") || lower.contains("what is your name") || lower.contains("your name") || lower.contains("who made you")) {
            val identity = "I am TITONOX, your personal AI assistant. Created and owned by Aditya Yadav. You can follow my official channels at TITONOXOFFICIAL on YouTube and Instagram."
            addMessage("user", trimmed)
            addMessage("model", identity, "IDENTITY")
            voiceManager.speak(identity)
            return
        }

        // Active Clarification Check: If system is awaiting user answer for a missing parameter / conflict
        if (activeClarification.value != null) {
            submitClarificationResponse(trimmed)
            return
        }

        // Active Confirmation Check: If system is awaiting user confirmation for a sensitive action
        if (activeConfirmation.value != null) {
            if (lower.contains("haan") || lower.contains("yes") || lower.contains("post") || lower.contains("confirm") || lower.contains("kar do")) {
                submitConfirmationDecision(true)
            } else {
                submitConfirmationDecision(false)
            }
            return
        }

        // 3. Stop / Abort / Pause check
        if (lower == "stop" || lower == "cancel" || lower == "abort" || lower == "ruko" || lower == "bas" || lower == "task band karo") {
            if (taskEngine.isRunning()) {
                pauseTask()
            } else {
                taskEngine.cancelCurrentTask()
                voiceManager.stopSpeaking()
                musicManager.pause()
                _taskState.value = TaskState.CANCELLED
                addMessage("user", trimmed)
                addMessage("model", "Task aborted.", "ABORT")
            }
            return
        }

        // 4. Resume check
        if (lower == "resume" || lower == "continue" || lower == "aage chalo" || lower.contains("continue where you stopped")) {
            if (_taskState.value == TaskState.PAUSED) {
                resumeTask()
                return
            }
        }

        // 5. Voice Orb Intent Routing Check (Natural language orb commands in Hindi/English)
        val orbVoiceResult = orbIntentRouter.handleCommand(trimmed)
        if (orbVoiceResult.handled) {
            val reply = orbVoiceResult.feedbackMessage ?: "Orb command executed."
            addMessage("user", trimmed)
            addMessage("model", reply, orbVoiceResult.intentName ?: "ORB_ACTION")
            voiceManager.speak(reply)
            _taskState.value = TaskState.COMPLETED
            logDev(
                intent = orbVoiceResult.intentName ?: "ORB_ACTION",
                tool = "OrbController",
                state = "SUCCESS",
                result = reply,
                latencyMs = 15L
            )
            return
        }

        addMessage("user", trimmed)
        _taskState.value = TaskState.UNDERSTANDING
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            val memList = memories.value.map { "${it.key}: ${it.value}" }
            val localResult = localCommandEngine.tryExecuteLocal(trimmed)

            when (localResult) {
                is LocalCommandResult.Handled -> {
                    if (localResult.actionTag == "PAUSE_OR_STOP") {
                        pauseTask()
                    } else if (localResult.actionTag == "RESUME") {
                        resumeTask()
                    } else {
                        _taskState.value = TaskState.COMPLETED
                        addMessage("model", localResult.displayMessage, localResult.actionTag)
                        voiceManager.speak(localResult.spokenReply)
                    }
                    logDev(
                        intent = "LOCAL_COMMAND",
                        tool = localResult.actionTag ?: "LocalEngine",
                        state = "SUCCESS",
                        result = localResult.displayMessage,
                        latencyMs = System.currentTimeMillis() - startTime
                    )
                }
                is LocalCommandResult.RequiresClarification -> {
                    _taskState.value = TaskState.CLARIFYING
                    addMessage("model", localResult.prompt.question, "CLARIFICATION")
                    voiceManager.speak(localResult.prompt.question)
                    taskEngine.setClarificationPrompt(localResult.prompt)
                }
                is LocalCommandResult.RequiresDisambiguation -> {
                    _taskState.value = TaskState.CLARIFYING
                    addMessage("model", localResult.prompt.title, "DISAMBIGUATION")
                    voiceManager.speak(localResult.prompt.title)
                    taskEngine.setDisambiguationPrompt(localResult.prompt)
                }
                is LocalCommandResult.RequiresConfirmation -> {
                    _taskState.value = TaskState.CONFIRMING
                    addMessage("model", localResult.confirmation.actionDetail, "CONFIRMATION")
                    voiceManager.speak(localResult.confirmation.actionDetail)
                    taskEngine.setConfirmationPrompt(localResult.confirmation)
                }
                is LocalCommandResult.MultiStepPlan -> {
                    val plan = ExecutionPlan(
                        id = "plan_${System.currentTimeMillis()}",
                        userQuery = trimmed,
                        taskTitle = localResult.taskTitle,
                        targetApp = localResult.targetApp,
                        steps = localResult.steps
                    )
                    logDev(
                        intent = "MULTI_STEP_TASK",
                        tool = "TaskEngine",
                        state = "EXECUTING",
                        result = "Created ${localResult.steps.size} steps",
                        latencyMs = System.currentTimeMillis() - startTime
                    )
                    taskEngine.executePlan(viewModelScope, plan)
                }
                is LocalCommandResult.NotHandled,
                is LocalCommandResult.PassToAI -> {
                    _taskState.value = TaskState.PLANNING

                    val aiSteps = geminiClient.planTaskSteps(trimmed)
                    if (aiSteps.size >= 2) {
                        val parsedSteps = aiSteps.mapIndexed { idx, obj ->
                            TaskStep(
                                stepNumber = idx + 1,
                                action = obj.optString("action", "speak"),
                                target = obj.optString("target", ""),
                                description = obj.optString("description", "Execute action")
                            )
                        }
                        val plan = ExecutionPlan(
                            id = "ai_plan_${System.currentTimeMillis()}",
                            userQuery = trimmed,
                            taskTitle = "AI Planned Task",
                            targetApp = "System",
                            steps = parsedSteps
                        )
                        taskEngine.executePlan(viewModelScope, plan)
                    } else {
                        val currentScreenBitmap = if (screenCaptureManager.isScreenCaptureActive.value) {
                            screenCaptureManager.latestBitmap
                        } else null

                        val reply = apiManager.processUserQuery(
                            prompt = trimmed,
                            conversationHistory = _messages.value,
                            userMemories = memList,
                            screenBitmap = currentScreenBitmap,
                            onActionExecuted = { actionFeedback ->
                                logDev(
                                    intent = "TOOL_EXECUTION",
                                    tool = "ToolRouter",
                                    state = "EXECUTED",
                                    result = actionFeedback,
                                    latencyMs = System.currentTimeMillis() - startTime
                                )
                            }
                        )
                        _taskState.value = TaskState.COMPLETED
                        addMessage("model", reply, if (currentScreenBitmap != null) "VISION_AI" else "AI_REASONING")
                        voiceManager.speak(reply)
                    }
                }
            }
        }
    }

    fun submitClarificationResponse(response: String) {
        addMessage("user", response)
        taskEngine.submitClarificationResponse(viewModelScope, response)
    }

    fun selectDisambiguationContact(contact: ContactRecord) {
        addMessage("user", "Selected: ${contact.name}")
        taskEngine.submitDisambiguationSelection(viewModelScope, contact)
    }

    fun submitConfirmationDecision(approved: Boolean) {
        addMessage("user", if (approved) "Haan, Post karo" else "Cancel")
        taskEngine.submitConfirmationDecision(viewModelScope, approved)
    }

    fun pauseTask() {
        taskEngine.pauseTask(userSpoken = true)
        _taskState.value = TaskState.PAUSED
        addMessage("model", "Task stopped. You can say resume anytime.", "PAUSE")
    }

    fun resumeTask() {
        taskEngine.resumeTask(viewModelScope)
        _taskState.value = TaskState.EXECUTING
        addMessage("model", "Resuming task from last verified step.", "RESUME")
    }

    fun cancelTask() {
        taskEngine.cancelCurrentTask(userSpoken = true)
        _taskState.value = TaskState.CANCELLED
        addMessage("model", "Task cancelled.", "CANCEL")
    }

    fun retryFailedStep() {
        taskEngine.retryFailedStep(viewModelScope)
        _taskState.value = TaskState.EXECUTING
        addMessage("model", "Retrying failed step.", "RETRY")
    }

    fun readCurrentScreen() {
        viewModelScope.launch {
            _taskState.value = TaskState.UNDERSTANDING
            val summary = accessibilityController.readCurrentScreen()
            _screenSummary.value = summary
            if (summary != null) {
                val app = summary.packageName.substringAfterLast('.')
                val spoken = "Inspecting $app. Found ${summary.buttonLabels.size} buttons and ${summary.textElements.size} text elements."
                addMessage("model", "Screen Inspector:\nPackage: ${summary.packageName}\nButtons: ${summary.buttonLabels.take(6).joinToString(", ")}\nTexts: ${summary.textElements.take(6).joinToString(", ")}", "SCREEN_READER")
                voiceManager.speak(spoken)
                _taskState.value = TaskState.COMPLETED
            } else {
                val fail = "Accessibility Service is not active. Please enable TITONOX in Accessibility Settings."
                addMessage("model", fail, "ACCESSIBILITY_REQUIRED")
                voiceManager.speak(fail)
                _taskState.value = TaskState.FAILED
            }
        }
    }

    fun analyzeScreenVision() {
        val bitmap = screenCaptureManager.latestBitmap
        if (bitmap == null) {
            val msg = "Screen capture is not active. Tap 'Start Screen Capture' first."
            addMessage("model", msg, "VISION_ALERT")
            voiceManager.speak(msg)
            return
        }

        viewModelScope.launch {
            _taskState.value = TaskState.UNDERSTANDING
            val prompt = "Analyze this Android screen. Explain what app is open, describe errors, buttons, dialogs, and actionable steps."
            val reply = geminiClient.generateResponse(
                prompt = prompt,
                screenBitmap = bitmap
            )
            _visionAnalysisResult.value = reply
            addMessage("model", "Vision Analysis:\n$reply", "VISION_RESULT")
            voiceManager.speak("Screen analysis complete. $reply")
            _taskState.value = TaskState.COMPLETED
        }
    }

    // Studio Actions
    fun addNote(title: String, content: String, tag: String = "General", folder: String = "Notes") {
        viewModelScope.launch {
            repository.saveNote(title = title, content = content, tag = tag, folder = folder)
            voiceManager.speak("Note '$title' saved.")
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    fun aiSummarizeNote(note: NoteEntity, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prompt = "Summarize this note in 2-3 concise bullet points:\nTitle: ${note.title}\nContent: ${note.content}"
            val summary = geminiClient.generateResponse(prompt)
            onResult(summary)
        }
    }

    fun aiRewriteNote(note: NoteEntity, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val prompt = "Rewrite and polish this note for high clarity, structure, and professional formatting:\nTitle: ${note.title}\nContent: ${note.content}"
            val polished = geminiClient.generateResponse(prompt)
            onResult(polished)
        }
    }

    fun addTodo(task: String, priority: String = "Normal", category: String = "General", dueDate: String = "") {
        viewModelScope.launch {
            repository.addTodo(task = task, priority = priority, category = category, dueDate = dueDate)
            voiceManager.speak("Added task: $task")
        }
    }

    fun toggleTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.toggleTodo(todo)
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    fun addCalendarEvent(title: String, description: String, dateString: String, timeString: String) {
        viewModelScope.launch {
            repository.addCalendarEvent(
                title = title,
                description = description,
                dateString = dateString,
                timeString = timeString
            )
            voiceManager.speak("Event '$title' scheduled for $dateString at $timeString.")
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(id)
        }
    }

    fun saveMemory(key: String, value: String) {
        viewModelScope.launch {
            repository.saveMemory(key, value)
            voiceManager.speak("Preference remembered.")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    private fun addMessage(role: String, text: String, badge: String? = null) {
        _messages.value = _messages.value + ChatMessage(role = role, text = text, actionBadge = badge)
    }

    private fun logDev(intent: String, tool: String, state: String, result: String, latencyMs: Long) {
        val newLog = DevLog(
            intent = intent,
            tool = tool,
            state = state,
            result = result,
            latencyMs = latencyMs
        )
        _devLogs.value = (listOf(newLog) + _devLogs.value).take(40)
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
        musicManager.release()
        screenCaptureManager.stopCapture()
    }
}
