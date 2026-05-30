# Operit AI 简化与增强实施方案

> 本方案基于对项目31个模块的深度研究，结合用户反馈修正，形成可执行的完整方案。

---

## 一、总体策略

### 1.1 核心原则

1. **先删后建**：先完成所有模块移除和简化，再进行新功能开发
2. **分层推进**：按 数据层 → 逻辑层 → UI层 顺序修改
3. **API兼容**：移除UI但保留API的功能，用 `@Deprecated` 标注
4. **增量验证**：每个阶段完成后确保项目可编译运行

### 1.2 分期规划

| 阶段 | 名称 | 核心目标 |
|------|------|---------|
| P0 | 代码清理 | 移除砍掉的模块，清理死代码 |
| P1 | 架构调整 | 权限系统重构、AutoGLM内置化、长程任务增强 |
| P2 | UI重构 | 侧边栏、聊天界面、设置页面、主题系统 |
| P3 | 新功能 | 待办、日程、上下文自动注入 |
| P4 | 收尾打磨 | 字符串清理、性能优化、兼容性测试 |

---

## 二、P0 - 代码清理

### 2.1 虚拟形象系统移除

**移除范围**：

| 目录/文件 | 说明 |
|----------|------|
| `core/avatar/` | 整个目录删除（含common/impl/factory，DragonBones/MMD/FBX/GLTF控制器、模型、视图、状态） |
| `ui/features/assistant/` | 整个目录删除（AvatarConfigSection、AvatarPreviewSection、AssistantConfigViewModel、WaifuModeSettingsScreen） |
| `ui/floating/ui/pet/` | 整个目录删除（AvatarEmotionManager） |
| `ui/components/ManagedDragonBonesView.kt` | 删除 |
| `data/model/DragonBones.kt` | 删除 |
| `data/model/CustomEmoji.kt` | 删除 |
| `data/model/CharacterGroupCard.kt` | 删除 |
| `data/repository/AvatarRepository.kt` | 删除 |
| `data/preferences/WaifuPreferences.kt` | 删除 |
| `app/src/main/cpp/dragonbones/` | 整个C++原生模块删除 |
| `app/src/main/cpp/fbx/` | 整个C++原生模块删除 |
| `app/src/main/cpp/mmd/` | 整个C++原生模块删除 |
| `app/src/main/assets/emoji/` | 表情资源目录删除 |
| `app/src/main/assets/pets/` | 桌宠资源目录删除 |
| `app/src/main/assets/dragonbones/` | DragonBones资源目录删除 |

**沙箱包API处理**：

`ToolPkgCommonBridgePlugin.kt` 中暴露给JS沙箱包的虚拟形象相关API（avatar、dragonbones、emoji、pet、waifu相关方法），处理方式：
- 保留方法签名，方法体改为返回空值/默认值
- 方法标注 `@Deprecated`
- 在方法注释中说明"虚拟形象功能已移除，此API仅保留兼容性"
- 沙箱包调用这些API时不会崩溃，但不会产生实际效果

**引用清理**：
- `OperitApplication.kt`：移除虚拟形象初始化代码
- `AIForegroundService.kt`：移除桌宠/虚拟形象相关逻辑（`startPetMode`、`stopPetMode`、桌宠悬浮窗控制）
- `FloatingWindowManager`：移除桌宠模式（PET模式）
- `OperitScreens.kt`：移除助手配置页面路由
- `DrawerContent.kt`：移除助手配置入口
- `NavItem.kt`：移除助手相关导航项
- `CMakeLists.txt`：移除 dragonbones、fbx、mmd 三个模块的编译配置

### 2.2 语音系统简化（保留API能力）

语音功能**不是完全移除**，而是移除用户直接交互功能，保留API调用能力。

**移除的用户交互功能**：

| 文件 | 处理方式 |
|------|---------|
| `services/assistant/OperitVoiceInteractionService.kt` | 删除（语音唤醒服务） |
| `services/assistant/OperitVoiceInteractionSessionService.kt` | 删除（语音会话服务） |
| `ui/floating/voice/SpeechInteractionManager.kt` | 删除（语音交互管理） |
| `data/preferences/WakeWordPreferences.kt` | 删除（唤醒词配置） |
| `widget/VoiceAssistantGlanceWidget.kt` | 删除（语音助手桌面小部件） |
| `ui/features/toolbox/screens/speechtotext/SpeechToTextScreen.kt` | 删除（STT独立界面） |
| `ui/features/toolbox/screens/texttospeech/TextToSpeechScreen.kt` | 删除（TTS独立界面） |
| `ui/features/settings/screens/SpeechServicesSettingsScreen.kt` | 删除（语音服务设置界面） |
| `ui/features/assistant/components/VoiceAutoAttachComponents.kt` | 删除（语音自动附加组件） |

**保留的API能力**：

| 文件 | 保留原因 |
|------|---------|
| `api/speech/SpeechService.kt` | 保留，工作流节点和沙箱包通过API调用TTS/STT |
| `api/voice/VoiceService.kt` | 保留，AI生成音频、工作流语音节点需要 |
| `data/preferences/SpeechServicesPreferences.kt` | 保留，API调用需要配置信息 |
| `core/tools/defaultTool/standard/StandardSpeechTools.kt` | 保留，工具注册中的语音工具（`get_speech_services_config`、`set_speech_services_config`、`test_tts_playback`） |
| `ToolRegistration.kt` 中的语音工具注册 | 保留，AI和工作流通过工具调用使用语音能力 |
| `ui/common/markdown/MarkdownAudioRenderer.kt` | 保留，AI生成音频时需要渲染播放 |

