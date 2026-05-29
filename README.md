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
| MCP/Skill/工具包 | 将MCP、Skill与包管理功能汇聚在一个页面上，完全兼容现有版本的包，需要将相关API接口进行深入的重定向 |
| 模型配置 | 保留，简化配置项 |
| 记忆库 + 对话历史 | 保留，简化配置项 |
| 终端环境 | 首次使用时后台静默安装必要依赖，简化UI，完全移除配置项 |
| 市场（MCP/Skill/提示词/工具包） | 保留市场功能，简化UI |
| 备份恢复 | 简化为一键备份/一键恢复 |
| 悬浮窗 | 移除输入相关功能，仅保留进度展示和后台保活能力。UI为一个黑色气泡，悬浮在屏幕中间上部 |
| web-chat模块 | 保留暴露的API（后续可能基于此进一步开发），移除Web UI |
| 工作流 | 完整保留，UI重构。功能入口级别与日历、待办等新功能同级 |
| 工具箱 | 全部保留，但弱化工具箱相关入口 |

### 二、砍掉的功能

| 功能模块 | 说明 |
|---------|------|
| 角色卡系统 | 移除角色卡群聊、互聊、导入导出、二维码分享等全部角色卡相关功能 |
| 桌宠 + 虚拟形象 | 移除DragonBones/FBX/MMD虚拟形象、桌宠功能、液态玻璃主题 |
| 语音交互 | 移除语音对话、TTS、STT、语音唤醒等全部语音功能 |
| 深度搜索 | 完全移除 |
| 对话框样式 (Bubble) | 聊天样式仅保留"命令框"(Cursor)，移除"对话框"(Bubble) |
| 经典输入模式 (Classic) | 输入框样式仅保留"智能体模式"(Agent)，移除"经典模式"(Classic) |
| 命令框样式配置 | 完全移除液态玻璃、水玻璃、自定义颜色等所有命令框样式配置项 |

### 三、UI风格指引

参考 shadcn/ui 设计系统，遵循以下原则：

| 原则 | 说明 |
|------|------|
| 排版系统 | 仅使用4种字号（大标题/副标题/正文/小字）和2种字重（半粗/常规） |
| 8pt网格 | 所有间距值必须能被8或4整除（如8/12/16/24/32dp） |
| 60/30/10色彩规则 | 60%中性色（背景）、30%互补色（文字/边框）、10%强调色（主色） |
| 语义化色彩 | 使用CSS变量定义语义色（background/primary/secondary/accent/muted/destructive等），每色配foreground |
| 简洁视觉结构 | 逻辑分组、刻意留白、对齐一致、功能优先于装饰 |
| 无障碍 | ARIA支持、键盘导航、WCAG 2.1 AA合规 |
| 暗色模式 | 原生支持，深色模式下调整亮度和饱和度 |
| 组件化 | 复用基础组件（Button/Card/Dialog/Input/Switch/Slider等），统一variant体系 |

注意：项目当前使用 Jetpack Compose (Material 3)，shadcn/ui 风格需要通过自定义 Theme 和组件样式来模拟，而非直接使用 shadcn/ui 库。核心是借鉴其设计理念（简洁、克制、语义化），而非照搬代码。

#### shadcn/ui 组件参考

参考 https://github.com/shadcn-ui/ui 和 https://ui.shadcn.com 的组件设计：

**侧边栏组件 (Sidebar)** - 参考 shadcn/ui Sidebar 组件结构：
```
SidebarProvider
├── Sidebar
│   ├── SidebarHeader     → 新建对话按钮
│   ├── SidebarContent    → 可滚动区域
│   │   ├── SidebarGroup  → 预留按钮区（工作流/日历/待办）
│   │   └── SidebarGroup  → 历史对话列表
│   ├── SidebarFooter     → 底部按钮（设置/工具）
│   └── SidebarRail       → 可选的宽度调节手柄
└── SidebarInset          → 主内容区域
```

