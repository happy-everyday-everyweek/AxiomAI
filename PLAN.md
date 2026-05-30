# Operit AI 简化与增强实施方案

> 本方案基于对项目全部模块的深度研究，结合用户反馈修正，形成可执行的完整方案。

---

## 一、项目完整模块清单

### 1.1 Gradle模块

| 模块 | 路径 | 说明 | 简化决策 |
|------|------|------|---------|
| app | /workspace/app/ | 主应用模块 | 保留 |
| mnn | /workspace/mnn/ | MNN本地模型推理 | 保留 |
| llama | /workspace/llama/ | Llama本地模型推理(JNI stub) | 保留 |
| quickjs | /workspace/quickjs/ | QuickJS JavaScript引擎 | 保留 |
| showerclient | /workspace/showerclient/ | Shower客户端通信 | 删除 |
| dragonbones | /workspace/dragonbones/ | DragonBones动画 | 删除 |
| fbx | /workspace/fbx/ | FBX动画 | 删除 |
| mmd | /workspace/mmd/ | MMD动画 | 删除 |
| tools/desktop | /workspace/tools/desktop/ | 桌面模式工具 | 保留 |
| tools/shower | /workspace/tools/shower/ | Shower服务端 | 删除 |

### 1.2 应用内功能模块（app/src/main/java/com/ai/assistance/operit/）

| 模块 | 路径 | 说明 | 简化决策 |
|------|------|------|---------|
| api/chat | api/chat/ | 聊天核心（EnhancedAIService, ChatRuntimeHolder, AIForegroundService） | 保留，简化FLOATING槽位 |
| api/chat/enhance | api/chat/enhance/ | 对话增强（ConversationMarkupManager, InputProcessor, ToolExecutionManager） | 保留 |
| api/chat/library | api/chat/library/ | 记忆库（MemoryLibrary, MemoryAutoSaveScheduler） | 保留 |
| api/chat/llmprovider | api/chat/llmprovider/ | LLM提供商（30+个Provider） | 保留 |
| api/speech | api/speech/ | 语音识别（SpeechService, SherpaSpeechProvider, DeepgramSttProvider） | 保留API，移除唤醒UI |
| api/voice | api/voice/ | 语音合成（VoiceService, 10+个VoiceProvider） | 保留API，移除独立UI |
| core/application | core/application/ | 应用核心（OperitApplication, ActivityLifecycleManager） | 保留 |
| core/avatar | core/avatar/ | 虚拟形象（AvatarController, AvatarModel, DragonBones/MMD/FBX/GLTF实现） | 删除 |
| core/chat | core/chat/ | 聊天逻辑（AIMessageManager, hooks, plugins） | 保留 |
| core/config | core/config/ | 系统配置（SystemPromptConfig, SystemToolPrompts, FunctionalPrompts） | 保留 |
| core/subpack | core/subpack/ | APK/EXE编辑工具 | 保留 |
| core/tools/agent | core/tools/agent/ | 代理系统（PhoneAgent, ShowerController, VirtualDisplayManager） | 部分删除 |
| core/tools/calculator | core/tools/calculator/ | 计算器 | 保留 |
| core/tools/climode | core/tools/climode/ | CLI工具模式（CliToolModeSupport） | 删除 |
| core/tools/condition | core/tools/condition/ | 条件评估器 | 保留 |
| core/tools/defaultTool | core/tools/defaultTool/ | 默认工具集（standard/accessibility/admin/debugger/root） | 部分删除 |
| core/tools/javascript | core/tools/javascript/ | JS引擎（JsEngine, JsToolManager, JsJavaBridge） | 保留 |
| core/tools/mcp | core/tools/mcp/ | MCP协议（MCPToolExecutor, MCPJson, MCPPackage） | 保留 |
| core/tools/packTool | core/tools/packTool/ | 沙箱包管理（PackageManager, ToolPkgParser, ToolPkgComposeDslParser） | 保留 |
| core/tools/skill | core/tools/skill/ | 技能管理（SkillManager） | 保留 |
| core/tools/system | core/tools/system/ | 系统工具（权限、Shell、终端、截图、无障碍安装） | 部分删除 |
| core/workflow | core/workflow/ | 工作流（WorkflowExecutor, WorkflowScheduler, WorkflowWorker） | 保留 |
| data/announcement | data/announcement/ | 远程公告 | 删除 |
| data/api | data/api/ | 外部API（GitHubApiService, MarketStatsApiService） | 保留 |
| data/backup | data/backup/ | 备份恢复（RoomDatabaseBackupManager, RawSnapshotBackupManager） | 保留，简化UI |
| data/collects | data/collects/ | 数据收集（模型定价、API配置） | 保留 |
| data/converter | data/converter/ | 聊天格式转换（ChatGPT, Markdown, ChatBox, GenericJson） | 部分删除 |
| data/dao | data/dao/ | 数据库DAO | 保留 |
| data/db | data/db/ | 数据库（AppDatabase, ObjectBox） | 保留 |
| data/exporter | data/exporter/ | 导出器（HTML, Markdown, Text） | 部分删除 |
| data/mcp | data/mcp/ | MCP数据层（MCPBridge, MCPLocalServer, MCPRepository） | 保留 |
| data/mnn | data/mnn/ | MNN模型下载管理 | 保留 |
| data/model | data/model/ | 数据模型（40+个模型类） | 部分删除 |
| data/preferences | data/preferences/ | 偏好管理（30+个Manager） | 部分删除 |
| data/repository | data/repository/ | 数据仓库 | 部分删除 |
| data/skill | data/skill/ | 技能数据层 | 保留 |
| data/updates | data/updates/ | 应用更新 | 保留 |
| integrations/externalchat | integrations/externalchat/ | 外部聊天集成 | 保留 |
| integrations/http | integrations/http/ | HTTP服务（ExternalChatHttpServer, WebChatHttpBridge） | 保留 |
| integrations/intent | integrations/intent/ | Intent集成 | 保留 |
| integrations/tasker | integrations/tasker/ | Tasker集成 | 保留 |
| plugins/chatview | plugins/chatview/ | 聊天视图钩子 | 保留 |
| plugins/lifecycle | plugins/lifecycle/ | 应用生命周期钩子 | 保留 |
| plugins/toolbox | plugins/toolbox/ | 工具箱插件 | 保留 |
| plugins/toolpkg | plugins/toolpkg/ | 沙箱包桥接（ToolPkgCommonBridgePlugin, ToolPkgToolLifecycleBridge） | 保留 |
| plugins/workflow | plugins/workflow/ | 工作流生命周期插件 | 保留 |
| provider | provider/ | ContentProvider（Memory, Workspace） | 保留 |
| services/assistant | services/assistant/ | 语音助手服务 | 删除 |
| services/core | services/core/ | 服务核心（ChatServiceCore, MessageProcessingDelegate） | 保留 |
| services/floating | services/floating/ | 悬浮窗管理 | 保留 |
| services/notification | services/notification/ | 通知监听服务 | 保留 |
| terminal | terminal/ | 终端管理（TerminalManager, SessionManager） | 保留，简化UI |
| ui/features/assistant | ui/features/assistant/ | 助手配置界面 | 删除 |
| ui/features/chat | ui/features/chat/ | 聊天界面 | 保留，重构 |
| ui/features/toolbox | ui/features/toolbox/ | 工具箱界面 | 保留，合并 |
| ui/features/workflow | ui/features/workflow/ | 工作流界面 | 保留 |
| ui/features/settings | ui/features/settings/ | 设置界面 | 保留，简化 |
| ui/floating | ui/floating/ | 悬浮窗UI | 保留 |
| widget | widget/ | 桌面小部件 | 部分删除 |

