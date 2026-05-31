package com.ai.assistance.operit.core.tools.system

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.core.tools.system.shell.ShellExecutor
import com.ai.assistance.operit.core.tools.system.shell.ShellExecutorFactory
import com.ai.assistance.operit.core.tools.system.shell.ShellProcess
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences

class AndroidShellExecutor {
    companion object {
        private const val TAG = "AndroidShellExecutor"
        private var context: Context? = null
        private val preferredPermissionLevelCacheLock = Any()
        @Volatile private var hasCachedPreferredPermissionLevel = false
        @Volatile private var cachedPreferredPermissionLevel: AndroidPermissionLevel? = null

        fun setContext(appContext: Context) {
            context = appContext.applicationContext
        }

        fun clearPreferredPermissionLevelCache() {
            synchronized(preferredPermissionLevelCacheLock) {
                cachedPreferredPermissionLevel = null
                hasCachedPreferredPermissionLevel = false
            }
        }

        private fun getPreferredPermissionLevelCached(): AndroidPermissionLevel? {
            if (hasCachedPreferredPermissionLevel) {
                return cachedPreferredPermissionLevel
            }

            synchronized(preferredPermissionLevelCacheLock) {
                if (!hasCachedPreferredPermissionLevel) {
                    cachedPreferredPermissionLevel =
                            androidPermissionPreferences.getPreferredPermissionLevel()
                    hasCachedPreferredPermissionLevel = true
                }
                return cachedPreferredPermissionLevel
            }
        }

        private fun getPermissionLevelLabel(level: AndroidPermissionLevel): String {
            return when (level) {
                AndroidPermissionLevel.STANDARD -> "STANDARD"
                AndroidPermissionLevel.ACCESSIBILITY -> "ACCESSIBILITY"
            }
        }

        private fun buildStrictUnavailableReason(
            level: AndroidPermissionLevel,
            executorAvailable: Boolean,
            permStatus: ShellExecutor.PermissionStatus
        ): String {
            val reasons = mutableListOf<String>()

            if (!executorAvailable) {
                reasons += "executor unavailable"
            }
            if (!permStatus.granted) {
                reasons += permStatus.reason.trim().ifEmpty { "permission not granted" }
            }

            val reasonText = reasons.distinct().joinToString("; ").ifBlank { "unknown reason" }
            return "Current ${getPermissionLevelLabel(level)} unavailable: $reasonText"
        }

        suspend fun executeShellCommand(command: String): CommandResult {
            return executeShellCommand(command, null)
        }

        suspend fun executeShellCommand(command: String, identityOverride: ShellIdentity?): CommandResult {
            val ctx = context ?: return CommandResult(false, "", "Context not initialized")

            val identity = identityOverride ?: ShellIdentity.DEFAULT

            val preferredLevel = getPreferredPermissionLevelCached()
            val actualLevel = preferredLevel ?: AndroidPermissionLevel.STANDARD

            val preferredExecutor = ShellExecutorFactory.getExecutor(ctx, actualLevel)
            val permStatus = preferredExecutor.hasPermission()
            val executorAvailable = preferredExecutor.isAvailable()

            if (executorAvailable && permStatus.granted) {
                val result = preferredExecutor.executeCommand(command, identity)
                return CommandResult(result.success, result.stdout, result.stderr, result.exitCode)
            }

            val reason = buildStrictUnavailableReason(actualLevel, executorAvailable, permStatus)

            AppLogger.d(TAG, "Strict permission mode enabled. $reason")
            return CommandResult(false, "", reason, -1)
        }

        suspend fun startShellProcess(command: String): ShellProcess {
            val ctx = context ?: throw IllegalStateException("Context not initialized")

            val preferredLevel = getPreferredPermissionLevelCached()
            val actualLevel = preferredLevel ?: AndroidPermissionLevel.STANDARD
            val preferredExecutor = ShellExecutorFactory.getExecutor(ctx, actualLevel)
            val permStatus = preferredExecutor.hasPermission()
            val executorAvailable = preferredExecutor.isAvailable()

            if (executorAvailable && permStatus.granted) {
                return preferredExecutor.startProcess(command)
            }

            val reason = buildStrictUnavailableReason(actualLevel, executorAvailable, permStatus)

            AppLogger.d(TAG, "Strict permission mode enabled. $reason")
            throw SecurityException(reason)
        }
    }

    data class CommandResult(
            val success: Boolean,
            val stdout: String,
            val stderr: String = "",
            val exitCode: Int = -1
    )
}

enum class ShellIdentity {
    DEFAULT,
    APP,
    ROOT,
    SHELL
}