**修改的文件**：
- `AIForegroundService.kt`：移除语音唤醒监听（`startWakeListening`、`stopWakeListening`、`startPersonalWakeListening`），保留TTS播放相关逻辑
- `ChatViewModel.kt`：移除语音朗读按钮状态管理，保留TTS工具调用支持
- `AIChatScreen.kt`：移除朗读按钮UI
- `AgentChatInputSection.kt`：移除语音输入按钮
- `OperitApplication.kt`：移除语音唤醒服务初始化，保留SpeechService初始化
- `SystemToolPrompts.kt`：保留语音工具提示词（AI通过工具调用使用语音能力）
- `FunctionalPrompts.kt`：移除语音交互相关提示词，保留语音合成/识别相关提示词

**工作流语音节点**：
- `WorkflowExecutor.kt` 中的语音相关节点类型保留
- 工作流编辑器中的语音节点保留
- 用户通过工作流仍可使用TTS/STT能力

### 2.3 角色卡系统（UI不可见，后端保留）

角色卡系统**不是移除**，而是在UI层面对用户不可见。所有后端逻辑、数据模型、API接口完整保留。

**移除的UI入口**：
- `OperitScreens.kt`：移除角色卡管理页面路由
- `DrawerContent.kt`：移除角色卡选择入口
- `NavItem.kt`：移除角色卡相关导航项
- `AIChatScreen.kt`：移除角色卡选择器面板（`CharacterSelectorPanel.kt`）
- `ChatScreenHeader.kt`：移除角色卡切换按钮

**保留的后端**：
- `data/preferences/CharacterCardManager.kt`：完整保留（1360行）
- `data/model/CharacterCard.kt`：完整保留
- `data/model/PromptTag.kt`：完整保留
- `data/preferences/PromptTagManager.kt`：完整保留
- `data/preferences/ActivePromptManager.kt`：完整保留
- `data/preferences/PromptVersionManager.kt`：完整保留
- `ChatViewModel.kt`：保留角色卡选择/绑定状态
- `ChatDao.kt`：保留 `characterId` 数据库字段
- `WebChatHttpBridge.kt`：保留所有角色卡API端点
- `ToolPkgCommonBridgePlugin.kt`：保留 `__operit_package_caller_card_id` 桥接字段
- `ToolRegistration.kt`：保留角色卡相关工具（`CliToolModeSupport.isToolNameAllowedForRoleCard`）

**沙箱包兼容**：
- 第三方沙箱包仍可通过API创建、选择、切换角色卡
- 角色卡的系统提示词注入逻辑保留
- 角色卡与记忆库的绑定逻辑保留

### 2.4 聊天样式简化

**移除范围**：

| 目录/文件 | 说明 |
|----------|------|
| `ui/features/chat/components/style/bubble/` | 整个目录删除（BubbleStyleChatMessage、BubbleAiMessageComposable、BubbleUserMessageComposable） |
| `ui/features/chat/components/style/input/classic/` | 整个目录删除（ClassicChatInputSection） |
| `data/model/ChatMessageDisplayMode.kt` | 删除 Bubble 枚举值，仅保留 CURSOR |

**修改**：
- `ChatScreenContent.kt`：移除样式选择逻辑，硬编码使用 Cursor 样式
- `ChatViewModel.kt`：移除 `chatStyle` 状态
- `DisplayPreferencesManager.kt`：移除聊天样式偏好字段
- `UserPreferencesManager.kt`：移除聊天样式和输入模式偏好

### 2.5 原有GUI操作能力移除（沙箱包层面）

AutoGLM和原有GUI操作能力**都是通过沙箱包实现的**，两者有重叠部分。核心原生API（`StandardUITools.kt` 中的 tap、swipe、clickElement、setText、pressKey、getPageInfo、captureScreenshot、runUiSubAgent）是共享的，被两个沙箱包共同调用。

**重叠分析**：
- `automatic_ui_base.js` 调用的原生API：`Tools.UI.tap`、`Tools.UI.longPress`、`Tools.UI.clickElement`、`Tools.UI.setText`、`Tools.UI.pressKey`、`Tools.UI.swipe`、`Tools.System.startApp`、`Tools.System.listApps`
- `automatic_ui_subagent.js` 调用的原生API：`Tools.UI.runSubAgent`（底层也调用上述基础操作）
- 两者都依赖 `StandardUITools.kt` 和 `PhoneAgent.kt` 提供的原生方法

**移除方案**：

| 文件 | 处理方式 |
|------|---------|
| `app/src/main/assets/packages/operit_editor.js` | 删除（原有简单GUI操作的编辑器沙箱包） |
| `examples/operit_editor.ts` | 删除 |
| `examples/operit_editor.js` | 删除 |

