# 语音系统简化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 移除面向用户的语音交互功能（唤醒词、语音输入按钮、朗读按钮、STT/TTS独立界面、语音设置界面、语音助手小部件），保留底层API能力（SpeechService、VoiceService及其Provider）供工作流和沙箱包调用。
**Architecture:** 语音系统分为两层：用户交互层（UI按钮、唤醒监听、独立界面）和API能力层（SpeechService/VoiceService接口及Provider实现）。本次重构仅删除用户交互层，API能力层完整保留。AIForegroundService中移除唤醒监听逻辑但保留TTS播放和前台服务基础功能。ChatViewModel移除朗读按钮状态管理但保留TTS工具调用链路。
**Tech Stack:** Kotlin, Jetpack Compose, Android Services

---

## Task 1: 删除语音交互服务文件

**Files:**
- `services/assistant/OperitVoiceInteractionService.kt` (删除)
- `services/assistant/OperitVoiceInteractionSessionService.kt` (删除)

**Steps:**
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/services/assistant/OperitVoiceInteractionService.kt`。此文件是 `VoiceInteractionService` 的实现，注册为系统数字助理，处理 `onReady()`、`onGetSupportedVoiceActions()` 等回调。整个文件不再需要。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/services/assistant/OperitVoiceInteractionSessionService.kt`。此文件是 `VoiceInteractionSessionService` 的实现，在用户触发助手时创建 `OperitVoiceInteractionSession`，启动悬浮窗并自动进入语音聊天。整个文件不再需要。
- [ ] 删除 `services/assistant/` 目录（如果目录为空）。

---

## Task 2: 删除语音交互管理器和唤醒词配置

**Files:**
- `ui/floating/voice/SpeechInteractionManager.kt` (删除)
- `data/preferences/WakeWordPreferences.kt` (删除)

**Steps:**
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/floating/voice/SpeechInteractionManager.kt`。此文件封装了语音识别、TTS、焦点获取、文本累积、静默检测等逻辑，被 `FloatingFullscreenModeViewModel` 和 `SiriBall` 引用。删除后需在 Task 8 中清理引用方。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/preferences/WakeWordPreferences.kt`。此文件管理唤醒词配置（`alwaysListeningEnabled`、`wakePhrase`、`wakeRecognitionMode`、`personalWakeTemplates`、`voiceAutoAttachItems` 等），被 `AIForegroundService`、`FloatingChatService`、`AssistantConfigScreen`、`OperitApplication` 等引用。删除后需在后续 Task 中清理所有引用方。
- [ ] 删除 `ui/floating/voice/` 目录（如果目录为空）。

---

## Task 3: 删除语音助手小部件

**Files:**
- `widget/VoiceAssistantGlanceWidget.kt` (删除)
- `widget/VoiceAssistantWidgetReceiver.kt` (删除)

**Steps:**
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/widget/VoiceAssistantGlanceWidget.kt`。此文件是 Glance 小部件实现，提供桌面快捷语音助手入口。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/widget/VoiceAssistantWidgetReceiver.kt`。此文件是小部件的 `GlanceAppWidgetReceiver` 实现，绑定 `VoiceAssistantGlanceWidget`。
- [ ] 删除资源文件 `app/src/main/res/xml/voice_assistant_widget_info.xml`。此文件定义小部件的元数据（最小尺寸、更新周期等）。

---

## Task 4: 删除STT/TTS独立界面

**Files:**
- `ui/features/toolbox/screens/speechtotext/SpeechToTextScreen.kt` (删除)
- `ui/features/toolbox/screens/speechtotext/SpeechToTextToolScreen.kt` (删除)
- `ui/features/toolbox/screens/texttospeech/TextToSpeechScreen.kt` (删除)
- `ui/features/toolbox/screens/texttospeech/TextToSpeechToolScreen.kt` (删除)

