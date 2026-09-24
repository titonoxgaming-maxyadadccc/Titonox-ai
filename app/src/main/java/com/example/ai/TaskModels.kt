package com.example.ai

enum class TaskState {
    IDLE,
    LISTENING,
    UNDERSTANDING,
    CLARIFYING,
    CONFIRMING,
    PLANNING,
    EXECUTING,
    VERIFYING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class StepStatus {
    PENDING,
    EXECUTING,
    VERIFYING,
    SUCCESS,
    FAILED,
    SKIPPED
}

data class ContactRecord(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val hasWhatsApp: Boolean = true,
    val avatarColor: Long = 0xFF00E5FF
)

data class ClarificationPrompt(
    val parameterKey: String,
    val question: String,
    val options: List<String> = emptyList(),
    val isDestructiveConflict: Boolean = false
)

data class ConfirmationPrompt(
    val actionTitle: String,
    val actionDetail: String,
    val riskLevel: String = "HIGH", // "HIGH", "MEDIUM"
    val targetApp: String = "System",
    val confirmButtonText: String = "Post / Confirm",
    val cancelButtonText: String = "Cancel"
)

data class DisambiguationPrompt(
    val title: String,
    val query: String,
    val contacts: List<ContactRecord>
)

data class TaskStep(
    val stepNumber: Int,
    val action: String,
    val target: String,
    val description: String,
    var status: StepStatus = StepStatus.PENDING,
    var verificationResult: String? = null,
    val fallbackAction: String? = null,
    val isDestructiveOrSensitive: Boolean = false
) {
    val isCompleted: Boolean
        get() = status == StepStatus.SUCCESS
}

data class TaskMemory(
    var activeIntent: String? = null,
    var targetApp: String? = null,
    var selectedMedia: String? = null,
    var selectedSong: String? = null,
    var enteredText: String? = null,
    var selectedContact: ContactRecord? = null,
    var existingContentFound: String? = null,
    var lastVerifiedStepIndex: Int = -1,
    var failedAttemptCount: Int = 0
)

data class ExecutionPlan(
    val id: String,
    val userQuery: String,
    val taskTitle: String,
    val targetApp: String,
    val steps: List<TaskStep>,
    var currentStepIndex: Int = 0,
    var state: TaskState = TaskState.PLANNING,
    val memory: TaskMemory = TaskMemory()
)

sealed class LocalCommandResult {
    data class Handled(
        val spokenReply: String,
        val displayMessage: String,
        val actionTag: String? = null
    ) : LocalCommandResult()

    data class MultiStepPlan(
        val steps: List<TaskStep>,
        val query: String,
        val taskTitle: String = "Automated Task",
        val targetApp: String = "System"
    ) : LocalCommandResult()

    data class RequiresClarification(
        val prompt: ClarificationPrompt,
        val partialPlan: ExecutionPlan? = null
    ) : LocalCommandResult()

    data class RequiresDisambiguation(
        val prompt: DisambiguationPrompt
    ) : LocalCommandResult()

    data class RequiresConfirmation(
        val confirmation: ConfirmationPrompt,
        val pendingPlan: ExecutionPlan
    ) : LocalCommandResult()

    object NotHandled : LocalCommandResult()
    object PassToAI : LocalCommandResult()
}
