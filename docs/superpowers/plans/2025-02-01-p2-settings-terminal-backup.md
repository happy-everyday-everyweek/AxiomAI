# P2-设置页面简化与终端/备份/工具箱 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将设置页面从7个分区简化为4个子页面，终端UI精简为命令输入/执行/结果三要素，备份恢复改为一键操作，合并MCP/Skill/包管理为统一页面，并将工具箱入口移入设置并重构工作流UI。
**Architecture:** 设置页面重构为4个子页面（模型+API配置、显示设置、工具箱、备份+关于），终端移除历史/建议/预设等辅助功能并实现首次静默安装，备份恢复仅保留一键备份/恢复，包管理三个Tab合并为单页小标题展示，工具箱从主导航移入设置子页面，工作流UI独立重构。
**Tech Stack:** Kotlin, Jetpack Compose

---

## 任务1：设置页面简化 - 创建4个子页面结构

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/SettingsScreen.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ModelApiSettingsScreen.kt` (新建)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/DisplaySettingsScreen.kt` (新建)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ToolboxSettingsScreen.kt` (新建)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/BackupAboutSettingsScreen.kt` (新建)

**步骤：**
- [ ] 重构 `SettingsScreen.kt`：移除当前7个分区（账号、个性化配置、AI模型配置、提示词配置、上下文和总结设置、数据和权限、外部调用），替换为4个导航入口项
- [ ] 4个入口项为：1) 模型+API配置 2) 显示设置 3) 工具箱 4) 备份+关于
- [ ] 每个入口项使用 `CompactSettingsItem` 组件，带图标和副标题
- [ ] `SettingsScreen` 的导航回调从18个缩减为4个：`navigateToModelApiSettings`、`navigateToDisplaySettings`、`navigateToToolboxSettings`、`navigateToBackupAboutSettings`
- [ ] 创建 `ModelApiSettingsScreen.kt`：包含4项配置（apiUrl、apiKey、modelName、temperature），从现有 `ModelConfig` 页面和 `ApiPreferences` 中提取核心字段
- [ ] 创建 `DisplaySettingsScreen.kt`：包含4项配置（明暗模式、主题选择、用户名、权限级别），从现有 `ThemeSettingsScreen`、`UserPreferencesSettingsScreen`、`GlobalDisplaySettingsScreen` 中提取
- [ ] 创建 `ToolboxSettingsScreen.kt`：作为工具箱入口页面，内容在任务4中实现
- [ ] 创建 `BackupAboutSettingsScreen.kt`：合并备份恢复和关于页面，备份部分在任务3中实现，关于部分从现有 `AboutScreen.kt` 迁移

**预期结果：**
SettingsScreen 从537行简化为约80行，仅包含4个导航入口。4个子页面各自独立。

---

## 任务2：设置页面简化 - 更新导航路由

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/common/NavItem.kt` (修改)

**步骤：**
- [ ] 在 `OperitScreens.kt` 中添加4个新的 Screen 对象：`ModelApiSettings`、`DisplaySettings`、`ToolboxSettings`、`BackupAboutSettings`
- [ ] 每个新 Screen 的 `Content` 分别渲染对应的子页面组件
- [ ] 修改 `Settings` Screen 的 `Content`，将18个导航回调替换为4个
- [ ] 在 `NavItem.kt` 中无需新增导航项，设置页面仍使用 `NavItem.Settings`
- [ ] 移除 `Settings` Screen 中不再需要的子页面导航回调：`navigateToModelConfig`、`navigateToThemeSettings`、`navigateToGlobalDisplaySettings`、`navigateToUserPreferences`、`navigateToChatBackupSettings` 等（这些功能已合并到4个子页面中）
- [ ] 保留 `navigateToToolPermissions` 等独立功能页面的路由（如仍需独立访问）

---

## 任务3：终端UI简化 - 移除辅助功能

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/shellexecutor/ShellExecutorScreen.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/toolbox/screens/shellexecutor/ShellCommandManager.kt` (修改)

**步骤：**
- [ ] 在 `ShellExecutorScreen.kt` 中移除以下UI元素和状态：
  - 移除 `commandHistory` 状态及历史记录列表渲染
  - 移除 `showSuggestions` 状态和命令建议下拉列表
  - 移除 `showPresets` 状态和预设命令面板
  - 移除 `presetCommands` 变量
  - 移除清除历史按钮
  - 移除重新执行按钮
  - 移除展开/收起功能
