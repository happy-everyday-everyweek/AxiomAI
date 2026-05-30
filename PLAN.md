# Operit AI 简化与增强实施方案

> 本方案基于对项目31个模块、322个问题的深度研究，对所有待确认问题做出默认决策，形成可执行的完整方案。

---

## 一、总体策略

### 1.1 核心原则

1. **先删后建**：先完成所有模块移除和简化，再进行新功能开发，避免新旧代码交织
2. **分层推进**：按 数据层 → 逻辑层 → UI层 顺序修改，确保每层可独立编译验证
3. **API兼容**：移除UI但保留API的功能，用 `@Deprecated` 标注而非直接删除
4. **增量验证**：每个阶段完成后确保项目可编译运行

### 1.2 分期规划

| 阶段 | 名称 | 核心目标 | 预计工作量 |
|------|------|---------|-----------|
| P0 | 代码清理 | 移除所有砍掉的模块，清理死代码 | 大 |
| P1 | 架构调整 | 权限系统重构、AutoGLM内置化、长程任务增强 | 大 |
| P2 | UI重构 | 侧边栏、聊天界面、设置页面、主题系统 | 大 |
| P3 | 新功能 | 待办、日程、上下文自动注入 | 中 |
| P4 | 收尾打磨 | 字符串清理、性能优化、兼容性测试 | 中 |

---

## 二、P0 - 代码清理

### 2.1 虚拟形象系统完整移除

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

**CMakeLists.txt 修改**：移除 dragonbones、fbx、mmd 三个模块的编译配置

**引用清理**：
- `OperitApplication.kt`：移除虚拟形象初始化代码
- `AIForegroundService.kt`：移除桌宠/虚拟形象相关逻辑（`startPetMode`、`stopPetMode`、桌宠悬浮窗控制）
- `FloatingWindowManager`：移除桌宠模式（PET模式）
- `CharacterCardManager.kt`：移除虚拟形象配置同步逻辑
- `OperitScreens.kt`：移除助手配置页面路由
- `DrawerContent.kt`：移除助手配置入口
- `NavItem.kt`：移除助手相关导航项

### 2.2 语音系统完整移除

**移除范围**：

| 目录/文件 | 说明 |
|----------|------|
| `api/speech/` | 整个目录删除（SpeechService等） |
| `api/voice/` | 整个目录删除（VoiceService等） |
| `data/preferences/SpeechServicesPreferences.kt` | 删除 |
| `core/tools/defaultTool/standard/StandardSpeechTools.kt` | 删除 |

**引用清理**：
- `AIForegroundService.kt`：移除语音唤醒监听（`startWakeListening`、`stopWakeListening`、`startPersonalWakeListening`）
- `AIMessageManager.kt`：移除TTS朗读逻辑
- `ChatViewModel.kt`：移除语音朗读状态管理
- `AIChatScreen.kt`：移除朗读按钮
- `ToolRegistration.kt`：移除语音相关工具注册
- `SystemToolPrompts.kt`：移除语音工具提示词
- `FunctionalPrompts.kt`：移除语音相关功能提示词
- `OperitApplication.kt`：移除语音服务初始化

### 2.3 角色卡系统移除

**移除范围**：

| 文件 | 处理方式 |
|------|---------|
| `data/preferences/CharacterCardManager.kt` | 删除（1360行） |
| `data/model/CharacterCard.kt` | 删除 |
| `data/model/PromptTag.kt` | 删除 |
| `data/preferences/PromptTagManager.kt` | 删除 |
| `data/preferences/ActivePromptManager.kt` | 删除 |
| `data/preferences/PromptVersionManager.kt` | 删除 |

**保留但简化**：
- `ChatViewModel.kt`：移除角色卡选择/绑定状态，但保留 `characterId` 字段（标记为 `@Deprecated`，沙箱包兼容）
- `ChatDao.kt`：保留 `characterId` 数据库字段（标记为 `@Deprecated`）
- `WebChatHttpBridge.kt`：移除 `/character-selector` 端点，保留其他角色卡API但标记 `@Deprecated`

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

### 2.5 原有GUI操作能力移除

**移除范围**：