**色彩变量** - 参考 shadcn/ui Sidebar 主题变量：
```
--sidebar-background / --sidebar-foreground
--sidebar-primary / --sidebar-primary-foreground
--sidebar-accent / --sidebar-accent-foreground
--sidebar-border / --sidebar-ring
```

**其他核心组件** - Button(variant: default/destructive/outline/ghost/link)、Card、Dialog、Input、Switch、Slider、Command(斜杠命令)、Sheet(底部弹出面板)

### 四、UI优化方向

#### 4.1 侧边栏重构

参考 shadcn/ui Sidebar 组件，对齐主流AI应用程序（如ChatGPT、Claude等），侧边栏结构重新设计：

```
SidebarProvider
├── Sidebar (collapsible="offcanvas")
│   ├── SidebarHeader
│   │   └── 新建对话按钮
│   ├── SidebarContent (可滚动)
│   │   ├── SidebarGroup → 预留按钮区
│   │   │   ├── 工作流
│   │   │   ├── [预留] 日历
│   │   │   └── [预留] 待办
│   │   └── SidebarGroup → 历史对话列表
│   │       ├── SidebarGroupLabel "历史对话"
│   │       └── SidebarMenu → 对话列表项
│   └── SidebarFooter
│       ├── 设置按钮
│       └── 工具按钮
└── SidebarInset → 主聊天区域
```

- 预留按钮区位于新建对话下方、对话历史列表上方
- 移除原有的快捷操作区（包管理、权限、工作流三个卡片）
- 移除AI功能分组（AI聊天、记忆库、助手配置）
- 移除底部快捷（关于、帮助、设置三个按钮）
- 原来分散在各处的功能入口整合到设置中
- 工具箱作为底部按钮保留，但弱化入口
- 侧边栏宽度默认 16rem，支持折叠为图标模式

#### 4.2 聊天界面简化

| 优化项 | 具体方案 |
|-------|---------|
| 聊天样式 | 仅保留"命令框"(Cursor)样式，移除"对话框"(Bubble)样式及其全部配置项 |
| 输入框样式 | 仅保留"智能体模式"(Agent)，移除"经典模式"(Classic)及其设置栏 |
| 命令框样式配置 | 完全移除所有配置项（液态玻璃、水玻璃、自定义颜色等），统一使用系统默认 |
| 工具调用展示 | 保留一行小灰字，简单说明使用的工具名称；成功后灰色变绿色，3秒后变回灰色；失败则变红色后变回灰色 |
| 模型选择 | 弱化模型选择，不使用框框住，改为灰色小字显示当前模型名 |
| 配置按钮(Tune) | 移除显式配置按钮。用户输入斜杠"/"时打开原来的配置面板（斜杠命令系统） |
| 附件功能 | 全部保留（截图、通知、位置、记忆、工具包、拍照），但很大一部分直接注入上下文无需用户手动选择（如用户通知、屏幕使用时间等） |
| 全屏输入 | 待确认 |
| 回复引用 | 待确认 |
| 待发送队列 | 待确认 |
| 输入框样式配置 | 完全移除（透明、浮动、液态玻璃、水玻璃） |

#### 4.3 斜杠命令系统

新增斜杠命令交互方式：
- 用户在输入框中输入 `/` 时，弹出原来的配置面板
- 这替代了原来的Tune设置按钮
- 具体命令列表和面板内容待确认

#### 4.4 上下文自动注入

附件功能中，很大一部分信息直接注入到对话上下文中，无需用户手动选择：
- 用户的通知
- 用户的屏幕使用时间
- 其他可自动获取的上下文信息
- 具体自动注入的内容清单待确认

#### 4.5 设置页面简化

| 优化项 | 具体方案 |
|-------|---------|
| 保留的子页面 | 模型+API配置、显示设置、权限设置、备份+关于 |
| 权限级别 | 移除Root和ADB级别，名称改为"基本"和"高级"（高级=无障碍） |
| 每个页面 | 都要彻底简化，大部分配置由系统自动管理 |