---

## 二、模块依赖关系图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        UI层 (Jetpack Compose)                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │
│  │ 聊天界面  │ │ 侧边栏   │ │ 设置页面  │ │ 工具箱   │ │ 工作流UI │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ │
└───────┼────────────┼────────────┼────────────┼────────────┼────────┘
        │            │            │            │            │
┌───────┼────────────┼────────────┼────────────┼────────────┼────────┐
│       │     ViewModel / Service层                              │
│  ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐│
│  │ChatVM    │ │Floating  │ │SettingsVM│ │AutoGlmVM │ │WorkflowVM││
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘│
└───────┼────────────┼────────────┼────────────┼────────────┼───────┘
        │            │            │            │            │
┌───────┼────────────┼────────────┼────────────┼────────────┼───────┐
│       │     核心服务层                                         │
│  ┌────▼────────────────────────────────────────────────────▼─────┐│
│  │              ChatServiceCore (聊天核心)                        ││
│  │    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐        ││
│  │    │AIMessageMgr  │ │ToolExecution │ │MsgProcessing │        ││
│  │    └──────┬───────┘ └──────┬───────┘ └──────┬───────┘        ││
│  └───────────┼────────────────┼────────────────┼────────────────┘│
└──────────────┼────────────────┼────────────────┼─────────────────┘
               │                │                │
┌──────────────┼────────────────┼────────────────┼─────────────────┐
│              │     工具执行层                                   │
│  ┌───────────▼────────────────▼────────────────▼──────────────┐  │
│  │              AIToolHandler (工具调度)                        │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │  │
│  │  │ToolReg   │ │JsToolMgr │ │MCPExec   │ │SkillMgr  │      │  │
│  │  │(内置工具)│ │(JS沙箱)  │ │(MCP工具) │ │(技能)    │      │  │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘      │  │
│  └───────┼────────────┼────────────┼────────────┼─────────────┘  │
└──────────┼────────────┼────────────┼────────────┼────────────────┘
           │            │            │            │
