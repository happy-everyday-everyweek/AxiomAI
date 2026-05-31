package com.ai.assistance.operit.data.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class ScheduleItem(
    @Id var id: Long = 0,
    @Index val scheduleId: String,
    var title: String,
    var description: String = "",
    var startTime: Long = 0,
    var endTime: Long = 0,
    var recurrence: String = RECURRENCE_NONE,
    var reminder: Long = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val RECURRENCE_NONE = "NONE"
        const val RECURRENCE_DAILY = "DAILY"
        const val RECURRENCE_WEEKLY = "WEEKLY"
        const val RECURRENCE_MONTHLY = "MONTHLY"
        const val RECURRENCE_YEARLY = "YEARLY"
    }
}
