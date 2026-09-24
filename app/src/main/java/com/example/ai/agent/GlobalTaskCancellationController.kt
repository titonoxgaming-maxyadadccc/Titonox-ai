package com.example.ai.agent

import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Global Emergency Stop & Cancellation Controller.
 * Instantly terminates all running jobs, pending tasks, speech, and accessibility operations.
 */
object GlobalTaskCancellationController {
    private const val TAG = "GlobalTaskCancel"

    private val _isCancelled = MutableStateFlow(false)
    val isCancelled: StateFlow<Boolean> = _isCancelled.asStateFlow()

    private val registeredJobs = CopyOnWriteArrayList<Job>()
    private val cancelCallbacks = CopyOnWriteArrayList<() -> Unit>()

    fun registerJob(job: Job) {
        registeredJobs.add(job)
        job.invokeOnCompletion {
            registeredJobs.remove(job)
        }
    }

    fun registerCancelCallback(callback: () -> Unit) {
        cancelCallbacks.add(callback)
    }

    fun unregisterCancelCallback(callback: () -> Unit) {
        cancelCallbacks.remove(callback)
    }

    /**
     * Emergency abort invoked by "Stop", "Cancel", "Abort", "Ruko", "Bas", or UI Stop button.
     */
    fun cancelAll(reason: String = "User requested emergency stop") {
        Log.w(TAG, "EMERGENCY STOP TRIGGERED: $reason")
        _isCancelled.value = true

        for (job in registeredJobs) {
            try {
                if (job.isActive) {
                    job.cancel()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling job", e)
            }
        }
        registeredJobs.clear()

        for (cb in cancelCallbacks) {
            try {
                cb()
            } catch (e: Exception) {
                Log.e(TAG, "Error executing cancellation callback", e)
            }
        }

        // Reset cancellation flag after propagation
        _isCancelled.value = false
    }
}
