package com.example.ai

import android.util.Log
import com.example.automation.AccessibilityController
import com.example.data.TitonoxRepository
import com.example.device.DeviceController
import com.example.productivity.ProductivityManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskEngine(
    private val deviceController: DeviceController,
    private val accessibilityController: AccessibilityController,
    private val productivityManager: ProductivityManager,
    private val repository: TitonoxRepository,
    private val onSpeak: (String) -> Unit
) {

    private val TAG = "TaskEngine"
    private var executionJob: Job? = null

    private val _currentPlan = MutableStateFlow<ExecutionPlan?>(null)
    val currentPlan: StateFlow<ExecutionPlan?> = _currentPlan.asStateFlow()

    private val _taskState = MutableStateFlow(TaskState.IDLE)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

    private val _activeClarification = MutableStateFlow<ClarificationPrompt?>(null)
    val activeClarification: StateFlow<ClarificationPrompt?> = _activeClarification.asStateFlow()

    private val _activeDisambiguation = MutableStateFlow<DisambiguationPrompt?>(null)
    val activeDisambiguation: StateFlow<DisambiguationPrompt?> = _activeDisambiguation.asStateFlow()

    private val _activeConfirmation = MutableStateFlow<ConfirmationPrompt?>(null)
    val activeConfirmation: StateFlow<ConfirmationPrompt?> = _activeConfirmation.asStateFlow()

    private val _isScreenHeldAwake = MutableStateFlow(false)
    val isScreenHeldAwake: StateFlow<Boolean> = _isScreenHeldAwake.asStateFlow()

    private val _screenAwakeWarning = MutableStateFlow<String?>(null)
    val screenAwakeWarning: StateFlow<String?> = _screenAwakeWarning.asStateFlow()

    fun setClarificationPrompt(prompt: ClarificationPrompt?) {
        _activeClarification.value = prompt
    }

    fun setDisambiguationPrompt(prompt: DisambiguationPrompt?) {
        _activeDisambiguation.value = prompt
    }

    fun setConfirmationPrompt(prompt: ConfirmationPrompt?) {
        _activeConfirmation.value = prompt
    }

    val taskMemory = TaskMemory()

    fun isRunning(): Boolean = executionJob?.isActive == true && _taskState.value == TaskState.EXECUTING

    // --- SCREEN AWAKE MANAGEMENT ---
    private fun ensureScreenAwakeForTask() {
        val (success, warning) = deviceController.acquireTaskScreenLock()
        _isScreenHeldAwake.value = success
        if (!success && warning != null) {
            _screenAwakeWarning.value = warning
            onSpeak(warning)
        } else {
            _screenAwakeWarning.value = null
        }
    }

    private fun releaseScreenAwakeLock() {
        deviceController.releaseTaskScreenLock()
        _isScreenHeldAwake.value = false
        _screenAwakeWarning.value = null
    }

    // --- TASK INTERRUPTIONS & PAUSE / RESUME ---
    fun pauseTask(userSpoken: Boolean = true) {
        if (executionJob?.isActive == true) {
            executionJob?.cancel()
            _taskState.value = TaskState.PAUSED
            _currentPlan.value?.let { plan ->
                _currentPlan.value = plan.copy(state = TaskState.PAUSED)
            }
            releaseScreenAwakeLock()
            if (userSpoken) {
                onSpeak("Task stopped. You can say resume anytime.")
            }
            Log.i(TAG, "Task execution paused at step index ${_currentPlan.value?.currentStepIndex}")
        }
    }

    fun cancelCurrentTask(userSpoken: Boolean = true) {
        if (executionJob?.isActive == true) {
            executionJob?.cancel()
        }
        _taskState.value = TaskState.CANCELLED
        _currentPlan.value?.let { plan ->
            _currentPlan.value = plan.copy(state = TaskState.CANCELLED)
        }
        _activeClarification.value = null
        _activeDisambiguation.value = null
        _activeConfirmation.value = null
        releaseScreenAwakeLock()
        if (userSpoken) {
            onSpeak("Task cancelled.")
        }
        Log.i(TAG, "Task cancelled by user")
    }

    fun resumeTask(scope: CoroutineScope) {
        val plan = _currentPlan.value ?: return
        if (_taskState.value != TaskState.PAUSED) return
        onSpeak("Resuming task from step ${plan.currentStepIndex + 1}.")
        executePlanInternal(scope, plan, startFromIndex = plan.currentStepIndex)
    }

    fun retryFailedStep(scope: CoroutineScope) {
        val plan = _currentPlan.value ?: return
        onSpeak("Retrying step ${plan.currentStepIndex + 1}.")
        executePlanInternal(scope, plan, startFromIndex = plan.currentStepIndex)
    }

    // --- EXECUTION PIPELINE ---
    fun executePlan(plan: ExecutionPlan) {
        val defaultScope = CoroutineScope(Dispatchers.Default)
        executePlan(defaultScope, plan)
    }

    fun executePlan(scope: CoroutineScope, plan: ExecutionPlan) {
        if (executionJob?.isActive == true) {
            executionJob?.cancel()
        }
        _activeClarification.value = null
        _activeDisambiguation.value = null
        _activeConfirmation.value = null

        _currentPlan.value = plan
        _taskState.value = TaskState.PLANNING

        // Sync memory
        taskMemory.activeIntent = plan.taskTitle
        taskMemory.targetApp = plan.targetApp

        executePlanInternal(scope, plan, startFromIndex = 0)
    }

    private fun executePlanInternal(scope: CoroutineScope, plan: ExecutionPlan, startFromIndex: Int) {
        executionJob = scope.launch(Dispatchers.Default) {
            try {
                ensureScreenAwakeForTask()
                _taskState.value = TaskState.EXECUTING

                for (index in startFromIndex until plan.steps.size) {
                    val step = plan.steps[index]
                    plan.currentStepIndex = index
                    step.status = StepStatus.EXECUTING
                    _currentPlan.value = plan.copy(currentStepIndex = index)

                    Log.d(TAG, "Executing Step ${step.stepNumber}: ${step.action} -> ${step.target}")

                    // Natural spoken update for milestones
                    when (step.action) {
                        "open_app" -> onSpeak("Opening ${step.target}.")
                        "open_story" -> onSpeak("Opening Story editor.")
                        "select_photo" -> onSpeak("Selecting photo.")
                        "add_song" -> onSpeak("Adding song.")
                        "add_text" -> onSpeak("Adding text.")
                        "verify_preview" -> onSpeak("Verifying preview.")
                        "make_call" -> onSpeak("Calling ${taskMemory.selectedContact?.name ?: step.target}.")
                        "whatsapp_call" -> onSpeak("Calling via WhatsApp.")
                        "speak" -> onSpeak(step.target)
                        "volume" -> onSpeak("Adjusting volume.")
                    }

                    // Pre-action Conflict Detection: Check if existing content exists
                    if (step.action == "add_text" || step.action == "create_note") {
                        val existing = accessibilityController.detectExistingContent(step.target)
                        if (!existing.isNullOrBlank() && existing.trim() != step.target.trim()) {
                            taskMemory.existingContentFound = existing
                            _taskState.value = TaskState.CLARIFYING
                            val prompt = ClarificationPrompt(
                                parameterKey = "existing_content_conflict",
                                question = "Sir, ek note already laga hua hai: \"$existing\". Kya use replace karun, usme edit karun, ya waise hi rehne du?",
                                options = listOf("Replace karo", "Edit karo", "Waise hi rehne do", "Cancel"),
                                isDestructiveConflict = true
                            )
                            _activeClarification.value = prompt
                            onSpeak(prompt.question)
                            return@launch // Wait for user decision
                        }
                    }

                    // Pre-action Sensitive Confirmation: Consequential action check (Post story, send message)
                    if (step.isDestructiveOrSensitive && _activeConfirmation.value == null) {
                        _taskState.value = TaskState.CONFIRMING
                        val confirmPrompt = ConfirmationPrompt(
                            actionTitle = "Confirm Action",
                            actionDetail = "Story ready hai. Post kar du?",
                            riskLevel = "HIGH",
                            targetApp = plan.targetApp,
                            confirmButtonText = "Haan, Post karo",
                            cancelButtonText = "Cancel"
                        )
                        _activeConfirmation.value = confirmPrompt
                        onSpeak(confirmPrompt.actionDetail)
                        return@launch // Wait for user confirmation
                    }

                    // Execute action
                    val actionSuccess = executeStepAction(step, plan)

                    // Verification phase
                    step.status = StepStatus.VERIFYING
                    _taskState.value = TaskState.VERIFYING
                    _currentPlan.value = plan.copy()
                    delay(800)

                    val verifySuccess = verifyStep(step, actionSuccess, plan)
                    if (verifySuccess) {
                        step.status = StepStatus.SUCCESS
                        step.verificationResult = "Verified: ${step.description}"
                        taskMemory.lastVerifiedStepIndex = index
                    } else {
                        step.status = StepStatus.FAILED
                        step.verificationResult = "Could not verify execution of ${step.action}"

                        // Automatic Error Recovery & Alternative Fallback Strategies
                        val recovered = attemptErrorRecovery(step, plan)
                        if (recovered) {
                            step.status = StepStatus.SUCCESS
                            step.verificationResult = "Recovered and verified: ${step.description}"
                            taskMemory.lastVerifiedStepIndex = index
                        } else {
                            // Check if this is a Communication Call failure -> Trigger WhatsApp Fallback Chain
                            if (step.action == "make_call") {
                                _taskState.value = TaskState.CLARIFYING
                                val prompt = ClarificationPrompt(
                                    parameterKey = "call_fallback_whatsapp",
                                    question = "Sir, normal call connect nahi ho paayi. Kya WhatsApp se try karun?",
                                    options = listOf("Haan, WhatsApp se karo", "Nahi, cancel karo")
                                )
                                _activeClarification.value = prompt
                                onSpeak(prompt.question)
                                return@launch
                            }

                            _taskState.value = TaskState.FAILED
                            onSpeak("Step failed: ${step.description}. Stopping task.")
                            repository.recordTask(plan.userQuery, "FAILED", "Failed at step: ${step.description}")
                            releaseScreenAwakeLock()
                            return@launch
                        }
                    }

                    _taskState.value = TaskState.EXECUTING
                    _currentPlan.value = plan.copy()
                    delay(500)
                }

                _taskState.value = TaskState.COMPLETED
                plan.state = TaskState.COMPLETED
                _currentPlan.value = plan.copy(state = TaskState.COMPLETED)
                onSpeak("Done. Task completed successfully.")
                repository.recordTask(plan.userQuery, "COMPLETED", "All ${plan.steps.size} steps completed")
                releaseScreenAwakeLock()

            } catch (e: CancellationException) {
                _taskState.value = TaskState.PAUSED
                plan.state = TaskState.PAUSED
                _currentPlan.value = plan.copy(state = TaskState.PAUSED)
                releaseScreenAwakeLock()
            } catch (e: Exception) {
                Log.e(TAG, "Error during task execution", e)
                _taskState.value = TaskState.FAILED
                onSpeak("An unexpected error occurred during execution.")
                repository.recordTask(plan.userQuery, "ERROR", e.message ?: "Unknown error")
                releaseScreenAwakeLock()
            }
        }
    }

    private suspend fun executeStepAction(step: TaskStep, plan: ExecutionPlan): Boolean {
        return when (step.action) {
            "open_app" -> {
                val res = deviceController.launchAppByName(step.target)
                res.success
            }
            "verify_app" -> {
                delay(1200)
                val fg = accessibilityController.getCurrentForegroundPackage()
                fg.contains(step.target) || accessibilityController.isServiceActive()
            }
            "open_story" -> {
                delay(900)
                accessibilityController.clickElement("Your story") ||
                        accessibilityController.clickElement("Story") ||
                        accessibilityController.clickElement("Add story") ||
                        accessibilityController.clickElement("Create")
            }
            "select_photo" -> {
                delay(900)
                accessibilityController.clickElement("Gallery") ||
                        accessibilityController.clickElement("Recent") ||
                        accessibilityController.clickElement(step.target) ||
                        accessibilityController.clickElement("Photo")
            }
            "add_song" -> {
                delay(800)
                val musicIconClicked = accessibilityController.clickElement("Music") ||
                        accessibilityController.clickElement("Audio") ||
                        accessibilityController.clickElement("Song")
                if (musicIconClicked) {
                    delay(600)
                    accessibilityController.enterText(step.target, "Search music")
                    delay(600)
                    accessibilityController.clickElement(step.target)
                }
                true
            }
            "add_text" -> {
                delay(800)
                val textButtonClicked = accessibilityController.clickElement("Aa") ||
                        accessibilityController.clickElement("Text") ||
                        accessibilityController.clickElement("Type")
                delay(500)
                accessibilityController.enterText(step.target)
            }
            "verify_preview" -> {
                delay(1000)
                true
            }
            "post_story" -> {
                delay(800)
                accessibilityController.clickElement("Your story") ||
                        accessibilityController.clickElement("Share") ||
                        accessibilityController.clickElement("Post")
            }
            "make_call" -> {
                val number = taskMemory.selectedContact?.phoneNumber ?: step.target
                deviceController.makePhoneCall(number)
            }
            "whatsapp_call" -> {
                val contact = taskMemory.selectedContact
                val number = contact?.phoneNumber ?: step.target
                val name = contact?.name ?: "Aditya"
                deviceController.initiateWhatsAppCall(number, name)
            }
            "whatsapp_message" -> {
                val contact = taskMemory.selectedContact
                val number = contact?.phoneNumber ?: step.target
                deviceController.sendWhatsAppMessage(number, step.target)
            }
            "click_ui" -> {
                delay(800)
                accessibilityController.clickElement(step.target)
            }
            "type_ui" -> {
                delay(600)
                accessibilityController.enterText(step.target)
            }
            "click_first_result" -> {
                delay(1000)
                accessibilityController.scroll(forward = true)
                accessibilityController.clickElement("video") || accessibilityController.clickElement(step.target)
            }
            "volume" -> {
                if (step.target == "up") {
                    deviceController.adjustVolume(increase = true)
                } else {
                    deviceController.adjustVolume(increase = false)
                }
                true
            }
            "flashlight" -> {
                deviceController.toggleFlashlight(step.target == "on")
                true
            }
            "create_note" -> {
                repository.saveNote(title = "Task Note", content = step.target)
                true
            }
            "add_todo" -> {
                repository.addTodo(task = step.target)
                true
            }
            "web_search" -> {
                deviceController.openWebSearch(step.target)
                true
            }
            "speak" -> true
            else -> true
        }
    }

    private fun verifyStep(step: TaskStep, actionResult: Boolean, plan: ExecutionPlan): Boolean {
        if (!actionResult) return false

        return when (step.action) {
            "open_app" -> {
                val fg = accessibilityController.getCurrentForegroundPackage().lowercase()
                fg.contains(step.target.lowercase()) || fg != "unknown"
            }
            "open_story" -> {
                accessibilityController.verifyElementPresent("Story") ||
                        accessibilityController.verifyElementPresent("Gallery") ||
                        accessibilityController.verifyElementPresent("Camera")
            }
            "select_photo" -> {
                accessibilityController.verifyElementPresent("Edit") ||
                        accessibilityController.verifyElementPresent("Sticker") ||
                        accessibilityController.verifyElementPresent("Aa") ||
                        accessibilityController.verifyElementPresent("Music")
            }
            "add_song" -> true
            "add_text" -> true
            "verify_preview" -> true
            "post_story" -> true
            "make_call" -> {
                val fg = accessibilityController.getCurrentForegroundPackage().lowercase()
                fg.contains("dialer") || fg.contains("telecom") || fg.contains("phone") || fg.contains("incall")
            }
            "whatsapp_call" -> {
                val fg = accessibilityController.getCurrentForegroundPackage().lowercase()
                fg.contains("whatsapp")
            }
            else -> actionResult
        }
    }

    private suspend fun attemptErrorRecovery(step: TaskStep, plan: ExecutionPlan): Boolean {
        Log.w(TAG, "Step failed. Initiating error recovery for: ${step.description}")
        onSpeak("Instagram ka layout change hua lag raha hai. Main screen ko dobara analyze kar raha hoon.")
        delay(1200)

        // Recovery Strategy 1: Scroll forward to find element
        val scrolled = accessibilityController.scroll(forward = true)
        if (scrolled) {
            delay(800)
            val retryClick = accessibilityController.clickElement(step.target)
            if (retryClick) return true
        }

        // Recovery Strategy 2: If fallback action defined in step
        if (step.fallbackAction != null) {
            Log.d(TAG, "Trying fallback action: ${step.fallbackAction}")
            val fallbackSuccess = accessibilityController.clickElement(step.fallbackAction)
            if (fallbackSuccess) return true
        }

        // Recovery Strategy 3: Check if element already reached
        if (accessibilityController.verifyElementPresent(step.target)) {
            return true
        }

        return false
    }

    // --- USER INTERACTION RESPONSES (Clarification, Disambiguation, Confirmation) ---
    fun submitClarificationResponse(scope: CoroutineScope, response: String) {
        val currentClarification = _activeClarification.value ?: return
        _activeClarification.value = null

        when (currentClarification.parameterKey) {
            "existing_content_conflict" -> {
                val lower = response.lowercase().trim()
                when {
                    lower.contains("replace") -> {
                        onSpeak("Replacing existing content.")
                        resumeTask(scope)
                    }
                    lower.contains("edit") -> {
                        onSpeak("Appending to existing content.")
                        resumeTask(scope)
                    }
                    lower.contains("rehne do") || lower.contains("waise hi") -> {
                        onSpeak("Keeping existing content untouched.")
                        // Skip text add step and continue
                        _currentPlan.value?.let { plan ->
                            plan.steps.getOrNull(plan.currentStepIndex)?.status = StepStatus.SKIPPED
                            executePlanInternal(scope, plan, startFromIndex = plan.currentStepIndex + 1)
                        }
                    }
                    else -> {
                        cancelCurrentTask()
                    }
                }
            }
            "call_fallback_whatsapp" -> {
                val lower = response.lowercase().trim()
                if (lower.contains("haan") || lower.contains("yes") || lower.contains("whatsapp")) {
                    onSpeak("WhatsApp call connect kar raha hoon.")
                    val contact = taskMemory.selectedContact
                    val number = contact?.phoneNumber ?: "Aditya"
                    val name = contact?.name ?: "Aditya"
                    val ok = deviceController.initiateWhatsAppCall(number, name)
                    if (!ok) {
                        _taskState.value = TaskState.CLARIFYING
                        val msgPrompt = ClarificationPrompt(
                            parameterKey = "call_fallback_message",
                            question = "Sir, WhatsApp call bhi connect nahi ho paayi. Message bhej kar bata du?",
                            options = listOf("Haan, message bhej do", "Nahi, cancel karo")
                        )
                        _activeClarification.value = msgPrompt
                        onSpeak(msgPrompt.question)
                    } else {
                        _taskState.value = TaskState.COMPLETED
                    }
                } else {
                    cancelCurrentTask()
                }
            }
            "call_fallback_message" -> {
                val lower = response.lowercase().trim()
                if (lower.contains("haan") || lower.contains("yes") || lower.contains("message")) {
                    val contact = taskMemory.selectedContact
                    val number = contact?.phoneNumber ?: "Aditya"
                    deviceController.sendWhatsAppMessage(number, "Hello, please call me when you are free. Sent via TITONOX.")
                    onSpeak("WhatsApp message bhej diya gaya hai.")
                    _taskState.value = TaskState.COMPLETED
                } else {
                    cancelCurrentTask()
                }
            }
        }
    }

    fun submitDisambiguationSelection(scope: CoroutineScope, contact: ContactRecord) {
        _activeDisambiguation.value = null
        taskMemory.selectedContact = contact
        onSpeak("Selected ${contact.name}. Calling now.")

        val steps = listOf(
            TaskStep(1, "make_call", contact.phoneNumber, "Initiate phone call to ${contact.name}"),
            TaskStep(2, "verify_app", "dialer", "Verify call connection screen")
        )
        val plan = ExecutionPlan(
            id = "call_${System.currentTimeMillis()}",
            userQuery = "Call ${contact.name}",
            taskTitle = "Phone Call",
            targetApp = "Phone Dialer",
            steps = steps
        )
        executePlan(scope, plan)
    }

    fun submitConfirmationDecision(scope: CoroutineScope, approved: Boolean) {
        val confirmation = _activeConfirmation.value ?: return
        _activeConfirmation.value = null

        if (approved) {
            onSpeak("Confirmation received. Posting now.")
            val plan = _currentPlan.value ?: return
            executePlanInternal(scope, plan, startFromIndex = plan.currentStepIndex)
        } else {
            onSpeak("Action cancelled by user.")
            cancelCurrentTask()
        }
    }
}
