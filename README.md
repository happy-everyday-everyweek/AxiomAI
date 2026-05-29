# Operit AI - 开发进度与待办

> 原项目介绍文档已归档至 [ARCHIVE_README.md](ARCHIVE_README.md) | [ARCHIVE_README(E).md](ARCHIVE_README(E).md)

---

## 当前状态

项目正处于 **UI优化 + 功能简化** 阶段，目标是让应用从功能堆砌走向精简专注。

---

## 需求对齐记录

### 一、保留并简化的功能

| 功能模块 | 简化方向 |
|---------|---------|
| AI对话 + 工具调用 | 核心功能保留，简化配置项，大部分规则由系统自动管理，用户无需接触具体规则和配置 |
| MCP/Skill/工具包 | 保留功能，简化UI |
| 模型配置 | 保留，简化配置项 |
| 记忆库 + 对话历史 | 保留，简化配置项 |
| 终端环境 | 首次使用时后台静默安装必要依赖，简化UI，完全移除配置项 |
| 市场（MCP/Skill/提示词/工具包） | 保留市场功能，简化UI |
| 备份恢复 | 简化为一键备份/一键恢复 |
| 悬浮窗 | 移除输入相关功能，仅保留进度展示和后台保活能力。UI为一个黑色气泡，悬浮在屏幕中间上部 |
| 侧边栏导航 | 保留侧边栏抽屉导航，精简菜单项 |
| web-chat模块 | 保留暴露的API（后续可能基于此进一步开发），移除Web UI |

### 二、砍掉的功能

| 功能模块 | 说明 |
|---------|------|
| 角色卡系统 | 移除角色卡群聊、互聊、导入导出、二维码分享等全部角色卡相关功能 |
| 桌宠 + 虚拟形象 | 移除DragonBones/FBX/MMD虚拟形象、桌宠功能、液态玻璃主题 |
| 语音交互 | 移除语音对话、TTS、STT、语音唤醒等全部语音功能 |
| 深度搜索 | 完全移除 |

### 三、UI优化方向

| 优化项 | 具体方案 |
|-------|---------|
| 聊天风格 | 统一为一种风格，移除气泡/光标等多种样式选择 |
| 工具调用展示 | 保留一行小灰字，简单说明使用的工具名称；成功后灰色变绿色，3秒后变回灰色；失败则变红色后变回灰色 |
| 设置页面 | 保留：模型+API配置、显示设置、权限设置、备份+关于；每个页面都要彻底简化 |
| 权限级别 | 移除Root和ADB级别，名称改为"基本"和"高级"（高级=无障碍） |
| 侧边栏 | 精简菜单项 |
| 整体原则 | 大部分规则由系统自动管理，用户不需要接触具体的规则和配置 |

### 四、待进一步确认的事项

- [ ] 侧边栏具体保留哪些菜单项
- [ ] 设置页面的具体简化方案（每个子页面保留哪些配置项）
- [ ] 聊天输入区域的具体简化方案
- [ ] 市场UI的具体简化方案
- [ ] 工具包管理的具体简化方案
- [ ] 记忆库UI的具体简化方案
- [ ] 对话历史管理的具体简化方案
- [ ] 终端UI的具体简化方案
- [ ] web-chat模块API的保留范围
- [ ] 砍掉功能后的数据迁移/兼容方案

---

## 项目代码结构分析

### 导航系统

**侧边栏（DrawerContent）** 当前包含以下导航项：

| 分类 | 导航项 | NavItem | 对应Screen |
|------|--------|---------|-----------|
| 快捷操作 | 包管理 | NavItem.Packages | Packages |
| 快捷操作 | 权限 | NavItem.ShizukuCommands | ShizukuCommands |
| 快捷操作 | 工作流 | NavItem.Workflow | Workflow |
| AI功能 | AI聊天 | NavItem.AiChat | AiChat |
| AI功能 | 记忆库 | NavItem.MemoryBase | MemoryBase |
| AI功能 | 助手配置 | NavItem.AssistantConfig | AssistantConfig |
| 底部快捷 | 关于 | NavItem.About | About |
| 底部快捷 | 帮助 | NavItem.Help | Help |
| 底部快捷 | 设置 | NavItem.Settings | Settings |
| 插件区 | 动态工具包页面 | - | ToolPkgComposeDsl |

**Screen路由表**（OperitScreens.kt 中定义的所有Screen）：

