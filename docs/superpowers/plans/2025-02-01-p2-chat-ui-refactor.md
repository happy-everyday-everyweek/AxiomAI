# P2-聊天界面重构 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 重构聊天界面，简化工具调用展示为一行小灰字并带状态颜色动画、弱化模型选择为灰色小字、移除Tune按钮改为斜杠命令面板、重构附件区域为自动注入+手动选择两区布局，并实现7个斜杠命令的Command面板。
**Architecture:** 工具调用展示通过修改 `CanvasToolSummaryRow`/`CanvasToolResultRow` 组件实现颜色状态动画；模型选择从弹出式选择器简化为灰色小字标签，点击触发 `/model` 命令；斜杠命令面板为新建 Composable，参考 shadcn/ui Command 组件风格，在输入框检测 `/` 前缀时弹出；附件区域重构为自动注入区（系统上下文）和手动选择区（用户主动附加）两部分。
**Tech Stack:** Kotlin, Jetpack Compose

---

## Task 1: 简化工具调用展示

- [ ] 修改工具调用行组件，实现"一行小灰字+状态颜色动画"效果

**Files:**
- `ui/features/chat/components/part/XmlCanvasSummaryComponents.kt`（`CanvasToolSummaryRow` 和 `CanvasToolResultRow` 组件）
- `ui/features/chat/components/part/ToolDisplayComponents.kt`（`CompactToolDisplay`、`DetailedToolDisplay` 组件）
- `ui/features/chat/components/part/ToolResultDisplay.kt`（`ToolResultDisplay` 组件）

**Steps:**
1. 在 `XmlCanvasSummaryComponents.kt` 中，修改 `CanvasToolSummaryRow` 组件：将 `titleColor` 参数默认值从 `MaterialTheme.colorScheme.primary` 改为灰色（`Color.Gray` 或 `MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)`），移除工具名称的强调色，统一为一行小灰字风格
2. 在 `XmlCanvasSummaryComponents.kt` 中，修改 `CanvasToolResultRow` 组件：添加 `resultState` 枚举参数（`PENDING`、`SUCCESS`、`FAILURE`），实现颜色动画逻辑——默认灰色，成功时灰色变绿色（`Color(0xFF4CAF50)`），3秒后变回灰色；失败时灰色变红色（`Color(0xFFF44336)`），3秒后变回灰色
3. 在 `XmlCanvasSummaryComponents.kt` 中，使用 `animateColorAsState` 实现颜色过渡动画，配合 `LaunchedEffect` + `delay(3000)` 实现3秒后自动回灰
4. 在 `ToolDisplayComponents.kt` 中，修改 `CompactToolDisplay` 调用 `CanvasToolSummaryRow` 时，将 `titleColor` 从 `MaterialTheme.colorScheme.primary` 改为灰色，与简化风格一致
5. 在 `ToolDisplayComponents.kt` 中，修改 `DetailedToolDisplay` 同样传递灰色 `titleColor`
6. 在 `ToolResultDisplay.kt` 中，修改 `ToolResultDisplay` 调用 `CanvasToolResultRow` 时，传入 `resultState` 参数：根据 `isSuccess` 传入 `SUCCESS` 或 `FAILURE`，触发颜色动画
7. 确保工具调用行只显示一行文字（工具名称+简短说明），移除多余的参数预览和图标装饰，保持极致简洁

---

## Task 2: 弱化模型选择为灰色小字

- [ ] 将模型选择器从弹出式面板简化为灰色小字标签，点击打开 `/model` 斜杠命令

