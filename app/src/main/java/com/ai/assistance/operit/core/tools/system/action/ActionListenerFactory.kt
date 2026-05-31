package com.ai.assistance.operit.core.tools.system.action

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences

class ActionListenerFactory {
    companion object {
        private const val TAG = "ActionListenerFactory"

        private val listeners = mutableMapOf<AndroidPermissionLevel, ActionListener>()

        fun getListener(context: Context, permissionLevel: AndroidPermissionLevel): ActionListener {
            listeners[permissionLevel]?.let {
                return it
            }

            val listener = when (permissionLevel) {
                AndroidPermissionLevel.ACCESSIBILITY -> AccessibilityActionListener(context)
                AndroidPermissionLevel.STANDARD -> StandardActionListener(context)
            }

            listener.initialize()

            listeners[permissionLevel] = listener

            AppLogger.d(TAG, "Created action listener for permission level: $permissionLevel")
            return listener
        }

        suspend fun getHighestAvailableListener(
            context: Context
        ): Pair<ActionListener, ActionListener.PermissionStatus> {

            val levels = listOf(
                AndroidPermissionLevel.ACCESSIBILITY,
                AndroidPermissionLevel.STANDARD
            )

            for (level in levels) {
                val listener = getListener(context, level)
                val permStatus = listener.hasPermission()

                if (listener.isAvailable() && permStatus.granted) {
                    AppLogger.d(TAG, "Found highest available action listener: ${listener.getPermissionLevel()}")
                    return Pair(listener, permStatus)
                }
            }

            AppLogger.d(TAG, "No available action listener found, falling back to STANDARD")
            val standardListener = getListener(context, AndroidPermissionLevel.STANDARD)
            return Pair(standardListener, standardListener.hasPermission())
        }

        fun getUserPreferredListener(context: Context): ActionListener {
            try {
                val preferredLevel = androidPermissionPreferences.getPreferredPermissionLevel()
                val actualLevel = preferredLevel ?: AndroidPermissionLevel.STANDARD
                return getListener(context, actualLevel)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error getting preferred permission level, falling back to STANDARD", e)
                return getListener(context, AndroidPermissionLevel.STANDARD)
            }
        }

        suspend fun getHighestAvailableListenerLegacy(context: Context): ActionListener {
            val (listener, _) = getHighestAvailableListener(context)
            return listener
        }

        fun clearCache(permissionLevel: AndroidPermissionLevel? = null) {
            if (permissionLevel != null) {
                listeners.remove(permissionLevel)
                AppLogger.d(TAG, "Cleared action listener cache for level: $permissionLevel")
            } else {
                listeners.clear()
                AppLogger.d(TAG, "Cleared all action listener caches")
            }
        }

        suspend fun getAvailableListeners(
            context: Context
        ): Map<AndroidPermissionLevel, Pair<ActionListener, ActionListener.PermissionStatus>> {
            val result = mutableMapOf<AndroidPermissionLevel, Pair<ActionListener, ActionListener.PermissionStatus>>()

            for (level in AndroidPermissionLevel.values()) {
                val listener = getListener(context, level)
                val status = listener.hasPermission()

                result[level] = Pair(listener, status)
            }

            return result
        }

        suspend fun stopAllListeners(): Boolean {
            var allStopped = true
            listeners.values.forEach { listener ->
                if (listener.isListening()) {
                    val stopped = listener.stopListening()
                    if (!stopped) {
                        allStopped = false
                        AppLogger.w(TAG, "Failed to stop listener: ${listener.getPermissionLevel()}")
                    }
                }
            }
            AppLogger.d(TAG, "All listeners stop result: $allStopped")
            return allStopped
        }
    }
}