| 文件 | 说明 |
|------|------|
| `core/tools/agent/PhoneAgent.kt` | 删除（760行） |
| `core/tools/agent/PhoneAgentJobRegistry.kt` | 删除 |
| `core/tools/agent/VirtualDisplayManager.kt` | 删除 |
| `core/tools/agent/ShowerBinderRegistry.kt` | 删除 |
| `core/tools/agent/ShowerController.kt` | 删除 |
| `core/tools/climode/CliToolModeSupport.kt` | 删除（688行） |
| `core/tools/defaultTool/standard/StandardUITools.kt` | 删除 |
| `showerclient/` | 整个Gradle模块删除 |
| `app/src/main/assets/shower-server.jar` | 删除 |
| `app/src/main/assets/packages/operit_editor.js` | 删除 |

**保留**：
- `core/tools/agent/ShowerBinderRegistry.kt`（app层）：暂时保留，AutoGLM可能需要类似机制
- `app/src/main/assets/accessibility.apk`：保留，AutoGLM需要
- `app/src/main/assets/desktop.apk`：保留，桌面模式可能需要

**修改**：
- `ToolRegistration.kt`：移除PhoneAgent和StandardUITools工具注册
- `SystemToolPrompts.kt`：移除原有GUI操作工具提示词
- `AIForegroundService.kt`：移除Shower服务管理逻辑
- `settings.gradle.kts`：移除 `:showerclient` 模块
- `OperitApplication.kt`：移除Shower初始化

### 2.6 液态玻璃主题移除

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
- `Theme.kt`：移除Glass效果支持，重构为shadcn/ui风格主题（明暗模式 + 语义化色彩变量）
- `UserPreferencesManager.kt`：移除所有主题自定义字段，仅保留 `isDarkMode`
- `Compose DSL渲染器`：移除LiquidGlass/WaterGlass渲染器组件
- `NavigationDrawerAppearance.kt`：简化为shadcn/ui Sidebar风格

### 2.7 深度搜索移除

**移除范围**：
- 搜索并删除所有 `deepsearch`、`deep_search`、`DeepSearch` 相关代码
- 移除对应的工具注册和提示词

### 2.8 P0阶段数据库迁移

需要新增 Room 数据库迁移（`AppDatabase.kt`）：
- 移除 `character_id` 列的约束（保留字段但不再使用）
- 移除 `character_group_id` 相关表
- 移除 `display_mode` 中 Bubble 相关的值

---

## 三、P1 - 架构调整

### 3.1 权限系统重构

**当前权限级别**：BASIC → ADB → ROOT → ACCESSIBILITY

**简化后**：BASIC（基本）→ ACCESSIBILITY（高级）

**修改文件**：

| 文件 | 修改内容 |
|------|---------|
| `AndroidPermissionPreferences.kt` | 移除 ROOT 和 ADB 级别，重命名 BASIC→基本、ACCESSIBILITY→高级 |
| `RootAuthorizer.kt` | 删除 |
| `ShizukuAuthorizer.kt` | 保留但简化，高级权限仍可使用Shizuku提升能力 |
| `ToolPermissionSystem.kt` | 简化为两级权限检查 |
| `AccessibilityShellExecutor.kt` | 保留，高级权限下仍需要Shell执行 |
| `OperitScreens.kt` | 权限设置页面简化为基本/高级切换 |

**Shizuku处理**：
- 保留Shizuku授权能力（高级权限下可选使用）
- 移除自动安装 `shizuku.apk` 的逻辑
- 保留 `shizuku.apk` 资源文件供高级用户手动使用

### 3.2 AutoGLM GUI操作能力内置化

**当前架构**：AutoGLM作为JS沙箱包（`automatic_ui_base.js` + `automatic_ui_subagent.js`）运行在QuickJS引擎中

**目标架构**：AutoGLM核心能力用Kotlin原生实现，保留JS包作为轻量配置层

**实现方案**：

1. **新建 `core/tools/autoglm/` 目录**：
   - `AutoGLMExecutor.kt` - AutoGLM任务执行器（Kotlin原生）
   - `AutoGLMConfigManager.kt` - AutoGLM配置管理
   - `AutoGLMPermissionManager.kt` - AutoGLM权限管理（依赖无障碍服务）

2. **修改 `ToolRegistration.kt`**：
   - 新增AutoGLM内置工具注册（替代JS包中的工具定义）
   - 移除原有PhoneAgent工具注册

3. **修改 `SystemToolPrompts.kt`**：
   - 新增AutoGLM内置工具提示词
   - 移除原有GUI操作工具提示词

4. **修改 `SystemPromptConfig.kt`**：
   - 新增AutoGLM GUI操作指南到系统提示词