**保留的文件**（AutoGLM需要）：
- `PhoneAgent.kt`：保留，提供 `@JavascriptInterface` 原生方法（tap、longPress、clickElement、setText、pressKey、swipe、startApp），AutoGLM沙箱包依赖这些方法
- `StandardUITools.kt`：保留，提供UI操作的基础实现（tap、swipe、getText、clickElement、setInputText、pressKey、getPageInfo、captureScreenshot、runUiSubAgent），AutoGLM沙箱包依赖这些方法
- `app/src/main/assets/packages/automatic_ui_base.js`：保留，AutoGLM的基础UI操作沙箱包
- `app/src/main/assets/packages/automatic_ui_subagent.js`：保留，AutoGLM的子代理沙箱包
- `ui/features/toolbox/screens/autoglm/AutoGlmToolScreen.kt`：保留
- `ui/features/toolbox/screens/autoglm/AutoGlmViewModel.kt`：保留
- `core/tools/agent/PhoneAgentJobRegistry.kt`：保留，AutoGLM任务注册需要

**移除的文件**（原有简单GUI操作专用）：
- `core/tools/climode/CliToolModeSupport.kt`：删除（688行，原有简单GUI操作的CLI模式支持）
- `core/tools/agent/VirtualDisplayManager.kt`：删除（虚拟显示管理，仅原有简单GUI操作使用）
- `core/tools/agent/ShowerBinderRegistry.kt`（app层）：删除
- `core/tools/agent/ShowerController.kt`：删除
- `showerclient/`：整个Gradle模块删除
- `app/src/main/assets/shower-server.jar`：删除

**修改的文件**：
- `ToolRegistration.kt`：移除 `CliToolModeSupport` 相关的工具注册（`SEARCH_TOOL_NAME`、`PROXY_TOOL_NAME`、`PACKAGE_PROXY_TOOL_NAME`），保留原有UI操作工具注册（AutoGLM依赖）
- `SystemToolPrompts.kt`：移除CliToolMode相关提示词
- `AIForegroundService.kt`：移除Shower服务管理逻辑
- `settings.gradle.kts`：移除 `:showerclient` 模块

### 2.6 深度搜索移除（沙箱包层面）

深度搜索是由沙箱包 `deepsearching` 提供的能力，不是应用内置功能。

**移除范围**：

| 文件 | 说明 |
|------|------|
| `examples/deepsearching/` | 整个目录删除（深度搜索沙箱包源码） |

**修改的文件**：
- `ui/features/chat/components/part/CustomXmlRenderer.kt`：移除深度搜索相关的XML渲染插件注册
- `strings.xml`（所有语言）：移除深度搜索相关字符串

**注意**：深度搜索是第三方沙箱包，用户可能自行安装。移除内置的 `deepsearching` 包即可，不需要修改应用核心代码。

### 2.7 液态玻璃主题移除

**移除范围**：

| 文件 | 说明 |
|------|------|
| `ui/theme/LiquidGlass.kt` | 删除 |
| `ui/theme/WaterGlass.kt` | 删除 |
| `data/model/SerializableColorScheme.kt` | 删除（107行） |
| `data/model/SerializableTypography.kt` | 删除（95行） |
| `data/preferences/ThemePreferenceSnapshot.kt` | 删除 |
| `ui/features/settings/screens/ThemeSettingsScreen.kt` | 删除 |
| `ui/features/settings/sections/ThemeSettingsCoreSections.kt` | 删除 |
| `ui/features/settings/sections/ThemeSettingsColorSection.kt` | 删除 |

**修改**：
- `Theme.kt`：移除Glass效果支持，重构为双主题系统（明快/温暖）
- `UserPreferencesManager.kt`：移除所有主题自定义字段，仅保留 `themeMode`（明快/温暖/暗色模式）
- `Compose DSL渲染器`：移除LiquidGlass/WaterGlass渲染器组件
- `NavigationDrawerAppearance.kt`：简化为shadcn/ui Sidebar风格

### 2.8 公告系统移除

**移除范围**：

| 文件 | 说明 |
|------|------|
| `data/announcement/RemoteAnnouncementRepository.kt` | 删除 |
| `data/preferences/RemoteAnnouncementPreferences.kt` | 删除 |

**修改的文件**：
- `OperitApplication.kt`：移除公告检查逻辑
- `OperitScreens.kt`：移除公告弹窗组件

### 2.9 P0阶段数据库迁移

需要新增 Room 数据库迁移（`AppDatabase.kt`）：
- 移除 `display_mode` 中 Bubble 相关的值

---

## 三、P1 - 架构调整

### 3.1 权限系统重构

**当前权限级别**：BASIC → ADB → ROOT → ACCESSIBILITY

**简化后**：BASIC（基本）→ ACCESSIBILITY（高级）

**移除的文件**：

| 文件 | 说明 |
|------|------|
| `RootAuthorizer.kt` | 删除 |
| `ShizukuAuthorizer.kt` | 删除 |
| `app/src/main/assets/shizuku.apk` | 删除 |

**修改的文件**：

| 文件 | 修改内容 |
|------|---------|
| `AndroidPermissionPreferences.kt` | 移除 ROOT、ADB、SHIZUKU 级别，仅保留 BASIC 和 ACCESSIBILITY，重命名为"基本"和"高级" |
| `ToolPermissionSystem.kt` | 简化为两级权限检查 |
| `AccessibilityShellExecutor.kt` | 保留，高级权限下仍需要Shell执行 |

