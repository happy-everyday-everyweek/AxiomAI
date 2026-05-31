package com.ai.assistance.operit.data.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class TodoItem(
    @Id var id: Long = 0,
    @Index val todoId: String,
    var title: String,
    var description: String = "",
    var dueDate: Long = 0,
    var priority: String = PRIORITY_MEDIUM,
    var status: String = STATUS_PENDING,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val PRIORITY_LOW = "LOW"
        const val PRIORITY_MEDIUM = "MEDIUM"
        const val PRIORITY_HIGH = "HIGH"
        const val PRIORITY_URGENT = "URGENT"
    }
}