#### 4.6 MCP/Skill/包管理合并

- 将MCP、Skill与包管理功能汇聚在一个页面上
- 完全兼容现有版本的包
- 需要将相关API接口进行深入的重定向
- 具体合并方案待确认

#### 4.7 整体原则

大部分规则由系统自动管理，用户不需要接触具体的规则和配置。

### 五、待进一步确认的事项

- [ ] 斜杠命令系统的具体命令列表和面板内容
- [ ] 上下文自动注入的具体内容清单（哪些信息自动注入，哪些需要手动选择）
- [ ] 设置页面每个子页面的具体简化方案（需深入研究每个页面的配置项后提出具体问题）
- [ ] MCP/Skill/包管理合并的具体页面设计方案
- [ ] 记忆库UI的具体简化方案
- [ ] 对话历史管理的具体简化方案
- [ ] 终端UI的具体简化方案
- [ ] web-chat模块API的保留范围
- [ ] 砍掉功能后的数据迁移/兼容方案
- [ ] 全屏输入、回复引用、待发送队列是否保留
- [ ] 工具箱入口弱化的具体方案

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

| Screen | 类型 | 说明 | 简化后状态 |
|--------|------|------|-----------|
| AiChat | 主页面 | AI聊天界面 | 保留 |
| MemoryBase | 主页面 | 记忆库 | 保留（入口移至设置或对话内） |
| Packages | 主页面 | 包管理器 | 合并到MCP/Skill/包管理统一页面 |
| Market | 子页面 | 统一市场 | 保留 |
| ArtifactManage/Detail/Publish | 子页面 | Artifact管理/详情/发布 | 保留 |
| SkillManage/Detail/Publish | 子页面 | Skill管理/详情/发布 | 保留 |
| MCPManage/PluginDetail/Publish | 子页面 | MCP管理/详情/发布 | 保留 |
| Toolbox | 主页面 | 工具箱 | 保留（弱化入口） |
| ShizukuCommands | 主页面 | 权限配置 | 保留（入口移至设置） |
| Settings | 主页面 | 设置 | 保留（侧边栏底部按钮） |
| Workflow/WorkflowDetail | 主页面 | 工作流列表/详情 | 保留（侧边栏预留按钮区） |
| AssistantConfig | 主页面 | 助手配置（虚拟形象等） | 移除（虚拟形象砍掉） |
| About | 主页面 | 关于 | 保留（入口移至设置） |
| Help | 主页面 | 帮助 | 待确认 |
| Agreement | 主页面 | 用户协议 | 保留 |
| TokenConfig | 子页面 | Token配置（WebView） | 保留 |
| UpdateHistory | 子页面 | 更新历史 | 保留 |
| ToolPermission | 设置子页面 | 工具权限 | 保留 |
| UserPreferencesGuide | 设置子页面 | 用户偏好引导 | 保留 |
| UserPreferencesSettings | 设置子页面 | 用户偏好设置 | 保留 |
| ModelConfig | 设置子页面 | 模型配置 | 保留 |
| SpeechServicesSettings | 设置子页面 | 语音服务设置 | 移除（语音砍掉） |
| ExternalHttpChatSettings | 设置子页面 | 外部HTTP聊天设置 | 待确认 |
| MnnModelDownload | 设置子页面 | MNN模型下载 | 保留 |
| PersonaCardGeneration | 设置子页面 | 人设卡生成 | 移除（角色卡砍掉） |
| WaifuModeSettings | 设置子页面 | 桌宠模式设置 | 移除（桌宠砍掉） |
| CustomEmojiManagement | 设置子页面 | 自定义表情管理 | 移除（桌宠砍掉） |
| TagMarket | 设置子页面 | 提示词市场 | 待确认 |
| ModelPromptsSettings | 设置子页面 | 模型提示词设置 | 保留 |
| FunctionalConfig | 设置子页面 | 功能配置 | 保留 |
| ThemeSettings | 设置子页面 | 主题设置 | 保留（大幅简化） |
| GlobalDisplaySettings | 设置子页面 | 全局显示设置 | 保留（大幅简化） |
| LayoutAdjustmentSettings | 设置子页面 | 布局调整设置 | 待确认 |
| ChatHistorySettings | 设置子页面 | 聊天历史设置 | 保留 |
| ChatBackupSettings | 设置子页面 | 聊天备份设置 | 保留（简化为一键操作） |
| LanguageSettings | 设置子页面 | 语言设置 | 保留 |
| TokenUsageStatistics | 设置子页面 | Token使用统计 | 待确认 |
| ContextSummarySettings | 设置子页面 | 上下文摘要设置 | 保留（简化） |
| GitHubAccount | 设置子页面 | GitHub账号 | 待确认 |
| FileManager | 工具箱子页面 | 文件管理器 | 保留 |
| Terminal/TerminalSetup/TerminalAutoConfig | 工具箱子页面 | 终端 | 保留 |
| AppPermissions | 工具箱子页面 | 应用权限 | 保留 |
| UIDebugger | 工具箱子页面 | UI调试器 | 待确认 |
| ShellExecutor | 工具箱子页面 | Shell执行器 | 保留 |
| Logcat | 工具箱子页面 | 日志查看 | 保留 |
| SqlViewer | 工具箱子页面 | SQL查看器 | 保留 |
| FFmpegToolbox | 工具箱子页面 | FFmpeg工具箱 | 保留 |
| MarkdownDemo | 工具箱子页面 | Markdown演示 | 移除（演示类） |
| ToolTester | 工具箱子页面 | 工具测试 | 移除（调试类） |
| TextToSpeech | 工具箱子页面 | TTS工具 | 移除（语音砍掉） |
| SpeechToText | 工具箱子页面 | STT工具 | 移除（语音砍掉） |
| DefaultAssistantGuide | 工具箱子页面 | 默认助手引导 | 待确认 |
| ProcessLimitRemover | 工具箱子页面 | 进程限制移除 | 待确认 |
| HtmlPackager | 工具箱子页面 | HTML打包器 | 保留 |
| AutoGlmOneClick/AutoGlmTool | 工具箱子页面 | AutoGLM工具 | 保留 |
| ToolPkgComposeDsl/ToolPkgPluginConfig | 动态页面 | 工具包Compose DSL页面 | 保留 |

