package com.ai.assistance.operit.data.repository

import android.content.Context
import com.ai.assistance.operit.data.db.ObjectBoxManager
import com.ai.assistance.operit.data.model.ScheduleItem
import com.ai.assistance.operit.data.model.ScheduleItem_
import com.ai.assistance.operit.util.AppLogger
import io.objectbox.Box

class ScheduleRepository(private val context: Context, private val profileId: String = "default") {

    companion object {
        private const val TAG = "ScheduleRepository"

        @Volatile
        private var INSTANCE: ScheduleRepository? = null

        fun getInstance(context: Context, profileId: String = "default"): ScheduleRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduleRepository(context.applicationContext, profileId).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val boxStore by lazy { ObjectBoxManager.get(context, profileId) }

    private val box: Box<ScheduleItem> by lazy { boxStore.boxFor(ScheduleItem::class.java) }

    fun insert(item: ScheduleItem): Long {
        item.updatedAt = System.currentTimeMillis()
        val id = box.put(item)
        AppLogger.d(TAG, "Inserted schedule item: scheduleId=${item.scheduleId}, id=$id")
        return id
    }

    fun update(item: ScheduleItem) {
        item.updatedAt = System.currentTimeMillis()
        box.put(item)
        AppLogger.d(TAG, "Updated schedule item: scheduleId=${item.scheduleId}")
    }

    fun delete(item: ScheduleItem) {
        box.remove(item)
        AppLogger.d(TAG, "Deleted schedule item: scheduleId=${item.scheduleId}")
    }

    fun deleteByScheduleId(scheduleId: String) {
        val item = getByScheduleId(scheduleId)
        if (item != null) {
            box.remove(item)
            AppLogger.d(TAG, "Deleted schedule item by scheduleId: $scheduleId")
        }
    }

    fun getByScheduleId(scheduleId: String): ScheduleItem? {
        return box.query().equal(ScheduleItem_.scheduleId, scheduleId).build().findFirst()
    }

    fun getByDateRange(startTime: Long, endTime: Long): List<ScheduleItem> {
        return box.query()
            .less(ScheduleItem_.startTime, endTime)
            .greater(ScheduleItem_.endTime, startTime)
            .order(ScheduleItem_.startTime)
            .build()
            .find()
    }

    fun getUpcomingSchedules(fromTime: Long = System.currentTimeMillis()): List<ScheduleItem> {
        return box.query()
            .greater(ScheduleItem_.startTime, fromTime)
            .order(ScheduleItem_.startTime)
            .build()
            .find()
    }

    fun getTodaySchedules(): List<ScheduleItem> {
        val now = System.currentTimeMillis()
        val startOfDay = now - (now % (24 * 60 * 60 * 1000L))
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L
        return getByDateRange(startOfDay, endOfDay)
    }

    fun getByRecurrence(recurrence: String): List<ScheduleItem> {
        return box.query().equal(ScheduleItem_.recurrence, recurrence).build().find()
    }

    fun getAll(): List<ScheduleItem> {
        return box.all
    }

    fun cleanupOldSchedules(olderThanMs: Long = 90 * 24 * 60 * 60 * 1000L) {
        val cutoff = System.currentTimeMillis() - olderThanMs
        val oldItems = box.query()
            .less(ScheduleItem_.endTime, cutoff)
            .equal(ScheduleItem_.recurrence, ScheduleItem.RECURRENCE_NONE)
            .build()
            .find()
        if (oldItems.isNotEmpty()) {
            box.remove(oldItems)
            AppLogger.d(TAG, "Cleaned up ${oldItems.size} old schedule items")
        }
    }
}