┌──────────┼────────────┼────────────┼────────────┼────────────────┐
│          │     原生能力层                                          │
│  ┌───────▼───────┐ ┌──▼───────────┐ ┌▼────────────┐ ┌▼────────┐│
│  │StandardTools  │ │JsEngine      │ │MCPBridge    │ │PhoneAgent││
│  │(UI/FS/Shell/  │ │(QuickJS)     │ │(MCP通信)    │ │(UI自动化)││
│  │ Http/Music/   │ │              │ │              │ │          ││
│  │ Browser/      │ │  ┌─────────┐ │ │              │ │          ││
│  │ Terminal/     │ │  │JavaBridge│ │ │              │ │          ││
│  │ Calculator/   │ │  │(78个API) │ │ │              │ │          ││
│  │ Speech/       │ │  └─────────┘ │ │              │ │          ││
│  │ Workflow)     │ │              │ │              │ │          ││
│  └───────┬───────┘ └──────────────┘ └──────────────┘ └────┬─────┘│
└──────────┼─────────────────────────────────────────────────┼──────┘
           │                                                  │
┌──────────┼──────────────────────────────────────────────────┼─────┐
│          │     系统服务层                                     │     │
│  ┌───────▼───────────────────────────────────────────────▼─────┐  │
│  │  权限系统 (STANDARD → ACCESSIBILITY)                        │  │
│  │  Shell执行器 (StandardShell → AccessibilityShell)           │  │
│  │  无障碍服务 (AccessibilityService, AccessibilityUITools)    │  │
│  │  屏幕捕获 (MediaProjectionCaptureManager)                   │  │
│  │  通知监听 (OperitNotificationListenerService)               │  │
│  └─────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────────────┐
│  沙箱包依赖关系                                                    │
│                                                                   │
│  automatic_ui_subagent ──→ Tools.UI.runSubAgent ──→ PhoneAgent    │
│  automatic_ui_base ────→ Tools.UI.tap/swipe/click ──→ StdUITools │
│  browser ──────────────→ Tools.Browser.* ──→ StdBrowserSession   │
│  所有JS包 ─────────────→ callTool/callToolAsync ──→ AIToolHandler│
│  所有JS包 ─────────────→ JavaBridge (78个API) ──→ JsEngine      │
│  所有JS包 ─────────────→ ToolPkgCommonBridgePlugin ──→ 桥接层   │
└───────────────────────────────────────────────────────────────────┘
```

---

## 三、沙箱包原生API完整清单

### 3.1 JsEngine @JavascriptInterface API（78个方法）

#### 工具调用类（3个）

| 方法 | 参数 | 功能 |
|------|------|------|
| `callTool` | toolType, toolName, paramsJson | 同步工具调用 |
| `callToolAsync` | callbackId, toolType, toolName, paramsJson | 异步工具调用 |
| `callToolAsyncStreaming` | callbackId, intermediateCallbackId, toolType, toolName, paramsJson | 流式异步工具调用 |

#### 包管理类（6个）

| 方法 | 参数 | 功能 |
|------|------|------|
| `isPackageImported` | packageName | 检查包是否已导入 |
| `importPackage` | packageName | 导入包 |
| `removePackage` | packageName | 移除包 |
| `usePackage` | packageName | 使用包 |
| `listImportedPackagesJson` | - | 列出已导入包 |
| `resolveToolName` | packageName, subpackageId, toolName, preferImported | 解析工具名 |

#### 资源读取类（3个）

| 方法 | 参数 | 功能 |
|------|------|------|
| `readToolPkgResource` | packageNameOrSubpackageId, resourceKey, outputFileName, internal | 读取包资源文件 |
| `readToolPkgTextResource` | packageNameOrSubpackageId, resourcePath | 读取包文本资源 |
| `getPluginConfigDir` | pluginId | 获取插件配置目录 |

#### 沙箱包注册钩子类（16个）

| 方法 | 功能 |
|------|------|
| `registerToolPkgToolboxUiModule` | 注册工具箱UI模块 |
| `registerToolPkgUiRoute` | 注册UI路由 |
| `registerToolPkgNavigationEntry` | 注册导航入口 |
| `registerToolPkgDesktopWidget` | 注册桌面小部件 |
| `registerToolPkgAppLifecycleHook` | 注册应用生命周期钩子 |
| `registerToolPkgMessageProcessingPlugin` | 注册消息处理插件 |
| `registerToolPkgXmlRenderPlugin` | 注册XML渲染插件 |
| `registerToolPkgInputMenuTogglePlugin` | 注册输入菜单切换插件 |
| `registerToolPkgChatInputHook` | 注册聊天输入钩子 |
| `registerToolPkgChatViewHook` | 注册聊天视图钩子 |
| `registerToolPkgToolLifecycleHook` | 注册工具生命周期钩子 |
| `registerToolPkgPromptInputHook` | 注册提示词输入钩子 |
| `registerToolPkgPromptHistoryHook` | 注册提示词历史钩子 |
| `registerToolPkgSystemPromptComposeHook` | 注册系统提示词组合钩子 |
| `registerToolPkgToolPromptComposeHook` | 注册工具提示词组合钩子 |
| `registerToolPkgSummaryGenerateHook` | 注册摘要生成钩子 |

#### Java桥接类（14个）

| 方法 | 功能 |
|------|------|
| `javaClassExists` | 检查Java类是否存在 |
| `javaLoadDex` | 加载DEX文件 |
| `javaLoadJar` | 加载JAR文件 |
| `javaListLoadedCodePaths` | 列出已加载代码路径 |
| `javaGetApplicationContext` | 获取应用上下文 |
| `javaGetCurrentActivity` | 获取当前Activity |
| `javaNewInstance` | 创建Java实例 |
| `javaCallStatic` | 调用静态方法 |
| `javaCallInstance` | 调用实例方法 |
| `javaCallStaticSuspend` | 异步调用静态方法 |
| `javaCallInstanceSuspend` | 异步调用实例方法 |
| `javaGetStaticField` | 获取静态字段 |
| `javaSetStaticField` | 设置静态字段 |
| `javaGetInstanceField` | 获取实例字段 |

#### 导航与UI类（6个）

| 方法 | 功能 |
|------|------|
| `navigateToRoute` | 导航到路由 |
| `listRoutes` | 列出所有路由 |
| `listHostRoutes` | 列出原生路由 |
| `composeWebViewControllerCommand` | WebView控制器命令 |
| `composeWebViewControllerCommandSuspend` | 异步WebView控制器命令 |
| `composeOpenFilePickerSuspend` | 打开文件选择器 |

#### 环境与通用类（14个）

| 方法 | 功能 |
|------|------|
| `decompress` | 解压缩数据 |
| `getEnvForCall` | 获取环境变量 |
| `setEnv` | 设置环境变量 |
| `setEnvs` | 批量设置环境变量 |
| `measureComposeText` | 测量Compose文本 |
| `invokeToolPkgIpcAsync` | 异步ToolPkg IPC调用 |
| `registerImageFromBase64` | 注册Base64图片 |
| `registerImageFromPath` | 注册路径图片 |
| `image_processing` | 图像处理 |
| `crypto` | 加密操作 |
| `sendCallIntermediateResult` | 发送中间结果 |
| `setCallResult` | 设置调用结果 |
| `setCallError` | 设置调用错误 |
| `reportError` / `reportErrorForCall` | 报告错误 |

#### 日志类（5个）

| 方法 | 功能 |
|------|------|
| `logInfo` | 记录Info日志 |
| `logInfoForCall` | 记录调用Info日志 |
| `logError` | 记录Error日志 |
| `logErrorForCall` | 记录调用Error日志 |
| `logDebug` | 记录Debug日志 |

#### Java对象管理类（4个）

| 方法 | 功能 |
|------|------|
| `__javaReleaseInstanceInternal` | 释放Java实例 |
| `javaHasInstanceMethod` | 检查实例方法是否存在 |
| `javaSetInstanceField` | 设置实例字段 |
| `javaPollPendingJsCallback` | 轮询待处理JS回调 |

### 3.2 callTool 路由的工具（通过ToolRegistration注册）

沙箱包通过 `callTool(toolType, toolName, paramsJson)` 调用原生工具。toolType对应工具分类，toolName对应具体工具。

**Standard级别工具**（basicTools + fileSystemTools + httpTools + memoryTools）：

| 分类 | 工具名 | 实现类 |
|------|--------|--------|
| UI操作 | tap, long_press, double_tap, swipe, click_element, set_text, press_key, get_page_info, capture_screenshot, run_ui_sub_agent | StandardUITools |
| 文件系统 | read_file, write_file, list_directory, create_directory, delete_file, move_file, copy_file | StandardFileSystemTools |
| HTTP | http_request | StandardHttpTools |
| 系统 | start_app, list_apps, get_device_info, send_intent, send_broadcast, modify_setting | StandardSystemOperationTools |
| Shell | execute_shell | StandardShellToolExecutor |
| 终端 | execute_terminal_command | StandardTerminalCommandExecutor |
| 音乐 | play_music, pause_music, resume_music, stop_music | StandardMusicPlaybackTools |
| 计算器 | calculate | StandardCalculator |
| 浏览器 | browser_navigate, browser_get_content, browser_click, browser_fill, browser_screenshot | StandardBrowserSessionTools |
| 网页访问 | visit_webpage | StandardWebVisitTool |
| 工作流 | list_workflows, run_workflow, create_workflow | StandardWorkflowTools |
| 聊天管理 | get_chat_history, create_chat, switch_chat | StandardChatManagerTool |
| 记忆 | query_memory, save_memory, update_memory, delete_memory | MemoryQueryToolExecutor |
| FFmpeg | ffmpeg_execute | StandardFFmpegTool |
| 设备信息 | get_device_info | StandardDeviceInfoToolExecutor |
| Intent | send_intent | StandardIntentToolExecutor |
| 广播 | send_broadcast | StandardSendBroadcastToolExecutor |
| 设置 | modify_setting | StandardSoftwareSettingsModifyTools |

**Accessibility级别工具**（accessibilityTools）：

| 工具名 | 实现类 |
|--------|--------|
| accessibility_tap, accessibility_swipe, accessibility_click_element, accessibility_set_text | AccessibilityUITools |
| accessibility_read_file, accessibility_write_file | AccessibilityFileSystemTools |
| accessibility_start_app, accessibility_list_apps, accessibility_execute_shell | AccessibilitySystemOperationTools |

**Admin级别工具**：AdminSystemOperationTools, AdminFileSystemTools, AdminUITools, AdminDeviceInfoToolExecutor

**Root级别工具**：RootSystemOperationTools

**Debugger级别工具**：DebuggerSystemOperationTools, DebuggerFileSystemTools, DebuggerUITools, DebuggerDeviceInfoToolExecutor

---

## 四、权限系统完整架构

### 4.1 当前权限级别

```
AndroidPermissionLevel 枚举定义：
├── STANDARD       - 基础权限，使用StandardShellExecutor
├── ACCESSIBILITY  - 无障碍权限，使用AccessibilityShellExecutor
├── ADMIN          - 设备管理员权限，使用AdminShellExecutor
├── ROOT           - Root权限，使用RootShellExecutor
├── DEBUGGER       - 调试器权限，使用DebuggerShellExecutor
└── SHIZUKU        - Shizuku权限（通过ShizukuAuthorizer）
```

### 4.2 Shell执行器继承体系

```
ShellExecutor (接口)
├── StandardShellExecutor      - 基础Shell执行
├── AccessibilityShellExecutor - 通过无障碍服务执行
├── AdminShellExecutor         - 通过设备管理员执行
├── RootShellExecutor          - 通过Root执行
└── DebuggerShellExecutor      - 通过调试器执行