- [ ] 保留3个核心要素：命令输入框（`commandInput`）、命令执行逻辑（`executeCommand`）、结果展示区域
- [ ] 在 `ShellCommandManager.kt` 中移除 `getPresetCommands()` 方法
- [ ] 在 `ShellCommandManager.kt` 中移除 `getSuggestedCommands()` 方法
- [ ] 在 `ShellCommandManager.kt` 中移除 `getCommandHistory()` 方法
- [ ] 保留 `executeCommand()` 方法不变

---

## 任务4：终端UI简化 - 首次使用后台静默安装

**文件：**
- `app/src/main/java/com/ai/assistance/operit/terminal/TerminalManager.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/terminal/EnvironmentSetupService.kt` (新建)
- `app/src/main/java/com/ai/assistance/operit/ui/features/startup/screens/PluginLoadingScreen.kt` (修改)

**步骤：**
- [ ] 创建 `EnvironmentSetupService.kt`：封装首次使用时的后台静默安装逻辑
- [ ] 安装流程按顺序执行以下步骤，每步检测是否已安装，已安装则跳过：
  1. Node.js 安装（通过终端执行 `node --version` 检测，未安装则执行安装命令）
  2. PNPM 安装（通过 `pnpm --version` 检测，未安装则 `npm install -g pnpm`）
  3. Python 环境安装（通过 `python --version` 检测）
  4. Python 链接创建（确保 `python` 命令可用，必要时创建 `python -> python3` 符号链接）
  5. Python 虚拟环境创建（在终端工作目录下 `python -m venv venv`）
  6. Pip 安装（在虚拟环境中确保 pip 可用）
  7. uv 安装（通过 `uv --version` 检测，未安装则 `pip install uv`）
- [ ] 在 `TerminalManager.kt` 的 `initializeEnvironment()` 方法中，调用 `EnvironmentSetupService` 执行静默安装
- [ ] 静默安装不阻塞UI，在后台协程中执行
- [ ] 安装进度通过 `StateFlow<EnvironmentSetupState>` 暴露，状态包含：`IDLE`、`CHECKING`、`INSTALLING(stepName)`、`COMPLETED`、`FAILED(error)`
- [ ] 在 `PluginLoadingScreen.kt` 的启动流程中，触发 `EnvironmentSetupService` 的安装检测
- [ ] 参考现有 `DemoStateManager.refreshNodejsPythonEnvironment()` 中的检测逻辑，复用 `Terminal.getInstance(context).executeCommand()` 进行环境检测

---

## 任务5：备份恢复简化 - 一键备份/恢复

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/BackupAboutSettingsScreen.kt` (修改，在任务1创建的基础上)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/components/BackupDialogs.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ChatBackupSettingsScreen.kt` (可删除或标记废弃)

**步骤：**
- [ ] 在 `BackupAboutSettingsScreen.kt` 中实现一键备份功能：
  - 一个"备份"按钮，点击后直接执行 `RawSnapshotBackupManager.exportToBackupDir()`，备份所有应用数据
  - 备份进度通过 `LinearProgressIndicator` 显示
  - 备份完成后显示文件路径和成功提示
- [ ] 在 `BackupAboutSettingsScreen.kt` 中实现一键恢复功能：
  - 一个"恢复"按钮，点击后弹出文件选择器（`ActivityResultContracts.OpenDocument`）
  - 选择备份文件后，直接执行 `RawSnapshotBackupManager.restoreFromBackup()`
  - 恢复前弹出确认对话框，提示恢复将覆盖当前数据
  - 恢复完成后提示重启应用
- [ ] 移除以下功能：
  - 角色卡导出/导入（`CharacterCardManagementCard`、`exportCharacterCards`、`importCharacterCards`）
  - 记忆导出/导入（`MemoryManagementCard`、`exportMemories`、`importMemories`）
  - 模型配置导出/导入（`ModelConfigManagementCard`、`exportModelConfigs`、`importModelConfigs`）
  - Room数据库备份/恢复（`RoomDbBackupListItem`、`RoomDatabaseBackupManager`、`RoomDatabaseRestoreManager`）
  - Raw快照备份/恢复的独立选项（合并到一键操作中）
  - 格式选择对话框（`ExportFormatDialog`、`ImportFormatDialog`）
  - 导入策略选择对话框（`MemoryImportStrategyDialog`）
