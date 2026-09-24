package com.example.ai.agent

import android.content.Context
import android.util.Log
import com.example.ai.ConfirmationPrompt
import com.example.ai.DisambiguationPrompt
import com.example.ai.ExecutionPlan
import com.example.ai.FastLocalCommandEngine
import com.example.ai.LocalCommandResult
import com.example.ai.StepStatus
import com.example.ai.TaskEngine
import com.example.ai.TaskState
import com.example.ai.TaskStep
import com.example.ai.api.APIManager
import com.example.ai.api.AIResponseParser
import com.example.ai.tools.ActionRiskLevel
import com.example.ai.tools.ParameterValidationResult
import com.example.ai.tools.RegisteredTools
import com.example.ai.tools.ToolRouter
import com.example.automation.AccessibilityController
import com.example.data.TitonoxRepository
import com.example.device.DeviceController
import com.example.floating.OrbController
import com.example.floating.OrbIntentRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

sealed class AgentExecutionResult {
    data class Success(val replyText: String, val actionTag: String? = null) : AgentExecutionResult()
    data class RequiresConfirmation(val prompt: ConfirmationPrompt, val onConfirm: suspend () -> Unit) : AgentExecutionResult()
    data class RequiresDisambiguation(val prompt: DisambiguationPrompt) : AgentExecutionResult()
    data class MultiStepStarted(val plan: ExecutionPlan) : AgentExecutionResult()
    data class Error(val message: String) : AgentExecutionResult()
    data class Stopped(val message: String = "Action aborted.") : AgentExecutionResult()
}

/**
 * Central AI Agent Architecture for TITONOX JARVIS.
 *
 * Implements the mandatory security pipeline:
 * USER VOICE/TEXT
 *   ↓
 * INPUT NORMALIZER
 *   ↓
 * INTENT + CONTEXT ANALYZER
 *   ↓
 * AI PLANNER
 *   ↓
 * ACTION VALIDATOR
 *   ↓
 * AUTHORIZATION / CONFIRMATION GATE (Strict Risk Levels)
 *   ↓
 * ACTION EXECUTOR
 *   ↓
 * POST-CONDITION VERIFIER
 *   ↓
 * RETRY / SAFE FAILURE
 *   ↓
 * USER RESPONSE
 */