ShellExecutorFactory - 根据AndroidPermissionLevel创建对应执行器
```

### 4.3 工具权限映射

```
STANDARD级别 → Standard*Tools (UI/FS/Shell/Http/Music/Browser/...)
ACCESSIBILITY级别 → Accessibility*Tools (UI/FS/System/...)
ADMIN级别 → Admin*Tools (UI/FS/System/DeviceInfo)
ROOT级别 → Root*Tools (System)
DEBUGGER级别 → Debugger*Tools (UI/FS/System/DeviceInfo)
```

### 4.4 简化后权限体系

```
简化前：STANDARD → ACCESSIBILITY → ADMIN → ROOT → SHIZUKU → DEBUGGER
简化后：BASIC(基本) → ADVANCED(高级)

BASIC = 原STANDARD级别的能力
ADVANCED = 原ACCESSIBILITY级别的能力

删除：ROOT、ADMIN、SHIZUKU、DEBUGGER 四个级别
删除：RootShellExecutor、AdminShellExecutor、DebuggerShellExecutor
删除：RootAuthorizer、ShizukuAuthorizer、ShizukuInstaller
删除：Root*Tools、Admin*Tools、Debugger*Tools
删除：shizuku.apk
```

---

## 五、GUI操作能力方案（修正）

### 5.1 三个GUI沙箱包

| 沙箱包ID | 文件 | 功能 | 决策 |
|---------|------|------|------|
| `Automatic_ui_subagent` | automatic_ui_subagent.js | AutoGLM子代理，执行复杂UI任务（run_subagent_main, run_subagent_virtual, run_subagent_parallel_virtual） | **保留，改为内置能力** |
| `browser` | browser.js | 浏览器操作（导航、获取内容、点击、填表、截图） | **保留，改为内置能力** |
| `Automatic_ui_base` | automatic_ui_base.js | 基础UI操作（tap, longPress, clickElement, setText, pressKey, swipe, startApp） | **移除** |

### 5.2 内置化方案

**Automatic_ui_subagent 内置化**：
- 将 `automatic_ui_subagent.js` 的逻辑迁移到Kotlin原生实现
- 新建 `core/tools/autoglm/AutoGLMSubAgentExecutor.kt`
- 调用 `PhoneAgent.kt` 的 `runSubAgent` 方法（保留PhoneAgent）
- 调用 `StandardUITools.kt` 的基础UI操作方法（保留StandardUITools）
- 在 `ToolRegistration.kt` 中注册为内置工具
- 在 `SystemToolPrompts.kt` 中新增内置工具提示词
- 保留JS包文件供第三方沙箱包通过JS接口调用

**browser 内置化**：
- 将 `browser.js` 的逻辑迁移到Kotlin原生实现
- 新建 `core/tools/browser/BrowserToolExecutor.kt`
- 调用 `StandardBrowserSessionTools.kt` 的浏览器操作方法
- 在 `ToolRegistration.kt` 中注册为内置工具
- 在 `SystemToolPrompts.kt` 中新增内置工具提示词
- 保留JS包文件供第三方沙箱包通过JS接口调用

**Automatic_ui_base 移除**：
- 删除 `app/src/main/assets/packages/automatic_ui_base.js`
- 删除 `examples/automatic_ui_base/` 源码目录
- `StandardUITools.kt` 保留（Automatic_ui_subagent和browser都依赖它）
- `PhoneAgent.kt` 保留（Automatic_ui_subagent依赖它）

### 5.3 保留的原生依赖

| 文件 | 保留原因 |
|------|---------|
| `PhoneAgent.kt` | AutoGLM子代理的runSubAgent方法 |
| `PhoneAgentJobRegistry.kt` | AutoGLM任务注册 |
| `StandardUITools.kt` | UI操作基础实现（tap, swipe, click, setText, pressKey, getPageInfo, captureScreenshot, runUiSubAgent） |
| `StandardBrowserSessionTools.kt` | 浏览器操作基础实现 |
| `BrowserToolSupport.kt` | 浏览器工具支持 |
| `AccessibilityUITools.kt` | 无障碍UI操作 |
| `AccessibilitySystemOperationTools.kt` | 无障碍系统操作 |

### 5.4 移除的文件

| 文件 | 说明 |
|------|------|
| `app/src/main/assets/packages/automatic_ui_base.js` | 基础UI操作沙箱包 |
| `examples/automatic_ui_base/` | 基础UI操作源码 |
| `core/tools/climode/CliToolModeSupport.kt` | CLI工具模式 |
| `core/tools/agent/VirtualDisplayManager.kt` | 虚拟显示管理 |
| `core/tools/agent/ShowerBinderRegistry.kt` (app层) | Shower绑定 |
| `core/tools/agent/ShowerController.kt` | Shower控制器 |
| `showerclient/` | Shower客户端Gradle模块 |
| `app/src/main/assets/shower-server.jar` | Shower服务端 |
| `app/src/main/assets/packages/operit_editor.js` | 编辑器沙箱包 |

---

## 六、P0 - 代码清理

### 6.1 虚拟形象系统移除

**移除范围**：

| 目录/文件 | 说明 |
|----------|------|
| `core/avatar/` | 整个目录删除 |
| `ui/features/assistant/` | 整个目录删除 |
| `ui/floating/ui/pet/` | 整个目录删除 |
| `ui/components/ManagedDragonBonesView.kt` | 删除 |
| `data/model/DragonBones.kt` | 删除 |
| `data/model/CustomEmoji.kt` | 删除 |
| `data/model/CharacterGroupCard.kt` | 删除 |
| `data/repository/AvatarRepository.kt` | 删除 |
| `data/repository/CustomEmojiRepository.kt` | 删除 |
| `data/preferences/WaifuPreferences.kt` | 删除 |
| `data/preferences/CustomEmojiPreferences.kt` | 删除 |
| `app/src/main/cpp/dragonbones/` | 整个C++模块删除 |
| `app/src/main/cpp/fbx/` | 整个C++模块删除 |
| `app/src/main/cpp/mmd/` | 整个C++模块删除 |
| `app/src/main/assets/emoji/` | 表情资源删除 |
| `app/src/main/assets/pets/` | 桌宠资源删除 |
| `app/src/main/assets/dragonbones/` | DragonBones资源删除 |

**沙箱包API处理**：
- `ToolPkgCommonBridgePlugin.kt` 中虚拟形象相关API：保留方法签名，方法体返回空值/默认值，标注 `@Deprecated`
- 沙箱包调用这些API时不会崩溃，但不产生实际效果

**引用清理**：
- `OperitApplication.kt`：移除虚拟形象初始化
- `AIForegroundService.kt`：移除桌宠逻辑
- `FloatingWindowManager`：移除PET模式
- `OperitScreens.kt`：移除助手配置页面路由
- `DrawerContent.kt`：移除助手配置入口
- `NavItem.kt`：移除助手导航项
- `CMakeLists.txt`：移除dragonbones/fbx/mmd编译配置

### 6.2 语音系统简化（保留API能力）

**移除的用户交互功能**：

| 文件 | 说明 |
|------|------|
| `services/assistant/OperitVoiceInteractionService.kt` | 语音唤醒服务 |
| `services/assistant/OperitVoiceInteractionSessionService.kt` | 语音会话服务 |
| `ui/floating/voice/SpeechInteractionManager.kt` | 语音交互管理 |
| `data/preferences/WakeWordPreferences.kt` | 唤醒词配置 |
| `widget/VoiceAssistantGlanceWidget.kt` | 语音助手小部件 |
| `ui/features/toolbox/screens/speechtotext/` | STT独立界面 |
| `ui/features/toolbox/screens/texttospeech/` | TTS独立界面 |
| `ui/features/settings/screens/SpeechServicesSettingsScreen.kt` | 语音设置界面 |
| `ui/features/assistant/components/VoiceAutoAttachComponents.kt` | 语音自动附加 |

**保留的API能力**：

| 文件 | 保留原因 |
|------|---------|
| `api/speech/SpeechService.kt` | 工作流和沙箱包通过API调用STT |
| `api/speech/SherpaSpeechProvider.kt` | 本地语音识别 |
| `api/speech/OpenAISttProvider.kt` | OpenAI语音识别 |
| `api/speech/DeepgramSttProvider.kt` | Deepgram语音识别 |
| `api/voice/VoiceService.kt` | AI生成音频、工作流语音节点 |
| `api/voice/` 所有VoiceProvider | TTS API调用能力 |
| `data/preferences/SpeechServicesPreferences.kt` | API配置 |
| `core/tools/defaultTool/standard/StandardSpeechTools.kt` | 语音工具注册 |
| `ui/common/markdown/MarkdownAudioRenderer.kt` | 音频渲染 |
| `ui/common/displays/WaveVisualizer.kt` | 波形可视化 |

**修改**：
- `AIForegroundService.kt`：移除语音唤醒监听，保留TTS播放
- `ChatViewModel.kt`：移除朗读按钮状态，保留TTS工具调用
- `AIChatScreen.kt`：移除朗读按钮
- `AgentChatInputSection.kt`：移除语音输入按钮
- `OperitApplication.kt`：移除语音唤醒初始化，保留SpeechService初始化

### 6.3 角色卡系统（UI不可见，后端保留）

**移除的UI入口**：
- `OperitScreens.kt`：移除角色卡管理页面路由
- `DrawerContent.kt`：移除角色卡选择入口
- `AIChatScreen.kt`：移除角色卡选择器面板
- `ChatScreenHeader.kt`：移除角色卡切换按钮

**保留的后端**：
- `CharacterCardManager.kt`、`CharacterCard.kt`、`PromptTag.kt`、`PromptTagManager.kt`、`ActivePromptManager.kt`、`PromptVersionManager.kt`、`CharacterCardToolAccessResolver.kt`、`CharacterGroupCardManager.kt`、`PersonaCardChatHistoryManager.kt` 全部保留
- `ChatViewModel.kt` 保留角色卡状态
- `ChatDao.kt` 保留characterId字段
- `WebChatHttpBridge.kt` 保留角色卡API端点
- `ToolPkgCommonBridgePlugin.kt` 保留角色卡桥接字段

### 6.4 聊天样式简化

- 删除 `ui/features/chat/components/style/bubble/` 整个目录
- 删除 `ui/features/chat/components/style/input/classic/` 整个目录
- `ChatMessageDisplayMode.kt` 仅保留 CURSOR
- `ChatScreenContent.kt` 硬编码Cursor样式

### 6.5 深度搜索移除（沙箱包层面）

- 删除 `examples/deepsearching/` 目录

### 6.6 液态玻璃主题移除

- 删除 `LiquidGlass.kt`、`WaterGlass.kt`、`SerializableColorScheme.kt`、`SerializableTypography.kt`、`ThemePreferenceSnapshot.kt`
- 删除 `ThemeSettingsScreen.kt`、`ThemeSettingsCoreSections.kt`、`ThemeSettingsColorSection.kt`

### 6.7 公告系统移除

- 删除 `data/announcement/RemoteAnnouncementRepository.kt`
- 删除 `data/preferences/RemoteAnnouncementPreferences.kt`

### 6.8 权限系统简化

**删除**：
- `RootAuthorizer.kt`、`ShizukuAuthorizer.kt`、`ShizukuInstaller.kt`
- `RootShellExecutor.kt`、`AdminShellExecutor.kt`、`DebuggerShellExecutor.kt`
- `core/tools/defaultTool/root/` 整个目录
- `core/tools/defaultTool/admin/` 整个目录
- `core/tools/defaultTool/debugger/` 整个目录
- `app/src/main/assets/shizuku.apk`

**修改**：
- `AndroidPermissionLevel.kt`：仅保留 STANDARD（重命名BASIC）和 ACCESSIBILITY（重命名ADVANCED）
- `AndroidPermissionPreferences.kt`：简化为两级
- `ShellExecutorFactory.kt`：仅创建Standard和Accessibility执行器
- `ToolPermissionSystem.kt`：简化为两级权限检查

### 6.9 无障碍服务内置化

- 将无障碍服务从辅助APK迁移到主程序
- 删除 `AccessibilityProviderInstaller.kt` 中的APK安装逻辑
- 删除 `app/src/main/assets/accessibility.apk`
- 删除 `IAccessibilityProvider.aidl`、`IAccessibilityEventCallback.aidl`
- 在 `AndroidManifest.xml` 中声明主程序的AccessibilityService
- `AccessibilityActionListener.kt` 从AIDL回调改为直接方法调用

---

## 七、P1 - 架构调整

### 7.1 AutoGLM和Browser内置化

（详见第五章GUI操作能力方案）

### 7.2 长程任务执行能力增强

新建 `core/task/` 目录：
- `TaskRecord.kt` - ObjectBox实体（taskId, chatId, status, checkpoint, retryCount, lastError）
- `TaskRepository.kt` - 任务仓库
- `TaskCheckpointManager.kt` - 检查点管理

修改 `ChatServiceCore.kt`：增加断点恢复
修改 `AIToolHandler.kt`：增加超时重试（120秒超时，3次重试，指数退避）
修改 `AIForegroundService.kt`：增强进度通知
修改 `ToolProgressBus.kt`：增加任务级别进度

### 7.3 模型配置简化

`ModelConfigManager.kt`：UI仅暴露4项（apiUrl, apiKey, modelName, temperature），其他参数系统自动计算

---

## 八、P2 - UI重构

### 8.1 主题系统（明快/温暖双主题）

**明快主题**：黑白灰配色，其他颜色仅用于强调
**温暖主题**：暖黄色、棕色等暖色调

两个主题除颜色外完全一致，都支持暗色模式变体。

### 8.2 侧边栏重构

```
AppSidebar
├── SidebarHeader → 新建对话按钮
├── SidebarContent
│   ├── SidebarGroup → 预留按钮区（工作流、[预留]日历、[预留]待办）
│   └── SidebarGroup → 历史对话列表
├── SidebarFooter → 设置按钮
└── SidebarRail → 宽度调节
```

### 8.3 聊天界面重构

- 工具调用展示：一行小灰字，成功灰→绿3秒→灰，失败灰→红3秒→灰
- 模型选择弱化：灰色小字显示模型名，点击打开 `/model`
- Tune按钮移除：输入 `/` 打开斜杠命令面板
- 附件重构：自动注入区（通知/屏幕/位置/使用时间/记忆）+ 手动选择区（截图/拍照/文件/工具包）

### 8.4 斜杠命令

| 命令 | 功能 |
|------|------|
| /think | 思考模式开关+质量级别 |
| /model | 模型选择 |
| /memory | 记忆选择+自动更新开关 |
| /tools | 工具开关+提示词管理 |
| /permission | 权限级别切换 |
| /context | 上下文长度配置 |
| /stream | 流式输出开关 |

### 8.5 设置页面（4个子页面）

1. 模型+API配置（4项）
2. 显示设置（4项：明暗模式、主题选择、用户名、权限级别）
3. 工具箱（MCP/Skill/包管理合并）
4. 备份+关于

### 8.6 终端UI简化

首次使用后台静默安装：Node.js、PNPM、Python环境、Python链接、Python虚拟环境、Pip、uv

### 8.7 备份恢复简化

一键备份/一键恢复

---

## 九、P3 - 新功能

### 9.1 待办功能

ObjectBox持久化，AI工具调用集成，侧边栏入口

### 9.2 日程功能

ObjectBox持久化，AI工具调用集成，侧边栏入口

### 9.3 上下文自动注入

通知/屏幕内容/位置/使用时间/记忆自动注入，Token上限500，优先级：记忆>通知>屏幕>位置>使用时间

---

## 十、P4 - 收尾打磨

- 字符串资源清理
- 模型定价数据完整保留
- 聊天格式导入保留ChatGPT+Markdown，导出保留Markdown+纯文本
- Compose DSL渲染器移除LiquidGlass组件
- 性能优化

---

## 十一、风险与注意事项

1. **沙箱包兼容性**：虚拟形象API保留签名返回空值、角色卡后端完整保留、语音API保留调用能力
2. **数据库迁移**：每次数据模型变更需Room迁移脚本
3. **无障碍服务内置化**：从辅助APK迁移到主程序，需确保服务声明和权限配置正确
4. **长程任务持久化**：检查点序列化格式需向前兼容
5. **上下文注入Token消耗**：严格预算控制
6. **C++模块编译**：移除dragonbones/fbx/mmd后确保CMakeLists.txt正确
7. **模型定价数据**：完整保留，不可删减
8. **权限系统简化**：移除ROOT/ADMIN/SHIZUKU/DEBUGGER后，需要这些权限的工具将不可用，需在工具提示词中说明
