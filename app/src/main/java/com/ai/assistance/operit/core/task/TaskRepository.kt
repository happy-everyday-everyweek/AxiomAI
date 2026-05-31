package com.ai.assistance.operit.core.task

import android.content.Context
import com.ai.assistance.operit.data.db.ObjectBoxManager
import com.ai.assistance.operit.util.AppLogger
import io.objectbox.Box

class TaskRepository(private val context: Context, private val profileId: String = "default") {

    companion object {
        private const val TAG = "TaskRepository"

        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context, profileId: String = "default"): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext, profileId).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val boxStore by lazy { ObjectBoxManager.get(context, profileId) }

    private val box: Box<TaskRecord> by lazy { boxStore.boxFor(TaskRecord::class.java) }

    fun insert(record: TaskRecord): Long {
        record.updatedAt = System.currentTimeMillis()
        val id = box.put(record)
        AppLogger.d(TAG, "Inserted task record: taskId=${record.taskId}, id=$id")
        return id
    }

    fun update(record: TaskRecord) {
        record.updatedAt = System.currentTimeMillis()
        box.put(record)
        AppLogger.d(TAG, "Updated task record: taskId=${record.taskId}, status=${record.status}")
    }

    fun delete(record: TaskRecord) {
        box.remove(record)
        AppLogger.d(TAG, "Deleted task record: taskId=${record.taskId}")
    }

    fun deleteByTaskId(taskId: String) {
        val record = getByTaskId(taskId)
        if (record != null) {
            box.remove(record)
            AppLogger.d(TAG, "Deleted task record by taskId: $taskId")
        }
    }

    fun getByTaskId(taskId: String): TaskRecord? {
        return box.query().equal(TaskRecord_.taskId, taskId).build().findFirst()
    }

    fun getByChatId(chatId: String): List<TaskRecord> {
        return box.query().equal(TaskRecord_.chatId, chatId).build().find()
    }

    fun getByStatus(status: String): List<TaskRecord> {
        return box.query().equal(TaskRecord_.status, status).build().find()
    }

    fun getRunningTasks(): List<TaskRecord> {
        return getByStatus(TaskRecord.STATUS_RUNNING)
    }

    fun getPendingTasks(): List<TaskRecord> {
        return getByStatus(TaskRecord.STATUS_PENDING)
    }

    fun getFailedTasksForRetry(maxRetryCount: Int = 3): List<TaskRecord> {
        return box.query()
            .equal(TaskRecord_.status, TaskRecord.STATUS_FAILED)
            .less(TaskRecord_.retryCount, maxRetryCount.toLong())
            .build()
            .find()
    }

    fun updateStatus(taskId: String, status: String) {
        val record = getByTaskId(taskId) ?: return
        record.status = status
        update(record)
    }

    fun updateCheckpoint(taskId: String, checkpoint: String) {
        val record = getByTaskId(taskId) ?: return
        record.checkpoint = checkpoint
        update(record)
    }

    fun incrementRetryCount(taskId: String, lastError: String = "") {
        val record = getByTaskId(taskId) ?: return
        record.retryCount++
        record.lastError = lastError
        update(record)
    }

    fun getAll(): List<TaskRecord> {
        return box.all
    }

    fun cleanupOldTasks(olderThanMs: Long = 7 * 24 * 60 * 60 * 1000L) {
        val cutoff = System.currentTimeMillis() - olderThanMs
        val oldRecords = box.query()
            .less(TaskRecord_.updatedAt, cutoff)
            .build()
            .find()
        if (oldRecords.isNotEmpty()) {
            box.remove(oldRecords)
            AppLogger.d(TAG, "Cleaned up ${oldRecords.size} old task records")
        }
    }
}