**无障碍服务内置化**：

当前架构：主程序通过 `AccessibilityProviderInstaller.kt` 安装 `accessibility.apk`（辅助APK），辅助APK运行无障碍服务，主程序通过 `IAccessibilityProvider.aidl` 和 `IAccessibilityEventCallback.aidl` 与辅助APK通信。

目标架构：将无障碍服务直接内置到主程序中，不再安装辅助APK。

**实现步骤**：
1. 在主程序的 `AndroidManifest.xml` 中声明 `AccessibilityService`
2. 将 `accessibility_service_config.xml` 移入主程序资源
3. 将辅助APK中的 `AccessibilityService` 实现类移入主程序
4. 移除 `AccessibilityProviderInstaller.kt` 中的APK安装逻辑
5. 移除 `IAccessibilityProvider.aidl` 和 `IAccessibilityEventCallback.aidl`（不再需要跨进程通信）
6. 移除 `app/src/main/assets/accessibility.apk`
7. `AccessibilityActionListener.kt`：从AIDL回调改为直接方法调用
8. `AccessibilityUITools.kt`：从AIDL代理调用改为直接调用主程序内的服务

**保留的文件**：
- `app/src/main/assets/desktop.apk`：保留，桌面模式独立运行需要

### 3.2 AutoGLM GUI操作能力内置化

AutoGLM当前通过沙箱包（`automatic_ui_base.js` + `automatic_ui_subagent.js`）实现，依赖 `PhoneAgent.kt` 和 `StandardUITools.kt` 提供的原生API。

**内置化方案**：

将AutoGLM从"默认启用的内置沙箱包"提升为"应用内置工具"，用户无需安装/管理沙箱包即可使用。

1. **修改 `ToolRegistration.kt`**：
   - 新增AutoGLM内置工具注册，直接调用 `StandardUITools.kt` 中的方法
   - 保留原有UI操作工具注册（AutoGLM沙箱包仍可使用）

2. **修改 `SystemToolPrompts.kt`**：
   - 新增AutoGLM内置工具提示词
   - 保留原有UI操作工具提示词（沙箱包兼容）

3. **修改 `SystemPromptConfig.kt`**：
   - 新增AutoGLM GUI操作指南到系统提示词

4. **沙箱包处理**：
   - `automatic_ui_base.js`：保留，第三方沙箱包仍可通过JS接口调用AutoGLM能力
   - `automatic_ui_subagent.js`：保留，AutoGLM子代理功能仍通过沙箱包提供
   - 新增内置工具与沙箱包工具的优先级：内置工具优先，沙箱包工具作为扩展

5. **UI入口**：
   - `AutoGlmToolScreen.kt`：保留，从工具箱页面移到聊天界面斜杠命令
   - `AutoGlmViewModel.kt`：保留，调整为同时支持内置工具和沙箱包

### 3.3 长程任务执行能力增强

**当前问题**：
- 后台任务依赖前台服务保活，但无持久化
- 网络中断或应用被杀后任务丢失
- 工具调用无超时重试机制
- 进度反馈仅通过通知

**增强方案**：

#### 3.3.1 任务持久化

新建 `core/task/` 目录：
- `TaskRecord.kt` - 任务记录数据模型（ObjectBox实体）
  - 字段：taskId, chatId, status(PENDING/RUNNING/PAUSED/COMPLETED/FAILED), createdAt, updatedAt, checkpoint(Json), retryCount, lastError
- `TaskRepository.kt` - 任务记录仓库
- `TaskCheckpointManager.kt` - 任务检查点管理（序列化工具调用链状态）

#### 3.3.2 断点恢复

修改 `ChatServiceCore.kt`：
- 每次工具调用完成后保存检查点
- 应用重启后扫描未完成任务，提示用户是否恢复
- 恢复时从最后一个检查点继续执行

#### 3.3.3 超时与重试

修改 `AIToolHandler.kt`：
- 新增工具执行超时配置（默认120秒）
- 新增自动重试机制（最多3次，指数退避）
- 新增网络中断检测（ConnectivityMonitor），中断时暂停任务

#### 3.3.4 进度反馈增强

修改 `AIForegroundService.kt`：
- 前台通知显示当前任务进度（步骤 x/n）
- 支持通过通知暂停/取消任务
- 悬浮窗黑色气泡显示进度百分比

修改 `ToolProgressBus.kt`：
- 新增任务级别的进度事件
- 支持嵌套任务进度

#### 3.3.5 后台保活增强

- `enableBackgroundKeepAlive` 默认值改为 `true`（已确认）
- 新增 WakeLock 管理：任务运行时获取 WakeLock，完成后释放
- 新增电池优化白名单引导

### 3.4 模型配置简化

**修改 `ModelConfigManager.kt`**：
- 保留字段：apiUrl, apiKey, modelName, temperature
- 移除UI暴露：topP, maxTokens, contextLength, maxContextLength, summaryEnabled, summaryThreshold, customHeaders
- 保留内部逻辑：以上字段由系统自动计算最优值

**修改 `ModelConfigData.kt`**：
- 保留所有字段定义（向后兼容）
- 新增 `companion object` 中的智能默认值计算方法

---