5. **JS包处理**：
   - `automatic_ui_base.js`：保留但改为调用Kotlin原生接口的桥接层
   - `automatic_ui_subagent.js`：保留但简化为配置和提示词定义
   - 第三方沙箱包仍可通过JS接口调用AutoGLM能力

6. **UI入口**：
   - `AutoGlmToolScreen.kt`：保留，从工具箱页面移到聊天界面斜杠命令
   - `AutoGlmViewModel.kt`：保留，调整为调用Kotlin原生接口

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
- 新增任务级别的进度事件（而非仅工具级别）
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

### 4.1 主题系统重构

**新建 `ui/theme/`**：

```
ui/theme/
├── Theme.kt              # 重写：shadcn/ui风格主题
├── Color.kt              # 重写：语义化色彩变量
├── Typography.kt         # 重写：4种字号+2种字重
├── Spacing.kt            # 新建：8pt网格间距系统
├── Components.kt         # 新建：统一组件样式
└── Shape.kt              # 保留但简化
```

**色彩系统**（参考shadcn/ui）：
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
- `AgentChatInputSection.kt`：保留，移除样式配置入口
- `ChatArea.kt`：保留，简化工具调用展示
- `ChatHistorySelector.kt`：移入侧边栏

**工具调用展示**：
- 一行小灰字显示工具名称
- 成功：灰色→绿色，3秒后→灰色
- 失败：灰色→红色，3秒后→灰色
- 实现：修改 `ChatArea.kt` 中的工具调用渲染逻辑

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
- 移除角色卡附件

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
2. **显示设置**：仅4项（明暗模式、用户名、权限级别、智能体模式）
3. **工具箱**：MCP/Skill/包管理合并页面
4. **备份+关于**：一键备份/恢复 + 应用信息

**移除的页面**：
- 主题设置页面（合并到显示设置的明暗模式切换）
- 终端设置页面
- 角色卡设置页面
- 语音设置页面
- 助手配置页面

### 4.6 终端UI简化

**修改 `OperitTerminalManager.kt`**：
- 首次使用时后台静默安装必要依赖（proot、busybox等）
- 完全移除配置项UI

**修改终端界面**：
- 仅保留：命令输入、命令执行、结果展示
- 移除：历史记录、命令建议、预设命令、清除历史、重新执行、展开/收起

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
   - 隐私：过滤银行、社交等敏感应用（可配置）

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

- 移除所有砍除功能相关的字符串（角色卡、虚拟形象、语音、Bubble、Classic、液态玻璃等）
- 清理顺序：中文 → 英文 → 日文
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
| 公告系统 | 保留但简化为仅显示关键公告 |
| GitHub认证 | 保留，市场发布功能需要 |
| 市场统计API | 保留，市场功能需要 |
| 计费模式 | 保留，Token统计需要 |
| 免费额度管理 | 保留但简化 |
| 技能可见性 | 保留，通过 `/tools` 命令管理 |
| 环境变量 | 保留，终端和脚本执行需要 |
| 用户协议 | 保留，法律合规 |
| 聊天格式导入 | 保留ChatGPT和Markdown，移除ChatBox和GenericJson |
| 聊天格式导出 | 保留Markdown和纯文本，移除HTML |
| 模型定价数据 | 保留但简化为仅关键模型 |

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

### 7.1 删除的文件/目录（约50+文件）

