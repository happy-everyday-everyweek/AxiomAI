package com.ai.assistance.operit.core.context.sources

import android.content.Context
import com.ai.assistance.operit.services.notification.OperitNotificationStore

object NotificationContextSource {
    fun collect(context: Context): String? {
        val notifications = OperitNotificationStore.snapshot(limit = 10, includeOngoing = false)
        if (notifications.isEmpty()) return null

        return buildString {
            appendLine("Recent notifications:")
            for (n in notifications) {
                appendLine("- [${n.packageName}] ${n.text}")
            }
        }
    }
}
