# P0 杂项移除 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 一次性移除深度搜索、液态玻璃主题、公告系统、简化悬浮窗、移除web-chat Web UI 五个P0功能，清理所有相关代码和引用。
**Architecture:** 五个任务各自独立，按A→B→C→D→E顺序执行。每个任务先删除目标文件，再清理所有引用点（import、调用、路由注册），最后编译验证。悬浮窗简化（Task D）涉及行为变更而非纯删除，需保留进度气泡并移除输入/PET模式。web-chat Web UI移除（Task E）仅移除前端资源和角色选择器端点，保留所有API端点。
**Tech Stack:** Kotlin, Jetpack Compose, CMake, Android

---

## Task A: 深度搜索移除（PLAN.md 6.5）

### 文件变更

- [ ] 删除 `examples/deepsearching/` 整个目录（含 src/、dist/、manifest.json、tsconfig.json）
- [ ] 从 `packages_whitelist.txt` 中移除 `deepsearching` 行

### 步骤

- [ ] 执行 `rm -rf examples/deepsearching/`
- [ ] 编辑 `packages_whitelist.txt`，删除包含 `deepsearching` 的行
- [ ] 全局搜索 `deepsearching` 确认无其他引用（已确认：仅在 examples/ 和 packages_whitelist.txt、README.md、PLAN.md、docs/ 中出现，Android 代码无引用）

---

## Task B: 液态玻璃主题移除（PLAN.md 6.6）

### 文件变更

- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/theme/LiquidGlass.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/theme/WaterGlass.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/model/SerializableColorScheme.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/model/SerializableTypography.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/preferences/ThemePreferenceSnapshot.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ThemeSettingsScreen.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/settings/sections/ThemeSettingsCoreSections.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/settings/sections/ThemeSettingsColorSection.kt`

### 引用清理（31个文件引用了上述模块）

- [ ] **Theme.kt**（`ui/theme/Theme.kt`）：移除 `LocalLiquidGlassBackdrop`、`LocalWaterGlassState`、`rememberLiquidState()`、`isWaterGlassSupported()` 相关的 CompositionLocal provider 和 import；移除 `liquidGlassBackdrop` 变量声明和 `waterGlassState` 变量声明
- [ ] **ThemeColorSchemeResolver.kt**（`ui/theme/ThemeColorSchemeResolver.kt`）：移除 `ThemePreferenceSnapshot` import 和参数引用，将 `snapshot: ThemePreferenceSnapshot` 参数从 `resolveColorScheme()` 中删除，改为直接使用传入的颜色参数
- [ ] **NavigationDrawerAppearance.kt**（`ui/main/components/NavigationDrawerAppearance.kt`）：移除 `liquidGlass()` modifier 调用，替换为普通 `background()` 或 `shadow()`
- [ ] **DrawerContent.kt**（`ui/main/components/DrawerContent.kt`）：移除 `liquidGlass()` modifier 调用
- [ ] **NavigationComponents.kt**（`ui/main/components/NavigationComponents.kt`）：移除 `liquidGlass()` modifier 调用
- [ ] **WebSessionFloatingTheme.kt**（`ui/features/websession/browser/WebSessionFloatingTheme.kt`）：移除 `LiquidGlass`/`WaterGlass` 相关 import 和调用
- [ ] **WorkspaceFileSelector.kt**（`ui/features/chat/webview/WorkspaceFileSelector.kt`）：移除 `liquidGlass()` modifier
- [ ] **ChatViewModel.kt**（`ui/features/chat/viewmodel/ChatViewModel.kt`）：移除 `SerializableColorScheme`/`SerializableTypography`/`ThemePreferenceSnapshot` 相关 import 和使用
- [ ] **AIChatScreen.kt**（`ui/features/chat/screens/AIChatScreen.kt`）：移除 `liquidGlass()` modifier 和相关 import
- [ ] **MessageImageGenerator.kt**（`ui/features/chat/util/MessageImageGenerator.kt`）：移除 `SerializableColorScheme`/`SerializableTypography` 相关引用
- [ ] **AgentChatInputSection.kt**（`ui/features/chat/components/style/input/agent/AgentChatInputSection.kt`）：移除 `liquidGlass()` modifier
- [ ] **ClassicChatInputSection.kt**（`ui/features/chat/components/style/input/classic/ClassicChatInputSection.kt`）：移除 `liquidGlass()` modifier
- [ ] **UserMessageComposable.kt**（`ui/features/chat/components/style/cursor/UserMessageComposable.kt`）：移除 `liquidGlass()` modifier
- [ ] **CursorStyleChatMessage.kt**（`ui/features/chat/components/style/cursor/CursorStyleChatMessage.kt`）：移除 `liquidGlass()` modifier
- [ ] **BubbleStyleChatMessage.kt**（`ui/features/chat/components/style/bubble/BubbleStyleChatMessage.kt`）：移除 `liquidGlass()` modifier
- [ ] **BubbleUserMessageComposable.kt**（`ui/features/chat/components/style/bubble/BubbleUserMessageComposable.kt`）：移除 `liquidGlass()` modifier
- [ ] **BubbleAiMessageComposable.kt**（`ui/features/chat/components/style/bubble/BubbleAiMessageComposable.kt`）：移除 `liquidGlass()` modifier
- [ ] **ChatScreenContent.kt**（`ui/features/chat/components/ChatScreenContent.kt`）：移除 `liquidGlass()` modifier
- [ ] **ChatArea.kt**（`ui/features/chat/components/ChatArea.kt`）：移除 `liquidGlass()` modifier
- [ ] **FloatingChatService.kt**（`services/FloatingChatService.kt`）：移除 `SerializableColorScheme`/`SerializableTypography`/`ThemePreferenceSnapshot` 相关引用
- [ ] **WebChatHttpBridge.kt**（`integrations/http/WebChatHttpBridge.kt`）：移除 `SerializableColorScheme`/`SerializableTypography` 相关引用
- [ ] **WebChatModels.kt**（`integrations/http/WebChatModels.kt`）：移除 `SerializableColorScheme`/`SerializableTypography` 相关序列化模型
- [ ] **UserPreferencesManager.kt**（`data/preferences/UserPreferencesManager.kt`）：移除 `ThemePreferenceSnapshot` 相关的偏好键和方法
- [ ] **OperitScreens.kt**（`ui/main/screens/OperitScreens.kt`）：移除 `ThemeSettingsScreen` import 和 `ThemeSettings` 路由定义（第976行 `data object ThemeSettings` 及第988行 `ThemeSettingsScreen()` 调用），移除所有 `navigateToThemeSettings` 回调传递
- [ ] 移除所有 `liquidGlass()` 调用后，将其替换为等效的 `shadow()` + `background()` 组合，保持视觉一致性
- [ ] 编译验证：`./gradlew assembleDebug` 确认无编译错误

---

## Task C: 公告系统移除（PLAN.md 6.7）

### 文件变更

- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/announcement/RemoteAnnouncementRepository.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/data/preferences/RemoteAnnouncementPreferences.kt`
- [ ] 删除 `app/src/main/java/com/ai/assistance/operit/ui/features/announcement/RemoteAnnouncementDialog.kt`

