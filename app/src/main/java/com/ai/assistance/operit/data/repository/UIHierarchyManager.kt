package com.ai.assistance.operit.data.repository

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityNodeInfo
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream
import java.io.StringReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object UIHierarchyManager {
    private const val TAG = "UIHierarchyManager"

    @Volatile
    private var accessibilityService: AccessibilityService? = null

    @Volatile
    private var lastFocusedNode: AccessibilityNodeInfo? = null

    private val _isBound = MutableStateFlow(false)
    val isBound = _isBound.asStateFlow()

    fun setAccessibilityService(service: AccessibilityService?) {
        accessibilityService = service
        _isBound.value = service != null
        if (service != null) {
            AppLogger.d(TAG, "AccessibilityService registered")
        } else {
            AppLogger.d(TAG, "AccessibilityService unregistered")
        }
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (accessibilityService != null) return true
        return checkAccessibilityEnabledInSettings(context)
    }

    private fun checkAccessibilityEnabledInSettings(context: Context): Boolean {
        val serviceString = context.packageName + "/.accessibility.OperitAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(serviceString)
    }

    suspend fun getUIHierarchy(context: Context): String {
        val service = accessibilityService ?: run {
            AppLogger.e(TAG, "AccessibilityService not available")
            return ""
        }
        return withContext(Dispatchers.IO) {
            try {
                val rootNode = service.rootInActiveWindow
                if (rootNode == null) {
                    AppLogger.e(TAG, "rootInActiveWindow is null")
                    return@withContext ""
                }
                val sb = StringBuilder()
                serializeNodeToXml(rootNode, sb, 0)
                rootNode.recycle()
                sb.toString()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to get UI hierarchy", e)
                ""
            }
        }
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
            AppLogger.e(TAG, "解析窗口信息时出错", e)
            return Pair(null, null)
        }
    }

    suspend fun performClick(context: Context, x: Int, y: Int): Boolean {
        val service = accessibilityService ?: run {
            AppLogger.w(TAG, "AccessibilityService not available, cannot click")
            return false
        }
        return try {
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val stroke = GestureDescription.StrokeDescription(path, 0L, 10L)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            service.dispatchGesture(gesture, null, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Click gesture failed", e)
            false
        }
    }

    suspend fun performLongPress(context: Context, x: Int, y: Int): Boolean {
        val service = accessibilityService ?: run {
            AppLogger.w(TAG, "AccessibilityService not available, cannot long press")
            return false
        }
        return try {
            val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
            val stroke = GestureDescription.StrokeDescription(path, 0L, 500L)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            service.dispatchGesture(gesture, null, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Long press gesture failed", e)
            false
        }
    }

    suspend fun performSwipe(context: Context, startX: Int, startY: Int, endX: Int, endY: Int, duration: Long): Boolean {
        val service = accessibilityService ?: run {
            AppLogger.w(TAG, "AccessibilityService not available, cannot swipe")
            return false
        }
        return try {
            val path = Path().apply {
                moveTo(startX.toFloat(), startY.toFloat())
                lineTo(endX.toFloat(), endY.toFloat())
            }
            val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            service.dispatchGesture(gesture, null, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Swipe gesture failed", e)
            false
        }
    }

    suspend fun performGlobalAction(context: Context, actionId: Int): Boolean {
        val service = accessibilityService ?: run {
            AppLogger.w(TAG, "AccessibilityService not available, cannot perform global action")
            return false
        }
        return try {
            service.performGlobalAction(actionId)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Global action failed", e)
            false
        }
    }

    suspend fun findFocusedNodeId(context: Context): String? {
        val service = accessibilityService ?: run {
            AppLogger.w(TAG, "AccessibilityService not available, cannot find focused node")
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val rootNode = service.rootInActiveWindow ?: return@withContext null
                val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                rootNode.recycle()
                if (focusedNode != null) {
                    lastFocusedNode = focusedNode
                    focusedNode.viewIdResourceName
                } else {
                    null
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Find focused node failed", e)
                null
            }
        }
    }

    suspend fun setTextOnNode(context: Context, nodeId: String, text: String): Boolean {
        val service = accessibilityService ?: return false
        return withContext(Dispatchers.Main) {
            try {
                val focusedNode = lastFocusedNode
                if (focusedNode != null) {
                    val args = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            text
                        )
                    }
                    val result = focusedNode.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args
                    )
                    focusedNode.recycle()
                    lastFocusedNode = null
                    result
                } else {
                    val rootNode = service.rootInActiveWindow ?: return@withContext false
                    val nodes = rootNode.findAccessibilityNodeInfosByViewId(nodeId)
                    rootNode.recycle()
                    if (nodes.isNullOrEmpty()) return@withContext false
                    val node = nodes[0]
                    val args = Bundle().apply {
                        putCharSequence(
                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                            text
                        )
                    }
                    val result = node.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args
                    )
                    for (n in nodes) n.recycle()
                    result
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Set text on node failed", e)
                false
            }
        }
    }

    suspend fun takeScreenshot(context: Context, path: String, format: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            AppLogger.w(TAG, "takeScreenshot requires API 30+")
            return false
        }
        val service = accessibilityService ?: return false
        return withContext(Dispatchers.IO) {
            try {
                val screenshotResult = withTimeoutOrNull(5000L) {
                    suspendCancellableCoroutine<AccessibilityService.ScreenshotResult> { cont ->
                        val executor = java.util.concurrent.Executor { r ->
                            android.os.Handler(android.os.Looper.getMainLooper()).post(r)
                        }
                        service.takeScreenshot(
                            android.view.Display.DEFAULT_DISPLAY,
                            executor,
                            object : AccessibilityService.TakeScreenshotCallback {
                                override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                                    if (cont.isActive) cont.resume(screenshot)
                                }
                                override fun onFailure(errorCode: Int) {
                                    if (cont.isActive) {
                                        AppLogger.e(TAG, "takeScreenshot error: $errorCode")
                                        cont.resumeWithException(Exception("Screenshot error code: $errorCode"))
                                    }
                                }
                            }
                        )
                    }
                } ?: run {
                    AppLogger.e(TAG, "takeScreenshot timed out")
                    return@withContext false
                }

                val hardwareBuffer = screenshotResult.hardwareBuffer
                val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
                if (bitmap == null) {
                    hardwareBuffer.close()
                    return@withContext false
                }
                val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                hardwareBuffer.close()

                if (softwareBitmap == null) return@withContext false

                val file = File(path)
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { fos ->
                    val compressFormat = when (format.lowercase()) {
                        "jpeg", "jpg" -> Bitmap.CompressFormat.JPEG
                        "webp" -> Bitmap.CompressFormat.WEBP
                        else -> Bitmap.CompressFormat.PNG
                    }
                    softwareBitmap.compress(compressFormat, 90, fos)
                }
                softwareBitmap.recycle()
                true
            } catch (e: Exception) {
                AppLogger.e(TAG, "Screenshot failed", e)
                false
            }
        }
    }

    suspend fun getCurrentActivityName(context: Context): String? {
        val service = accessibilityService ?: return null
        return withContext(Dispatchers.IO) {
            try {
                val rootNode = service.rootInActiveWindow ?: return@withContext null
                val activityName = rootNode.className?.toString()
                rootNode.recycle()
                activityName
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to get current activity name", e)
                null
            }
        }
    }

    private fun serializeNodeToXml(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        val indent = "  ".repeat(depth)
        sb.append("$indent<node")
        appendAttr(sb, "index", node.parent?.let { parent ->
            (0 until parent.childCount).firstOrNull { parent.getChild(it) == node }?.toString()
        } ?: "0")
        appendAttr(sb, "text", node.text?.toString())
        appendAttr(sb, "resource-id", node.viewIdResourceName)
        appendAttr(sb, "package", node.packageName?.toString())
        appendAttr(sb, "class", node.className?.toString())
        appendAttr(sb, "content-desc", node.contentDescription?.toString())
        appendAttr(sb, "checkable", node.isCheckable.toString())
        appendAttr(sb, "checked", node.isChecked.toString())
        appendAttr(sb, "clickable", node.isClickable.toString())
        appendAttr(sb, "enabled", node.isEnabled.toString())
        appendAttr(sb, "focusable", node.isFocusable.toString())
        appendAttr(sb, "focused", node.isFocused.toString())
        appendAttr(sb, "scrollable", node.isScrollable.toString())
        appendAttr(sb, "long-clickable", node.isLongClickable.toString())
        appendAttr(sb, "password", node.isPassword.toString())
        appendAttr(sb, "selected", node.isSelected.toString())
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        appendAttr(sb, "bounds", bounds.toShortString())

        if (node.childCount == 0) {
            sb.append(" />\n")
        } else {
            sb.append(">\n")
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    serializeNodeToXml(child, sb, depth + 1)
                    child.recycle()
                }
            }
            sb.append("$indent</node>\n")
        }
    }

    private fun appendAttr(sb: StringBuilder, name: String, value: String?) {
        sb.append(" $name=\"${escapeXml(value ?: "")}\"")
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