## 四、P2 - UI重构

### 4.1 主题系统重构（双主题）

提供两种主题：**明快** 和 **温暖**。两个主题除颜色不同外，所有完全一致。

**明快主题**：
- 仅使用黑白灰配色
- 其他颜色仅用于强调（如链接、错误、成功状态）

**温暖主题**：
- 偏暖色调：暖黄色、棕色
- 背景使用暖白色/暖灰色

**暗色模式**：两个主题都支持暗色模式变体

**新建/重写 `ui/theme/`**：

```
ui/theme/
├── Theme.kt              # 重写：双主题 + 暗色模式
├── Color.kt              # 重写：明快/温暖两套语义化色彩变量
├── Typography.kt         # 重写：4种字号+2种字重
├── Spacing.kt            # 新建：8pt网格间距系统
├── Components.kt         # 新建：统一组件样式
└── Shape.kt              # 保留但简化
```

**色彩系统**（参考shadcn/ui，每个主题一套）：
```
background / foreground
primary / primary-foreground
secondary / secondary-foreground
accent / accent-foreground
muted / muted-foreground
destructive / destructive-foreground
border / input / ring
```

**排版系统**：
- 大标题：24sp / SemiBold
- 副标题：18sp / SemiBold
- 正文：14sp / Regular
- 小字：12sp / Regular

### 4.2 侧边栏重构

**新建组件**：

```
ui/main/components/sidebar/
├── AppSidebar.kt           # 主侧边栏
├── SidebarHeader.kt        # 新建对话按钮
├── SidebarContent.kt       # 可滚动区域
├── SidebarGroup.kt         # 分组组件
├── SidebarFooter.kt        # 底部按钮
└── SidebarRail.kt          # 宽度调节手柄
```

**侧边栏结构**：
```
AppSidebar
├── SidebarHeader → 新建对话按钮
├── SidebarContent
│   ├── SidebarGroup → 预留按钮区
│   │   ├── 工作流
│   │   ├── [预留] 日历
│   │   └── [预留] 待办
│   └── SidebarGroup → 历史对话列表
├── SidebarFooter → 设置按钮
└── SidebarRail → 宽度调节
```

**移除**：
- `DrawerContent.kt`：重写为 `AppSidebar.kt`
- 快捷操作区（包管理、权限、工作流三个卡片）
- AI功能分组（AI聊天、记忆库、助手配置）
- 底部快捷（关于、帮助、设置三个按钮）

### 4.3 聊天界面重构

**保留的组件**：
- `CursorStyleChatMessage.kt`：保留，调整为shadcn/ui风格
- `AgentChatInputSection.kt`：保留，移除样式配置入口和语音输入按钮
- `ChatArea.kt`：保留，简化工具调用展示
- `ChatHistorySelector.kt`：移入侧边栏

**工具调用展示**：
- 一行小灰字显示工具名称
- 成功：灰色→绿色，3秒后→灰色
- 失败：灰色→红色，3秒后→灰色

**模型选择弱化**：
- 移除模型选择框
- 改为灰色小字显示当前模型名
- 点击模型名打开斜杠命令 `/model`

**Tune按钮移除**：
- 移除显式配置按钮
- 输入 `/` 时打开斜杠命令面板

**附件功能重构**：
- 自动注入区：通知、屏幕内容、位置、屏幕使用时间、记忆
- 手动选择区：截图、拍照、文件、工具包
- 移除角色卡附件选择UI

### 4.4 斜杠命令系统

**新建 `ui/features/chat/components/SlashCommandPanel.kt`**：

参考 shadcn/ui Command 组件风格，输入 `/` 时弹出命令面板：

| 命令 | 功能 | 实现 |
|------|------|------|
| /think | 思考模式开关+质量级别 | 修改 SystemPromptConfig |
| /model | 模型选择 | 弹出模型选择Sheet |
| /memory | 记忆选择+自动更新开关 | 弹出记忆配置Sheet |
| /tools | 工具开关+工具提示词管理 | 弹出工具配置Sheet |
| /permission | 权限级别切换 | 基本/高级切换 |
| /context | 上下文长度配置 | 弹出配置Sheet |
| /stream | 流式输出开关 | 切换开关 |

### 4.5 设置页面重构

**保留4个子页面**：

1. **模型+API配置**：仅4项（API URL、API Key、模型名称、温度）
2. **显示设置**：仅4项（明暗模式、主题选择（明快/温暖）、用户名、权限级别）
3. **工具箱**：MCP/Skill/包管理合并页面
4. **备份+关于**：一键备份/恢复 + 应用信息

**移除的页面**：
- 主题设置页面（合并到显示设置的主题选择）
- 终端设置页面
- 语音设置页面
- 助手配置页面

### 4.6 终端UI简化

**首次使用时后台静默安装的依赖**：
- Node.js
- PNPM
- Python环境
- Python链接
- Python虚拟环境
- Pip
- uv

**修改终端界面**：
- 仅保留：命令输入、命令执行、结果展示
- 移除：历史记录、命令建议、预设命令、清除历史、重新执行、展开/收起
- 完全移除配置项UI

### 4.7 备份恢复简化

**修改 `RoomDatabaseBackupManager.kt`**：
- 简化为一键备份/一键恢复
- 移除格式选择、导入策略选择
- 移除角色卡/记忆/模型配置的单独导出/导入

