package com.ai.assistance.operit.core.tools.system.shell

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences

class ShellExecutorFactory {
    companion object {
        private const val TAG = "ShellExecutorFactory"

        private val executors = mutableMapOf<AndroidPermissionLevel, ShellExecutor>()

        fun getExecutor(context: Context, permissionLevel: AndroidPermissionLevel): ShellExecutor {
            executors[permissionLevel]?.let {
                return it
            }

            val executor =
                    when (permissionLevel) {
                        AndroidPermissionLevel.ACCESSIBILITY -> AccessibilityShellExecutor(context)
                        AndroidPermissionLevel.STANDARD -> StandardShellExecutor(context)
                    }

            executor.initialize()

            executors[permissionLevel] = executor

            return executor
        }

        fun getHighestAvailableExecutor(
                context: Context
        ): Pair<ShellExecutor, ShellExecutor.PermissionStatus> {

            val levels =
                    listOf(
                            AndroidPermissionLevel.ACCESSIBILITY,
                            AndroidPermissionLevel.STANDARD
                    )

            for (level in levels) {
                val executor = getExecutor(context, level)
                val permStatus = executor.hasPermission()

                if (executor.isAvailable() && permStatus.granted) {
                    AppLogger.d(TAG, "Found highest available executor: ${executor.getPermissionLevel()}")
                    return Pair(executor, permStatus)
                }
            }

            AppLogger.d(TAG, "No available executor found, falling back to STANDARD")
            val standardExecutor = getExecutor(context, AndroidPermissionLevel.STANDARD)
            return Pair(standardExecutor, standardExecutor.hasPermission())
        }

        fun getUserPreferredExecutor(context: Context): ShellExecutor {
            try {
                val preferredLevel = androidPermissionPreferences.getPreferredPermissionLevel()
                val actualLevel = preferredLevel ?: AndroidPermissionLevel.STANDARD
                return getExecutor(context, actualLevel)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error getting preferred permission level, falling back to STANDARD", e)
                return getExecutor(context, AndroidPermissionLevel.STANDARD)
            }
        }

        fun getHighestAvailableExecutorLegacy(context: Context): ShellExecutor {
            val (executor, _) = getHighestAvailableExecutor(context)
            return executor
        }

        fun clearCache(permissionLevel: AndroidPermissionLevel? = null) {
            if (permissionLevel != null) {
                executors.remove(permissionLevel)
                AppLogger.d(TAG, "Cleared executor cache for level: $permissionLevel")
            } else {
                executors.clear()
                AppLogger.d(TAG, "Cleared all executor caches")
            }
        }

        fun getAvailableExecutors(
                context: Context
        ): Map<AndroidPermissionLevel, Pair<ShellExecutor, ShellExecutor.PermissionStatus>> {
            val result =
                    mutableMapOf<
                            AndroidPermissionLevel,
                            Pair<ShellExecutor, ShellExecutor.PermissionStatus>>()

            for (level in AndroidPermissionLevel.values()) {
                val executor = getExecutor(context, level)
                val status = executor.hasPermission()

                result[level] = Pair(executor, status)
            }

            return result
        }
    }
}
