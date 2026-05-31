package com.ai.assistance.operit.core.context

import android.content.Context
import com.ai.assistance.operit.util.AppLogger

object ContextInjector {

    private const val TAG = "ContextInjector"
    const val TOKEN_LIMIT = 500

    private val autoInjectSources: List<ContextSource> = listOf(
        ContextSource.Memory,
        ContextSource.Notifications,
        ContextSource.Screen,
        ContextSource.Location,
        ContextSource.UsageTime
    )

    suspend fun collectContext(context: Context, tokenLimit: Int = TOKEN_LIMIT): String? {
        val parts = mutableListOf<Pair<String, String>>()
        var remainingTokens = tokenLimit

        val sortedSources = autoInjectSources.sortedBy { it.priority }

        for (source in sortedSources) {
            if (remainingTokens <= 0) break

            try {
                val content = source.collect(context)
                if (!content.isNullOrBlank()) {
                    val tokenCount = estimateTokens(content)
                    if (tokenCount <= remainingTokens) {
                        parts.add(source.name to content)
                        remainingTokens -= tokenCount
                    } else {
                        val truncated = truncateToTokenLimit(content, remainingTokens)
                        if (!truncated.isNullOrBlank()) {
                            parts.add(source.name to truncated)
                            remainingTokens = 0
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to collect context from ${source.name}: ${e.message}")
            }
        }

        if (parts.isEmpty()) return null

        return buildString {
            appendLine("[Auto-Injected Context]")
            for ((name, content) in parts) {
                appendLine("## $name")
                appendLine(content)
                appendLine()
            }
            appendLine("[/Auto-Injected Context]")
        }
    }

    fun estimateTokens(text: String): Int {
        return text.length / 4
    }

    private fun truncateToTokenLimit(text: String, tokenLimit: Int): String? {
        val charLimit = tokenLimit * 4
        if (text.length <= charLimit) return text
        val truncated = text.take(charLimit)
        val lastNewline = truncated.lastIndexOf('\n')
        return if (lastNewline > charLimit / 2) {
            truncated.take(lastNewline) + "\n..."
        } else {
            truncated + "..."
        }
    }
}