### 引用清理

- [ ] **OperitApp.kt**（`ui/main/OperitApp.kt`）：
  - 移除 `import com.ai.assistance.operit.data.announcement.RemoteAnnouncementDisplay`
  - 移除 `import com.ai.assistance.operit.data.announcement.RemoteAnnouncementRepository`
  - 移除 `import com.ai.assistance.operit.data.preferences.RemoteAnnouncementPreferences`
  - 移除 `import com.ai.assistance.operit.ui.features.announcement.RemoteAnnouncementDialog`
  - 移除第101行 `val remoteAnnouncementRepository = remember { RemoteAnnouncementRepository() }`
  - 移除第102行 `val remoteAnnouncementPreferences = remember { RemoteAnnouncementPreferences(context) }`
  - 移除第298行 `var remoteAnnouncement by remember { mutableStateOf<RemoteAnnouncementDisplay?>(null) }`
  - 移除第300行 `dismissRemoteAnnouncement()` 函数定义
  - 移除第496-501行 `RemoteAnnouncementDialog(...)` 调用及其 `onAcknowledge` 回调
  - 移除所有与公告相关的 LaunchedEffect 或数据获取逻辑
- [ ] 全局搜索 `RemoteAnnouncement` 确认无其他引用
- [ ] 编译验证

---

## Task D: 悬浮窗简化（README 阶段二）

### 目标

将悬浮窗功能简化为仅保留进度气泡（黑色圆形气泡，悬浮在屏幕中间上部），移除PET模式和输入功能。