**Steps:**
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/speechtotext/SpeechToTextScreen.kt`。此文件是 STT 独立演示界面，包含录音、识别结果展示等 UI。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/speechtotext/SpeechToTextToolScreen.kt`。此文件是 STT 工具箱界面封装，引用 `SpeechToTextScreen`。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/texttospeech/TextToSpeechScreen.kt`。此文件是 TTS 独立演示界面，包含文本输入、语音播放、参数调节等 UI。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/texttospeech/TextToSpeechToolScreen.kt`。此文件是 TTS 工具箱界面封装，引用 `TextToSpeechScreen`。
- [ ] 删除 `ui/features/toolbox/screens/speechtotext/` 目录（如果目录为空）。
- [ ] 删除 `ui/features/toolbox/screens/texttospeech/` 目录（如果目录为空）。

---

## Task 5: 删除语音设置界面和语音自动附加组件

**Files:**
- `ui/features/settings/screens/SpeechServicesSettingsScreen.kt` (删除)
- `ui/features/assistant/components/VoiceAutoAttachComponents.kt` (删除)

**Steps:**
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/SpeechServicesSettingsScreen.kt`。此文件是语音服务设置界面（2330行），包含 STT 引擎选择、TTS 服务配置（SimpleTTS/OpenAI/HTTP/VITS）、语速音调调节、TTS清洁正则等。这些配置能力由 `SpeechServicesPreferences` 保留，仅删除 UI 层。
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/assistant/components/VoiceAutoAttachComponents.kt`。此文件是语音自动附加组件（416行），在语音对话中根据关键词自动附加屏幕OCR、通知、位置、时间等信息。与唤醒词系统紧密耦合，一并移除。

---

## Task 6: 清理AndroidManifest和资源文件

**Files:**
- `AndroidManifest.xml` (修改)
- `res/xml/interaction_service.xml` (删除)
- `res/xml/voice_assistant_widget_info.xml` (删除，已在 Task 3 中提及)

**Steps:**
- [ ] 从 `app/src/main/AndroidManifest.xml` 中移除 `OperitVoiceInteractionService` 的 `<service>` 声明（约第461-471行），包含 `android.permission.BIND_VOICE_INTERACTION` 权限和 `android.voice_interaction` meta-data。
- [ ] 从 `app/src/main/AndroidManifest.xml` 中移除 `OperitVoiceInteractionSessionService` 的 `<service>` 声明（约第474-477行），包含 `android.permission.BIND_VOICE_INTERACTION` 权限。
- [ ] 从 `app/src/main/AndroidManifest.xml` 中移除 `VoiceAssistantWidgetReceiver` 的 `<receiver>` 声明（约第382-392行），包含 `APPWIDGET_UPDATE` intent-filter 和 `voice_assistant_widget_info` meta-data。
- [ ] 删除 `app/src/main/res/xml/interaction_service.xml`。此文件定义 `VoiceInteractionService` 的配置（支持的语音操作等）。
- [ ] 删除 `app/src/main/res/xml/voice_assistant_widget_info.xml`。此文件定义语音助手小部件的元数据。

---

## Task 7: 清理导航路由和屏幕注册

**Files:**
- `ui/main/screens/OperitScreens.kt` (修改)
- `ui/main/screens/ScreenRouteRegistry.kt` (修改)

**Steps:**
- [ ] 从 `OperitScreens.kt` 中移除 `SpeechServicesSettings` data object 定义（约第807-824行），它引用了已删除的 `SpeechServicesSettingsScreen`。
- [ ] 从 `OperitScreens.kt` 中移除 `TextToSpeech` data object 定义（约第1366-1378行），它引用了已删除的 `TextToSpeechToolScreen`。
- [ ] 从 `OperitScreens.kt` 中移除 `SpeechToText` data object 定义（约第1383-1395行），它引用了已删除的 `SpeechToTextToolScreen`。
- [ ] 从 `OperitScreens.kt` 中移除相关 import 语句：`SpeechServicesSettingsScreen`、`SpeechToTextToolScreen`、`TextToSpeechToolScreen`。
- [ ] 从 `OperitScreens.kt` 中移除所有对 `navigateToSpeechServicesSettings`、`navigateTo(TextToSpeech)`、`navigateTo(SpeechToText)` 的调用。
- [ ] 从 `ScreenRouteRegistry.kt` 中移除 `Screen.TextToSpeech` 路由注册（约第196行）和 `Screen.SpeechToText` 路由注册（约第205行）。
- [ ] 检查并移除其他导航配置中对这三个屏幕的引用（如侧边栏菜单项、设置页面链接等）。