- [ ] 在 `BackupDialogs.kt` 中移除 `ExportFormatDialog`、`ImportFormatDialog`、`MemoryImportStrategyDialog` 组件
- [ ] 移除 `ChatBackupSettingsScreen.kt` 中不再需要的枚举：`RoomDatabaseBackupOperation`、`RoomDatabaseRestoreOperation`（如该文件被废弃则一并删除）

---

## 任务6：合并MCP/Skill/包管理为一个页面

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/features/packages/screens/PackageManagerScreen.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/packages/components/PackageTab.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ToolboxSettingsScreen.kt` (修改，在任务1创建的基础上)

**步骤：**
- [ ] 修改 `PackageTab.kt`：删除 `PackageTab` 枚举（不再需要Tab切换）
- [ ] 重构 `PackageManagerScreen.kt`：
  - 移除 `TabRow` 和 `selectedTab` 状态
  - 移除 `PackageTab.PACKAGES`、`PackageTab.SKILLS`、`PackageTab.MCP` 的Tab切换逻辑
  - 改为单页垂直滚动布局，使用3个小标题（`SettingsSection` 风格）分区展示：
    1. "包管理" 小标题：展示所有 `ToolPackage` 列表（原 PACKAGES Tab 内容）
    2. "Skill" 小标题：展示所有 Skill 列表（原 SKILLS Tab 内容）
    3. "MCP" 小标题：展示所有 MCP 服务器列表（原 MCP Tab 内容）
  - 每个小标题下保留原有的列表渲染逻辑（`PackagesList`、Skill列表、MCP列表）
  - 市场功能合并：在每个小标题区域添加"浏览市场"按钮，分别导航到对应的市场页面（`onNavigateToMCPMarket`、`onNavigateToSkillMarket`、`onNavigateToArtifactMarket`）
- [ ] 确保 `PackageManager.getInstance()` 的包加载逻辑不变，完全兼容现有版本的 `.toolpkg`、`.hjson`、`.js`、`.ts` 包
- [ ] 确保 `MCPRepository` 和 `SkillRepository` 的数据加载逻辑不变
- [ ] 在 `ToolboxSettingsScreen.kt` 中添加"包管理"入口项，点击导航到 `PackageManagerScreen`

---

## 任务7：弱化工具箱入口 - 移入设置页面

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/common/NavItem.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/ToolboxSettingsScreen.kt` (修改)

**步骤：**
- [ ] 在 `NavItem.kt` 中移除 `Toolbox` 对象（工具箱不再作为独立导航项）
- [ ] 在 `DrawerContent.kt` 中移除 `NavItem.Toolbox` 相关的侧边栏入口
  - 移除 `SidebarQuickActionCard` 中对 `NavItem.Toolbox` 的引用
  - 移除 `quickActionItems` 集合中的 `NavItem.Toolbox`
- [ ] 在 `OperitScreens.kt` 中移除 `Terminal`、`TerminalSetup`、`TerminalAutoConfig` Screen 对象中的 `navItem = NavItem.Toolbox` 引用，改为 `navItem = NavItem.Settings`
- [ ] 在 `ToolboxSettingsScreen.kt` 中添加工具箱入口列表，包含：
  - 终端（导航到 `Terminal` Screen）
  - 文件管理器（导航到 `FileManagerToolScreen`）
  - Shell执行器（导航到 `ShellExecutorToolScreen`）
  - 日志查看器（导航到 `LogcatToolScreen`）
  - UI调试器（导航到 `UIDebuggerToolScreen`）
  - FFmpeg工具箱（导航到 `FFmpegToolboxToolScreen`）
  - 应用权限管理（导航到 `AppPermissionsToolScreen`）
  - 包管理（导航到 `Packages` Screen，同任务6）
- [ ] 每个入口项使用 `CompactSettingsItem` 组件，与设置页面风格一致

---