```
core/avatar/                          # 整个目录
core/tools/agent/PhoneAgent.kt
core/tools/agent/PhoneAgentJobRegistry.kt
core/tools/agent/VirtualDisplayManager.kt
core/tools/agent/ShowerBinderRegistry.kt (app层)
core/tools/agent/ShowerController.kt
core/tools/climode/CliToolModeSupport.kt
core/tools/defaultTool/standard/StandardUITools.kt
core/tools/defaultTool/standard/StandardSpeechTools.kt
api/speech/                           # 整个目录
api/voice/                            # 整个目录
ui/features/assistant/                # 整个目录
ui/features/chat/components/style/bubble/  # 整个目录
ui/features/chat/components/style/input/classic/  # 整个目录
ui/floating/ui/pet/                   # 整个目录
ui/components/ManagedDragonBonesView.kt
ui/theme/LiquidGlass.kt
ui/theme/WaterGlass.kt
ui/features/settings/screens/ThemeSettingsScreen.kt
ui/features/settings/sections/ThemeSettingsCoreSections.kt
ui/features/settings/sections/ThemeSettingsColorSection.kt
data/model/DragonBones.kt
data/model/CustomEmoji.kt
data/model/CharacterGroupCard.kt
data/model/SerializableColorScheme.kt
data/model/SerializableTypography.kt
data/preferences/CharacterCardManager.kt
data/preferences/PromptTagManager.kt
data/preferences/ActivePromptManager.kt
data/preferences/PromptVersionManager.kt
data/preferences/WaifuPreferences.kt
data/preferences/SpeechServicesPreferences.kt
data/preferences/ThemePreferenceSnapshot.kt
data/repository/AvatarRepository.kt
showerclient/                         # 整个Gradle模块
app/src/main/cpp/dragonbones/         # 整个C++模块
app/src/main/cpp/fbx/                 # 整个C++模块
app/src/main/cpp/mmd/                 # 整个C++模块
app/src/main/assets/emoji/            # 表情资源
app/src/main/assets/pets/             # 桌宠资源
app/src/main/assets/dragonbones/      # DragonBones资源
app/src/main/assets/shower-server.jar
app/src/main/assets/packages/operit_editor.js
```

### 7.2 新建的文件/目录

```
core/tools/autoglm/                   # AutoGLM内置化
├── AutoGLMExecutor.kt
├── AutoGLMConfigManager.kt
└── AutoGLMPermissionManager.kt

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

ui/theme/Color.kt                     # 主题重构
ui/theme/Typography.kt
ui/theme/Spacing.kt
ui/theme/Components.kt
```

### 7.3 重大修改的文件

| 文件 | 修改内容 |
|------|---------|
| `OperitApplication.kt` | 移除虚拟形象/语音/Shower初始化，新增AutoGLM/任务/上下文注入初始化 |
| `AIForegroundService.kt` | 移除桌宠/语音/Shower逻辑，增强任务进度通知 |
| `ChatServiceCore.kt` | 增加任务持久化和断点恢复 |
| `ChatViewModel.kt` | 移除角色卡/语音/样式状态，增加上下文注入 |
| `AIMessageManager.kt` | 移除TTS逻辑，增加自动注入上下文拼接 |
| `ToolRegistration.kt` | 移除PhoneAgent/语音工具，新增AutoGLM/待办/日程工具 |
| `SystemToolPrompts.kt` | 移除原有GUI/语音工具提示词，新增AutoGLM/待办/日程提示词 |
| `SystemPromptConfig.kt` | 增加AutoGLM指南和上下文注入说明 |
| `EnhancedAIService.kt` | 移除FLOATING槽位，简化为单槽位 |
| `ChatRuntimeHolder.kt` | 移除FLOATING支持 |
| `AIToolHandler.kt` | 增加超时重试机制 |
| `ToolProgressBus.kt` | 增加任务级别进度 |
| `Theme.kt` | 重构为shadcn/ui风格 |
| `OperitScreens.kt` | 侧边栏重构、移除助手配置页面、新增待办/日程页面 |
| `AIChatScreen.kt` | 移除朗读按钮、Tune按钮，增加斜杠命令、工具调用状态展示 |
| `ModelConfigManager.kt` | 简化配置项暴露 |
| `AndroidPermissionPreferences.kt` | 简化为两级权限 |
| `AppDatabase.kt` | 新增迁移脚本 |
| `settings.gradle.kts` | 移除 `:showerclient` |
| `CMakeLists.txt` | 移除dragonbones/fbx/mmd |

---

## 八、风险与注意事项

1. **沙箱包兼容性**：移除角色卡和原有GUI操作能力后，第三方沙箱包可能依赖这些API。所有移除UI但保留API的功能需用 `@Deprecated` 标注，并在文档中说明迁移路径。

2. **数据库迁移**：每次数据模型变更都需要Room迁移脚本，需确保升级路径完整。

3. **AutoGLM内置化**：从JS包迁移到Kotlin原生实现时，需确保功能等价性。建议先实现Kotlin版本，保留JS包作为降级方案。

4. **长程任务持久化**：检查点序列化格式需要向前兼容，避免版本升级后无法恢复旧任务。

5. **上下文自动注入的Token消耗**：需要严格的Token预算控制，避免自动注入挤占用户对话空间。

6. **C++模块编译**：移除dragonbones/fbx/mmd后需确保CMakeLists.txt正确，不影响其他C++模块编译。