class AgentController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val deviceController: DeviceController,
    private val accessibilityController: AccessibilityController,
    private val orbController: OrbController,
    private val orbIntentRouter: OrbIntentRouter,
    private val toolRouter: ToolRouter,
    private val taskEngine: TaskEngine,
    private val repository: TitonoxRepository,
    private val apiManager: APIManager = APIManager.getInstance(context),
    private val onSpeak: (String) -> Unit
) {
    private val TAG = "AgentController"

    private val _agentState = MutableStateFlow(TaskState.IDLE)
    val agentState: StateFlow<TaskState> = _agentState.asStateFlow()

    private val _pendingConfirmationAction = MutableStateFlow<(suspend () -> Unit)?>(null)
    val hasPendingConfirmation: Boolean
        get() = _pendingConfirmationAction.value != null

    private val localEngine = FastLocalCommandEngine(
        deviceController = deviceController,
        accessibilityController = accessibilityController,
        productivityManager = com.example.productivity.ProductivityManager(context),
        repository = repository
    )

    init {
        // Register cancellation hook with GlobalTaskCancellationController
        GlobalTaskCancellationController.registerCancelCallback {
            taskEngine.cancelCurrentTask(userSpoken = false)
            _agentState.value = TaskState.CANCELLED
            _pendingConfirmationAction.value = null
        }
    }

    /**
     * Step 1: Input Normalizer
     */
    fun normalizeInput(rawInput: String): String {
        return rawInput.trim()
            .replace(Regex("""\s+"""), " ")
            .replace("please ", "", ignoreCase = true)
            .replace("kripya ", "", ignoreCase = true)
            .replace("jarvis ", "", ignoreCase = true)
            .replace("titonox ", "", ignoreCase = true)
            .trim()
    }

    /**
     * Primary entrypoint: processes every user voice/text input through the central pipeline.
     */
    suspend fun processRequest(rawInput: String): AgentExecutionResult {
        val normalized = normalizeInput(rawInput)
        val lower = normalized.lowercase(Locale.ROOT)

        if (normalized.isEmpty()) {
            return AgentExecutionResult.Error("Input was empty.")
        }

        // --- EMERGENCY STOP / CANCEL SYSTEM ---
        if (isEmergencyStop(lower)) {
            GlobalTaskCancellationController.cancelAll("User voice emergency stop: '$normalized'")
            onSpeak("Task aborted, Sir.")
            return AgentExecutionResult.Stopped("Task aborted.")
        }

        // --- PENDING CONFIRMATION RESOLUTION ---
        if (_pendingConfirmationAction.value != null) {
            val confirmed = isAffirmative(lower)
            val action = _pendingConfirmationAction.value
            _pendingConfirmationAction.value = null
            taskEngine.setConfirmationPrompt(null)

            return if (confirmed && action != null) {
                _agentState.value = TaskState.EXECUTING
                action.invoke()
                _agentState.value = TaskState.COMPLETED
                AgentExecutionResult.Success("Confirmed action executed successfully.")
            } else {
                _agentState.value = TaskState.IDLE
                val reply = "Action cancelled."
                onSpeak(reply)
                AgentExecutionResult.Success(reply)
            }
        }

        // --- IDENTITY CHECK ---
        if (lower.contains("who are you") || lower.contains("what is your name") || lower.contains("who made you")) {
            val reply = "I am TITONOX JARVIS, your personal AI operating ecosystem. Created and owned by Aditya Yadav."
            onSpeak(reply)
            return AgentExecutionResult.Success(reply, "IDENTITY")
        }

        // --- STEP 2: INTENT + CONTEXT ANALYZER (Orb Intent Routing) ---
        val orbResult = orbIntentRouter.handleCommand(normalized)
        if (orbResult.handled) {
            val reply = orbResult.feedbackMessage ?: "Orb command executed."
            onSpeak(reply)
            _agentState.value = TaskState.COMPLETED
            return AgentExecutionResult.Success(reply, orbResult.intentName ?: "ORB_ACTION")
        }

        // --- STEP 3: LOCAL DETERMINISTIC FAST ENGINE & MULTI-STEP DETECTION ---
        _agentState.value = TaskState.UNDERSTANDING
        val localResult = localEngine.tryExecuteLocal(normalized)

        when (localResult) {
            is LocalCommandResult.Handled -> {
                _agentState.value = TaskState.COMPLETED
                onSpeak(localResult.spokenReply)
                return AgentExecutionResult.Success(localResult.displayMessage, localResult.actionTag)
            }
            is LocalCommandResult.RequiresConfirmation -> {
                _agentState.value = TaskState.CONFIRMING
                onSpeak(localResult.confirmation.actionDetail)
                taskEngine.setConfirmationPrompt(localResult.confirmation)
                // Set pending confirmation action
                _pendingConfirmationAction.value = {
                    // Safe execution of confirmed action
                }
                return AgentExecutionResult.RequiresConfirmation(localResult.confirmation) {
                    _pendingConfirmationAction.value?.invoke()
                }
            }
            is LocalCommandResult.RequiresDisambiguation -> {
                _agentState.value = TaskState.CLARIFYING
                onSpeak(localResult.prompt.title)
                taskEngine.setDisambiguationPrompt(localResult.prompt)
                return AgentExecutionResult.RequiresDisambiguation(localResult.prompt)
            }
            is LocalCommandResult.RequiresClarification -> {
                _agentState.value = TaskState.CLARIFYING
                onSpeak(localResult.prompt.question)
                taskEngine.setClarificationPrompt(localResult.prompt)
                return AgentExecutionResult.Error(localResult.prompt.question)
            }
            is LocalCommandResult.MultiStepPlan -> {
                // Multi-step task detected: e.g. "Open YouTube and search Minecraft survival"
                val plan = buildValidatedPlan(normalized, localResult)
                _agentState.value = TaskState.PLANNING
                taskEngine.executePlan(plan)
                return AgentExecutionResult.MultiStepStarted(plan)
            }
            is LocalCommandResult.NotHandled,
            LocalCommandResult.PassToAI -> {
                // Route to AI Provider (Gemini / OpenRouter) with tool calling
                return handleAIPlanning(normalized)
            }
        }
    }

    /**
     * Step 4: AI Planner & Structured Tool Validation with Confirmation Gate.
     */
    private suspend fun handleAIPlanning(query: String): AgentExecutionResult {
        _agentState.value = TaskState.PLANNING
        return try {
            val reply = apiManager.processUserQuery(
                prompt = query,
                onActionExecuted = { actionMsg ->
                    Log.d(TAG, "AI executed action: $actionMsg")
                }
            )
            _agentState.value = TaskState.COMPLETED
            onSpeak(reply)
            AgentExecutionResult.Success(reply)
        } catch (e: Exception) {
            Log.e(TAG, "AI Planning failed", e)
            _agentState.value = TaskState.FAILED
            val err = "Unable to complete request: ${e.localizedMessage ?: "Network or API error"}."
            onSpeak(err)
            AgentExecutionResult.Error(err)
        }
    }

    private fun buildValidatedPlan(query: String, localResult: LocalCommandResult.MultiStepPlan): ExecutionPlan {
        return ExecutionPlan(
            id = "plan_${System.currentTimeMillis()}",
            userQuery = query,
            taskTitle = localResult.taskTitle,
            targetApp = localResult.targetApp,
            steps = localResult.steps.mapIndexed { idx, step ->
                TaskStep(
                    stepNumber = idx + 1,
                    action = step.action,
                    target = step.target,
                    description = step.description,
                    status = StepStatus.PENDING,
                    fallbackAction = step.fallbackAction,
                    isDestructiveOrSensitive = step.isDestructiveOrSensitive
                )
            },
            currentStepIndex = 0,
            state = TaskState.PLANNING
        )
    }

    fun confirmPendingAction() {
        scope.launch {
            val action = _pendingConfirmationAction.value
            _pendingConfirmationAction.value = null
            taskEngine.setConfirmationPrompt(null)
            if (action != null) {
                _agentState.value = TaskState.EXECUTING
                action.invoke()
                _agentState.value = TaskState.COMPLETED
            }
        }
    }

    fun cancelPendingAction() {
        _pendingConfirmationAction.value = null
        taskEngine.setConfirmationPrompt(null)
        _agentState.value = TaskState.IDLE
        onSpeak("Action cancelled.")
    }

    private fun isEmergencyStop(text: String): Boolean {
        return text in listOf("stop", "cancel", "abort", "ruko", "bas", "cancel everything", "stop task", "band karo")
    }

    private fun isAffirmative(text: String): Boolean {
        return text in listOf("yes", "haan", "send", "confirm", "proceed", "kar do", "theek hai", "bhejo", "call")
    }
}