**Files:**
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt`（`AgentModelSelectorPopup`、`AgentModelSelectorItem` 组件，约第1478-2100行）
- `ui/features/chat/components/style/input/classic/ClassicChatSettingsBar.kt`（`ModelSelectorItem` 组件，约第1500-1600行）

**Steps:**
1. 在 `AgentChatInputSection.kt` 中，移除 `AgentModelSelectorPopup` 弹出面板组件的调用（保留组件定义以备后续清理，但不再在输入区域触发它）
2. 在 `AgentChatInputSection.kt` 中，找到当前显示模型名的位置（`AgentModelSelectorItem` 内的 `currentModelName` 文本），将其样式改为灰色小字：`fontSize = 11.sp`，`color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)`，不使用框框住，不使用强调色
3. 在模型名灰色小字上添加 `clickable` 修饰符，点击时触发 `/model` 斜杠命令面板打开（与 Task 3 的斜杠命令面板集成）
4. 移除模型选择区域的 `Icons.Outlined.DataObject` 图标和 `Icons.Outlined.Info` 信息按钮，仅保留灰色小字模型名
5. 在 `ClassicChatSettingsBar.kt` 中，对 `ModelSelectorItem` 做同样的简化：将模型名显示改为灰色小字，移除图标装饰，点击触发 `/model` 命令
6. 确保模型名在输入区域中以非侵入方式显示，不占用过多视觉空间

---

## Task 3: 移除Tune按钮，实现斜杠命令面板

- [ ] 移除输入区域的Tune按钮，输入 `/` 时弹出斜杠命令面板，实现7个命令

**Files:**
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt`（Tune按钮位置、输入框文本监听）
- `ui/features/chat/components/style/input/classic/ClassicChatSettingsBar.kt`（Tune按钮位置）
- 新建: `ui/features/chat/components/slashcommand/SlashCommandPanel.kt`（斜杠命令面板组件）
- 新建: `ui/features/chat/components/slashcommand/SlashCommandDefinitions.kt`（命令定义与处理逻辑）
- `ui/features/chat/viewmodel/ChatViewModel.kt`（命令执行逻辑）
- `ui/features/chat/components/style/input/common/InputMenuTogglePluginRegistry.kt`（现有开关逻辑参考）

**Steps:**
1. 在 `AgentChatInputSection.kt` 中，定位Tune按钮（通常为设置/调优图标按钮，控制思考模式、模型选择等弹出面板），移除该按钮及其点击处理逻辑
2. 新建 `SlashCommandDefinitions.kt`，定义7个斜杠命令的数据结构：
   - `/think`：思考模式开关+质量级别，映射到 `enableThinkingMode`/`thinkingQualityLevel` 状态
   - `/model`：模型选择，映射到 `FunctionConfigMapping`/`ModelConfigSummary` 状态
   - `/memory`：记忆选择+自动更新开关，映射到 `enableMemoryAutoUpdate` 状态
   - `/tools`：工具开关+提示词管理，映射到 `enableTools`/`toolPromptVisibility` 状态
   - `/permission`：权限级别切换，映射到 `PermissionLevel` 状态
   - `/context`：上下文长度配置，映射到 `enableMaxContextMode`/上下文长度参数
   - `/stream`：流式输出开关，映射到 `disableStreamOutput` 状态
3. 新建 `SlashCommandPanel.kt`，实现参考 shadcn/ui Command 组件风格的斜杠命令面板：
   - 使用 `Popup` 或 `DropdownMenu` 实现，定位在输入框上方
   - 支持模糊搜索过滤命令列表
   - 每个命令项显示：命令名、简短描述、当前状态值
   - 选中命令后展开子选项（如 `/think` 展开质量级别选择，`/model` 展开模型列表）
   - 使用 `LazyColumn` 渲染命令列表，支持键盘导航
   - 面板样式：圆角卡片、半透明背景、紧凑间距
