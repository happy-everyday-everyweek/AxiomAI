package com.ai.assistance.operit.core.tools.defaultTool

import android.content.Context
import com.ai.assistance.operit.core.tools.defaultTool.accessbility.*
import com.ai.assistance.operit.core.tools.defaultTool.standard.*
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences

object ToolGetter {

    fun getFileSystemTools(context: Context): StandardFileSystemTools {
        return when (androidPermissionPreferences.getPreferredPermissionLevel()) {
            AndroidPermissionLevel.ACCESSIBILITY -> AccessibilityFileSystemTools(context)
            AndroidPermissionLevel.STANDARD -> StandardFileSystemTools(context)
            null -> StandardFileSystemTools(context)
        }
    }

    fun getShellToolExecutor(context: Context): StandardShellToolExecutor {
        return StandardShellToolExecutor(context)
    }

    fun getUITools(context: Context): StandardUITools {
        return when (androidPermissionPreferences.getPreferredPermissionLevel()) {
            AndroidPermissionLevel.ACCESSIBILITY -> AccessibilityUITools(context)
            AndroidPermissionLevel.STANDARD -> StandardUITools(context)
            null -> StandardUITools(context)
        }
    }

    fun getSystemOperationTools(context: Context): StandardSystemOperationTools {
        return when (androidPermissionPreferences.getPreferredPermissionLevel()) {
            AndroidPermissionLevel.ACCESSIBILITY -> AccessibilitySystemOperationTools(context)
            AndroidPermissionLevel.STANDARD -> StandardSystemOperationTools(context)
            null -> StandardSystemOperationTools(context)
        }
    }

    fun getDeviceInfoToolExecutor(context: Context): StandardDeviceInfoToolExecutor {
        return when (androidPermissionPreferences.getPreferredPermissionLevel()) {
            AndroidPermissionLevel.ACCESSIBILITY -> AccessibilityDeviceInfoToolExecutor(context)
            AndroidPermissionLevel.STANDARD -> StandardDeviceInfoToolExecutor(context)
            null -> StandardDeviceInfoToolExecutor(context)
        }
    }

    fun getHttpTools(context: Context): StandardHttpTools {
        return StandardHttpTools(context)
    }

    fun getWebVisitTool(context: Context): StandardWebVisitTool {
        return StandardWebVisitTool(context)
    }

    fun getBrowserSessionTools(context: Context): StandardBrowserSessionTools {
        return StandardBrowserSessionTools(context)
    }

    fun getIntentToolExecutor(context: Context): StandardIntentToolExecutor {
        return StandardIntentToolExecutor(context)
    }

    fun getSendBroadcastToolExecutor(context: Context): StandardSendBroadcastToolExecutor {
        return StandardSendBroadcastToolExecutor(context)
    }

    fun getTerminalCommandExecutor(context: Context): StandardTerminalCommandExecutor {
        return StandardTerminalCommandExecutor(context)
    }

    fun getMusicPlaybackTools(context: Context): StandardMusicPlaybackTools {
        return StandardMusicPlaybackTools(context)
    }

    fun getMemoryQueryToolExecutor(context: Context): MemoryQueryToolExecutor {
        return MemoryQueryToolExecutor(context)
    }

    fun getFFmpegToolExecutor(context: Context): StandardFFmpegToolExecutor {
        return StandardFFmpegToolExecutor(context)
    }

    fun getFFmpegInfoToolExecutor(): StandardFFmpegInfoToolExecutor {
        return StandardFFmpegInfoToolExecutor()
    }

    fun getFFmpegConvertToolExecutor(context: Context): StandardFFmpegConvertToolExecutor {
        return StandardFFmpegConvertToolExecutor(context)
    }

    fun getCalculator() = StandardCalculator

    fun getWorkflowTools(context: Context): StandardWorkflowTools {
        return StandardWorkflowTools(context)
    }

    fun getChatManagerTool(context: Context): StandardChatManagerTool {
        return StandardChatManagerTool(context)
    }

    fun getSoftwareSettingsModifyTools(context: Context): StandardSoftwareSettingsModifyTools {
        return StandardSoftwareSettingsModifyTools(context)
    }

    fun getTodoTools(context: Context): StandardTodoTools {
        return StandardTodoTools(context)
    }

    fun getScheduleTools(context: Context): StandardScheduleTools {
        return StandardScheduleTools(context)
    }
}
