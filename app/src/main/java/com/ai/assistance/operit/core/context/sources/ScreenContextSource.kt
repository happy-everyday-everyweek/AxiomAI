package com.ai.assistance.operit.core.context.sources

import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.ai.assistance.operit.core.tools.defaultTool.accessbility.AccessibilityDeviceInfoToolExecutor
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel

object ScreenContextSource {
    fun collect(context: Context): String? {
        val permissionLevel = androidPermissionPreferences.getPreferredPermissionLevel()
        if (permissionLevel != AndroidPermissionLevel.ACCESSIBILITY) {
            return null
        }

        try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return null
            return "Screen accessibility service is active. Current foreground app info available via accessibility tools."
        } catch (e: Exception) {
            return null
        }
    }
}
