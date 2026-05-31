package com.ai.assistance.operit.core.tools.defaultTool.standard

import android.content.Context
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ScheduleItem
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.data.repository.ScheduleRepository
import com.ai.assistance.operit.util.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class StandardScheduleTools(private val context: Context) {

    companion object {
        private const val TAG = "StandardScheduleTools"
    }

    private val scheduleRepository = ScheduleRepository(context)

    suspend fun createSchedule(tool: AITool): ToolResult {
        return try {
            val title = tool.parameters.find { it.name == "title" }?.value
            if (title.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule title cannot be empty"
                )
            }

            val description = tool.parameters.find { it.name == "description" }?.value ?: ""
            val startTimeStr = tool.parameters.find { it.name == "start_time" }?.value
            val startTime = startTimeStr?.toLongOrNull() ?: System.currentTimeMillis()
            val endTimeStr = tool.parameters.find { it.name == "end_time" }?.value
            val endTime = endTimeStr?.toLongOrNull() ?: startTime + 3600000L
            val recurrence = tool.parameters.find { it.name == "recurrence" }?.value
                ?.takeIf { it in listOf(ScheduleItem.RECURRENCE_NONE, ScheduleItem.RECURRENCE_DAILY, ScheduleItem.RECURRENCE_WEEKLY, ScheduleItem.RECURRENCE_MONTHLY, ScheduleItem.RECURRENCE_YEARLY) }
                ?: ScheduleItem.RECURRENCE_NONE
            val reminderStr = tool.parameters.find { it.name == "reminder" }?.value
            val reminder = reminderStr?.toLongOrNull() ?: 0L

            val scheduleId = UUID.randomUUID().toString()
            val item = ScheduleItem(
                scheduleId = scheduleId,
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                recurrence = recurrence,
                reminder = reminder
            )

            scheduleRepository.insert(item)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Schedule created: $scheduleId - $title (start=$startTime, end=$endTime, recurrence=$recurrence)")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to create schedule", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to create schedule: ${e.message}"
            )
        }
    }

    suspend fun listSchedules(tool: AITool): ToolResult {
        return try {
            val startTimeStr = tool.parameters.find { it.name == "start_time" }?.value
            val endTimeStr = tool.parameters.find { it.name == "end_time" }?.value

            val items = when {
                !startTimeStr.isNullOrBlank() && !endTimeStr.isNullOrBlank() -> {
                    val start = startTimeStr.toLongOrNull() ?: 0L
                    val end = endTimeStr.toLongOrNull() ?: Long.MAX_VALUE
                    scheduleRepository.getByDateRange(start, end)
                }
                else -> scheduleRepository.getUpcomingSchedules()
            }

            val json = JSONArray()
            items.forEach { item ->
                json.put(scheduleItemToJson(item))
            }

            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Schedules (${items.size}):\n${json.toString(2)}")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to list schedules", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to list schedules: ${e.message}"
            )
        }
    }

    suspend fun getSchedule(tool: AITool): ToolResult {
        return try {
            val scheduleId = tool.parameters.find { it.name == "schedule_id" }?.value
            if (scheduleId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule ID cannot be empty"
                )
            }

            val item = scheduleRepository.getByScheduleId(scheduleId)
            if (item == null) {
                ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule not found: $scheduleId"
                )
            } else {
                ToolResult(
                    toolName = tool.name,
                    success = true,
                    result = StringResultData(scheduleItemToJson(item).toString(2))
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get schedule", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to get schedule: ${e.message}"
            )
        }
    }

    suspend fun updateSchedule(tool: AITool): ToolResult {
        return try {
            val scheduleId = tool.parameters.find { it.name == "schedule_id" }?.value
            if (scheduleId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule ID cannot be empty"
                )
            }

            val item = scheduleRepository.getByScheduleId(scheduleId)
            if (item == null) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule not found: $scheduleId"
                )
            }

            tool.parameters.find { it.name == "title" }?.value?.let { item.title = it }
            tool.parameters.find { it.name == "description" }?.value?.let { item.description = it }
            tool.parameters.find { it.name == "start_time" }?.value?.toLongOrNull()?.let { item.startTime = it }
            tool.parameters.find { it.name == "end_time" }?.value?.toLongOrNull()?.let { item.endTime = it }
            tool.parameters.find { it.name == "recurrence" }?.value?.let {
                if (it in listOf(ScheduleItem.RECURRENCE_NONE, ScheduleItem.RECURRENCE_DAILY, ScheduleItem.RECURRENCE_WEEKLY, ScheduleItem.RECURRENCE_MONTHLY, ScheduleItem.RECURRENCE_YEARLY)) {
                    item.recurrence = it
                }
            }
            tool.parameters.find { it.name == "reminder" }?.value?.toLongOrNull()?.let { item.reminder = it }

            scheduleRepository.update(item)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Schedule updated: $scheduleId - ${item.title}")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update schedule", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to update schedule: ${e.message}"
            )
        }
    }

    suspend fun deleteSchedule(tool: AITool): ToolResult {
        return try {
            val scheduleId = tool.parameters.find { it.name == "schedule_id" }?.value
            if (scheduleId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Schedule ID cannot be empty"
                )
            }

            scheduleRepository.deleteByScheduleId(scheduleId)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Schedule deleted: $scheduleId")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete schedule", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to delete schedule: ${e.message}"
            )
        }
    }

    private fun scheduleItemToJson(item: ScheduleItem): JSONObject {
        return JSONObject().apply {
            put("schedule_id", item.scheduleId)
            put("title", item.title)
            put("description", item.description)
            put("start_time", item.startTime)
            put("end_time", item.endTime)
            put("recurrence", item.recurrence)
            put("reminder", item.reminder)
            put("created_at", item.createdAt)
            put("updated_at", item.updatedAt)
        }
    }
}