### 文件变更

- [ ] **FloatingMode.kt**（`ui/floating/FloatingMode.kt`）：移除 `WINDOW`、`FULLSCREEN`、`SCREEN_OCR`、`RESULT_DISPLAY` 枚举值，仅保留 `BALL` 和 `VOICE_BALL`
- [ ] **FloatingWindowManager.kt**（`services/floating/FloatingWindowManager.kt`）：
  - 移除 `FloatingWindowCallback` 中的输入相关方法：`onSendMessage`、`onAttachmentRequest`、`onRemoveAttachment`、`getAttachments`、`getInputProcessingState`
  - 移除 `setFocusable()` 方法及其所有调用
  - 移除 `scheduleImeShow()` 方法
  - 移除 `cancelFocusBeforeExit()` 方法
  - 移除 `ensureFocusDismissView()` 方法和 `focusDismissView` 字段
  - 移除 `setFocusDismissOverlayEnabled()` 方法
  - 移除 `FullscreenRainbowStatusIndicator()` Composable
  - 移除 `TopBarStatusIndicator()` Composable
  - 简化 `createLayoutParams()`：仅处理 BALL/VOICE_BALL 模式的布局参数
  - 简化 `switchMode()`：仅处理 BALL/VOICE_BALL 之间的切换
  - 简化 `FloatingChatUi()`：移除 `FloatingChatWindow` 调用，替换为简单的黑色圆形气泡 Composable
  - 移除 `applyFullscreenBlur()` 方法
  - 移除 `applyFullscreenOverlayWindowPolicy()` 方法
  - 移除 `onMove()` 中对 WINDOW/FULLSCREEN 模式的处理
  - 移除 `updateWindowSizeInLayoutParams()` 方法
  - 移除 `sizeAnimator` 字段
  - 移除 `pendingImeFocusRunnable` 字段
  - 移除 `focusDismissOverlayRequested` 字段
  - 移除 `windowDisplayEnabled`、`windowPersistentHidden` 字段
  - 简化 `refreshWindowAndIndicatorVisibility()`：仅处理气泡可见性
  - 将气泡默认位置设为屏幕中间上部（gravity = TOP | CENTER_HORIZONTAL，y偏移约屏幕高度的1/6）
  - 气泡UI：黑色圆形，直径约48dp，带 `CircularProgressIndicator` 显示进度
- [ ] **FloatingChatWindow.kt**（`ui/floating/FloatingChatWindow.kt`）：大幅简化，移除所有 WINDOW/FULLSCREEN/SCREEN_OCR/RESULT_DISPLAY 模式的 UI，仅保留 BALL/VOICE_BALL 的气泡渲染
- [ ] **FloatingWindowState.kt**（`services/floating/FloatingWindowState.kt`）：移除 `windowWidth`、`windowHeight`、`windowScale`、`lastWindowPositionX/Y`、`lastWindowScale`、`fullscreenSystemBlurActive`、`isAtEdge`、`ballExploding`、`showInputDialog`、`userMessage` 等与窗口/输入模式相关的状态字段
- [ ] **FloatingChatBallMode.kt**（`ui/floating/ui/ball/FloatingChatBallMode.kt`）：简化为纯黑色圆形气泡，移除输入相关UI
- [ ] **FloatingVoiceBallMode.kt**（`ui/floating/ui/ball/FloatingVoiceBallMode.kt`）：简化为纯黑色圆形气泡
- [ ] **FloatingResultDisplay.kt**（`ui/floating/ui/ball/FloatingResultDisplay.kt`）：删除此文件（RESULT_DISPLAY 模式已移除）
- [ ] 删除 `ui/floating/ui/fullscreen/` 整个目录
- [ ] 删除 `ui/floating/ui/window/` 整个目录
- [ ] 删除 `ui/floating/ui/screenocr/` 整个目录（如存在）
- [ ] **FloatContext.kt**（`ui/floating/FloatContext.kt`）：移除 `onSendMessage`、`onCancelMessage`、`onAttachmentRequest`、`onRemoveAttachment`、`onInputFocusRequest`、`showInputDialog`、`userMessage`、`attachments` 等输入相关字段
- [ ] **FloatingChatService.kt**（`services/FloatingChatService.kt`）：移除 PET 模式相关逻辑，移除 `onSendMessage`、`onAttachmentRequest`、`onRemoveAttachment` 回调实现，简化 `FloatingWindowCallback` 实现
- [ ] **AIForegroundService.kt**（`api/chat/AIForegroundService.kt`）：移除 `setWakeListeningSuspendedForFloatingFullscreen` 调用
- [ ] **ChatViewModel.kt**（`ui/features/chat/viewmodel/ChatViewModel.kt`）：移除 `FloatingWindowDelegate` 中对悬浮窗输入功能的支持
- [ ] **FloatingWindowDelegate.kt**（`ui/features/chat/viewmodel/FloatingWindowDelegate.kt`）：移除输入相关方法
- [ ] **OperitAssistActivity.kt**（`services/assistant/OperitAssistActivity.kt`）：移除对 WINDOW/FULLSCREEN 模式的引用
- [ ] **OperitVoiceInteractionSessionService.kt**（`services/assistant/OperitVoiceInteractionSessionService.kt`）：移除对 WINDOW/FULLSCREEN 模式的引用
- [ ] **StandardChatManagerTool.kt**（`core/tools/defaultTool/standard/StandardChatManagerTool.kt`）：移除对悬浮窗输入功能的引用
- [ ] **VoiceAssistantGlanceWidget.kt**（`widget/VoiceAssistantGlanceWidget.kt`）：移除对非 BALL 模式的引用
- [ ] 编译验证