### 设置页面层级

SettingsScreen 当前分为以下6个分组，共18个子页面：

| 分组 | 子页面 | 简化后状态 |
|------|--------|-----------|
| 账号 | GitHub账号 | 待确认 |
| 个性化配置 | 用户偏好、语言、主题、全局显示、布局调整 | 保留显示相关，移除桌宠/角色卡相关 |
| AI模型配置 | 模型参数、功能模型、语音服务 | 保留模型参数和功能模型，移除语音服务 |
| 提示词配置 | 提示词、人设卡生成、桌宠模式设置 | 保留提示词，移除人设卡和桌宠 |
| 上下文和总结 | 上下文摘要设置 | 保留 |
| 数据和权限 | 工具权限、数据备份、聊天历史管理、Token使用统计 | 保留，简化 |
| 外部调用 | 外部HTTP聊天设置 | 待确认 |

### 聊天样式系统

当前支持两种聊天样式（strings.xml中的中文名）：

| 样式 | 代码名 | 中文名 | 实现文件 | 简化后状态 |
|------|--------|--------|---------|-----------|
| 命令框 | ChatStyle.CURSOR | 命令框 | CursorStyleChatMessage.kt, AiMessageComposable.kt, UserMessageComposable.kt | 保留 |
| 对话框 | ChatStyle.BUBBLE | 对话框 | BubbleStyleChatMessage.kt, BubbleAiMessageComposable.kt, BubbleUserMessageComposable.kt | 移除 |