---

## Task 8: 修改AIForegroundService - 移除语音唤醒监听

**Files:**
- `api/chat/AIForegroundService.kt` (修改)

**Steps:**
- [ ] 移除 `WakeWordPreferences` 的 import 和所有 `wakePrefs` 相关字段声明，包括：`wakePrefs` 实例、`wakeListeningEnabled`、`currentWakePhrase`、`wakePhraseRegexEnabled`、`wakeRecognitionMode`、`personalWakeTemplates`、`wakeListeningSuspendedForIme`、`wakeListeningSuspendedForFloatingFullscreen`、`wakeHandoffPending`、`wakeStopInProgress`、`pendingWakeTriggeredAtMs`、`lastWakeTriggerAtMs`、`wakeMonitorJob`、`wakeListeningJob`、`personalWakeJob`、`personalWakeListener`、`wakeResumeJob`、`wakeStateApplyJob`、`wakeStateMutex`、`wakeListeningMicActiveForRecordingDetection` 等与唤醒相关的所有状态变量。
- [ ] 移除 `PersonalWakeListener` 和 `SpeechPrerollStore` 的 import。
- [ ] 移除 companion object 中的唤醒相关常量：`ACTION_TOGGLE_WAKE_LISTENING`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_IME`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_FLOATING_FULLSCREEN`、`ACTION_PREPARE_WAKE_HANDOFF`、`REQUEST_CODE_TOGGLE_WAKE_LISTENING`、`EXTRA_IME_VISIBLE`、`EXTRA_FLOATING_FULLSCREEN_ACTIVE`。
- [ ] 移除 `startWakeMonitoring()` 方法整体（约第1358行起），该方法收集 `WakeWordPreferences` 的各 Flow 并启动唤醒监听。
- [ ] 移除 `stopWakeMonitoring()` 方法整体（约第1424行起），该方法取消 `wakeMonitorJob`。
- [ ] 移除 `startWakeListening()` 和 `startWakeListeningLocked()` 方法整体（约第1530行起），包含 STT 模式唤醒监听的启动逻辑。
- [ ] 移除 `startPersonalWakeListening()` 方法整体（约第1690行起），包含个人化唤醒模板的监听逻辑。
- [ ] 移除 `stopWakeListening()` 和 `stopWakeListeningLocked()` 方法整体（约第1741行起），包含停止唤醒监听和释放 Provider 的逻辑。
- [ ] 移除 `triggerWakeLaunch()` 方法整体（约第1808行起），该方法在唤醒词命中后启动悬浮窗。
- [ ] 移除 `scheduleWakeResume()` 方法整体（约第1771行起），该方法在唤醒交互结束后恢复唤醒监听。
- [ ] 移除 `applyWakeListeningState()` 和 `applyWakeListeningStateLocked()` 方法整体（约第541行起），该方法根据各条件决定是否启停唤醒监听。
- [ ] 移除 `updateWakeListeningSuspendedForIme()` 方法（约第520行起）和 `setWakeListeningSuspendedForFloatingFullscreen()` 静态方法。
- [ ] 移除 `ensureMicrophoneForeground()` 中与唤醒相关的逻辑（保留方法本身，因为 TTS 播放可能仍需要前台服务类型提升）。
- [ ] 在 `onStartCommand()` 中移除对 `ACTION_TOGGLE_WAKE_LISTENING`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_IME`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_FLOATING_FULLSCREEN`、`ACTION_PREPARE_WAKE_HANDOFF` 的处理分支（约第1169-1210行）。
- [ ] 在 `onCreate()` 中移除 `startWakeMonitoring()` 调用（约第944行）。
- [ ] 在 `onDestroy()` 中移除 `stopWakeMonitoring()` 调用（约第1296行）。
- [ ] 移除 `createNotification()` 中与唤醒监听状态相关的通知按钮和文案逻辑。
- [ ] 保留 TTS 播放相关逻辑：`voiceService` 字段、`speak()` 调用、`speakingStateFlow` 监听、前台服务通知等。
- [ ] 保留 `ACTION_ENSURE_MICROPHONE_FOREGROUND` 和 `ACTION_START_OR_REFRESH_EXTERNAL_HTTP` / `ACTION_STOP_EXTERNAL_HTTP` 等非唤醒相关的 Action 处理。

