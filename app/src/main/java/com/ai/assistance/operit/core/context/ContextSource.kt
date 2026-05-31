package com.ai.assistance.operit.core.context

sealed interface ContextSource {
    val name: String
    val priority: Int
    suspend fun collect(context: android.content.Context): String?

    data object Notifications : ContextSource {
        override val name = "notifications"
        override val priority = 2
        override suspend fun collect(context: android.content.Context): String? {
            return com.ai.assistance.operit.core.context.sources.NotificationContextSource.collect(context)
        }
    }

    data object Screen : ContextSource {
        override val name = "screen"
        override val priority = 3
        override suspend fun collect(context: android.content.Context): String? {
            return com.ai.assistance.operit.core.context.sources.ScreenContextSource.collect(context)
        }
    }

    data object Location : ContextSource {
        override val name = "location"
        override val priority = 4
        override suspend fun collect(context: android.content.Context): String? {
            return com.ai.assistance.operit.core.context.sources.LocationContextSource.collect(context)
        }
    }

    data object UsageTime : ContextSource {
        override val name = "usage_time"
        override val priority = 5
        override suspend fun collect(context: android.content.Context): String? {
            return com.ai.assistance.operit.core.context.sources.UsageTimeContextSource.collect(context)
        }
    }

    data object Memory : ContextSource {
        override val name = "memory"
        override val priority = 1
        override suspend fun collect(context: android.content.Context): String? {
            return com.ai.assistance.operit.core.context.sources.MemoryContextSource.collect(context)
        }
    }
}