命令框样式下的配置项（ThemeSettingsCoreSections.kt） - 全部移除：
- 命令式用户消息框跟随主题色
- 命令式用户消息磨砂玻璃背景（液态玻璃）
- 命令式用户消息水玻璃背景
- 命令式用户消息框颜色自定义

对话框样式下的配置项（需移除）：
- 显示头像、更宽的气泡
- 用户/AI气泡磨砂玻璃/水玻璃背景
- 图片气泡渲染模式（.9/九宫格平铺）
- 圆角气泡、气泡颜色、文字样式、字体、气泡图片等大量配置

### 输入框样式系统

当前支持两种输入框样式：

| 样式 | 代码名 | 说明 | 简化后状态 |
|------|--------|------|-----------|
| 智能体模式 | INPUT_STYLE_AGENT | AgentChatInputSection.kt，带功能按钮面板的输入框 | 保留并简化 |
| 经典模式 | INPUT_STYLE_CLASSIC | ClassicChatInputSection.kt + ClassicChatSettingsBar.kt，传统输入框+侧边设置栏 | 移除 |

智能体模式输入框(AgentChatInputSection)简化方案：

| 功能 | 简化方案 |
|------|---------|
| 文本输入框 | 保留 |
| 发送/排队/取消按钮 | 保留 |
| 模型选择器 | 弱化，改为灰色小字显示当前模型名，不使用框框住 |
| 配置按钮(Tune) | 移除显式按钮，改为斜杠命令触发 |
| 附件按钮(+) | 全部保留，但很大一部分直接注入上下文无需用户手动选择 |
| 全屏输入 | 待确认 |
| 回复引用 | 待确认 |
| 待发送队列 | 待确认 |
| 输入框样式配置 | 完全移除（透明、浮动、液态玻璃、水玻璃） |

### 悬浮窗系统

FloatingWindowManager 当前支持以下模式：

| 模式 | 说明 | 简化后状态 |
|------|------|-----------|
| 全屏模式 | 完整聊天界面 | 移除 |
| 窗口模式 | 可调整大小的浮动窗口 | 移除 |
| 球形模式 | 最小化球状图标 | 移除 |
| 语音球模式 | 语音交互球 | 移除（语音砍掉） |
| 结果展示模式 | 仅展示结果 | 保留（简化为黑色进度气泡） |

简化后悬浮窗：仅保留一个黑色气泡，悬浮在屏幕中间上部，展示进度和后台保活。

### web-chat模块

- 技术栈：React 19 + Vite + react-markdown
- 通信方式：通过HTTP API与Android端通信
- Android端：ExternalChatHttpServer 暴露HTTP服务
- 桥接层：WebChatHttpBridge -> WebChatActionBridge / WebChatManagementBridge / WebChatInputSettingsBridge
- 前端API：chatApi.ts 封装HTTP请求
- 简化方案：保留API层，移除Web UI（React组件）

### 需要砍除的代码模块清单