---

## Task E: web-chat Web UI 移除（README 阶段二）

### 目标

移除 web-chat 的 Web UI 前端资源，保留所有 API 端点。仅移除角色选择器相关端点（/character-selector）。

### 文件变更

- [ ] 删除 `web-chat/` 目录下的前端资源文件（保留目录结构以便后续可能的API文档放置）：
  - 删除 `web-chat/index.html`
  - 删除 `web-chat/vite.config.ts`
  - 删除 `web-chat/package.json`
  - 删除 `web-chat/tsconfig.json`
  - 删除 `web-chat/public/` 目录
  - 删除 `web-chat/src/` 整个目录（含所有 .tsx、.ts、.css 文件）
  - 删除 `web-chat/scripts/` 目录
- [ ] 或者直接删除整个 `web-chat/` 目录（如果确认无其他用途）

### API 端点清理

- [ ] **WebChatHttpBridge.kt**（`integrations/http/WebChatHttpBridge.kt`）：
  - 移除 `CHARACTER_SELECTOR_PATH` 常量（第2764行）
  - 移除 `handleCharacterSelector()` 方法（第291-294行）
  - 移除 `buildCharacterSelectorResponse()` 方法（第1445行起）
  - 移除路由分发中对 `CHARACTER_SELECTOR_PATH` 的匹配（第121-122行）
  - 移除 `handleSetActivePrompt()` 中对 `buildCharacterSelectorResponse()` 的调用（第333行），改为返回简单的成功响应
  - 保留所有其他 API 端点（bootstrap、active-prompt、model-selector、memory-selector、input-settings、chats、uploads、assets 等）
  - 移除静态资源服务逻辑中对 `web-chat/index.html` 的引用（`DEFAULT_STATIC_ASSET` 常量），改为返回 404
- [ ] **WebChatModels.kt**（`integrations/http/WebChatModels.kt`）：
  - 移除 `WebCharacterSelectorResponse` 数据类（第461行）
  - 移除 `WebCharacterCardSelectorItem` 数据类（第427行）
  - 移除 `WebCharacterGroupSelectorItem` 数据类（第443行）

### 引用清理

- [ ] 全局搜索 `character-selector` 和 `CharacterSelector` 确认无其他 Android 端引用（已确认：仅 WebChatHttpBridge.kt 和 WebChatModels.kt 使用）
- [ ] 检查 `web-chat/scripts/sync-to-android-assets.mjs` 是否被构建流程引用，若是则需从构建脚本中移除相关步骤
- [ ] 编译验证

---

## 验证清单

- [ ] 执行 `./gradlew assembleDebug` 编译通过
- [ ] 全局搜索 `deepsearching`、`LiquidGlass`、`WaterGlass`、`SerializableColorScheme`、`SerializableTypography`、`ThemePreferenceSnapshot`、`RemoteAnnouncement`、`character-selector` 确认无残留引用
- [ ] 确认悬浮窗简化后气泡正常显示在屏幕中间上部
- [ ] 确认 web-chat API 端点（除 character-selector 外）仍可正常访问
