package com.ai.assistance.operit.api.chat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.graphics.PixelFormat
import com.ai.assistance.operit.util.AppLogger
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ai.assistance.operit.R
import com.ai.assistance.operit.core.chat.AIMessageManager
import com.ai.assistance.operit.core.application.ActivityLifecycleManager
import com.ai.assistance.operit.core.application.ForegroundServiceCompat
import com.ai.assistance.operit.data.preferences.ExternalHttpApiConfig
import com.ai.assistance.operit.data.preferences.ExternalHttpApiPreferences
import com.ai.assistance.operit.data.preferences.SpeechServicesPreferences
import com.ai.assistance.operit.integrations.http.ExternalChatHttpServer
import com.ai.assistance.operit.integrations.http.ExternalChatHttpState
import com.ai.assistance.operit.services.FloatingChatService
import com.ai.assistance.operit.services.UIDebuggerService
import com.ai.assistance.operit.data.preferences.DisplayPreferencesManager
import com.ai.assistance.operit.data.repository.WorkflowRepository
import com.ai.assistance.operit.ui.main.MainActivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.isActive
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

class AIForegroundService : Service() {

    companion object {
        private const val TAG = "AIForegroundService"
        private const val NOTIFICATION_ID = 1
        private const val REPLY_NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "AI_SERVICE_CHANNEL"
        private const val REPLY_CHANNEL_ID_PREFIX = "AI_REPLY_COMPLETE_CHANNEL"
        private val REPLY_VIBRATION_PATTERN = longArrayOf(0L, 250L, 150L, 250L)

        private const val ACTION_CANCEL_CURRENT_OPERATION = "com.ai.assistance.operit.action.CANCEL_CURRENT_OPERATION"
        private const val REQUEST_CODE_CANCEL_CURRENT_OPERATION = 9002

        private const val ACTION_EXIT_APP = "com.ai.assistance.operit.action.EXIT_APP"
        private const val REQUEST_CODE_EXIT_APP = 9003

        private const val REPLY_NOTIFICATION_TAG_PREFIX = "ai_reply:"

        private const val ACTION_START_OR_REFRESH_EXTERNAL_HTTP =
            "com.ai.assistance.operit.action.START_OR_REFRESH_EXTERNAL_HTTP"
        private const val ACTION_STOP_EXTERNAL_HTTP =
            "com.ai.assistance.operit.action.STOP_EXTERNAL_HTTP"

        val isRunning = java.util.concurrent.atomic.AtomicBoolean(false)
        private val activeReplyNotificationTags = ConcurrentHashMap.newKeySet<String>()
        private val externalHttpStateFlow = MutableStateFlow(ExternalChatHttpState())
        val externalHttpState = externalHttpStateFlow.asStateFlow()

        const val EXTRA_STATE = "extra_state"
        const val STATE_RUNNING = "running"
        const val STATE_IDLE = "idle"

        private fun buildReplyNotificationTag(chatId: String?): String {
            val suffix = chatId?.ifBlank { "default" } ?: "default"
            return "$REPLY_NOTIFICATION_TAG_PREFIX$suffix"
        }

        private fun createMainActivityPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        }

        private fun buildReplyNotificationChannelId(
            enableSound: Boolean,
            enableVibration: Boolean
        ): String =
            when {
                enableSound && enableVibration -> "${REPLY_CHANNEL_ID_PREFIX}_sound_vibration"
                enableSound -> "${REPLY_CHANNEL_ID_PREFIX}_sound"
                enableVibration -> "${REPLY_CHANNEL_ID_PREFIX}_vibration"
                else -> "${REPLY_CHANNEL_ID_PREFIX}_silent"
            }

        private fun getReplyNotificationChannelNameRes(
            enableSound: Boolean,
            enableVibration: Boolean
        ): Int =
            when {
                enableSound && enableVibration -> R.string.service_chat_complete_reminder_sound_vibration
                enableSound -> R.string.service_chat_complete_reminder_sound
                enableVibration -> R.string.service_chat_complete_reminder_vibration
                else -> R.string.service_chat_complete_reminder
            }