#### 角色卡系统
- `data/model/CharacterCard.kt` - 角色卡数据模型
- `data/model/CharacterGroupCard.kt` - 角色组卡模型
- `data/model/CharacterCardChatStats.kt` / `CharacterGroupChatStats.kt` - 统计
- `data/preferences/CharacterCardManager.kt` - 角色卡管理
- `data/preferences/CharacterGroupCardManager.kt` - 角色组管理
- `data/preferences/PersonaCardChatHistoryManager.kt` - 角色卡聊天历史
- `data/preferences/CharacterCardBilingualData.kt` - 双语数据
- `data/preferences/CharacterCardToolAccessResolver.kt` - 工具访问
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
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt` - Agent输入中的语音按钮
- `data/preferences/SpeechServicesPreferences.kt` - 语音偏好
- `data/preferences/WakeWordPreferences.kt` - 唤醒词偏好
- `services/assistant/OperitVoiceInteractionService.kt` - 语音交互服务
- `services/assistant/OperitVoiceInteractionSessionService.kt` - 语音交互会话

#### 对话框样式 (Bubble)
- `ui/features/chat/components/style/bubble/` - 整个气泡样式目录
- ThemeSettingsCoreSections.kt 中所有 bubble 相关配置项
- UserPreferencesManager 中所有 bubble 相关偏好字段

#### 经典输入模式 (Classic)
- `ui/features/chat/components/style/input/classic/` - 整个经典输入目录
- ClassicChatInputSection.kt, ClassicChatSettingsBar.kt

#### 命令框样式配置
- ThemeSettingsCoreSections.kt 中所有 cursor 相关配置项
- UserPreferencesManager 中 cursorLiquidGlass, cursorWaterGlass, cursorCustomColor 等偏好字段
- 输入框样式配置（透明、浮动、液态玻璃、水玻璃）

#### 深度搜索
- `examples/deepsearching/` - 深度搜索插件示例

#### 液态玻璃主题
- `ui/theme/liquidGlass` - 液态玻璃主题效果（DrawerContent中大量使用）
- 命令框样式中的液态玻璃/水玻璃配置项

---

## 待办事项

### 阶段一：需求对齐（进行中）

- [x] 初步需求收集
- [x] 整理需求到README
- [x] 深入研究项目代码结构
- [x] 确认聊天样式简化方案（保留命令框，移除对话框）
- [x] 确认输入框简化方案（保留智能体模式，移除经典模式）
- [x] 确认侧边栏重构方案（对齐主流AI应用，新建对话+预留按钮区+历史列表+底部设置/工具）
- [x] 确认工作流保留方案（完整保留，UI重构，与日历/待办同级入口）
- [x] 确认输入框简化方案（弱化模型选择为灰色小字，移除Tune按钮改用斜杠命令，附件全部保留但自动注入上下文）
- [x] 确认命令框样式配置全部移除
- [x] 确认MCP/Skill/包管理合并为一个页面
- [x] 确认工具箱全部保留但弱化入口
- [ ] 确认斜杠命令系统的具体命令列表
- [ ] 确认上下文自动注入的具体内容清单
- [ ] 确认设置页面每个子页面的具体简化方案
- [ ] 确认记忆库/对话历史的具体简化方案
- [ ] 确认MCP/Skill/包管理合并页面的具体设计
- [ ] 确认全屏输入、回复引用、待发送队列是否保留

### 阶段二：功能砍除

- [ ] 移除角色卡系统相关代码
- [ ] 移除桌宠+虚拟形象相关代码
- [ ] 移除语音交互相关代码
- [ ] 移除深度搜索相关代码
- [ ] 移除对话框(Bubble)样式及全部配置项
- [ ] 移除经典输入模式(Classic)及设置栏
- [ ] 移除命令框样式全部配置项
- [ ] 移除输入框样式配置（透明、浮动、液态玻璃、水玻璃）
- [ ] 简化悬浮窗功能（移除输入功能，仅保留进度气泡）
- [ ] 移除web-chat的Web UI（保留API）
- [ ] 清理砍除功能后的残留引用和资源

### 阶段三：UI简化

- [ ] 重构侧边栏（新建对话+预留按钮区+历史列表+底部设置/工具）
- [ ] 弱化模型选择为灰色小字
- [ ] 实现斜杠命令系统（输入/打开配置面板）
- [ ] 实现上下文自动注入（通知、屏幕使用时间等）
- [ ] 简化工具调用展示（灰/绿/红状态文字）
- [ ] 简化设置页面（保留模型+API、显示、权限、备份+关于）
- [ ] 权限级别重命名（基本/高级）
- [ ] 合并MCP/Skill/包管理为一个页面
- [ ] 简化备份恢复为一键操作
- [ ] 简化终端UI（移除配置项）
- [ ] 弱化工具箱入口
- [ ] 工作流UI重构

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
