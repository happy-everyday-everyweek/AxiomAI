package com.ai.assistance.operit.core.tools

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ToolProgressEvent(
    val toolName: String,
    val progress: Float,
    val message: String = "",
    val priority: Int = 0,
    val level: Int = 0,
    val taskId: String? = null,
    val taskStatus: String? = null,
    val retryCount: Int = 0
)

data class TaskProgressEvent(
    val taskId: String,
    val chatId: String,
    val status: String,
    val progress: Float = 0f,
    val message: String = "",
    val checkpoint: String = "",
    val retryCount: Int = 0,
    val lastError: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object ToolProgressBus {
    const val SUMMARY_PROGRESS_TOOL_NAME: String = "__SUMMARY__"

    private val _progress = MutableStateFlow<ToolProgressEvent?>(null)
    val progress: StateFlow<ToolProgressEvent?> = _progress.asStateFlow()

    private val _taskProgress = MutableStateFlow<TaskProgressEvent?>(null)
    val taskProgress: StateFlow<TaskProgressEvent?> = _taskProgress.asStateFlow()

    private fun priorityForTool(toolName: String): Int {
        return when (toolName) {
            SUMMARY_PROGRESS_TOOL_NAME -> 1000
            "grep_context" -> 100
            "grep_code" -> 10
            "find_files" -> 5
            else -> 0
        }
    }

    fun update(toolName: String, progress: Float, message: String = "") {
        update(toolName = toolName, progress = progress, message = message, priority = priorityForTool(toolName))
    }

    fun update(toolName: String, progress: Float, message: String = "", priority: Int, level: Int = 0) {
        update(toolName = toolName, progress = progress, message = message, priority = priority, level = level, taskId = null, taskStatus = null, retryCount = 0)
    }

    fun update(
        toolName: String,
        progress: Float,
        message: String = "",
        priority: Int = priorityForTool(toolName),
        level: Int = 0,
        taskId: String? = null,
        taskStatus: String? = null,
        retryCount: Int = 0
    ) {
        val next = ToolProgressEvent(
            toolName = toolName,
            progress = progress,
            message = message,
            priority = priority,
            level = level,
            taskId = taskId,
            taskStatus = taskStatus,
            retryCount = retryCount
        )

        val current = _progress.value
        val shouldReplace =
            current == null ||
                current.toolName == next.toolName ||
                current.progress >= 1f ||
                next.priority > current.priority ||
                (next.priority == current.priority && next.level >= current.level)

        if (shouldReplace) {
            _progress.value = next
        }
    }

    fun updateTaskProgress(event: TaskProgressEvent) {
        _taskProgress.value = event
        val toolMessage = if (event.message.isNotBlank()) event.message else "Task ${event.taskId.take(8)}: ${event.status}"
        update(
            toolName = "__TASK_${event.taskId}__",
            progress = event.progress,
            message = toolMessage,
            priority = 500,
            taskId = event.taskId,
            taskStatus = event.status,
            retryCount = event.retryCount
        )
    }

    fun updateTaskProgress(
        taskId: String,
        chatId: String,
        status: String,
        progress: Float = 0f,
        message: String = "",
        retryCount: Int = 0,
        lastError: String = ""
    ) {
        updateTaskProgress(TaskProgressEvent(
            taskId = taskId,
            chatId = chatId,
            status = status,
            progress = progress,
            message = message,
            retryCount = retryCount,
            lastError = lastError
        ))
    }

    fun clearTaskProgress(taskId: String) {
        val current = _taskProgress.value
        if (current?.taskId == taskId) {
            _taskProgress.value = null
        }
        val currentTool = _progress.value
        if (currentTool?.taskId == taskId) {
            _progress.value = null
        }
    }

    fun clear() {
        _progress.value = null
    }
}