### 4.8 MCP/Skill/包管理合并

**新建统一管理页面**：
```
ui/features/toolbox/screens/
├── AddonManagerScreen.kt    # 统一管理页面
├── McpSection.kt            # MCP服务器区域
├── SkillSection.kt          # Skill插件区域
├── ToolPkgSection.kt        # 工具包区域
└── MarketSection.kt         # 统一市场入口
```

- 不保留3个Tab，改为小标题展示
- 市场功能合并（MCP市场、Skill市场、提示词市场统一入口）
- 完全兼容现有版本的包

### 4.9 移除的UI组件的沙箱包API处理

移除Bubble样式、Classic输入、液态玻璃等UI组件后，沙箱包可能通过 `ToolPkgCommonBridgePlugin.kt` 中的 `XmlRenderPlugin` 和 `InputMenuTogglePlugin` 引用这些组件。

**处理方式**：
- `ToolPkgComposeDslGeneratedRenderers.kt`：移除LiquidGlass/WaterGlass渲染器组件，保留其他渲染器
- `ToolPkgComposeDslWebView.kt`：保留，增加安全限制
- `XmlRenderPluginRegistry.kt`：保留，移除深度搜索相关的XML渲染插件
- `ToolPkgCommonBridgePlugin.kt` 中的 `InputMenuTogglePlugin`：保留方法签名，沙箱包注册的液态玻璃相关toggle不再生效

---

## 五、P3 - 新功能

### 5.1 待办功能

**数据模型**（新建 `data/model/TodoItem.kt`）：
```kotlin
@Entity
data class TodoItem(
    @Id var id: Long = 0,
    var title: String,
    var description: String = "",
    var status: TodoStatus = TodoStatus.PENDING,
    var priority: TodoPriority = TodoPriority.MEDIUM,
    var dueDate: Date? = null,
    var reminderTime: Date? = null,
    var repeatRule: TodoRepeatRule = TodoRepeatRule.NONE,
    var tags: List<String> = emptyList(),
    var relatedMemoryIds: List<Long> = emptyList(),
    var relatedChatId: Long? = null,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var completedAt: Date? = null
)

enum class TodoStatus { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }
enum class TodoPriority { LOW, MEDIUM, HIGH, URGENT }
enum class TodoRepeatRule { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }
```

**仓库层**（新建 `data/repository/TodoRepository.kt`）：
- ObjectBox持久化
- CRUD操作
- 按状态/优先级/到期日查询
- 与记忆库关联

**AI集成**：
- AI可创建/更新/完成待办事项（通过工具调用）
- 待办创建工具注册到 `ToolRegistration.kt`
- 工具提示词添加到 `SystemToolPrompts.kt`
- AI可基于对话内容自动建议待办

**UI**：
- 侧边栏预留按钮区入口
- 待办列表页面（按优先级分组）
- 待办详情Sheet
- 新建待办Sheet

### 5.2 日程功能

**数据模型**（新建 `data/model/CalendarEvent.kt`）：
```kotlin
@Entity
data class CalendarEvent(
    @Id var id: Long = 0,
    var title: String,
    var description: String = "",
    var startTime: Date,
    var endTime: Date? = null,
    var isAllDay: Boolean = false,
    var location: String = "",
    var reminderMinutes: Int = 15,
    var repeatRule: EventRepeatRule = EventRepeatRule.NONE,
    var relatedMemoryIds: List<Long> = emptyList(),
    var relatedChatId: Long? = null,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date()
)

enum class EventRepeatRule { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }
```

**仓库层**（新建 `data/repository/CalendarRepository.kt`）：
- ObjectBox持久化
- 按日/周/月查询
- 与记忆库关联

**AI集成**：
- AI可创建/更新/删除日程（通过工具调用）
- 日程创建工具注册到 `ToolRegistration.kt`
- 工具提示词添加到 `SystemToolPrompts.kt`
- AI可基于对话内容自动建议日程

**UI**：
- 侧边栏预留按钮区入口
- 日历视图页面（日/周/月视图）
- 日程详情Sheet
- 新建日程Sheet

### 5.3 上下文自动注入

**新建 `core/context/` 目录**：

```
core/context/
├── ContextInjectionManager.kt     # 自动注入管理器
├── NotificationInjector.kt        # 通知注入器
├── ScreenContentInjector.kt       # 屏幕内容注入器
├── LocationInjector.kt            # 位置注入器
├── ScreenTimeInjector.kt          # 屏幕使用时间注入器
├── MemoryAutoInjector.kt          # 记忆自动注入器
└── ContextInjectionConfig.kt      # 注入配置
```

**实现方案**：

1. **通知注入**：
   - 依赖 `OperitNotificationListenerService`
   - 格式：纯文本（应用名 + 通知标题 + 通知内容）
   - 缓存：保留最近20条通知，每次发送消息时取最新5条
   - 隐私：过滤银行、社交敏感应用（可配置）

2. **屏幕内容注入**：
   - 依赖无障碍服务获取 AccessibilityNodeInfo 树
   - 提取当前Activity的可见文字内容
   - 缓存：30秒内复用