4. 在 `AgentChatInputSection.kt` 中，监听输入框文本变化：当 `userMessage.text` 以 `/` 开头时，显示 `SlashCommandPanel`；当用户选择命令后，将命令文本替换到输入框或直接执行命令
5. 在 `AgentChatInputSection.kt` 中，处理命令执行结果：对于开关类命令（`/think`、`/stream` 等），直接切换状态并显示 Toast 反馈；对于选择类命令（`/model`、`/permission` 等），在面板中展开子选项供用户选择
6. 在 `ClassicChatSettingsBar.kt` 中，同样移除Tune按钮，添加斜杠命令面板支持
7. 在 `ChatViewModel.kt` 中，添加 `executeSlashCommand(command: String, args: List<String>)` 方法，集中处理各命令的状态切换逻辑
8. 确保斜杠命令面板在浮动窗口模式（`FloatingChatWindow`）下也能正常工作

---

## Task 4: 附件区域重构

- [ ] 将附件区域重构为自动注入区+手动选择区两区布局

**Files:**
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt`（附件面板布局，约第900-1200行附件相关区域）
- `ui/features/chat/components/AttachmentPreview.kt`（附件预览组件）
- `ui/features/chat/components/AttachmentChip.kt`（附件标签组件）
- `ui/features/chat/attachments/AttachmentUtils.kt`（附件工具类）
- `ui/features/chat/viewmodel/ChatViewModel.kt`（附件状态管理）
- `data/model/AttachmentInfo.kt`（附件数据模型）

**Steps:**
1. 在 `AttachmentInfo.kt` 中，添加 `source` 字段区分附件来源：`AUTO_INJECTED`（自动注入）和 `MANUAL_SELECTED`（手动选择），枚举值为 `Notification`、`Screen`、`Location`、`UsageTime`、`Memory`、`Screenshot`、`Photo`、`File`、`ToolPackage`
2. 在 `AgentChatInputSection.kt` 中，重构附件面板布局为两个区域：
   - **自动注入区**：显示系统自动附加的上下文信息，包括通知（`onAttachNotifications`）、屏幕内容（`onAttachScreenContent`）、位置（`onAttachLocation`）、使用时间、记忆（`onAttachMemory`）。每个项目以小标签形式显示，带开关控制是否自动注入，标签样式为半透明背景+小字
   - **手动选择区**：显示用户主动附加的内容，包括截图、拍照（`onTakePhoto`）、文件（`onAttachmentRequest`）、工具包（`onAttachPackage`）。每个项目以图标按钮形式排列，点击后触发对应操作
3. 在 `AgentChatInputSection.kt` 中，将自动注入区和手动选择区用分隔线或标题区分，自动注入区在上，手动选择区在下
4. 自动注入区的每个项目显示为紧凑的开关行：左侧图标+标签名，右侧 `Switch` 控件，开启时自动在发送消息时注入对应上下文
5. 手动选择区的项目以图标网格排列：截图图标、相机图标、文件图标、工具包图标，每个图标下方显示小字标签
6. 在 `ChatViewModel.kt` 中，添加自动注入状态的管理逻辑：为每个自动注入类型维护一个 `StateFlow<Boolean>` 开关状态，在发送消息时根据开关状态自动附加对应内容
7. 更新 `AttachmentPreview.kt`，根据 `AttachmentInfo.source` 区分显示样式：自动注入的附件以更淡的颜色显示，手动选择的附件以正常颜色显示
8. 确保附件区域在面板收起时仅显示已附加项目的数量提示，展开时显示完整两区布局

---

## Task 5: 编译验证与清理

- [ ] 执行项目编译，确认无编译错误，清理残留引用

**Files:**
- 全项目

**Steps:**
1. 执行 `./gradlew compileDebugKotlin` 编译项目
2. 修复所有编译错误（未解析的引用、缺少参数、类型不匹配等）
3. 搜索残留的 `AgentModelSelectorPopup` 直接调用（应已替换为斜杠命令触发）
4. 搜索残留的Tune按钮引用（`Icons.Outlined.Tune` 或类似），确认已全部移除
5. 确认 `SlashCommandPanel` 在所有输入模式（Agent、Classic）下均可正常触发
6. 确认工具调用颜色动画在深色/浅色主题下均可正常显示
7. 确认附件区域两区布局在不同屏幕尺寸下均可正常显示
