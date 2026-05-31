package com.ai.assistance.operit.core.tools.defaultTool.standard

import android.content.Context
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.TodoItem
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.data.repository.TodoRepository
import com.ai.assistance.operit.util.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class StandardTodoTools(private val context: Context) {

    companion object {
        private const val TAG = "StandardTodoTools"
    }

    private val todoRepository = TodoRepository(context)

    suspend fun createTodo(tool: AITool): ToolResult {
        return try {
            val title = tool.parameters.find { it.name == "title" }?.value
            if (title.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo title cannot be empty"
                )
            }

            val description = tool.parameters.find { it.name == "description" }?.value ?: ""
            val dueDateStr = tool.parameters.find { it.name == "due_date" }?.value
            val dueDate = dueDateStr?.toLongOrNull() ?: 0L
            val priority = tool.parameters.find { it.name == "priority" }?.value
                ?.takeIf { it in listOf(TodoItem.PRIORITY_LOW, TodoItem.PRIORITY_MEDIUM, TodoItem.PRIORITY_HIGH, TodoItem.PRIORITY_URGENT) }
                ?: TodoItem.PRIORITY_MEDIUM
            val status = tool.parameters.find { it.name == "status" }?.value
                ?.takeIf { it in listOf(TodoItem.STATUS_PENDING, TodoItem.STATUS_IN_PROGRESS, TodoItem.STATUS_COMPLETED, TodoItem.STATUS_CANCELLED) }
                ?: TodoItem.STATUS_PENDING

            val todoId = UUID.randomUUID().toString()
            val item = TodoItem(
                todoId = todoId,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                status = status
            )

            val id = todoRepository.insert(item)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Todo created: $todoId - $title (priority=$priority, status=$status)")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to create todo", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to create todo: ${e.message}"
            )
        }
    }

    suspend fun listTodos(tool: AITool): ToolResult {
        return try {
            val statusFilter = tool.parameters.find { it.name == "status" }?.value
            val priorityFilter = tool.parameters.find { it.name == "priority" }?.value

            val items = when {
                !statusFilter.isNullOrBlank() -> todoRepository.getByStatus(statusFilter)
                else -> todoRepository.getActiveTodos()
            }

            val filtered = if (!priorityFilter.isNullOrBlank()) {
                items.filter { it.priority == priorityFilter }
            } else {
                items
            }

            val json = JSONArray()
            filtered.forEach { item ->
                json.put(todoItemToJson(item))
            }

            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Todos (${filtered.size}):\n${json.toString(2)}")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to list todos", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to list todos: ${e.message}"
            )
        }
    }

    suspend fun getTodo(tool: AITool): ToolResult {
        return try {
            val todoId = tool.parameters.find { it.name == "todo_id" }?.value
            if (todoId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo ID cannot be empty"
                )
            }

            val item = todoRepository.getByTodoId(todoId)
            if (item == null) {
                ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo not found: $todoId"
                )
            } else {
                ToolResult(
                    toolName = tool.name,
                    success = true,
                    result = StringResultData(todoItemToJson(item).toString(2))
                )
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get todo", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to get todo: ${e.message}"
            )
        }
    }

    suspend fun updateTodo(tool: AITool): ToolResult {
        return try {
            val todoId = tool.parameters.find { it.name == "todo_id" }?.value
            if (todoId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo ID cannot be empty"
                )
            }

            val item = todoRepository.getByTodoId(todoId)
            if (item == null) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo not found: $todoId"
                )
            }

            tool.parameters.find { it.name == "title" }?.value?.let { item.title = it }
            tool.parameters.find { it.name == "description" }?.value?.let { item.description = it }
            tool.parameters.find { it.name == "due_date" }?.value?.toLongOrNull()?.let { item.dueDate = it }
            tool.parameters.find { it.name == "priority" }?.value?.let {
                if (it in listOf(TodoItem.PRIORITY_LOW, TodoItem.PRIORITY_MEDIUM, TodoItem.PRIORITY_HIGH, TodoItem.PRIORITY_URGENT)) {
                    item.priority = it
                }
            }
            tool.parameters.find { it.name == "status" }?.value?.let {
                if (it in listOf(TodoItem.STATUS_PENDING, TodoItem.STATUS_IN_PROGRESS, TodoItem.STATUS_COMPLETED, TodoItem.STATUS_CANCELLED)) {
                    item.status = it
                }
            }

            todoRepository.update(item)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Todo updated: $todoId - ${item.title}")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update todo", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to update todo: ${e.message}"
            )
        }
    }

    suspend fun deleteTodo(tool: AITool): ToolResult {
        return try {
            val todoId = tool.parameters.find { it.name == "todo_id" }?.value
            if (todoId.isNullOrBlank()) {
                return ToolResult(
                    toolName = tool.name,
                    success = false,
                    result = StringResultData(""),
                    error = "Todo ID cannot be empty"
                )
            }

            todoRepository.deleteByTodoId(todoId)
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("Todo deleted: $todoId")
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to delete todo", e)
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Failed to delete todo: ${e.message}"
            )
        }
    }

    private fun todoItemToJson(item: TodoItem): JSONObject {
        return JSONObject().apply {
            put("todo_id", item.todoId)
            put("title", item.title)
            put("description", item.description)
            put("due_date", item.dueDate)
            put("priority", item.priority)
            put("status", item.status)
            put("created_at", item.createdAt)
            put("updated_at", item.updatedAt)
        }
    }
}
