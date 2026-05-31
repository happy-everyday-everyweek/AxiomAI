package com.ai.assistance.operit.data.repository

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object UIHierarchyManager {
    private const val TAG = "UIHierarchyManager"

    @Volatile
    var accessibilityServiceInstance: AccessibilityService? = null

    private val _isBound = MutableStateFlow(false)
    val isBound = _isBound.asStateFlow()

    fun setServiceInstance(service: AccessibilityService?) {
        accessibilityServiceInstance = service
        _isBound.value = service != null
        AppLogger.d(TAG, "Accessibility service instance updated: ${service != null}")
    }

    fun isProviderAppInstalled(context: Context): Boolean = true

    fun launchProviderInstall(context: Context) {
    }

    fun isUpdateNeeded(context: Context): Boolean = false

    private suspend fun ensureServiceAvailable(context: Context): Boolean {
        if (accessibilityServiceInstance != null) {
            return true
        }
        AppLogger.w(TAG, "Accessibility service instance is null")
        return false
    }

    suspend fun bindToService(context: Context): Boolean {
        return accessibilityServiceInstance != null
    }

    fun unbindFromService(context: Context) {
    }

    suspend fun getUIHierarchy(context: Context): String {
        if (!ensureServiceAvailable(context)) {
            AppLogger.e(TAG, "Accessibility service not available, cannot get UI hierarchy")
            return ""
        }
        return try {
            val service = accessibilityServiceInstance ?: return ""
            val rootNode = service.rootInActiveWindow ?: return ""
            buildXmlFromNode(rootNode)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get UI hierarchy", e)
            ""
        }
    }

    private fun buildXmlFromNode(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        sb.append("<node")
        appendNodeAttributes(sb, node)
        if (node.childCount > 0) {
            sb.append(">")
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    sb.append(buildXmlFromNode(child))
                }
            }
            sb.append("</node>")
        } else {
            sb.append("/>")
        }
        return sb.toString()
    }

    private fun appendNodeAttributes(sb: StringBuilder, node: AccessibilityNodeInfo) {
        node.packageName?.let { sb.append(" package=\"").append(escapeXml(it.toString())).append("\"") }
        node.className?.let { sb.append(" class=\"").append(escapeXml(it.toString())).append("\"") }
        sb.append(" bounds=\"").append(node.boundsInScreen.toString()).append("\"")
        node.text?.let { sb.append(" text=\"").append(escapeXml(it.toString())).append("\"") }
        node.contentDescription?.let { sb.append(" content-desc=\"").append(escapeXml(it.toString())).append("\"") }
        sb.append(" checkable=\"").append(node.isCheckable).append("\"")
        sb.append(" checked=\"").append(node.isChecked).append("\"")
        sb.append(" clickable=\"").append(node.isClickable).append("\"")
        sb.append(" enabled=\"").append(node.isEnabled).append("\"")
        sb.append(" focusable=\"").append(node.isFocusable).append("\"")
        sb.append(" focused=\"").append(node.isFocused).append("\"")
        sb.append(" scrollable=\"").append(node.isScrollable).append("\"")
        sb.append(" long-clickable=\"").append(node.isLongClickable).append("\"")
        sb.append(" selected=\"").append(node.isSelected).append("\"")
        node.viewIdResourceName?.let { sb.append(" resource-id=\"").append(escapeXml(it)).append("\"") }
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    fun extractWindowInfo(xmlHierarchy: String): Pair<String?, String?> {
        if (xmlHierarchy.isEmpty()) {
            return Pair(null, null)
        }
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlHierarchy))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "node") {
                            val rootPackage = parser.getAttributeValue(null, "package")
                            return Pair(rootPackage, null)
                        }
                    }
                }
                eventType = parser.next()
            }

            return Pair(null, null)

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error parsing window info", e)
            return Pair(null, null)
        }
    }

    suspend fun performClick(context: Context, x: Int, y: Int): Boolean {
        if (!ensureServiceAvailable(context)) {
            AppLogger.w(TAG, "Accessibility service not available, cannot perform click")
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            val rootNode = service.rootInActiveWindow ?: return false
            val clickedNode = findNodeAtPosition(rootNode, x, y)
            clickedNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Click action failed", e)
            false
        }
    }

    suspend fun performLongPress(context: Context, x: Int, y: Int): Boolean {
        if (!ensureServiceAvailable(context)) {
            AppLogger.w(TAG, "Accessibility service not available, cannot perform long press")
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            val rootNode = service.rootInActiveWindow ?: return false
            val node = findNodeAtPosition(rootNode, x, y)
            node?.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK) ?: false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Long press action failed", e)
            false
        }
    }

    suspend fun performSwipe(context: Context, startX: Int, startY: Int, endX: Int, endY: Int, duration: Long): Boolean {
        if (!ensureServiceAvailable(context)) {
            AppLogger.w(TAG, "Accessibility service not available, cannot perform swipe")
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            val path = android.graphics.Path()
            path.moveTo(startX.toFloat(), startY.toFloat())
            path.lineTo(endX.toFloat(), endY.toFloat())
            val stroke = android.accessibilityservice.AccessibilityService.GestureDescription.StrokeDescription(path, 0, duration)
            val gestureBuilder = android.accessibilityservice.AccessibilityService.GestureDescription.Builder()
            gestureBuilder.addStroke(stroke)
            service.dispatchGesture(gestureBuilder.build(), null, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Swipe action failed", e)
            false
        }
    }

    suspend fun performGlobalAction(context: Context, actionId: Int): Boolean {
        if (!ensureServiceAvailable(context)) {
            AppLogger.w(TAG, "Accessibility service not available, cannot perform global action")
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            service.performGlobalAction(actionId)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Global action failed", e)
            false
        }
    }

    suspend fun findFocusedNodeId(context: Context): String? {
        if (!ensureServiceAvailable(context)) {
            return null
        }
        return try {
            val service = accessibilityServiceInstance ?: return null
            val rootNode = service.rootInActiveWindow ?: return null
            val focusNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
            focusNode?.viewIdResourceName
        } catch (e: Exception) {
            AppLogger.e(TAG, "Find focused node failed", e)
            null
        }
    }

    suspend fun setTextOnNode(context: Context, nodeId: String, text: String): Boolean {
        if (!ensureServiceAvailable(context)) {
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            val rootNode = service.rootInActiveWindow ?: return false
            val node = findNodeByViewId(rootNode, nodeId)
            if (node != null) {
                val arguments = android.os.Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else {
                false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Set text on node failed", e)
            false
        }
    }

    @android.annotation.SuppressLint("NewApi")
    suspend fun takeScreenshot(context: Context, path: String, format: String): Boolean {
        if (!ensureServiceAvailable(context)) {
            return false
        }
        return try {
            val service = accessibilityServiceInstance ?: return false
            val displayMetrics = context.resources.displayMetrics
            val screenRect = android.graphics.Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            val screenshot = service.takeScreenshot(android.view.Display.DEFAULT_DISPLAY, screenRect)
            if (screenshot != null) {
                val bitmap = android.graphics.Bitmap.wrapHardwareBuffer(screenshot.hardwareBuffer, screenshot.colorSpace)
                    ?.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                if (bitmap != null) {
                    val file = java.io.File(path)
                    file.parentFile?.mkdirs()
                    val outputStream = java.io.FileOutputStream(file)
                    if (format.equals("png", ignoreCase = true)) {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                    } else {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                    }
                    outputStream.flush()
                    outputStream.close()
                    bitmap.recycle()
                    screenshot.hardwareBuffer.close()
                    screenshot.colorSpace?.let { }
                    true
                } else {
                    screenshot.hardwareBuffer.close()
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Take screenshot failed", e)
            false
        }
    }

    suspend fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = accessibilityServiceInstance
        if (service != null) {
            return true
        }

        return try {
            val expectedComponentName = android.content.ComponentName(context, "com.ai.assistance.operit.OperitAccessibilityService")
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentStr = colonSplitter.next()
                if (componentStr.equals(expectedComponentName.flattenToString(), ignoreCase = true)) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error checking accessibility service status", e)
            false
        }
    }

    suspend fun getCurrentActivityName(context: Context): String? {
        if (!ensureServiceAvailable(context)) {
            return null
        }
        return try {
            val service = accessibilityServiceInstance ?: return null
            val rootNode = service.rootInActiveWindow ?: return null
            val packageName = rootNode.packageName?.toString()
            val windowNode = findNodeWithClassName(rootNode)
            val className = windowNode?.className?.toString()
            className ?: packageName
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get current activity name", e)
            null
        }
    }

    private fun findNodeAtPosition(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val bounds = android.graphics.Rect()
        root.getBoundsInScreen(bounds)
        if (!bounds.contains(x, y)) {
            return null
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findNodeAtPosition(child, x, y)
            if (result != null) {
                return result
            }
        }
        return if (root.isClickable) root else root
    }

    private fun findNodeByViewId(root: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        return nodes.firstOrNull()
    }

    private fun findNodeWithClassName(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val className = root.className?.toString() ?: return null
        if (className.contains("Activity") || className.contains("Dialog")) {
            return root
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val result = findNodeWithClassName(child)
            if (result != null) {
                return result
            }
        }
        return null
    }
}