| Screen | 类型 | 说明 |
|--------|------|------|
| AiChat | 主页面 | AI聊天界面 |
| MemoryBase | 主页面 | 记忆库 |
| Packages | 主页面 | 包管理器 |
| Market | 子页面 | 统一市场（MCP/Skill/Artifact Tab） |
| ArtifactManage/Detail/Publish | 子页面 | Artifact管理/详情/发布 |
| SkillManage/Detail/Publish | 子页面 | Skill管理/详情/发布 |
| MCPManage/PluginDetail/Publish | 子页面 | MCP管理/详情/发布 |
| Toolbox | 主页面 | 工具箱 |
| ShizukuCommands | 主页面 | 权限配置（ShizukuDemo） |
| Settings | 主页面 | 设置 |
| Workflow/WorkflowDetail | 主页面 | 工作流列表/详情 |
| AssistantConfig | 主页面 | 助手配置（虚拟形象等） |
| About | 主页面 | 关于 |
| Help | 主页面 | 帮助 |
| Agreement | 主页面 | 用户协议 |
| TokenConfig | 子页面 | Token配置（WebView） |
| UpdateHistory | 子页面 | 更新历史 |
| ToolPermission | 设置子页面 | 工具权限 |
| UserPreferencesGuide | 设置子页面 | 用户偏好引导 |
| UserPreferencesSettings | 设置子页面 | 用户偏好设置 |
| ModelConfig | 设置子页面 | 模型配置 |
| SpeechServicesSettings | 设置子页面 | 语音服务设置 |
| ExternalHttpChatSettings | 设置子页面 | 外部HTTP聊天设置 |
| MnnModelDownload | 设置子页面 | MNN模型下载 |
| PersonaCardGeneration | 设置子页面 | 人设卡生成 |
| WaifuModeSettings | 设置子页面 | 桌宠模式设置 |
| CustomEmojiManagement | 设置子页面 | 自定义表情管理 |
| TagMarket | 设置子页面 | 提示词市场 |
| ModelPromptsSettings | 设置子页面 | 模型提示词设置 |
| FunctionalConfig | 设置子页面 | 功能配置 |
| ThemeSettings | 设置子页面 | 主题设置 |
| GlobalDisplaySettings | 设置子页面 | 全局显示设置 |
| LayoutAdjustmentSettings | 设置子页面 | 布局调整设置 |
| ChatHistorySettings | 设置子页面 | 聊天历史设置 |
| ChatBackupSettings | 设置子页面 | 聊天备份设置 |
| LanguageSettings | 设置子页面 | 语言设置 |
| TokenUsageStatistics | 设置子页面 | Token使用统计 |
| ContextSummarySettings | 设置子页面 | 上下文摘要设置 |
| GitHubAccount | 设置子页面 | GitHub账号 |
| FileManager | 工具箱子页面 | 文件管理器 |
| Terminal/TerminalSetup/TerminalAutoConfig | 工具箱子页面 | 终端 |
| AppPermissions | 工具箱子页面 | 应用权限 |
| UIDebugger | 工具箱子页面 | UI调试器 |
| ShellExecutor | 工具箱子页面 | Shell执行器 |
| Logcat | 工具箱子页面 | 日志查看 |
| SqlViewer | 工具箱子页面 | SQL查看器 |
| FFmpegToolbox | 工具箱子页面 | FFmpeg工具箱 |
| MarkdownDemo | 工具箱子页面 | Markdown演示 |
| ToolTester | 工具箱子页面 | 工具测试 |
| TextToSpeech | 工具箱子页面 | TTS工具 |
| SpeechToText | 工具箱子页面 | STT工具 |
| DefaultAssistantGuide | 工具箱子页面 | 默认助手引导 |
| ProcessLimitRemover | 工具箱子页面 | 进程限制移除 |
| HtmlPackager | 工具箱子页面 | HTML打包器 |
| AutoGlmOneClick/AutoGlmTool | 工具箱子页面 | AutoGLM工具 |
| ToolPkgComposeDsl/ToolPkgPluginConfig | 动态页面 | 工具包Compose DSL页面 |

### 设置页面层级

SettingsScreen 当前分为以下6个分组，共18个子页面：

| 分组 | 子页面 |
|------|--------|
| 账号 | GitHub账号 |
| 个性化配置 | 用户偏好、语言、主题、全局显示、布局调整 |
| AI模型配置 | 模型参数、功能模型、语音服务 |
| 提示词配置 | 提示词、人设卡生成、桌宠模式设置 |
| 上下文和总结 | 上下文摘要设置 |
| 数据和权限 | 工具权限、数据备份、聊天历史管理、Token使用统计 |
| 外部调用 | 外部HTTP聊天设置 |

### 聊天样式系统

当前支持两种聊天样式：

| 样式 | 实现文件 | 说明 |
|------|---------|------|
| 气泡模式 (Bubble) | BubbleStyleChatMessage.kt, BubbleAiMessageComposable.kt, BubbleUserMessageComposable.kt | 气泡式消息展示，支持液体玻璃效果 |
| 光标模式 (Cursor) | CursorStyleChatMessage.kt, AiMessageComposable.kt, UserMessageComposable.kt | 光标式消息展示，支持思考过程展示 |

### 悬浮窗系统

FloatingWindowManager 当前支持以下模式：

| 模式 | 说明 |
|------|------|
| 全屏模式 | 完整聊天界面 |
| 窗口模式 | 可调整大小的浮动窗口 |
| 球形模式 | 最小化球状图标 |
| 语音球模式 | 语音交互球 |
| 结果展示模式 | 仅展示结果 |

### web-chat模块

