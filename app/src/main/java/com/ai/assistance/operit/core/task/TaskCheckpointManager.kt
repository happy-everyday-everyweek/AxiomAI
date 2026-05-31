package com.ai.assistance.operit.core.task

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TaskCheckpoint(
    val taskId: String,
    val chatId: String,
    val lastProcessedMessageIndex: Int = -1,
    val lastToolInvocationIndex: Int = -1,
    val partialResult: String = "",
    val metadata: Map<String, String> = emptyMap()
)

class TaskCheckpointManager(private val context: Context) {

    companion object {
        private const val TAG = "TaskCheckpointManager"

        @Volatile
        private var INSTANCE: TaskCheckpointManager? = null

        fun getInstance(context: Context): TaskCheckpointManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskCheckpointManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val repository by lazy { TaskRepository.getInstance(context) }

    fun saveCheckpoint(checkpoint: TaskCheckpoint) {
        try {
            val serialized = json.encodeToString(checkpoint)
            repository.updateCheckpoint(checkpoint.taskId, serialized)
            AppLogger.d(TAG, "Checkpoint saved for task: ${checkpoint.taskId}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save checkpoint for task: ${checkpoint.taskId}", e)
        }
    }

    fun restoreCheckpoint(taskId: String): TaskCheckpoint? {
        try {
            val record = repository.getByTaskId(taskId) ?: return null
            if (record.checkpoint.isBlank()) return null
            return json.decodeFromString<TaskCheckpoint>(record.checkpoint)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to restore checkpoint for task: $taskId", e)
            return null
        }
    }

    fun createAndSaveCheckpoint(
        taskId: String,
        chatId: String,
        lastProcessedMessageIndex: Int = -1,
        lastToolInvocationIndex: Int = -1,
        partialResult: String = "",
        metadata: Map<String, String> = emptyMap()
    ): TaskCheckpoint {
        val checkpoint = TaskCheckpoint(
            taskId = taskId,
            chatId = chatId,
            lastProcessedMessageIndex = lastProcessedMessageIndex,
            lastToolInvocationIndex = lastToolInvocationIndex,
            partialResult = partialResult,
            metadata = metadata
        )
        saveCheckpoint(checkpoint)
        return checkpoint
    }

    fun clearCheckpoint(taskId: String) {
        repository.updateCheckpoint(taskId, "")
        AppLogger.d(TAG, "Checkpoint cleared for task: $taskId")
    }

    fun hasCheckpoint(taskId: String): Boolean {
        val record = repository.getByTaskId(taskId) ?: return false
        return record.checkpoint.isNotBlank()
    }

    fun registerTask(taskId: String, chatId: String): TaskRecord {
        val existing = repository.getByTaskId(taskId)
        if (existing != null) {
            return existing
        }
        val record = TaskRecord(
            taskId = taskId,
            chatId = chatId,
            status = TaskRecord.STATUS_PENDING
        )
        repository.insert(record)
        return record
    }

    fun markRunning(taskId: String) {
        repository.updateStatus(taskId, TaskRecord.STATUS_RUNNING)
    }

    fun markCompleted(taskId: String) {
        repository.updateStatus(taskId, TaskRecord.STATUS_COMPLETED)
        clearCheckpoint(taskId)
    }

    fun markFailed(taskId: String, error: String) {
        val record = repository.getByTaskId(taskId) ?: return
        record.status = TaskRecord.STATUS_FAILED
        record.lastError = error
        repository.update(record)
    }

    fun markPaused(taskId: String) {
        repository.updateStatus(taskId, TaskRecord.STATUS_PAUSED)
    }

    fun resumeTask(taskId: String): TaskCheckpoint? {
        val record = repository.getByTaskId(taskId) ?: return null
        if (record.status != TaskRecord.STATUS_PAUSED && record.status != TaskRecord.STATUS_FAILED) {
            return null
        }
        repository.updateStatus(taskId, TaskRecord.STATUS_RUNNING)
        return restoreCheckpoint(taskId)
    }

    fun getResumableTasks(chatId: String): List<TaskRecord> {
        val tasks = repository.getByChatId(chatId)
        return tasks.filter {
            it.status == TaskRecord.STATUS_PAUSED || it.status == TaskRecord.STATUS_FAILED
        }
    }
}