## 任务8：工作流UI重构

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/features/workflow/screens/WorkflowListScreen.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/workflow/screens/WorkflowDetailScreen.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/workflow/components/DraggableNodeCard.kt` (修改)
- `app/src/main/java/com/ai/assistance/operit/ui/features/workflow/components/ScheduleConfigDialog.kt` (修改)

**步骤：**
- [ ] 重构 `WorkflowListScreen.kt`：
  - 移除 `isSelectionMode` 批量选择模式
  - 移除 `selectedWorkflowIds` 批量选择状态
  - 移除 `showDeleteSelectedDialog` 批量删除确认
  - 简化 FAB 菜单：仅保留"创建工作流"和"从模板创建"两个选项
  - 工作流列表项简化为：名称、描述、启用状态开关、点击进入详情
  - 移除 `SpeedDialAction` 中的批量删除按钮
- [ ] 重构 `WorkflowDetailScreen.kt`：
  - 简化 FAB 菜单：仅保留"运行/停止"和"添加节点"两个选项
  - 移除 `showConnectionMenu` 连接菜单
  - 节点操作简化：长按节点显示编辑/删除选项（替代复杂的操作菜单）
  - 保留节点拖拽、执行状态显示、执行日志查看功能
- [ ] 重构 `DraggableNodeCard.kt`：
  - 简化节点卡片UI：移除多余的边框动画，保留执行状态颜色指示
  - 节点类型图标更直观：TriggerNode 用闪电图标、ExecuteNode 用播放图标、ConditionNode 用分支图标、LogicNode 用逻辑图标
- [ ] `ScheduleConfigDialog.kt` 保持不变（定时配置功能保留）

---

## 任务9：清理废弃的设置子页面路由

**文件：**
- `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt` (修改)

**步骤：**
- [ ] 评估以下 Screen 对象是否仍需保留独立路由，如功能已合并到4个子页面中则标记为废弃：
  - `ModelConfig` -> 合并到 `ModelApiSettings`
  - `ThemeSettings` -> 合并到 `DisplaySettings`
  - `GlobalDisplaySettings` -> 合并到 `DisplaySettings`
  - `UserPreferencesSettings` -> 合并到 `DisplaySettings`
  - `ChatBackupSettings` -> 合并到 `BackupAboutSettings`
  - `FunctionalConfig` -> 合并到 `ModelApiSettings`
  - `ModelPromptsSettings` -> 合并到 `ModelApiSettings`
  - `ChatHistorySettings` -> 合并到 `BackupAboutSettings`
  - `LanguageSettings` -> 合并到 `DisplaySettings`
  - `SpeechServicesSettings` -> 合并到 `ModelApiSettings`
  - `ExternalHttpChatSettings` -> 合并到 `ModelApiSettings`
  - `PersonaCardGeneration` -> 合并到 `ModelApiSettings`
  - `WaifuModeSettings` -> 合并到 `DisplaySettings`
  - `TokenUsageStatistics` -> 合并到 `BackupAboutSettings`
  - `ContextSummarySettings` -> 合并到 `ModelApiSettings`
  - `LayoutAdjustmentSettings` -> 合并到 `DisplaySettings`
- [ ] 对每个被合并的 Screen，将其 `Content` 中的导航调用重定向到对应的新子页面
- [ ] 移除 `Settings` Screen 中对这些旧路由的导航回调参数
- [ ] 更新所有引用这些旧路由的导航代码

---

## 任务10：编译验证与最终清理

**步骤：**
- [ ] 执行全项目编译，确保无编译错误
- [ ] 全局搜索 `PackageTab`，确认无残留引用
- [ ] 全局搜索 `NavItem.Toolbox`，确认无残留引用
- [ ] 全局搜索 `ExportFormatDialog`、`ImportFormatDialog`、`MemoryImportStrategyDialog`，确认无残留引用
- [ ] 全局搜索 `CharacterCardManagementCard`、`MemoryManagementCard`、`ModelConfigManagementCard`、`RoomDbBackupListItem`，确认无残留引用
- [ ] 验证4个设置子页面导航正常
- [ ] 验证终端静默安装流程正常
- [ ] 验证一键备份/恢复功能正常
- [ ] 验证合并后的包管理页面正常显示包、Skill、MCP列表
- [ ] 验证工具箱入口从侧边栏移除，在设置页面可正常访问
- [ ] 验证工作流列表和详情页简化后的功能正常