- 技术栈：React 19 + Vite + react-markdown
- 通信方式：通过HTTP API与Android端通信
- Android端：ExternalChatHttpServer 暴露HTTP服务
- 桥接层：WebChatHttpBridge -> WebChatActionBridge / WebChatManagementBridge / WebChatInputSettingsBridge
- 前端API：chatApi.ts 封装HTTP请求

### 需要砍除的代码模块清单

#### 角色卡系统
- `data/model/CharacterCard.kt` - 角色卡数据模型
- `data/model/CharacterGroupCard.kt` - 角色组卡模型
- `data/preferences/CharacterCardManager.kt` - 角色卡管理
- `data/preferences/CharacterGroupCardManager.kt` - 角色组管理
- `data/preferences/PersonaCardChatHistoryManager.kt` - 角色卡聊天历史
- `data/preferences/CharacterCardBilingualData.kt` - 双语数据
- `data/preferences/CharacterCardToolAccessResolver.kt` - 工具访问
- `data/model/CharacterCardChatStats.kt` / `CharacterGroupChatStats.kt` - 统计
- `ui/features/settings/screens/PersonaCardGenerationScreen.kt` - 人设卡生成页面
- `ui/features/chat/components/CharacterSelectorPanel.kt` - 角色选择器
- `ui/features/chat/components/CharacterCardMemoryBindingSwitchConfirmDialog.kt` - 记忆绑定确认
- `ui/features/chat/components/CharacterCardModelBindingSwitchConfirmDialog.kt` - 模型绑定确认

#### 桌宠 + 虚拟形象
- `core/avatar/` - 整个虚拟形象模块（AvatarModel, AvatarController, AvatarView等）
- `core/avatar/impl/dragonbones/` - DragonBones实现
- `core/avatar/impl/fbx/` - FBX实现
- `core/avatar/impl/mmd/` - MMD实现
- `core/avatar/impl/gltf/` - GLTF实现
- `data/preferences/WaifuPreferences.kt` - 桌宠偏好
- `data/model/DragonBones.kt` - DragonBones模型
- `ui/features/assistant/` - 助手配置页面（AvatarConfigSection, AvatarPreviewSection等）
- `ui/features/settings/screens/WaifuModeSettingsScreen.kt` - 桌宠模式设置
- `ui/features/settings/screens/CustomEmojiManagementScreen.kt` - 自定义表情管理
- `ui/components/ManagedDragonBonesView.kt` - DragonBones视图组件
- `dragonbones/` - DragonBones C++原生模块
- `fbx/` - FBX原生模块
- `mmd/` - MMD原生模块

#### 语音交互
- `api/speech/` - 语音识别（SherpaSpeechProvider, DeepgramSttProvider等）
- `api/voice/` - 语音合成（VoiceService, OpenAIVoiceProvider, DoubaoVoiceProvider等）
- `ui/features/assistant/components/VoiceAutoAttachComponents.kt` - 语音自动绑定
- `ui/features/settings/screens/SpeechServicesSettingsScreen.kt` - 语音服务设置
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt` - Agent输入（语音相关）
- `data/preferences/SpeechServicesPreferences.kt` - 语音偏好
- `data/preferences/WakeWordPreferences.kt` - 唤醒词偏好
- `services/assistant/OperitVoiceInteractionService.kt` - 语音交互服务
- `services/assistant/OperitVoiceInteractionSessionService.kt` - 语音交互会话

#### 深度搜索
- `examples/deepsearching/` - 深度搜索插件示例

#### 液态玻璃主题
- `ui/theme/liquidGlass` - 液态玻璃主题效果（DrawerContent中大量使用）

---

## 待办事项

### 阶段一：需求对齐（进行中）

- [x] 初步需求收集
- [x] 整理需求到README
- [x] 深入研究项目代码结构
- [ ] 继续与用户对齐需求细节
- [ ] 完善README中的待确认事项

### 阶段二：功能砍除

- [ ] 移除角色卡系统相关代码
- [ ] 移除桌宠+虚拟形象相关代码
- [ ] 移除语音交互相关代码
- [ ] 移除深度搜索相关代码
- [ ] 简化悬浮窗功能（移除输入功能，仅保留进度气泡）
- [ ] 移除web-chat的Web UI（保留API）
- [ ] 清理砍除功能后的残留引用和资源

### 阶段三：UI简化

- [ ] 统一聊天风格为一种
- [ ] 简化工具调用展示（灰/绿/红状态文字）
- [ ] 精简侧边栏菜单项
- [ ] 简化设置页面（保留模型+API、显示、权限、备份+关于）
- [ ] 权限级别重命名（基本/高级）
- [ ] 简化市场UI
- [ ] 简化备份恢复为一键操作
- [ ] 简化终端UI（移除配置项）

### 阶段四：系统规则自动化

- [ ] 将常用配置项改为系统默认值，用户无需手动配置
- [ ] 终端首次使用自动安装依赖
- [ ] 其他规则自动化处理

### 阶段五：验证与清理

- [ ] 清理无用资源文件
- [ ] 清理无用依赖
- [ ] 验证核心功能正常
- [ ] 更新文档

---

## 执行方式

按照本README的待办事项逐步执行，每完成一步更新README中的完成状态。