---

## Task 9: 修改ChatViewModel - 移除朗读按钮状态

**Files:**
- `ui/features/chat/viewmodel/ChatViewModel.kt` (修改)

**Steps:**
- [ ] 移除朗读按钮相关的状态字段：`_isSpeechSessionActive`（第144行）、`isSpeechSessionActive`（第145行）、`_isSpeechPaused`（第146行）、`isSpeechPaused`（第147行）。注意：`_isPlaying` 和 `isPlaying` 需要保留，因为 TTS 工具调用仍需要跟踪播放状态。
- [ ] 移除 `isAutoReadEnabled` 字段（第150行）及其 lazy 委托 `apiConfigDelegate.enableAutoRead`。
- [ ] 移除 `toggleAutoRead()` 方法（约第3065行），该方法切换自动朗读开关。
- [ ] 移除 `enableAutoReadAndSpeak()` 方法（约第3088行），该方法在特定场景下启用自动朗读并播放。
- [ ] 移除 `logSpeechState()` 辅助方法（约第125行），该方法记录语音会话状态日志。
- [ ] 移除 `speechPreview()` 辅助方法（约第119行），该方法截断语音文本用于日志。
- [ ] 移除 `speechControlsHideJob` 字段（约第114行），该 Job 用于控制语音控制按钮的自动隐藏。
- [ ] 清理 `voiceStateCollectionJob` 中对 `_isSpeechSessionActive` 和 `_isSpeechPaused` 的引用（约第2890-2910行），移除朗读会话管理逻辑，仅保留 `_isPlaying` 状态的更新。
- [ ] 保留 `voiceService` 字段和 TTS 工具调用链路：当 AI 调用 TTS 工具时，仍通过 `voiceService.speak()` 播放语音，`_isPlaying` 状态仍需正确更新。
- [ ] 保留 `SpeechServicesPreferences` 引用，因为 TTS 工具调用仍需要读取配置。

---

## Task 10: 修改AIChatScreen - 移除朗读按钮

**Files:**
- `ui/features/chat/screens/AIChatScreen.kt` (修改)

**Steps:**
- [ ] 移除 `isAutoReadEnabled` 状态收集（约第320行）：`val isAutoReadEnabled by actualViewModel.isAutoReadEnabled.collectAsState()`。
- [ ] 移除所有传递 `isAutoReadEnabled` 和 `onToggleAutoRead` 参数的调用点（约第1108-1109行、第1168行、第1941-1942行），涉及 `ChatInputBottomBar` 和 `AgentChatInputSection` 的调用。
- [ ] 移除 `requestMicrophonePermissionLauncher`（约第145-161行），该 launcher 用于语音输入的麦克风权限请求，朗读按钮移除后不再需要。
- [ ] 清理因参数移除导致的编译错误，确保所有调用链不再传递 `isAutoReadEnabled` / `onToggleAutoRead`。

---

## Task 11: 修改AgentChatInputSection - 移除语音输入按钮和朗读按钮

