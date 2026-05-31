package com.ai.assistance.operit

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.ai.assistance.operit.data.repository.UIHierarchyManager
import com.ai.assistance.operit.util.AppLogger

class OperitAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "OperitAccessibilityService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AppLogger.d(TAG, "Accessibility service connected")
        UIHierarchyManager.setServiceInstance(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        AppLogger.w(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.d(TAG, "Accessibility service destroyed")
        UIHierarchyManager.setServiceInstance(null)
    }
}
