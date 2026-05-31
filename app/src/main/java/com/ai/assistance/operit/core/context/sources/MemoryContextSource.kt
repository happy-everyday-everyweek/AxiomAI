package com.ai.assistance.operit.core.context.sources

import android.content.Context
import com.ai.assistance.operit.data.db.ObjectBoxManager
import com.ai.assistance.operit.data.model.Memory
import com.ai.assistance.operit.data.model.Memory_

object MemoryContextSource {
    fun collect(context: Context): String? {
        try {
            val boxStore = ObjectBoxManager.get(context, "default")
            val memoryBox = boxStore.boxFor(Memory::class.java)

            val recentMemories = memoryBox.query()
                .orderDesc(Memory_.updatedAt)
                .build()
                .find()
                .take(5)

            if (recentMemories.isEmpty()) return null

            return buildString {
                appendLine("Recent memory entries:")
                for (memory in recentMemories) {
                    val content = memory.content.take(200)
                    appendLine("- [${memory.type}] ${memory.title}: $content")
                }
            }
        } catch (e: Exception) {
            return null
        }
    }
}