3. **位置注入**：
   - 优先GPS，降级网络定位
   - 格式：城市名 + 区域名
   - 缓存：5分钟内复用

4. **屏幕使用时间注入**：
   - 使用 UsageStatsManager API
   - 格式：今日Top5应用使用时长
   - 缓存：5分钟内复用

5. **记忆自动注入**：
   - 复用现有 `MemorySearchConfig` 评分体系
   - 综合评分：关键词(10.0) + 向量(5.0) + 标签(3.0) + 图谱边(0.4) + 重要性(2.0) + 可信度(1.0) + 时间衰减
   - 最大注入5条记忆
   - 触发时机：每次用户发送消息时

6. **Token消耗控制**：
   - 自动注入总计不超过500 Token
   - 动态调整：根据剩余上下文空间计算
   - 优先级：记忆 > 通知 > 屏幕内容 > 位置 > 使用时间

7. **权限不足处理**：
   - 静默跳过，仅注入有权限的信息
   - 在斜杠命令 `/context` 中显示哪些注入项可用

8. **系统提示词修改**：
   - 在 `SystemPromptConfig.kt` 中新增自动注入上下文的说明段落
   - 指导AI如何利用注入的上下文信息

---

## 六、P4 - 收尾打磨

### 6.1 字符串资源清理

- 移除所有砍除功能相关的字符串（虚拟形象、语音唤醒、Bubble、Classic、液态玻璃、公告、深度搜索）
- 清理顺序：中文 → 英文 → 其他语言
- 使用工具自动检测未使用的字符串资源

### 6.2 C++模块审查

| 模块 | 决策 |
|------|------|
| `streamnative/` | 保留，AI响应解析核心依赖 |
| `mnn/` | 保留，本地模型推理能力 |
| `llama/` | 保留但标记为实验性（仅JNI stub） |
| `quickjs/` | 保留，沙箱包JS执行依赖 |
| `dragonbones/` | 删除（P0） |
| `fbx/` | 删除（P0） |
| `mmd/` | 删除（P0） |

### 6.3 辅助系统决策

| 模块 | 决策 |
|------|------|
| GitHub认证 | 保留，市场发布功能需要 |
| 市场统计API | 保留，市场功能需要 |
| 计费模式 | 保留，Token统计需要 |
| 免费额度管理 | 保留但简化 |
| 技能可见性 | 保留，通过 `/tools` 命令管理 |
| 环境变量 | 保留，终端和脚本执行需要 |
| 用户协议 | 保留，法律合规 |
| 聊天格式导入 | 保留ChatGPT和Markdown，移除ChatBox和GenericJson |
| 聊天格式导出 | 保留Markdown和纯文本，移除HTML |
| 模型定价数据 | **完整保留**，所有模型定价数据不可删减 |

### 6.4 Compose DSL渲染系统

| 组件 | 决策 |
|------|------|
| ToolPkgComposeDslGeneratedRenderers | 保留但移除LiquidGlass渲染器 |
| ToolPkgComposeDslWebView | 保留但增加安全限制 |
| XmlRenderPluginRegistry | 保留 |
| ToolPkgComposeDslDebugDumpReceiver | 保留，仅Debug构建 |

### 6.5 性能优化

- 审查 `ChatViewModel.kt` 的状态管理，减少不必要的重组
- 优化 `ChatArea.kt` 的消息列表渲染性能
- 审查 `ToolProgressBus.kt` 的事件频率

---

## 七、文件影响总览

### 7.1 删除的文件/目录

```
core/avatar/                          # 整个目录
core/tools/agent/VirtualDisplayManager.kt
core/tools/agent/ShowerBinderRegistry.kt (app层)
core/tools/agent/ShowerController.kt
core/tools/climode/CliToolModeSupport.kt
core/tools/system/RootAuthorizer.kt
core/tools/system/ShizukuAuthorizer.kt
data/announcement/                    # 整个目录
data/preferences/WaifuPreferences.kt
data/preferences/WakeWordPreferences.kt
data/preferences/RemoteAnnouncementPreferences.kt
data/model/DragonBones.kt
data/model/CustomEmoji.kt
data/model/CharacterGroupCard.kt
data/model/SerializableColorScheme.kt
data/model/SerializableTypography.kt
data/preferences/ThemePreferenceSnapshot.kt
data/repository/AvatarRepository.kt
services/assistant/OperitVoiceInteractionService.kt
services/assistant/OperitVoiceInteractionSessionService.kt
ui/features/assistant/                # 整个目录
ui/features/chat/components/style/bubble/  # 整个目录
ui/features/chat/components/style/input/classic/  # 整个目录
ui/floating/ui/pet/                   # 整个目录
ui/floating/voice/SpeechInteractionManager.kt
ui/components/ManagedDragonBonesView.kt
ui/theme/LiquidGlass.kt
ui/theme/WaterGlass.kt
ui/features/settings/screens/ThemeSettingsScreen.kt
ui/features/settings/sections/ThemeSettingsCoreSections.kt
ui/features/settings/sections/ThemeSettingsColorSection.kt
ui/features/settings/screens/SpeechServicesSettingsScreen.kt
ui/features/toolbox/screens/speechtotext/  # 整个目录
ui/features/toolbox/screens/texttospeech/  # 整个目录
widget/VoiceAssistantGlanceWidget.kt
showerclient/                         # 整个Gradle模块
examples/deepsearching/               # 整个目录
app/src/main/cpp/dragonbones/         # 整个C++模块
app/src/main/cpp/fbx/                 # 整个C++模块
app/src/main/cpp/mmd/                 # 整个C++模块
app/src/main/assets/emoji/            # 表情资源
app/src/main/assets/pets/             # 桌宠资源
app/src/main/assets/dragonbones/      # DragonBones资源
app/src/main/assets/shower-server.jar
app/src/main/assets/shizuku.apk
app/src/main/assets/accessibility.apk  # 内置化后不再需要
app/src/main/assets/packages/operit_editor.js
```

