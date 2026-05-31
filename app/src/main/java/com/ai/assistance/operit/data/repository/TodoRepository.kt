package com.ai.assistance.operit.data.repository

import android.content.Context
import com.ai.assistance.operit.data.db.ObjectBoxManager
import com.ai.assistance.operit.data.model.TodoItem
import com.ai.assistance.operit.data.model.TodoItem_
import com.ai.assistance.operit.util.AppLogger
import io.objectbox.Box

class TodoRepository(private val context: Context, private val profileId: String = "default") {

    companion object {
        private const val TAG = "TodoRepository"

        @Volatile
        private var INSTANCE: TodoRepository? = null

        fun getInstance(context: Context, profileId: String = "default"): TodoRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TodoRepository(context.applicationContext, profileId).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val boxStore by lazy { ObjectBoxManager.get(context, profileId) }

    private val box: Box<TodoItem> by lazy { boxStore.boxFor(TodoItem::class.java) }

    fun insert(item: TodoItem): Long {
        item.updatedAt = System.currentTimeMillis()
        val id = box.put(item)
        AppLogger.d(TAG, "Inserted todo item: todoId=${item.todoId}, id=$id")
        return id
    }

    fun update(item: TodoItem) {
        item.updatedAt = System.currentTimeMillis()
        box.put(item)
        AppLogger.d(TAG, "Updated todo item: todoId=${item.todoId}, status=${item.status}")
    }

    fun delete(item: TodoItem) {
        box.remove(item)
        AppLogger.d(TAG, "Deleted todo item: todoId=${item.todoId}")
    }

    fun deleteByTodoId(todoId: String) {
        val item = getByTodoId(todoId)
        if (item != null) {
            box.remove(item)
            AppLogger.d(TAG, "Deleted todo item by todoId: $todoId")
        }
    }

    fun getByTodoId(todoId: String): TodoItem? {
        return box.query().equal(TodoItem_.todoId, todoId).build().findFirst()
    }

    fun getByStatus(status: String): List<TodoItem> {
        return box.query().equal(TodoItem_.status, status).build().find()
    }

    fun getPendingTodos(): List<TodoItem> {
        return getByStatus(TodoItem.STATUS_PENDING)
    }

    fun getActiveTodos(): List<TodoItem> {
        return box.query()
            .equal(TodoItem_.status, TodoItem.STATUS_PENDING)
            .or()
            .equal(TodoItem_.status, TodoItem.STATUS_IN_PROGRESS)
            .orderDesc(TodoItem_.priority)
            .order(TodoItem_.dueDate)
            .build()
            .find()
    }

    fun getOverdueTodos(): List<TodoItem> {
        val now = System.currentTimeMillis()
        return box.query()
            .less(TodoItem_.dueDate, now)
            .equal(TodoItem_.status, TodoItem.STATUS_PENDING)
            .build()
            .find()
    }

    fun updateStatus(todoId: String, status: String) {
        val item = getByTodoId(todoId) ?: return
        item.status = status
        update(item)
    }

    fun getAll(): List<TodoItem> {
        return box.all
    }

    fun cleanupOldTasks(olderThanMs: Long = 30 * 24 * 60 * 60 * 1000L) {
        val cutoff = System.currentTimeMillis() - olderThanMs
        val oldItems = box.query()
            .equal(TodoItem_.status, TodoItem.STATUS_COMPLETED)
            .less(TodoItem_.updatedAt, cutoff)
            .build()
            .find()
        if (oldItems.isNotEmpty()) {
            box.remove(oldItems)
            AppLogger.d(TAG, "Cleaned up ${oldItems.size} old completed todo items")
        }
    }
}