        private fun ensureReplyNotificationChannel(
            context: Context,
            enableSound: Boolean,
            enableVibration: Boolean
        ): String {
            val channelId = buildReplyNotificationChannelId(enableSound, enableVibration)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return channelId
            }
            val replyChannel =
                NotificationChannel(
                    channelId,
                    context.getString(
                        getReplyNotificationChannelNameRes(
                            enableSound = enableSound,
                            enableVibration = enableVibration
                        )
                    ),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.service_notify_when_complete)
                    setSound(
                        if (enableSound) android.provider.Settings.System.DEFAULT_NOTIFICATION_URI else null,
                        if (enableSound) {
                            android.media.AudioAttributes.Builder()
                                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        } else {
                            null
                        }
                    )
                    enableVibration(enableVibration)
                    vibrationPattern =
                        if (enableVibration) {
                            REPLY_VIBRATION_PATTERN
                        } else {
                            longArrayOf(0L)
                        }
                }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(replyChannel)
            return channelId
        }

        fun notifyReplyCompleted(
            context: Context,
            chatId: String?,
            rawReplyContent: String?,
            notifyReplyOverride: Boolean? = null
        ) {
            try {
                AppLogger.d(TAG, "检查是否需要发送会话完成通知: chatId=$chatId")

                if (ActivityLifecycleManager.getCurrentActivity() != null) {
                    AppLogger.d(TAG, "应用在前台，无需发送会话完成通知")
                    return
                }

                val appContext = context.applicationContext
                val displayPreferences = DisplayPreferencesManager.getInstance(appContext)
                val globalEnableReplyNotification = runBlocking {
                    displayPreferences.enableReplyNotification.first()
                }
                val shouldNotify = notifyReplyOverride ?: globalEnableReplyNotification
                if (!shouldNotify) {
                    AppLogger.d(TAG, "回复通知已禁用，跳过发送")
                    return
                }

                if (rawReplyContent.isNullOrBlank()) {
                    AppLogger.d(TAG, "回复内容为空，跳过发送回复通知: chatId=$chatId")
                    return
                }

                val enableReplyNotificationSound = runBlocking {
                    displayPreferences.enableReplyNotificationSound.first()
                }
                val enableReplyNotificationVibration = runBlocking {
                    displayPreferences.enableReplyNotificationVibration.first()
                }
                val replyChannelId =
                    ensureReplyNotificationChannel(
                        context = appContext,
                        enableSound = enableReplyNotificationSound,
                        enableVibration = enableReplyNotificationVibration
                    )

                var notificationDefaults = NotificationCompat.DEFAULT_LIGHTS
                if (enableReplyNotificationSound) {
                    notificationDefaults = notificationDefaults or NotificationCompat.DEFAULT_SOUND
                }
                if (enableReplyNotificationVibration) {
                    notificationDefaults = notificationDefaults or NotificationCompat.DEFAULT_VIBRATE
                }
                val notificationBuilder =
                    NotificationCompat.Builder(appContext, replyChannelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(
                            appContext.getString(R.string.notification_ai_reply_title)
                        )
                        .setContentText(
                            rawReplyContent
                                .take(100)
                                .ifEmpty {
                                    appContext.getString(R.string.notification_ai_reply_content)
                                }
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(notificationDefaults)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentIntent(createMainActivityPendingIntent(appContext))
                        .setAutoCancel(true)

                if (rawReplyContent.isNotEmpty()) {
                    notificationBuilder.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(rawReplyContent)
                            .setBigContentTitle(
                                appContext.getString(R.string.notification_ai_reply_title)
                            )
                    )
                }

                val manager =
                    appContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                val tag = buildReplyNotificationTag(chatId)
                activeReplyNotificationTags.add(tag)
                manager.notify(tag, REPLY_NOTIFICATION_ID, notificationBuilder.build())
                AppLogger.d(TAG, "AI回复通知已发送: chatId=$chatId, tag=$tag")
            } catch (e: Exception) {
                AppLogger.e(TAG, "发送AI回复通知失败: ${e.message}", e)
            }
        }

        fun ensureRunningForExternalHttp(context: Context) {
            startServiceForAction(context, ACTION_START_OR_REFRESH_EXTERNAL_HTTP)
        }

        fun refreshBackgroundKeepAlive(context: Context) {
            val appContext = context.applicationContext
            val keepAliveEnabled = runCatching {
                runBlocking {
                    DisplayPreferencesManager.getInstance(appContext).enableBackgroundKeepAlive.first()
                }
            }.getOrDefault(false)
            if (!keepAliveEnabled && !isRunning.get()) {
                return
            }
            val intent = Intent(appContext, AIForegroundService::class.java).apply {
                putExtra(EXTRA_STATE, STATE_IDLE)
            }
            try {
                if (isRunning.get()) {
                    appContext.startService(intent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to refresh background keep alive: ${e.message}", e)
            }
        }

        fun stopExternalHttp(context: Context) {
            externalHttpStateFlow.value =
                externalHttpStateFlow.value.copy(isRunning = false, lastError = null)
            if (!isRunning.get()) {
                return
            }
            startServiceForAction(context, ACTION_STOP_EXTERNAL_HTTP)
        }

        private fun startServiceForAction(context: Context, action: String) {
            val appContext = context.applicationContext
            val intent = Intent(appContext, AIForegroundService::class.java).apply {
                this.action = action
            }
            try {
                if (isRunning.get()) {
                    appContext.startService(intent)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to start action $action: ${e.message}", e)
            }
        }

        private fun hasPersistentForegroundResponsibilityConfigured(context: Context): Boolean {
            val appContext = context.applicationContext
            val backgroundKeepAliveEnabled = runCatching {
                runBlocking {
                    DisplayPreferencesManager.getInstance(appContext).enableBackgroundKeepAlive.first()
                }
            }.getOrDefault(false)
            val externalHttpEnabled = runCatching {
                ExternalHttpApiPreferences.getInstance(appContext).getConfigSync().let { config ->
                    config.enabled && ExternalHttpApiPreferences.isValidPort(config.port)
                }
            }.getOrDefault(false)
            return backgroundKeepAliveEnabled || externalHttpEnabled
        }
    }

    private var isAiBusy: Boolean = false
    @Volatile
    private var hideRuntimeTaskViewEnabled: Boolean = false
    @Volatile
    private var backgroundKeepAliveEnabled: Boolean = false
    @Volatile
    private var lastAppliedRuntimeTaskViewHidden: Boolean? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val chatRuntimeHolder by lazy { ChatRuntimeHolder.getInstance(applicationContext) }
    private val workflowRepository by lazy { WorkflowRepository(applicationContext) }
    private val externalHttpPreferences by lazy { ExternalHttpApiPreferences.getInstance(applicationContext) }

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var keepAliveOverlayView: View? = null
    private var keepAliveOverlayPermissionLogged = false

    private var externalHttpMonitorJob: Job? = null
    private var externalHttpServer: ExternalChatHttpServer? = null
    private var externalHttpCurrentPort: Int? = null

    private fun startOrRefreshExternalHttpServer(config: ExternalHttpApiConfig = externalHttpPreferences.getConfigSync()) {
        if (!config.enabled) {
            AppLogger.i(TAG, "External HTTP API disabled, stopping runtime")
            stopExternalHttpServer(portOverride = config.port, lastError = null)
            stopSelfIfIdle(ignoreAppForeground = true)
            return
        }

        if (!ExternalHttpApiPreferences.isValidPort(config.port)) {
            val message = "Invalid port: ${config.port}"
            AppLogger.w(TAG, message)
            stopExternalHttpServer(portOverride = config.port, lastError = message)
            stopSelfIfIdle(ignoreAppForeground = true)
            return
        }

        if (externalHttpServer != null && externalHttpCurrentPort == config.port) {
            updateExternalHttpState(
                ExternalChatHttpState(
                    isRunning = true,
                    port = config.port,
                    lastError = null
                )
            )
            return
        }

        stopExternalHttpServer()

        try {
            val newServer = ExternalChatHttpServer(applicationContext, externalHttpPreferences, serviceScope)
            newServer.startServer()
            externalHttpServer = newServer
            externalHttpCurrentPort = config.port
            updateExternalHttpState(
                ExternalChatHttpState(
                    isRunning = true,
                    port = config.port,
                    lastError = null
                )
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start external HTTP server", e)
            stopExternalHttpServer(
                portOverride = config.port,
                lastError = e.message ?: "Failed to start server"
            )
            stopSelfIfIdle(ignoreAppForeground = true)
        }
    }

    private fun stopExternalHttpServer(portOverride: Int? = null, lastError: String? = null) {
        runCatching {
            externalHttpServer?.stopServer()
        }.onFailure { error ->
            AppLogger.e(TAG, "Failed to stop external HTTP server", error)
        }

        val stoppedPort = portOverride ?: externalHttpCurrentPort ?: externalHttpStateFlow.value.port
        externalHttpServer = null
        externalHttpCurrentPort = null
        updateExternalHttpState(
            ExternalChatHttpState(
                isRunning = false,
                port = stoppedPort,
                lastError = lastError
            )
        )
    }

    private fun updateExternalHttpState(
        newState: ExternalChatHttpState,
        refreshNotification: Boolean = true
    ) {
        externalHttpStateFlow.value = newState
        if (refreshNotification) {
            refreshServiceNotification()
        }
    }

    private fun refreshServiceNotification() {
        if (!isRunning.get()) {
            return
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun isExternalHttpEnabledNow(): Boolean {
        return runCatching {
            externalHttpPreferences.getConfigSync().let { config ->
                config.enabled && ExternalHttpApiPreferences.isValidPort(config.port)
            }
        }.getOrDefault(false)
    }

    private fun stopSelfIfIdle(ignoreAppForeground: Boolean = false) {
        val externalHttpEnabled = externalHttpStateFlow.value.isRunning || isExternalHttpEnabledNow()
        if (isAiBusy || backgroundKeepAliveEnabled || externalHttpEnabled) {
            return
        }
        if (!ignoreAppForeground && ActivityLifecycleManager.getCurrentActivity() != null) {
            return
        }

        AppLogger.d(TAG, "No active foreground responsibilities, stopping AIForegroundService")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        isRunning.set(true)
        AppLogger.d(TAG, "AI 前台服务创建。")
        chatRuntimeHolder
        createNotificationChannel()
        val notification = createNotification()
        ForegroundServiceCompat.startForeground(
            service = this,
            notificationId = NOTIFICATION_ID,
            notification = notification,
            types = ForegroundServiceCompat.buildTypes(
                dataSync = true,
                specialUse = runCatching { externalHttpPreferences.getEnabled() }.getOrDefault(false)
            )
        )
        observeRuntimeTaskViewPreference()
        observeBackgroundKeepAlivePreference()
        observeChatRuntimeStats()
        startExternalHttpMonitoring()
        AppLogger.d(TAG, "AI 前台服务已启动。")
    }

    private fun observeRuntimeTaskViewPreference() {
        serviceScope.launch {
            try {
                DisplayPreferencesManager
                    .getInstance(applicationContext)
                    .hideRuntimeTaskView
                    .collectLatest { enabled ->
                        hideRuntimeTaskViewEnabled = enabled
                        updateRuntimeTaskViewVisibility()
                    }
            } catch (e: Exception) {
                AppLogger.e(TAG, "监听运行时任务视图隐藏设置失败: ${e.message}", e)
            }
        }
    }

    private fun observeBackgroundKeepAlivePreference() {
        serviceScope.launch {
            try {
                DisplayPreferencesManager
                    .getInstance(applicationContext)
                    .enableBackgroundKeepAlive
                    .collectLatest { enabled ->
                        backgroundKeepAliveEnabled = enabled
                        updateKeepAliveOverlayVisibility()
                        if (enabled) {
                            refreshServiceNotification()
                        } else {
                            stopSelfIfIdle(ignoreAppForeground = true)
                        }
                    }
            } catch (e: Exception) {
                AppLogger.e(TAG, "监听后台保活设置失败: ${e.message}", e)
            }
        }
    }

    private fun updateAiBusyState(isBusy: Boolean) {
        isAiBusy = isBusy
        updateRuntimeTaskViewVisibility()
    }

    private fun updateRuntimeTaskViewVisibility() {
        val shouldHide = hideRuntimeTaskViewEnabled && isAiBusy
        if (lastAppliedRuntimeTaskViewHidden == shouldHide) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return

        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val appTasks = activityManager?.appTasks.orEmpty()
            if (appTasks.isEmpty()) {
                AppLogger.d(TAG, "更新运行时任务视图隐藏状态时未找到任务: hidden=$shouldHide")
                return
            }
            appTasks.forEach { task ->
                try {
                    task.setExcludeFromRecents(shouldHide)
                } catch (e: Exception) {
                    AppLogger.e(TAG, "设置任务最近任务可见性失败: hidden=$shouldHide", e)
                }
            }
            lastAppliedRuntimeTaskViewHidden = shouldHide
            AppLogger.d(TAG, "运行时任务视图隐藏状态已更新: hidden=$shouldHide, taskCount=${appTasks.size}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "更新运行时任务视图隐藏状态失败: hidden=$shouldHide", e)
        }
    }

    private fun observeChatRuntimeStats() {
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                chatRuntimeHolder.activeConversationCount,
                chatRuntimeHolder.currentSessionToolCount
            ) { _, _ ->
                Unit
            }.collect {
                if (!isRunning.get()) return@collect
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, createNotification())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXIT_APP) {
            isRunning.set(false)
            updateAiBusyState(false)

            try {
                AIMessageManager.cancelCurrentOperation()
            } catch (e: Exception) {
                AppLogger.e(TAG, "退出时取消当前AI任务失败: ${e.message}", e)
            }

            try {
                stopService(Intent(this, FloatingChatService::class.java))
            } catch (e: Exception) {
                AppLogger.e(TAG, "退出时停止 FloatingChatService 失败: ${e.message}", e)
            }

            try {
                stopService(Intent(this, UIDebuggerService::class.java))
            } catch (e: Exception) {
                AppLogger.e(TAG, "退出时停止 UIDebuggerService 失败: ${e.message}", e)
            }

            stopExternalHttpServer(lastError = null)

            try {
                val activity = ActivityLifecycleManager.getCurrentActivity()
                activity?.runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activity.finishAndRemoveTask()
                    } else {
                        activity.finish()
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "退出时关闭前台界面失败: ${e.message}", e)
            }

            try {
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(NOTIFICATION_ID)
                activeReplyNotificationTags.forEach { tag ->
                    manager.cancel(tag, REPLY_NOTIFICATION_ID)
                }
                activeReplyNotificationTags.clear()
                manager.cancel(REPLY_NOTIFICATION_ID)
            } catch (e: Exception) {
                AppLogger.e(TAG, "退出时取消通知失败: ${e.message}", e)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                @Suppress("DEPRECATION")
                stopForeground(Service.STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }

            stopSelf()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_START_OR_REFRESH_EXTERNAL_HTTP || intent == null) {
            startOrRefreshExternalHttpServer()
            return if (externalHttpStateFlow.value.isRunning) START_STICKY else START_NOT_STICKY
        }

        if (intent?.action == ACTION_STOP_EXTERNAL_HTTP) {
            val configuredPort = runCatching { externalHttpPreferences.getPort() }.getOrNull()
            stopExternalHttpServer(portOverride = configuredPort, lastError = null)
            stopSelfIfIdle(ignoreAppForeground = true)
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_CANCEL_CURRENT_OPERATION) {
            try {
                AIMessageManager.cancelCurrentOperation()
                updateAiBusyState(false)
            } catch (e: Exception) {
                AppLogger.e(TAG, "取消当前AI任务失败: ${e.message}", e)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, createNotification())
            return START_NOT_STICKY
        }

        intent?.let {
            val state = it.getStringExtra(EXTRA_STATE)
            if (state != null) {
                updateAiBusyState(state == STATE_RUNNING)
                val externalHttpEnabled =
                    externalHttpStateFlow.value.isRunning || isExternalHttpEnabledNow()
                if (!isAiBusy &&
                    !backgroundKeepAliveEnabled &&
                    !externalHttpEnabled
                ) {
                    AppLogger.d(TAG, "服务进入空闲且无持久后台职责，停止前台服务并移除通知")
                    stopSelfIfIdle(ignoreAppForeground = true)
                    return START_NOT_STICKY
                }
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, createNotification())
            }
        }

        return if (isExternalHttpEnabledNow()) START_STICKY else START_NOT_STICKY
    }

    override fun onDestroy() {
        val stoppedPort = externalHttpCurrentPort ?: externalHttpStateFlow.value.port
        runCatching {
            externalHttpServer?.stopServer()
        }.onFailure { error ->
            AppLogger.e(TAG, "Failed to stop external HTTP server", error)
        }
        externalHttpServer = null
        externalHttpCurrentPort = null
        updateExternalHttpState(
            ExternalChatHttpState(
                isRunning = false,
                port = stoppedPort,
                lastError = null
            ),
            refreshNotification = false
        )
        super.onDestroy()
        isRunning.set(false)
        updateAiBusyState(false)
        hideKeepAliveOverlay()
        externalHttpMonitorJob?.cancel()
        externalHttpMonitorJob = null
        try {
            serviceScope.cancel()
        } catch (_: Exception) {
        }
        AppLogger.d(TAG, "AI 前台服务已销毁。")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.service_operit_running)
            val serviceChannel =
                    NotificationChannel(
                            CHANNEL_ID,
                            channelName,
                            NotificationManager.IMPORTANCE_LOW
                    )
                    .apply {
                        description = getString(R.string.service_keep_background)
                    }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startExternalHttpMonitoring() {
        if (externalHttpMonitorJob?.isActive == true) return

        if (isExternalHttpEnabledNow()) {
            startOrRefreshExternalHttpServer()
        } else {
            updateExternalHttpState(
                externalHttpStateFlow.value.copy(
                    isRunning = false,
                    port = runCatching { externalHttpPreferences.getPort() }.getOrNull(),
                    lastError = null
                )
            )
        }

        externalHttpMonitorJob =
            serviceScope.launch {
                kotlinx.coroutines.flow.combine(
                    externalHttpPreferences.enabledFlow,
                    externalHttpPreferences.portFlow
                ) { enabled, port ->
                    enabled to port
                }.collectLatest { (enabled, port) ->
                    AppLogger.d(TAG, "External HTTP config updated: enabled=$enabled, port=$port")
                    if (enabled) {
                        startOrRefreshExternalHttpServer(
                            config = externalHttpPreferences.getConfigSync()
                        )
                    } else {
                        stopExternalHttpServer(portOverride = port, lastError = null)
                        stopSelfIfIdle(ignoreAppForeground = true)
                    }
                }
            }
    }

    private fun updateKeepAliveOverlayVisibility() {
        if (backgroundKeepAliveEnabled) {
            showKeepAliveOverlayIfPossible()
        } else {
            hideKeepAliveOverlay()
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post {
                try {
                    action()
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Error on main thread", e)
                }
            }
        }
    }

    private fun showKeepAliveOverlayIfPossible() {
        if (keepAliveOverlayView != null) return
        if (!android.provider.Settings.canDrawOverlays(this)) {
            if (!keepAliveOverlayPermissionLogged) {
                keepAliveOverlayPermissionLogged = true
                AppLogger.w(TAG, "Keep-alive overlay skipped: missing overlay permission")
            }
            return
        }

        runOnMainThread {
            if (keepAliveOverlayView != null) return@runOnMainThread
            try {
                val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val view = View(this)
                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                val params = WindowManager.LayoutParams(
                    1,
                    1,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    x = 0
                    y = 0
                }

                wm.addView(view, params)
                keepAliveOverlayView = view
                AppLogger.d(TAG, "Keep-alive overlay shown")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to show keep-alive overlay: ${e.message}", e)
                keepAliveOverlayView = null
            }
        }
    }

    private fun hideKeepAliveOverlay() {
        val view = keepAliveOverlayView ?: return
        runOnMainThread {
            val current = keepAliveOverlayView ?: return@runOnMainThread
            try {
                val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                wm.removeView(current)
                AppLogger.d(TAG, "Keep-alive overlay hidden")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to hide keep-alive overlay: ${e.message}", e)
            } finally {
                if (keepAliveOverlayView === view) {
                    keepAliveOverlayView = null
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val externalHttpSnapshot = externalHttpStateFlow.value
        val title = getString(R.string.service_operit_running)
        val activeConversationCount = chatRuntimeHolder.activeConversationCount.value
        val currentSessionToolCount = chatRuntimeHolder.currentSessionToolCount.value
        val contentText =
            if (isAiBusy && activeConversationCount > 0) {
                val statsText = getString(
                    R.string.service_running_stats,
                    activeConversationCount,
                    currentSessionToolCount
                )
                if (externalHttpSnapshot.isRunning && externalHttpSnapshot.port != null) {
                    getString(
                        R.string.service_running_with_http,
                        statsText,
                        externalHttpSnapshot.port
                    )
                } else {
                    statsText
                }
            } else if (externalHttpSnapshot.isRunning && externalHttpSnapshot.port != null) {
                getString(
                    R.string.service_running_http_listening,
                    externalHttpSnapshot.port
                )
            } else {
                getString(R.string.service_operit_running)
            }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        builder.setContentIntent(contentPendingIntent)

        val exitIntent = Intent(this, AIForegroundService::class.java).apply {
            action = ACTION_EXIT_APP
        }
        val exitPendingIntent = PendingIntent.getService(
            this,
            REQUEST_CODE_EXIT_APP,
            exitIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(R.string.service_exit),
            exitPendingIntent
        )

        if (isAiBusy) {
            val cancelIntent = Intent(this, AIForegroundService::class.java).apply {
                action = ACTION_CANCEL_CURRENT_OPERATION
            }
            val pendingIntent = PendingIntent.getService(
                this,
                REQUEST_CODE_CANCEL_CURRENT_OPERATION,
                cancelIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            builder.addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.service_stop),
                pendingIntent
            )
        }

        return builder.build()
    }

}