### 7.2 新建的文件/目录

```
core/task/                            # 长程任务增强
├── TaskRecord.kt
├── TaskRepository.kt
└── TaskCheckpointManager.kt

core/context/                         # 上下文自动注入
├── ContextInjectionManager.kt
├── NotificationInjector.kt
├── ScreenContentInjector.kt
├── LocationInjector.kt
├── ScreenTimeInjector.kt
├── MemoryAutoInjector.kt
└── ContextInjectionConfig.kt

data/model/TodoItem.kt                # 待办功能
data/model/CalendarEvent.kt           # 日程功能
data/repository/TodoRepository.kt
data/repository/CalendarRepository.kt

ui/main/components/sidebar/           # 侧边栏重构
├── AppSidebar.kt
├── SidebarHeader.kt
├── SidebarContent.kt
├── SidebarGroup.kt
├── SidebarFooter.kt
└── SidebarRail.kt

ui/features/chat/components/SlashCommandPanel.kt
ui/features/toolbox/screens/AddonManagerScreen.kt
ui/features/todo/                     # 待办UI
ui/features/calendar/                 # 日程UI

ui/theme/Color.kt                     # 主题重构（明快/温暖双主题）
ui/theme/Typography.kt
ui/theme/Spacing.kt
ui/theme/Components.kt
```

### 7.3 重大修改的文件

| 文件 | 修改内容 |
|------|---------|
| `OperitApplication.kt` | 移除虚拟形象/语音唤醒/Shower/公告初始化，新增任务/上下文注入初始化 |
| `AIForegroundService.kt` | 移除桌宠/语音唤醒/Shower逻辑，增强任务进度通知 |
| `ChatServiceCore.kt` | 增加任务持久化和断点恢复 |
| `ChatViewModel.kt` | 移除语音朗读按钮状态，增加上下文注入 |
| `AIMessageManager.kt` | 移除语音朗读逻辑，增加自动注入上下文拼接 |
| `ToolRegistration.kt` | 移除CliToolMode工具注册，新增AutoGLM/待办/日程工具 |
| `SystemToolPrompts.kt` | 移除CliToolMode提示词，新增AutoGLM/待办/日程提示词 |
| `SystemPromptConfig.kt` | 增加AutoGLM指南和上下文注入说明 |
| `EnhancedAIService.kt` | 移除FLOATING槽位，简化为单槽位 |
| `ChatRuntimeHolder.kt` | 移除FLOATING支持 |
| `AIToolHandler.kt` | 增加超时重试机制 |
| `ToolProgressBus.kt` | 增加任务级别进度 |
| `Theme.kt` | 重构为明快/温暖双主题 |
| `OperitScreens.kt` | 侧边栏重构、移除助手配置/语音设置/公告页面、新增待办/日程页面 |
| `AIChatScreen.kt` | 移除朗读按钮、Tune按钮、角色卡选择器，增加斜杠命令、工具调用状态展示 |
| `ModelConfigManager.kt` | 简化配置项暴露 |
| `AndroidPermissionPreferences.kt` | 简化为两级权限 |
| `AccessibilityProviderInstaller.kt` | 改为内置无障碍服务，移除APK安装逻辑 |
| `AppDatabase.kt` | 新增迁移脚本 |
| `settings.gradle.kts` | 移除 `:showerclient` |
| `CMakeLists.txt` | 移除dragonbones/fbx/mmd |
| `AndroidManifest.xml` | 新增主程序AccessibilityService声明，移除语音唤醒Service声明 |
| `ToolPkgCommonBridgePlugin.kt` | 虚拟形象相关API改为返回空值/默认值 |

---

## 八、风险与注意事项

1. **沙箱包兼容性**：虚拟形象API保留方法签名返回空值、角色卡系统后端完整保留、语音API保留调用能力，确保第三方沙箱包不崩溃。

2. **数据库迁移**：每次数据模型变更都需要Room迁移脚本，需确保升级路径完整。

3. **无障碍服务内置化**：从辅助APK迁移到主程序时，需确保无障碍服务声明正确、权限配置完整、事件回调机制从AIDL改为直接调用。

4. **长程任务持久化**：检查点序列化格式需要向前兼容，避免版本升级后无法恢复旧任务。

5. **上下文自动注入的Token消耗**：需要严格的Token预算控制，避免自动注入挤占用户对话空间。

6. **C++模块编译**：移除dragonbones/fbx/mmd后需确保CMakeLists.txt正确，不影响streamnative/mnn/quickjs模块编译。

7. **模型定价数据**：必须完整保留，不可删减任何模型定价信息。