**Files:**
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt` (修改)

**Steps:**
- [ ] 移除函数参数 `isAutoReadEnabled: Boolean = false`（第210行）和 `onToggleAutoRead: () -> Unit = {}`（第211行）。
- [ ] 移除 `Icons.Default.Mic` 的 import（第31行），该图标用于语音输入按钮。
- [ ] 移除 `Icons.AutoMirrored.Outlined.VolumeOff` 和 `Icons.AutoMirrored.Rounded.VolumeUp` 的 import（第24-25行），这些图标用于朗读按钮。
- [ ] 移除输入菜单中麦克风按钮（语音输入入口）的 UI 代码，搜索 `Icons.Default.Mic` 的使用位置（约第1073行和第1373行），移除对应的 IconButton 或菜单项。
- [ ] 移除朗读按钮的 UI 代码（约第2411-2417行），包含 `VolumeUp`/`VolumeOff` 图标切换和 `onToggleAutoRead` 回调。
- [ ] 移除内部子组件中传递 `isAutoReadEnabled` / `onToggleAutoRead` 的参数（约第1435-1436行）。
- [ ] 清理因参数移除导致的子组件签名变化，确保所有调用方和定义方一致。

---

## Task 12: 修改OperitApplication - 移除语音唤醒初始化

**Files:**
- `core/application/OperitApplication.kt` (修改)

**Steps:**
- [ ] 移除 `WakeWordPreferences` 的 import（第43行）。
- [ ] 修改 `startGlobalAIForegroundServiceIfNeeded()` 方法（约第475-497行），移除 `alwaysListeningEnabled` 的读取逻辑（第477-479行）。修改条件判断：原来在 `alwaysListeningEnabled || externalHttpEnabled` 时启动前台服务，现在仅在 `externalHttpEnabled` 时启动。修改后的条件为：`if (!externalHttpEnabled || AIForegroundService.isRunning.get()) return`。
- [ ] 保留 `SpeechService` 相关的初始化逻辑（如果存在），确保 API 层的语音服务仍可正常初始化。

---

## Task 13: 清理悬浮窗中的语音交互引用

**Files:**
- `ui/floating/ui/fullscreen/viewmodel/FloatingFullscreenModeViewModel.kt` (修改)
- `ui/floating/ui/ball/SiriBall.kt` (修改)
- `services/FloatingChatService.kt` (修改)

**Steps:**
- [ ] 在 `FloatingFullscreenModeViewModel.kt` 中，移除 `SpeechInteractionManager` 的实例化和所有引用（约第39行 `speechManager` 字段）。移除 `WakeWordPreferences` 的引用（`wakePrefs` 字段和 `inactivityTimeoutSeconds` 等）。移除 `initialize()` 方法中 `speechManager.initialize()` 调用（约第427行）。移除 `enterWaveMode()` 和语音捕获相关方法。移除 `EXTRA_AUTO_ENTER_VOICE_CHAT` 和 `EXTRA_WAKE_LAUNCHED` 的处理。将全屏模式从语音交互模式简化为纯文本输入模式。
- [ ] 在 `SiriBall.kt` 中，移除 `SpeechInteractionManager` 的引用，移除语音输入相关的 UI 交互（长按说话、语音波形等）。
- [ ] 在 `FloatingChatService.kt` 中，移除 `EXTRA_AUTO_ENTER_VOICE_CHAT` 和 `EXTRA_WAKE_LAUNCHED` 的 intent extra 处理（约第423行）。移除 `WakeWordPreferences` 的引用（`wakePrefs` 字段）。移除 `AIForegroundService.setWakeListeningSuspendedForFloatingFullscreen()` 调用（约第410行）。移除 `wakeLaunched` 和 `autoEnterVoiceChat` 状态变量及其处理逻辑。

---

## Task 14: 清理助手配置界面中的语音相关UI

**Files:**
- `ui/features/assistant/screens/AssistantConfigScreen.kt` (修改)

**Steps:**
- [ ] 移除 `WakeWordPreferences` 的 import 和实例化（约第61行 `wakePrefs`）。
- [ ] 移除所有从 `WakeWordPreferences` 收集的状态变量：`wakeListeningEnabled`、`wakePhrase`、`wakePhraseRegexEnabled`、`wakeRecognitionMode`、`personalWakeTemplates`、`inactivityTimeoutSeconds`、`wakeGreetingEnabled`、`wakeGreetingText`、`wakeCreateNewChatOnWakeEnabled`、`autoNewChatGroup`、`voiceAutoAttachEnabled`、`voiceAutoAttachItems`。
- [ ] 移除 `LaunchedEffect(wakePrefs) { wakePrefs.migrateVoiceAutoAttachItemsIfNeeded() }` 调用。
- [ ] 移除界面中所有与唤醒词配置相关的 UI 区块：唤醒开关、唤醒词输入、识别模式选择、个人化唤醒模板、不活跃超时、唤醒问候语、语音自动附加等。
- [ ] 移除 `VoiceAutoAttachComponents` 的 import 和使用。
- [ ] 移除 `requestMicPermissionLauncher`（如果仅用于唤醒词功能）。

---

## Task 15: 清理EnhancedAIService中的唤醒词引用

**Files:**
- `api/chat/EnhancedAIService.kt` (修改)

**Steps:**
- [ ] 搜索并移除 `WakeWordPreferences` 的 import 和所有引用。`EnhancedAIService` 可能在发送消息时检查唤醒相关状态，需逐一清理。
- [ ] 确保消息发送和 AI 对话的核心链路不依赖唤醒词系统。

---

## Task 16: 清理字符串资源和其它资源

**Files:**
- `res/values/strings.xml` (修改)
- `res/values-en/strings.xml` (修改)
- `res/values-es/strings.xml` (修改)
- `res/values-pt-rBR/strings.xml` (修改)
- `res/values-ms/strings.xml` (修改)
- `res/values-id/strings.xml` (修改)

**Steps:**
- [ ] 在所有语言的 `strings.xml` 中，搜索并移除与以下功能相关的字符串资源：唤醒词（`wake_word_*`）、语音助手小部件（`voice_assistant_*`）、语音交互（`voice_interaction_*`）、语音自动附加（`voice_auto_attach_*`、`voice_keyword_*`）、STT独立界面（`speech_to_text_*`）、TTS独立界面（`text_to_speech_*`）、语音设置界面（`speech_services_settings_*`）、朗读按钮（`auto_read_*`）。
- [ ] 保留与 API 层相关的字符串资源：TTS Provider 错误提示（`openai_tts_error_*`、`mimo_tts_error_*`、`deepgram_*`）、STT Provider 错误提示、语音工具相关字符串。
- [ ] 检查 `res/drawable/` 和 `res/mipmap/` 中是否有语音助手小部件专用的图标资源，如有则删除。

---

## Task 17: 编译验证和最终清理

**Files:**
- 项目全局 (验证)

**Steps:**
- [ ] 执行项目编译（`./gradlew assembleDebug` 或等效命令），确认无编译错误。
- [ ] 搜索整个代码库中残留的 `WakeWordPreferences`、`SpeechInteractionManager`、`OperitVoiceInteractionService`、`OperitVoiceInteractionSessionService`、`VoiceAssistantGlanceWidget`、`VoiceAssistantWidgetReceiver`、`VoiceAutoAttachComponents`、`SpeechServicesSettingsScreen`、`SpeechToTextScreen`、`SpeechToTextToolScreen`、`TextToSpeechScreen`、`TextToSpeechToolScreen` 引用，逐一清理。
- [ ] 搜索残留的 `isAutoReadEnabled`、`toggleAutoRead`、`onToggleAutoRead` 引用，确保已全部移除。
- [ ] 搜索残留的 `ACTION_TOGGLE_WAKE_LISTENING`、`ACTION_PREPARE_WAKE_HANDOFF`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_IME`、`ACTION_SET_WAKE_LISTENING_SUSPENDED_FOR_FLOATING_FULLSCREEN` 引用，确保已全部移除。
- [ ] 验证保留的 API 能力文件未被误删或误改：`api/speech/SpeechService.kt`、`api/speech/SherpaSpeechProvider.kt`、`api/speech/OpenAISttProvider.kt`、`api/speech/DeepgramSttProvider.kt`、`api/voice/VoiceService.kt`、`api/voice/` 目录下所有 VoiceProvider、`data/preferences/SpeechServicesPreferences.kt`、`core/tools/defaultTool/standard/StandardSpeechTools.kt`、`ui/common/markdown/MarkdownAudioRenderer.kt`、`ui/common/displays/WaveVisualizer.kt`。
- [ ] 确认 `SpeechServiceFactory.createWakeSpeechService()` 方法可以移除（因为唤醒功能已删除），但 `SpeechServiceFactory.createSpeechService()` 必须保留。
