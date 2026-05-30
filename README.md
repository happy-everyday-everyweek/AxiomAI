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
│       └── 设置按钮（工具箱入口移入设置页面）
└── SidebarInset → 主聊天区域
```

- 预留按钮区位于新建对话下方、对话历史列表上方
- 移除原有的快捷操作区（包管理、权限、工作流三个卡片）
- 移除AI功能分组（AI聊天、记忆库、助手配置）
- 移除底部快捷（关于、帮助、设置三个按钮）
- 原来分散在各处的功能入口整合到设置中
- 工具箱入口移入设置页面，不再作为侧边栏底部独立按钮
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
| 附件功能 | 自动注入（无需用户手动选择）：通知、屏幕内容、位置、屏幕使用时间、记忆（自动检索匹配合适的记忆）。手动选择：截图、拍照、文件、工具包 |
| 全屏输入 | 保留 |
| 回复引用 | 保留 |
| 待发送队列 | 保留 |
| 输入框样式配置 | 完全移除（透明、浮动、液态玻璃、水玻璃） |

#### 4.3 斜杠命令系统

新增斜杠命令交互方式，替代原来的Tune设置按钮：
- 用户在输入框中输入 `/` 时，弹出命令面板
- 参考shadcn/ui Command组件风格

| 命令 | 功能 |
|------|------|
| /think | 思考模式开关+质量级别 |
| /model | 模型选择 |
| /memory | 记忆选择+自动更新开关 |
| /tools | 工具开关+工具提示词管理 |
| /permission | 权限级别切换 |
| /context | 上下文长度配置 |
| /stream | 流式输出开关 |

#### 4.4 上下文自动注入

附件功能中，以下信息自动注入到对话上下文中，无需用户手动选择：

| 自动注入项 | 说明 |
|-----------|------|
| 通知 | 用户的通知信息自动注入 |
| 屏幕内容 | 当前屏幕内容自动注入 |
| 位置 | 用户位置信息自动注入 |
| 屏幕使用时间 | 屏幕使用时间自动注入 |
| 记忆 | 自动检索匹配合适的记忆并注入 |

手动选择的附件项：

| 手动选择项 | 说明 |
|-----------|------|
| 截图 | 用户手动截图 |
| 拍照 | 用户手动拍照 |
| 文件 | 用户手动选择文件 |
| 工具包 | 用户手动选择工具包 |

#### 4.5 设置页面简化

| 优化项 | 具体方案 |
|-------|---------|
| 保留的子页面 | 模型+API配置、显示设置、权限设置、备份+关于 |
| 权限级别 | 移除Root和ADB级别，名称改为"基本"和"高级"（高级=无障碍） |
| 每个页面 | 都要彻底简化，大部分配置由系统自动管理 |
| 工具箱入口 | 移入设置页面，不再作为侧边栏底部独立按钮 |

**模型配置页面简化** - 只保留4项：
| 保留项 | 说明 |
|-------|------|
| API URL | API服务地址 |
| API Key | API密钥 |
| 模型名称 | 使用的模型名称 |
| 温度 | 生成温度参数 |

移除的配置项（由系统自动管理）：Top P、最大token数、上下文长度、最大上下文长度、摘要开关/阈值、自定义请求头、高级设置

**主题/显示设置简化** - 只保留4项：
| 保留项 | 说明 |
|-------|------|
| 明暗模式 | 主题模式切换 |
| 用户名 | 全局用户名 |
| 权限级别 | 基本/高级（无障碍） |
| 智能体模式 | 多智能体协作（默认）/ 单智能体 |

移除的配置项（由系统自动管理）：主题色、聊天样式、头像、显示选项、字体、背景、工具折叠模式、FPS、通知、导航动画、后台保活、虚拟屏幕、截图设置、软件身份、Root执行模式

**终端UI简化** - 只保留：
| 保留项 | 说明 |
|-------|------|
| 命令输入 | 输入shell命令 |
| 命令执行 | 执行命令 |
| 结果展示 | 显示命令输出 |

移除的功能：历史记录、命令建议、预设命令、清除历史、重新执行、展开/收起

**备份恢复页面简化** - 只保留：
| 保留项 | 说明 |
|-------|------|
| 一键备份 | 导出所有数据 |
| 一键恢复 | 导入所有数据 |

移除的功能：角色卡导出/导入、记忆导出/导入、模型配置导出/导入、Room数据库备份/恢复、Raw快照备份/恢复、格式选择、导入策略选择

**关于页面简化** - 只保留：
| 保留项 | 说明 |
|-------|------|
| 应用名称+版本号 | 应用基本信息 |
| 开源许可 | 第三方库许可 |
| 更新日志 | 版本变更记录 |

**web-chat API保留** - 保留所有API端点，仅移除角色选择器相关端点（`/character-selector`）

#### 4.6 MCP/Skill/包管理合并

- 将MCP、Skill与包管理功能汇聚在一个页面入口中
- 不保留3个Tab，改为小标题展示（如"MCP服务器"、"Skill插件"、"工具包"三个小标题区域）
- 市场功能也合并在一起（MCP市场、Skill市场、提示词市场统一为一个市场入口）
- 完全兼容现有版本的包，需要将相关API接口进行深入的重定向
- 需要确保第三方沙箱包的兼容性

#### 4.7 记忆库简化

| 功能 | 简化方案 |
|------|---------|
| 搜索栏 | 保留 |
| 图谱可视化 | 保留但简化 |
| 文件夹导航 | 移除 |
| 浮动按钮（创建/导入/框选/链接） | 移除 |
| 框选模式 | 移除 |
| 链接模式 | 移除 |
| 记忆条目列表 | 保留 |
| 编辑已有记忆 | 保留修改功能 |
| 删除记忆 | 保留 |
| 导入导出 | 保留 |

#### 4.8 对话历史简化

| 功能 | 简化方案 |
|------|---------|
| 新建对话 | 保留 |
| 历史对话列表 | 保留 |
| 长按菜单（删除/编辑） | 保留 |
| 新建群组 | 移除UI，保留API（第三方沙箱包兼容） |
| 搜索对话 | 移除（角色卡已在删除清单中） |
| 显示模式设置 | 移除 |
| 自动切换角色卡/对话 | 移除（角色卡已在删除清单中） |
| 拖拽排序 | 移除 |
| 滑动删除 | 移除 |
| 对话分组 | 移除UI展示，保留API（第三方沙箱包兼容） |

#### 4.9 沙箱包API兼容

- 对话分组功能：移除UI展示，但保留相关API接口，确保第三方沙箱包可以调用
- MCP/Skill/包管理合并后，需要将相关API接口进行深入的重定向，确保现有沙箱包的兼容性
- 所有移除UI但保留API的功能，需要在API层面做好兼容标记

#### 4.10 系统默认值调整

简化后，以下配置项的默认值需要调整（由系统自动管理，用户无需手动配置）：

| 配置项 | 当前默认值 | 简化后默认值 | 说明 |
|-------|-----------|-------------|------|
| 后台保活 (enableBackgroundKeepAlive) | false | **true** | 非常重要！后台保活可以强化智能体的长程任务执行能力，简化后应默认开启 |
| 回复通知 (enableReplyNotification) | true | true | 保持不变 |
| 回车发送 (enableEnterToSend) | false | true | 简化后默认开启回车发送 |
| 导航动画 (enableNavigationAnimation) | true | true | 保持不变 |
| 虚拟显示 (enableExperimentalVirtualDisplay) | true | true | 保持不变 |
| 工具折叠模式 (toolCollapseMode) | MULTIPLE_READ_ONLY | MULTIPLE_READ_ONLY | 保持不变，系统自动管理 |
| 截图格式 | JPG | JPG | 保持不变 |
| 截图质量 | 75 | 75 | 保持不变 |
| 截图缩放 | 75% | 75% | 保持不变 |

#### 4.11 记忆检索匹配策略

当前代码中已实现的记忆检索评分体系（MemorySearchConfig）：

| 评分维度 | 当前默认权重 | 说明 |
|---------|-------------|------|
| keywordWeight | 10.0 | 关键词匹配权重 |
| tagWeight | 0.0 | 标签匹配权重 |
| vectorWeight | 0.0 | 向量嵌入相似度权重 |
| edgeWeight | 0.4 | 图谱边关联权重 |

评分模式（MemoryScoreMode）：
- BALANCED - 平衡模式
- KEYWORD_FIRST - 关键词优先
- SEMANTIC_FIRST - 语义优先

Memory 实体包含的评分相关字段：
- credibility: Float (0.0-1.0) - 可信度
- importance: Float (0.0-1.0) - 重要性
- embedding: Embedding? - 向量嵌入
- tags: ToMany<MemoryTag> - 标签
- links/backlinks: ToMany<MemoryLink> - 图谱关联（含weight字段）
- properties: ToMany<MemoryProperty> - 扩展属性

简化后的记忆自动检索方案：
- 触发时机：每次用户发送消息时自动检索 + AI判断需要记忆时也触发
- 最大注入条数：5条
- 匹配策略：综合评分（关键词+向量+标签+图谱边+重要性+可信度+时间衰减），具体权重待确认
- 需要进一步确认：各评分维度的最优权重配置

#### 4.12 整体原则

大部分规则由系统自动管理，用户不需要接触具体的规则和配置。

### 五、待进一步确认的提问清单

> 以下问题按模块分组，每个模块覆盖其核心文件的关键决策点。标注 [已确认] 的问题保留原有结论，标注 [待确认] 的问题需要逐一回复。

---

## 模块一：聊天核心系统

> 涉及文件：EnhancedAIService.kt, ChatRuntimeHolder.kt, ChatRuntimeSlot.kt, ChatViewModel.kt, AIMessageManager.kt, AIChatScreen.kt

#### C1: FLOATING运行时槽位的去留 [待确认]

ChatRuntimeSlot 定义了 MAIN 和 FLOATING 两种运行时槽位，ChatRuntimeHolder 为每种槽位维护独立的 ChatServiceCore 实例。悬浮窗简化为黑色进度气泡后，不再需要完整的浮动聊天运行时。
- A: 移除 FLOATING 槽位，只保留 MAIN，悬浮窗仅做进度展示不承载聊天逻辑
- B: 保留 FLOATING 槽位但简化其能力，仅用于后台任务状态追踪
- C: 保留双槽位不变，悬浮窗简化只影响UI层

#### C2: EnhancedAIService 中的跨会话同步逻辑 [待确认]

EnhancedAIService.syncMainChatSelectionToFloating() 在主聊天和浮动聊天之间同步选择状态。简化后是否需要保留此同步机制？
- A: 完全移除，浮动聊天不再存在
- B: 保留但改为单向（主→浮），仅同步关键状态
- C: 保留双向同步

#### C3: ChatViewModel 中的角色卡相关状态 [待确认]

ChatViewModel 中存在角色卡选择、角色卡绑定、角色卡记忆绑定等状态管理逻辑。砍掉角色卡后：
- A: 完全移除所有角色卡相关状态和方法
- B: 保留内部数据结构但移除UI暴露，确保沙箱包兼容

#### C4: AIMessageManager 中的语音朗读逻辑 [待确认]

AIMessageManager 包含语音朗读（TTS）相关的消息处理逻辑。砍掉语音后：
- A: 完全移除TTS相关逻辑
- B: 移除TTS逻辑但保留消息类型的扩展点，以备未来可能恢复

#### C5: AIMessageManager 中的流式输出与工具调用循环 [待确认]

当前 AIMessageManager 的流式输出逻辑与工具调用循环紧密耦合。简化后工具调用展示改为灰/绿/红文字，流式输出逻辑是否需要调整？
- A: 流式输出逻辑不变，仅UI层调整展示方式
- B: 流式输出需要增加工具调用状态的中间标记（灰→绿/红），以便UI层感知状态变化

#### C6: ChatViewModel 中的附件处理逻辑 [待确认]

当前附件处理支持手动选择（截图、拍照、文件、工具包）。简化后需要增加自动注入（通知、屏幕内容、位置、屏幕使用时间、记忆）。自动注入的附件是否在UI上对用户可见？
- A: 完全不可见，用户不知道有自动注入的内容
- B: 可见但标注为"自动注入"，用户可手动移除
- C: 在消息发送前显示自动注入的摘要，用户确认后发送

#### C7: AIChatScreen 中的角色选择器面板 [待确认]

AIChatScreen 中集成了 CharacterSelectorPanel。砍掉角色卡后，该面板需要移除。但面板的位置是否需要替换为其他功能入口？
- A: 完全移除，不替换
- B: 替换为斜杠命令的快捷入口面板
- C: 替换为模型/智能体模式切换面板

#### C8: ChatViewModel 中的对话摘要触发机制 [待确认]

当前对话摘要有多种触发方式（手动、自动阈值触发）。简化后摘要配置项被移除，摘要行为由系统自动管理。系统自动管理的策略是什么？
- A: 固定阈值（如上下文超过一定token数时自动摘要）
- B: 动态阈值（根据模型上下文长度自动计算）
- C: 完全不摘要，依赖模型的上下文窗口

#### C9: ChatRuntimeHolder 中多实例的内存管理 [待确认]

ChatRuntimeHolder 使用 ConcurrentHashMap 缓存多个 ChatServiceCore 实例。简化后如果移除 FLOATING 槽位，是否需要优化内存管理策略？
- A: 简化为单实例，移除缓存机制
- B: 保留缓存机制但限制最大实例数为1
- C: 保持不变

#### C10: AIMessageManager 中的消息历史限制 [待确认]

AIMessageManager 在构建AI请求时有消息历史限制逻辑。简化后上下文长度由系统自动管理，具体的限制策略是什么？
- A: 根据当前模型的maxTokens自动计算，保留最近N条消息
- B: 固定保留最近50条消息
- C: 保留全部消息，由模型端自行截断

#### C11: ChatViewModel 中的待发送队列 [待确认]

当前有待发送队列功能，用户可以在AI回复期间排队发送多条消息。简化后此功能的行为是否调整？
- A: 保持不变
- B: 简化为只允许排队1条消息
- C: 移除排队功能，AI回复期间禁止发送

#### C12: AIChatScreen 中的消息编辑功能 [待确认]

当前支持编辑已发送的消息。简化后是否保留？
- A: 保留，功能不变
- B: 保留但简化UI（移除编辑弹窗的某些选项）
- C: 移除

#### C13: ChatScreenContent 中的多选模式 [待确认]

当前支持多选消息进行删除/分享/导出。简化后是否保留？
- A: 保留，功能不变
- B: 保留但仅支持删除，移除分享和导出
- C: 移除多选模式

#### C14: AIMessageManager 中的插件处理逻辑 [待确认]

AIMessageManager 在处理AI响应时涉及插件（ToolPkgHook）的回调。简化后插件系统是否保持不变？
- A: 保持不变，沙箱包兼容性优先
- B: 简化插件回调机制，移除部分钩子类型

#### C15: ChatViewModel 中的回复引用功能 [待确认]

当前支持引用某条消息进行回复。简化后此功能的具体行为？
- A: 保持不变
- B: 保留但简化UI展示

---

## 模块二：系统提示词与工具提示词

> 涉及文件：SystemPromptConfig.kt, SystemToolPrompts.kt, SystemToolPromptsInternal.kt, FunctionalPrompts.kt

#### P1: SystemPromptConfig 中的自我介绍Section与内置人格 [待确认]

已确认保留多智能体协作人格（Axiom/Atlas/Vigil/Flux/Aura）和单智能体模式。具体问题：多智能体协作人格的提示词是硬编码在SystemPromptConfig中，还是通过PromptTag系统动态注入？
- A: 硬编码在SystemPromptConfig中，作为内置常量
- B: 通过PromptTag系统注入，CHARACTER类型标签
- C: 混合方式：基础人格硬编码，扩展人格通过标签注入

#### P2: SystemPromptConfig 中的自定义提示词应用方式 [待确认]

applyCustomPrompts 方法允许用户自定义提示词覆盖系统默认。简化后用户是否还能自定义提示词？
- A: 完全移除自定义提示词功能
- B: 保留但仅通过斜杠命令间接配置（如/think影响思考模式提示词）
- C: 保留完整的自定义提示词功能

#### P3: SystemToolPrompts 中的语音工具提示词清理范围 [待确认]

SystemToolPrompts 中包含TTS/STT等语音工具的提示词描述。砍掉语音后需要移除这些描述。具体需要确认：移除的是哪些工具名称？
- A: 仅移除TTS和STT工具
- B: 移除TTS、STT、语音唤醒、语音球相关所有工具
- C: 移除所有语音相关工具，包括语音自动绑定组件的工具描述

#### P4: SystemToolPrompts 中的工具分类结构 [待确认]

当前工具分为basicTools、fileSystemTools、httpTools、memoryTools等分类。简化后是否调整分类？
- A: 保持当前分类不变
- B: 合并部分分类（如fileSystemTools和httpTools合并为"数据工具"）
- C: 重新设计分类体系

#### P5: SystemToolPromptsInternal 中的内部工具 [待确认]

SystemToolPromptsInternal 定义了内部使用的工具提示词。简化后这些内部工具是否保留？
- A: 全部保留，内部工具对用户不可见不影响简化
- B: 审查并移除不再需要的内部工具

#### P6: FunctionalPrompts 中的群聊角色响应排序提示词 [待确认]

FunctionalPrompts 包含群聊角色响应排序的提示词模板。砍掉角色卡后群聊功能也移除，该提示词是否清理？
- A: 完全移除
- B: 保留但标记为deprecated，确保沙箱包兼容

#### P7: FunctionalPrompts 中的UI自动化提示词 [待确认]

FunctionalPrompts 包含UI控制器提示词（uiAutomationAgentPrompt）。该提示词与PhoneAgent配合使用。简化后PhoneAgent是否保留？
- A: 保留PhoneAgent和UI自动化提示词
- B: 移除PhoneAgent但保留提示词模板（沙箱包可能使用）
- C: 全部移除

#### P8: FunctionalPrompts 中的摘要提示词 [待确认]

FunctionalPrompts 包含对话摘要提示词（SUMMARY_PROMPT）。简化后摘要由系统自动管理，摘要提示词是否需要调整？
- A: 保持不变
- B: 简化摘要提示词，减少输出格式要求
- C: 根据自动管理策略重新设计摘要提示词

#### P9: SystemPromptConfig 中的包系统指南Section [待确认]

PACKAGE_SYSTEM_GUIDELINES_SECTION 向AI说明如何使用沙箱包系统。MCP/Skill/包管理合并后，该Section的描述是否需要更新？
- A: 需要更新，反映合并后的统一管理方式
- B: 不需要更新，包系统内部逻辑未变，只是UI合并

#### P10: SystemPromptConfig 中的工作区规则Section [待确认]

WORKSPACE_GUIDELINES_SECTION 向AI说明工作区规则。简化后工作区功能是否保留？
- A: 保留工作区功能，Section不变
- B: 保留但简化Section内容
- C: 移除工作区功能

#### P11: SystemToolPrompts 中的工具可见性控制 [待确认]

SystemToolPrompts 通过 toolVisibility 参数控制工具显示。简化后是否需要调整工具可见性策略？
- A: 保持当前策略不变
- B: 默认隐藏更多工具，只显示核心工具
- C: 根据权限级别动态调整可见性

#### P12: FunctionalPrompts 中的知识图谱处理提示词 [待确认]

FunctionalPrompts 包含知识图谱处理的提示词。记忆库简化后图谱可视化只读，知识图谱处理提示词是否调整？
- A: 保持不变，图谱处理逻辑不受UI简化影响
- B: 简化提示词，移除图谱编辑相关的指导

---

## 模块三：工具系统

> 涉及文件：ToolRegistration.kt, AIToolHandler.kt, AIToolHook.kt, ToolExecutionLimits.kt, ToolProgressBus.kt, MCPToolExecutor.kt, SkillManager.kt, PackageManagerToolPkgFacade.kt, ToolPkgParser.kt, ToolPkgComposeDslParser.kt

#### T1: ToolRegistration 中的语音工具注册 [待确认]

ToolRegistration 注册了所有可用工具，包括语音相关工具。砍掉语音后需要移除哪些工具的注册？
- A: 仅移除TTS/STT工具注册
- B: 移除所有语音相关工具（TTS/STT/语音唤醒/语音球）
- C: 移除语音工具注册但保留工具名称常量，确保沙箱包兼容

#### T2: ToolRegistration 中的角色卡管理工具 [待确认]

StandardChatManagerTool 中包含角色卡相关的聊天管理功能（切换角色卡、群聊管理等）。砍掉角色卡后：
- A: 完全移除角色卡相关工具方法
- B: 移除UI暴露但保留工具注册，确保沙箱包兼容
- C: 保留但标记为deprecated

#### T3: AIToolHandler 中的工具权限系统 [待确认]

AIToolHandler 通过 ToolGetter 根据权限级别过滤可用工具。权限级别重命名为"基本/高级"后，工具过滤逻辑是否需要调整？
- A: 仅重命名，逻辑不变
- B: 调整过滤规则，高级权限下也隐藏部分敏感工具
- C: 重新设计权限过滤策略

#### T4: AIToolHandler 中的工具钩子机制 [待确认]

AIToolHandler 支持 AIToolHook 钩子机制，用于监听工具执行过程。简化后钩子机制是否保留？
- A: 完全保留，沙箱包依赖此机制
- B: 简化钩子类型，移除不常用的钩子事件

#### T5: ToolExecutionLimits 中的限制值调整 [待确认]

ToolExecutionLimits 定义了工具执行的限制（最大文件读取字节数、最大文本结果长度等）。简化后是否需要调整这些限制值？
- A: 保持不变
- B: 适当放宽限制，提升用户体验
- C: 收紧限制，减少资源消耗

#### T6: ToolProgressBus 中的进度广播机制 [待确认]

ToolProgressBus 用于广播工具执行进度。简化后工具调用展示为一行文字，进度广播机制是否还需要？
- A: 保留，进度信息用于悬浮窗气泡展示
- B: 简化，只广播开始/完成/失败三种状态
- C: 移除，工具调用展示不需要进度

#### T7: MCPToolExecutor 的错误处理策略 [待确认]

MCPToolExecutor 处理MCP工具调用时的错误处理。简化后错误信息如何展示给用户？
- A: 工具调用行变红，用户可点击查看详细错误
- B: 工具调用行变红，不提供详细错误信息
- C: 工具调用行变红后自动变回灰色，错误信息记录到日志

#### T8: SkillManager 中的技能扫描与加载 [待确认]

SkillManager 负责技能的扫描、加载和导入。MCP/Skill/包管理合并后，SkillManager 的内部逻辑是否需要调整？
- A: 保持不变，合并只影响UI层
- B: 调整技能加载流程，与MCP和包管理统一入口

#### T9: PackageManagerToolPkgFacade 中的UI路由查询 [待确认]

PackageManagerToolPkgFacade 提供工具包的UI路由和导航入口查询。合并后原来的独立路由需要重定向：
- A: 保持原有路由不变，新增统一页面路由
- B: 原有路由重定向到统一页面
- C: 移除原有路由，只保留统一页面路由

#### T10: ToolPkgParser 中的manifest解析 [待确认]

ToolPkgParser 解析工具包的manifest文件。简化后manifest格式是否需要调整？
- A: 保持不变，确保向后兼容
- B: 支持新格式但兼容旧格式

#### T11: ToolPkgComposeDslParser 的DSL能力范围 [待确认]

ToolPkgComposeDslParser 解析Compose DSL UI定义。简化后DSL支持的能力是否需要限制？
- A: 保持不变，沙箱包需要完整的DSL能力
- B: 限制部分DSL能力（如移除液态玻璃相关DSL）

#### T12: AIToolHandler 中的工具调用计数与限制 [待确认]

当前有工具调用次数限制（每轮对话最大工具调用次数）。简化后限制策略是否调整？
- A: 保持不变
- B: 放宽限制，允许更多工具调用
- C: 收紧限制，减少资源消耗

#### T13: ToolRegistration 中的终端工具 [待确认]

ToolRegistration 注册了终端相关工具。终端UI简化后，终端工具的注册和能力是否调整？
- A: 工具注册不变，仅UI简化
- B: 移除终端配置相关工具，保留基本执行能力

#### T14: MCPToolExecutor 与MCPBridge的通信方式 [待确认]

MCPToolExecutor 通过MCPBridge与外部MCP服务通信。简化后通信方式是否调整？
- A: 保持不变
- B: 优化通信协议，减少延迟

---

## 模块四：记忆库系统

> 涉及文件：MemoryLibrary.kt, MemoryRepository.kt, Memory.kt, MemorySearchConfig.kt, MemorySearchSettingsPreferences.kt, Embedding.kt, EmbeddingConverter.kt

#### M1: MemoryLibrary 中的自动分类功能 [待确认]

MemoryLibrary 有 autoCategorizeMemories() 自动分类记忆到文件夹的功能。简化后移除了文件夹导航UI，自动分类功能是否保留？
- A: 保留自动分类逻辑，虽然UI不展示文件夹但数据层保留分类信息
- B: 移除自动分类逻辑，所有记忆平铺存储
- C: 保留分类但简化为自动标签，不使用文件夹层级

#### M2: Memory 实体中的文档节点功能 [待确认]

Memory 支持 documentPath/isDocumentNode/documentChunks，这是文档嵌入功能。是否保留？
- A: 保留，文档嵌入是有用功能
- B: 移除，简化记忆库
- C: 保留数据模型但移除UI入口

#### M3: Memory 实体中的文件夹路径字段 [待确认]

Memory 有 folderPath 字段用于文件夹分类。移除文件夹导航UI后，该字段是否保留？
- A: 保留字段，数据层不变
- B: 移除字段，所有记忆不再有文件夹归属

#### M4: MemoryLibrary 中的自动保存触发时机 [待确认]

当前 MemoryLibrary 有自动保存功能。简化后自动保存的触发时机是否调整？
- A: 保持不变（AI判断需要保存时触发 + 定时自动保存）
- B: 仅保留AI判断触发，移除定时自动保存
- C: 仅保留定时自动保存，移除AI判断触发

#### M5: 记忆检索评分模式 [待重新提问]

已确认权重配置为语义优先（keywordWeight=5.0, tagWeight=2.0, vectorWeight=8.0, edgeWeight=1.0）。评分模式选择哪种？
- A: BALANCED（平衡模式）
- B: KEYWORD_FIRST（关键词优先）
- C: SEMANTIC_FIRST（语义优先）
- D: 新增混合模式（根据查询类型自动切换）

#### M6: 记忆自动注入的最大条数动态调整 [待确认]

已确认最多5条。是否需要根据上下文剩余空间动态调整注入条数？
- A: 固定5条，不动态调整
- B: 动态调整，上下文空间不足时减少注入条数
- C: 固定5条，但如果检索到的记忆相关性都很低则不注入

#### M7: Embedding 向量嵌入的维度管理 [待确认]

Embedding 实体包含嵌入维度和向量值。不同嵌入模型产生不同维度的向量。简化后如何处理维度不一致的问题？
- A: 固定使用一种嵌入模型，确保维度一致
- B: 支持多维度，检索时只比较同维度的向量
- C: 切换嵌入模型时重新计算所有向量

#### M8: MemorySearchSettingsPreferences 中的配置项 [待确认]

MemorySearchSettingsPreferences 管理记忆检索的参数配置。简化后这些配置是否对用户可见？
- A: 完全隐藏，系统自动管理
- B: 通过斜杠命令 /memory 暴露部分配置
- C: 保留在设置页面但简化

#### M9: 记忆库的图谱关联（links/backlinks） [待确认]

Memory 实体有 links 和 backlinks 关联。图谱可视化简化为只读后，关联的创建方式是否调整？
- A: 保留AI自动创建关联，用户不可手动创建
- B: 保留AI自动创建，同时保留API供沙箱包使用
- C: 移除关联创建功能

#### M10: 记忆库的标签系统 [待确认]

Memory 有 tags 关联（MemoryTag）。简化后标签系统是否保留？
- A: 保留，标签用于检索评分
- B: 保留但移除用户手动管理标签的UI
- C: 移除标签系统

#### M11: Memory 实体中的 properties 扩展属性 [待确认]

Memory 有 properties 扩展属性（MemoryProperty）。简化后是否保留？
- A: 保留，扩展属性对沙箱包有用
- B: 移除，简化数据模型

#### M12: 记忆库的导入导出格式 [待确认]

当前记忆库支持导入导出。简化后导入导出格式是否调整？
- A: 保持不变
- B: 简化为一键导出JSON，一键导入JSON
- C: 合并到全局一键备份/恢复中，不再单独提供

#### M13: 记忆库的 credibility 和 importance 字段 [待确认]

已确认纳入评分但低权重（importance=1.0, credibility=0.5）。这两个字段的值由谁设置？
- A: 完全由AI在保存记忆时自动设置
- B: AI设置默认值，用户可手动调整
- C: 系统根据记忆来源和使用频率自动计算

#### M14: 记忆检索的时间衰减因子 [待确认]

已确认时间衰减作为一项权重因子。具体的衰减公式是什么？
- A: 线性衰减（距离现在越久分数越低）
- B: 指数衰减（近期记忆权重远高于远期）
- C: 阶梯衰减（今天/本周/本月/更早，不同阶梯不同权重）

#### M15: 记忆库的搜索功能 [待确认]

简化后保留搜索栏。搜索是仅搜索标题和内容，还是也搜索标签和属性？
- A: 仅搜索标题和内容
- B: 搜索标题、内容和标签
- C: 全文搜索（包括属性和关联信息）

---

## 模块五：工作流系统

> 涉及文件：Workflow.kt, WorkflowExecutor.kt, WorkflowScheduler.kt, WorkflowWorker.kt

#### W1: 工作流触发机制 [待确认]

当前工作流支持定时触发(cron)和语音唤醒触发。语音砍掉后：
- A: 只保留定时触发
- B: 保留定时触发 + 事件触发（如应用启动时、收到通知时等）
- C: 保留定时触发 + 事件触发 + 意图触发（如外部Intent）

#### W2: WorkflowExecutor 中的节点类型 [待确认]

Workflow 支持触发、执行、条件、逻辑和提取等节点类型。简化后是否需要调整节点类型？
- A: 保持不变
- B: 移除语音相关节点类型
- C: 新增自动注入相关节点类型

#### W3: WorkflowScheduler 的调度方式 [待确认]

WorkflowScheduler 使用 WorkManager 管理定时调度。简化后调度方式是否调整？
- A: 保持不变
- B: 增加更灵活的调度选项（如仅WiFi下执行、仅充电时执行）

#### W4: 工作流UI重构的范围 [待确认]

工作流UI需要重构。重构的范围是什么？
- A: 仅调整入口位置（移至侧边栏预留按钮区），内部UI不变
- B: 调整入口 + 简化工作流编辑器UI
- C: 全面重构工作流UI，参考shadcn/ui风格

#### W5: 工作流的执行日志 [待确认]

Workflow 有 executionLog（WorkflowExecutionLog）。简化后执行日志是否保留？
- A: 保留，用户可查看历史执行记录
- B: 保留但简化，只记录成功/失败状态
- C: 移除执行日志

#### W6: 工作流与记忆库的交互 [待确认]

工作流执行过程中是否可以读写记忆库？
- A: 保留完整的记忆库读写能力
- B: 仅保留读取能力
- C: 移除工作流与记忆库的交互

#### W7: 工作流的错误处理策略 [待确认]

WorkflowExecutor 执行工作流时遇到错误如何处理？
- A: 停止执行并通知用户
- B: 跳过错误节点继续执行
- C: 根据节点类型决定（条件节点跳过，执行节点停止）

#### W8: 工作流模板系统 [待确认]

ToolPkgTemplateModels 定义了工作流模板。简化后模板系统是否保留？
- A: 保留，用户可从模板创建工作流
- B: 保留但移除模板市场UI
- C: 移除模板系统

#### W9: 工作流的后台执行限制 [待确认]

后台保活默认开启后，工作流在后台执行是否有时间限制？
- A: 无限制，依赖后台保活持续执行
- B: 设置最大执行时间（如30分钟）
- C: 根据工作流复杂度动态设置限制

#### W10: 工作流与沙箱包的集成 [待确认]

工作流是否可以通过沙箱包扩展？
- A: 保留完整的沙箱包集成能力
- B: 保留但限制沙箱包可调用的工作流操作
- C: 移除沙箱包与工作流的集成

---

## 模块六：数据模型与偏好管理

> 涉及文件：CharacterCardManager.kt, ModelConfigManager.kt, ApiPreferences.kt, DisplayPreferencesManager.kt, PromptTagManager.kt, ActivePromptManager.kt, PromptVersionManager.kt, FunctionalConfigManager.kt, ExternalHttpApiPreferences.kt

#### D1: CharacterCardManager 的清理策略 [待确认]

CharacterCardManager 是角色卡系统的核心管理器，涉及1360行代码。砍掉角色卡后的清理策略：
- A: 完全删除文件及所有引用
- B: 保留数据模型但移除管理逻辑，确保数据库迁移兼容
- C: 保留最小化的管理器，仅用于数据迁移

#### D2: 角色卡数据迁移方案 [待重新提问]

已有用户可能保存了角色卡数据。砍掉角色卡后这些数据如何处理？
- A: 静默丢弃，不提供迁移
- B: 将角色卡的设定文本提取为PromptTag（CHARACTER类型），保留用户数据
- C: 提供一次性迁移提示，让用户选择保留哪些设定

#### D3: ModelConfigManager 中的配置项简化 [待确认]

已确认模型配置只保留4项（API URL、API Key、模型名称、温度）。ModelConfigManager 中其他配置项（Top P、最大token数、上下文长度等）如何处理？
- A: 完全移除字段和逻辑，由系统自动计算
- B: 保留字段但移除UI入口，使用智能默认值
- C: 保留字段和逻辑，通过斜杠命令可访问

#### D4: ApiPreferences 中的多API Key管理 [待确认]

ApiKeyInfo 支持多API Key管理。简化后是否保留多Key功能？
- A: 保留，多Key有助于负载均衡和容错
- B: 简化为单Key
- C: 保留多Key但简化UI

#### D5: DisplayPreferencesManager 中的配置项清理 [待确认]

DisplayPreferencesManager 管理FPS显示、通知开关、截图设置等。简化后哪些配置项需要移除？
- A: 仅移除与砍掉功能相关的配置（如液态玻璃、虚拟形象相关）
- B: 移除所有用户可配置的显示偏好，全部由系统自动管理
- C: 保留核心配置（明暗模式、通知开关），移除其他

#### D6: PromptTagManager 中的CHARACTER类型标签 [待确认]

已确认CHARACTER类型标签保留但对用户不可见。具体行为：
- A: 保留标签类型，用户不可创建/编辑CHARACTER标签，但系统内置标签仍注入提示词
- B: 移除CHARACTER标签类型，角色设定完全通过硬编码人格实现
- C: 将CHARACTER标签转换为FUNCTION类型，统一管理

#### D7: ActivePromptManager 的简化 [待确认]

ActivePromptManager 管理当前激活的提示词（角色卡或角色组）。砍掉角色卡后：
- A: 完全移除，激活提示词改为固定使用内置人格
- B: 简化为仅管理智能体模式（多智能体/单智能体）的切换
- C: 保留但仅用于沙箱包的提示词管理

#### D8: PromptVersionManager 的保留 [待确认]

PromptVersionManager 管理提示词版本控制。简化后是否保留版本管理？
- A: 保留，版本管理有助于系统升级时自动更新提示词
- B: 移除，简化提示词管理逻辑

#### D9: FunctionalConfigManager 中的功能-模型映射 [待确认]

FunctionalConfigManager 管理不同功能类型与模型配置的映射。简化后是否保留？
- A: 保留，不同功能可能需要不同模型配置
- B: 移除，所有功能使用同一模型配置
- C: 保留但简化为仅支持搜索功能使用不同模型

#### D10: ExternalHttpApiPreferences 中的安全配置 [待确认]

ExternalHttpApiPreferences 管理外部HTTP API的启用状态、端口和访问令牌。简化后安全配置是否调整？
- A: 保持不变
- B: 默认启用访问令牌，增强安全性
- C: 移除端口配置，使用固定端口

#### D11: CharacterGroupCardManager 的清理 [待确认]

CharacterGroupCardManager 管理角色组卡。砍掉角色卡后是否与CharacterCardManager一起清理？
- A: 是，一起完全删除
- B: 保留数据模型用于数据库迁移兼容

#### D12: PersonaCardChatHistoryManager 的清理 [待确认]

PersonaCardChatHistoryManager 管理角色卡聊天历史。砍掉角色卡后聊天历史如何处理？
- A: 完全删除，角色卡绑定的聊天历史转为普通聊天
- B: 保留历史数据但移除角色卡关联

#### D13: CharacterCardToolAccessResolver 的清理 [待确认]

CharacterCardToolAccessResolver 解析角色卡的工具访问配置。砍掉角色卡后工具访问如何控制？
- A: 完全移除，工具访问仅由权限级别控制
- B: 保留工具访问控制逻辑但改为全局配置

#### D14: ModelConfigData 中的 providerType [待确认]

ModelConfigData 包含 providerType 字段标识API提供商。简化后是否保留多提供商支持？
- A: 保留，用户可能使用不同提供商
- B: 简化为仅支持OpenAI兼容API
- C: 保留但简化提供商列表

#### D15: CloudEmbeddingConfig 的保留 [待确认]

CloudEmbeddingConfig 管理云端嵌入API配置。已确认优先本地嵌入。云端嵌入配置是否保留？
- A: 保留作为回退方案
- B: 移除，仅支持本地嵌入
- C: 保留但隐藏配置UI

---

## 模块七：UI系统

> 涉及文件：OperitScreens.kt, DrawerContent.kt, AgentChatInputSection.kt, SettingsScreen.kt, FloatingWindowDelegate.kt, ChatHistorySelector.kt, AttachmentSelector.kt, NavItem.kt, AppRouteCatalog.kt

#### U1: OperitScreens 中的Screen路由清理 [待确认]

OperitScreens 定义了所有Screen路由。砍掉功能后需要移除的Screen路由如何处理？
- A: 完全移除路由定义和对应的Composable
- B: 保留路由定义但重定向到主页面
- C: 移除路由定义但保留Composable供沙箱包使用

#### U2: DrawerContent 的侧边栏重构细节 [待确认]

侧边栏重构为新建对话+预留按钮区+历史列表+底部设置。历史列表的加载策略是什么？
- A: 一次性加载全部历史对话
- B: 分页加载，滚动到底部时加载更多
- C: 仅加载最近20条，搜索时加载更多

#### U3: AgentChatInputSection 中的语音按钮移除 [待确认]

AgentChatInputSection 中有语音输入按钮。砍掉语音后该按钮位置如何处理？
- A: 完全移除，不替换
- B: 替换为斜杠命令按钮
- C: 替换为附件按钮

#### U4: AgentChatInputSection 中的Tune按钮移除 [待确认]

已确认移除Tune按钮，改为斜杠命令触发。Tune按钮的位置是否替换？
- A: 完全移除，不替换
- B: 替换为斜杠命令快捷按钮
- C: 替换为模型切换按钮

#### U5: 斜杠命令面板的实现方式 [待确认]

已确认参考shadcn/ui Command组件。斜杠命令面板是独立组件还是嵌入输入框？
- A: 独立弹出面板（类似VS Code命令面板）
- B: 嵌入输入框上方（类似shadcn/ui Command）
- C: 底部弹出Sheet

#### U6: SettingsScreen 的分组重构 [待确认]

已确认设置页面简化为4个子页面。设置页面的导航方式是什么？
- A: 列表式（点击进入子页面）
- B: Tab式（顶部标签切换）
- C: 折叠式（同一页面内折叠展开）

#### U7: FloatingWindowDelegate 的简化 [待确认]

悬浮窗简化为黑色进度气泡后，FloatingWindowDelegate 的逻辑如何调整？
- A: 完全重写，仅保留进度展示逻辑
- B: 保留框架但移除输入和交互逻辑
- C: 移除FloatingWindowDelegate，新建简单的进度气泡组件

#### U8: ChatHistorySelector 中的分组功能 [待确认]

已确认分组保留API移除UI。ChatHistorySelector 中分组相关的UI代码如何处理？
- A: 完全移除分组UI代码
- B: 隐藏分组UI但保留代码，确保沙箱包兼容
- C: 保留分组UI但简化为仅显示分组名称

#### U9: AttachmentSelector 中的自动注入UI [待确认]

自动注入的附件（通知、屏幕内容等）在AttachmentSelector中如何体现？
- A: 不在AttachmentSelector中体现，完全透明注入
- B: 在AttachmentSelector中显示自动注入项的开关
- C: 在AttachmentSelector中显示自动注入项的状态（已注入/未注入）

#### U10: NavItem 的清理 [待确认]

砍掉功能后需要移除的NavItem（如AssistantConfig、Help等）如何处理？
- A: 完全移除NavItem定义
- B: 保留但标记为deprecated
- C: 移除UI暴露但保留枚举值

#### U11: 工具调用展示的点击交互 [待确认]

已提出工具调用展示简化为一行灰/绿/红文字。用户点击该文字后的行为：
- A: 可点击展开查看工具调用的详细参数和结果
- B: 不可点击，只显示一行状态文字
- C: 长按可查看详情，短按无反应

#### U12: 模型选择的弱化展示 [待确认]

已确认弱化模型选择为灰色小字。点击该灰色小字后的行为：
- A: 打开模型选择面板
- B: 打开斜杠命令 /model
- C: 打开模型配置设置页面

#### U13: 帮助页面的去留 [待确认]

当前有Help页面。简化后是否保留？
- A: 保留，帮助信息对用户有价值
- B: 移除，帮助信息整合到设置页面的关于部分
- C: 移除独立页面，改为首次使用引导

#### U14: 市场页面的统一 [待确认]

MCP市场、Skill市场、提示词市场合并为一个市场入口。市场页面的展示方式：
- A: Tab切换（MCP/Skill/提示词三个Tab）
- B: 瀑布流混合展示，标签区分类型
- C: 搜索优先，首页展示推荐，搜索结果按类型过滤

#### U15: 标签市场的去留 [待确认]

TagMarket 提供预设标签的浏览和安装。简化后是否保留？
- A: 保留但简化UI
- B: 移除，标签由系统自动管理
- C: 保留但合并到统一市场中

---

## 模块八：后台服务系统

> 涉及文件：AIForegroundService.kt, ForegroundServiceCompat.kt, ActivityLifecycleManager.kt, OperitApplication.kt

#### S1: 后台保活默认开启后的通知行为 [待重新提问]

后台保活默认开启后，前台服务通知如何展示？
- A: 常驻通知栏，显示"Operit AI正在运行"
- B: 常驻通知栏，显示当前AI任务进度
- C: 仅在有活跃AI任务时显示通知，空闲时隐藏

#### S2: AIForegroundService 中的唤醒监听清理 [待确认]

AIForegroundService 包含 startWakeListeningLocked() 和 startPersonalWakeListening() 唤醒监听。砍掉语音后：
- A: 完全移除唤醒监听逻辑
- B: 保留唤醒监听框架但移除语音唤醒实现
- C: 保留但改为其他触发方式（如蓝牙耳机按键）

#### S3: AIForegroundService 中的外部HTTP服务管理 [待确认]

AIForegroundService 管理外部HTTP聊天服务的启停。简化后是否调整？
- A: 保持不变
- B: 将HTTP服务管理移至独立组件
- C: 默认不启动HTTP服务，用户手动启用

#### S4: ForegroundServiceCompat 的兼容性 [待确认]

ForegroundServiceCompat 处理不同Android版本的前台服务启动。简化后是否需要更新兼容逻辑？
- A: 保持不变
- B: 更新以适配Android 14+的前台服务类型限制
- C: 简化，移除旧版本兼容代码

#### S5: ActivityLifecycleManager 中的虚拟屏幕状态清理 [待确认]

ActivityLifecycleManager 在应用前后台切换时管理虚拟屏幕和Shower状态。简化后虚拟屏幕相关逻辑是否保留？
- A: 保留，PhoneAgent依赖虚拟屏幕
- B: 移除，虚拟屏幕功能不再需要
- C: 保留但简化状态管理

#### S6: OperitApplication 中的初始化顺序 [待确认]

OperitApplication 在启动时初始化多个组件。简化后是否需要调整初始化顺序或移除部分初始化？
- A: 移除角色卡、语音、虚拟形象相关的初始化
- B: 仅移除初始化，保留延迟加载机制
- C: 全面优化初始化流程，提升启动速度

#### S7: 后台保活与电池优化的冲突 [待确认]

后台保活默认开启可能与系统电池优化冲突。如何处理？
- A: 引导用户将应用加入电池优化白名单
- B: 自动请求忽略电池优化
- C: 不处理，由用户自行管理

#### S8: AIForegroundService 中的悬浮窗控制 [待确认]

AIForegroundService 控制悬浮窗的显示。悬浮窗简化后控制逻辑如何调整？
- A: 简化为仅控制进度气泡的显示/隐藏
- B: 完全重写悬浮窗控制逻辑
- C: 保留控制框架但移除模式切换逻辑

#### S9: 前台服务的停止条件 [待确认]

当前前台服务在什么条件下停止？简化后停止条件是否调整？
- A: AI任务完成后自动停止
- B: 用户手动停止
- C: AI任务完成后延迟一段时间（如5分钟）再停止，避免频繁启停

#### S10: OperitApplication 中的全局异常处理 [待确认]

OperitApplication 是否有全局异常处理机制？简化后是否需要增强？
- A: 保持当前异常处理不变
- B: 增加全局异常捕获和日志记录
- C: 增加用户友好的错误提示

---

## 模块九：Web-Chat与外部API

> 涉及文件：ExternalChatHttpServer.kt, WebChatHttpBridge.kt, ExternalHttpApiPreferences.kt, web-chat/前端模块

#### E1: WebChatHttpBridge 中的角色选择器API [待确认]

已确认移除 /character-selector API。移除后该端点是返回404还是重定向？
- A: 返回404
- B: 返回空响应（200 OK，空数据）
- C: 重定向到模型选择器API

#### E2: WebChatHttpBridge 中的聊天组管理API [待确认]

已确认聊天分组保留API移除UI。/api/web/chat-groups API是否保留？
- A: 保留，沙箱包可能依赖
- B: 移除，聊天分组功能完全废弃
- C: 保留但标记为deprecated

#### E3: ExternalChatHttpServer 的认证机制 [待确认]

ExternalChatHttpServer 支持访问令牌认证。简化后认证机制是否调整？
- A: 保持不变
- B: 默认启用认证，增强安全性
- C: 增加IP白名单功能

#### E4: web-chat前端模块的移除范围 [待确认]

已确认移除Web UI保留API。web-chat目录下的React组件是否完全删除？
- A: 完全删除web-chat/src/下的所有React组件
- B: 保留chatApi.ts等API封装，移除UI组件
- C: 保留最小化的健康检查页面

#### E5: WebChatHttpBridge 中的流式响应 [待确认]

WebChatHttpBridge 支持流式响应。简化后流式响应是否保留？
- A: 保留，外部调用者依赖流式响应
- B: 移除，改为轮询方式
- C: 保留但优化流式响应的性能

#### E6: ExternalChatHttpServer 的端口配置 [待确认]

当前端口通过ExternalHttpApiPreferences配置。简化后是否固定端口？
- A: 保持可配置
- B: 固定使用某个端口（如8080）
- C: 自动选择可用端口

#### E7: WebChatHttpBridge 中的文件上传API [待确认]

/api/web/uploads API支持文件上传。简化后是否保留？
- A: 保留，外部调用者可能需要上传文件
- B: 移除，简化API
- C: 保留但限制上传大小

#### E8: WebChatHttpBridge 中的记忆操作API [待确认]

/api/web/actions 支持记忆更新和对话摘要。简化后是否保留？
- A: 保留
- B: 移除记忆更新，保留对话摘要
- C: 全部保留但简化参数

---

## 模块十：备份系统

> 涉及文件：RoomDatabaseBackupManager.kt, RawSnapshotBackupManager.kt, RoomDatabaseBackupPreferences.kt, RoomDatabaseBackupRestoreLock.kt, RoomDatabaseBackupScheduler.kt, RoomDatabaseBackupWorker.kt, RoomDatabaseRestoreManager.kt, OperitBackupDirs.kt

#### B1: 备份系统的简化范围 [待确认]

已确认简化为一键备份/恢复。当前的两种备份方式（Room数据库备份和Raw快照备份）如何合并？
- A: 仅保留Room数据库备份，移除Raw快照备份
- B: 合并为统一的备份流程，内部仍使用两种方式
- C: 保留两种方式但UI只暴露一个按钮

#### B2: RawSnapshotBackupManager 的去留 [待确认]

RawSnapshotBackupManager 实现Raw快照备份。简化后是否完全移除？
- A: 完全移除
- B: 保留但标记为deprecated
- C: 保留作为内部备份机制

#### B3: RoomDatabaseBackupScheduler 的自动备份 [待确认]

RoomDatabaseBackupScheduler 管理自动备份调度。简化后是否保留自动备份？
- A: 保留自动备份，用户无感知
- B: 移除自动备份，仅支持手动一键备份
- C: 保留自动备份但频率降低

#### B4: 备份恢复的冲突处理 [待确认]

恢复备份时如果当前已有数据，如何处理冲突？
- A: 直接覆盖当前数据
- B: 合并数据（保留两者）
- C: 提示用户选择覆盖或合并

#### B5: RoomDatabaseBackupRestoreLock 的作用 [待确认]

RoomDatabaseBackupRestoreLock 防止并发恢复操作。简化后是否保留？
- A: 保留，防止数据损坏
- B: 移除，简化恢复流程

#### B6: 备份文件格式 [待确认]

当前备份文件格式是什么？简化后是否需要调整？
- A: 保持当前格式不变
- B: 统一为ZIP格式，包含数据库和配置文件
- C: 使用标准格式（如JSON）以提高兼容性

#### B7: 角色卡数据在备份中的处理 [待确认]

砍掉角色卡后，旧版备份中可能包含角色卡数据。恢复时如何处理？
- A: 忽略角色卡数据，恢复其他数据
- B: 将角色卡设定转换为PromptTag后恢复
- C: 提示用户角色卡数据不可恢复

#### B8: 备份的存储位置 [待确认]

OperitBackupDirs 定义了备份目录。简化后备份存储位置是否调整？
- A: 保持不变
- B: 使用系统标准的Downloads目录
- C: 使用应用私有目录，通过Share Intent导出

#### B9: 大数据量备份的性能 [待确认]

数据量较大时备份可能耗时较长。是否需要进度提示？
- A: 需要进度提示，显示百分比
- B: 需要进度提示，仅显示"备份中..."
- C: 不需要，后台静默完成

#### B10: 备份的加密 [待确认]

当前备份是否加密？简化后是否需要增加加密？
- A: 保持当前状态（不加密）
- B: 增加可选的密码加密
- C: 默认加密，使用设备凭据

---

## 模块十一：代理系统

> 涉及文件：PhoneAgent.kt, VirtualDisplayManager.kt, ShowerController.kt, ShowerBinderRegistry.kt, ShowerBinderReceiver.kt, CliToolModeSupport.kt

#### A1: PhoneAgent 的保留 [待确认]

PhoneAgent 是基于AI的手机自动化代理。简化后是否保留？
- A: 保留，手机自动化是核心功能
- B: 保留但简化，移除部分自动化能力
- C: 移除，手机自动化过于复杂

#### A2: VirtualDisplayManager 的保留 [待确认]

VirtualDisplayManager 管理虚拟显示。PhoneAgent依赖虚拟显示进行屏幕感知。简化后是否保留？
- A: 保留，PhoneAgent需要虚拟显示
- B: 保留但简化，仅支持截图功能
- C: 移除，改用无障碍服务的屏幕捕获

#### A3: ShowerController 与Shower服务的交互 [待确认]

ShowerController 封装了对Shower服务的调用。简化后Shower服务的依赖是否保留？
- A: 保留，Shower服务提供虚拟显示能力
- B: 移除Shower依赖，改用系统无障碍服务
- C: 保留但简化交互接口

#### A4: CliToolModeSupport 中的隐藏工具目录 [待确认]

CliToolModeSupport 构建隐藏工具目录，提供安全的工具调用方式。简化后是否保留？
- A: 保留，CLI工具模式对高级用户有价值
- B: 移除，简化工具调用机制
- C: 保留但简化隐藏工具的构建逻辑

#### A5: PhoneAgent 的Vision-Language模型依赖 [待确认]

PhoneAgent 依赖Vision-Language模型解析屏幕内容。该模型的配置是否简化？
- A: 保持当前配置方式
- B: 简化为自动选择可用模型
- C: 移除VLM依赖，改用纯文本屏幕解析

#### A6: ShowerBinderRegistry 的Binder通信 [待确认]

ShowerBinderRegistry 管理与Shower服务的Binder通信。简化后是否保留？
- A: 保留，Binder通信是跨进程交互的基础
- B: 移除，改用其他IPC方式
- C: 保留但简化注册逻辑

#### A7: PhoneAgent 的操作执行确认 [待确认]

PhoneAgent 执行操作前是否需要用户确认？
- A: 保持当前确认策略
- B: 增加确认频率，提升安全性
- C: 减少确认频率，提升自动化效率

#### A8: CliToolModeSupport 中的代理调用规则 [待确认]

CliToolModeSupport 定义了代理调用规则。简化后规则是否调整？
- A: 保持不变
- B: 简化规则，移除复杂的调用链
- C: 重新设计规则体系

#### A9: PhoneAgentJobRegistry 的任务注册 [待确认]

PhoneAgentJobRegistry 管理PhoneAgent的任务注册。简化后是否保留？
- A: 保留，任务注册是PhoneAgent的核心机制
- B: 简化任务注册流程
- C: 移除，改用直接调用方式

#### A10: 代理系统与权限级别的关联 [待确认]

PhoneAgent 的操作受权限级别控制。权限重命名为"基本/高级"后，代理系统的权限检查是否调整？
- A: 仅重命名，逻辑不变
- B: 高级权限下PhoneAgent才能使用
- C: 基本权限下也可使用部分PhoneAgent功能

---

## 模块十二：MCP系统

> 涉及文件：MCPBridge.kt, MCPRepository.kt, MCPLocalServer.kt, MCPJson.kt, MCPPackage.kt, MCPServerConfig.kt, MCPTool.kt, MCPToolParameter.kt

#### M1: MCPBridge 的服务注册机制 [待确认]

MCPBridge 实现MCP服务注册和工具调用。合并后MCPBridge的接口是否调整？
- A: 保持不变，合并只影响UI
- B: 调整注册接口，统一MCP/Skill/包管理的注册流程
- C: 保持接口不变但优化内部实现

#### M2: MCPLocalServer 的本地服务管理 [待确认]

MCPLocalServer 管理本地MCP服务器的启停。简化后本地MCP服务是否保留？
- A: 保留，本地MCP服务对沙箱包有用
- B: 移除，简化MCP架构
- C: 保留但优化启动流程

#### M3: MCPRepository 的配置持久化 [待确认]

MCPRepository 管理MCP配置的持久化。合并后配置存储是否调整？
- A: 保持不变
- B: 合并MCP/Skill/包管理的配置存储
- C: 迁移到统一的配置管理器

#### M4: MCPServerConfig 的配置项 [待确认]

MCPServerConfig 定义MCP服务器配置。简化后配置项是否调整？
- A: 保持不变
- B: 简化配置项，移除高级选项
- C: 增加配置验证和错误提示

#### M5: MCPTool 的工具描述格式 [待确认]

MCPTool 定义MCP工具的描述格式。简化后是否需要调整工具描述的详细程度？
- A: 保持不变
- B: 简化描述，减少token消耗
- C: 根据权限级别动态调整描述详细程度

#### M6: MCP与Skill的统一管理 [待确认]

合并后MCP和Skill在数据层是否统一管理？
- A: 保持独立管理，仅UI层合并
- B: 统一为扩展管理器，内部区分类型
- C: 完全统一，不再区分MCP和Skill

#### M7: MCPBridge 的错误恢复机制 [待确认]

MCPBridge 与外部MCP服务通信时可能断连。错误恢复策略是什么？
- A: 自动重连
- B: 标记为不可用，等待用户手动重连
- C: 自动重连有限次数，超过后标记不可用

#### M8: MCP工具的权限控制 [待确认]

MCP工具是否受权限级别控制？
- A: 是，与系统工具一样受权限级别过滤
- B: 否，MCP工具一旦安装即可使用
- C: 部分受控，敏感操作需要权限

#### M9: MCP服务器的健康检查 [待确认]

MCP服务器是否有健康检查机制？
- A: 有，定期检查服务器可用性
- B: 没有，仅在调用时检测
- C: 有，在应用启动和工具调用前检查

#### M10: MCP配置的导入导出 [待确认]

MCP配置是否支持导入导出？
- A: 支持，通过全局备份恢复
- B: 不支持，用户需手动配置
- C: 支持独立的MCP配置导入导出

---

## 模块十三：沙箱包系统

> 涉及文件：ToolPkgParser.kt, ToolPkgComposeDslParser.kt, ToolPkgHookModels.kt, ToolPkgMainRegistrationScriptParser.kt, ToolPkgTemplateModels.kt, PackageDebugRefreshReceiver.kt, ToolPkgDebugInstallReceiver.kt, ToolPkgComposeDslDebugDumpReceiver.kt

#### P1: ToolPkgParser 的manifest版本兼容 [待确认]

ToolPkgParser 解析不同版本的manifest。简化后是否需要更新manifest版本号？
- A: 保持当前版本号，确保向后兼容
- B: 新增版本号但兼容旧版本
- C: 强制升级manifest版本

#### P2: ToolPkgHookModels 中的钩子类型 [待确认]

ToolPkgHookModels 定义了多种钩子类型（XML渲染、生命周期、提示词轮次等）。简化后是否需要移除部分钩子类型？
- A: 保持不变，沙箱包可能依赖各种钩子
- B: 移除与砍掉功能相关的钩子（如语音相关钩子）
- C: 审查并移除不常用的钩子类型

#### P3: ToolPkgMainRegistrationScriptParser 中的UI模块注册 [待确认]

ToolPkgMainRegistrationScriptParser 解析UI模块注册。合并后UI模块的路由如何处理？
- A: 保持原有路由，新增统一入口
- B: 原有路由重定向到统一页面下的子路由
- C: 完全重新设计路由体系

#### P4: ToolPkgMainRegistrationScriptParser 中的导航条目注册 [待确认]

沙箱包可以注册导航条目。合并后导航条目的展示位置如何调整？
- A: 保持原有位置
- B: 统一展示在合并页面的对应区域
- C: 移除导航条目注册能力

#### P5: PackageDebugRefreshReceiver 的调试功能 [待确认]

PackageDebugRefreshReceiver 用于调试时刷新包。简化后调试功能是否保留？
- A: 保留，开发者需要调试功能
- B: 移除，减少攻击面
- C: 保留但仅在Debug构建中可用

#### P6: ToolPkgComposeDslDebugDumpReceiver 的调试功能 [待确认]

ToolPkgComposeDslDebugDumpReceiver 用于调试时Dump DSL。同上，是否保留？
- A: 保留，仅Debug构建可用
- B: 移除
- C: 保留但增加权限控制

#### P7: ToolPkgTemplateModels 中的工作区模板 [待确认]

ToolPkgTemplateModels 定义了工作区模板。简化后工作区模板是否保留？
- A: 保留，用户可从模板创建工作区
- B: 移除，简化模板系统
- C: 保留但移除模板市场UI

#### P8: 沙箱包的JavaScript引擎 [待确认]

沙箱包通过JsEngine执行JavaScript代码。简化后JsEngine的能力是否限制？
- A: 保持不变，沙箱包需要完整的JS能力
- B: 限制部分API访问（如移除语音相关API）
- C: 增加沙箱安全限制

#### P9: 沙箱包与系统工具的交互 [待确认]

沙箱包通过ToolPkgCommonBridgePlugin与系统交互。合并后桥接接口是否调整？
- A: 保持不变，确保兼容性
- B: 调整部分接口，增加统一管理相关接口
- C: 完全重新设计桥接接口

#### P10: 沙箱包的生命周期管理 [待确认]

ToolPkgToolLifecycleBridge 管理包的生命周期。简化后生命周期管理是否调整？
- A: 保持不变
- B: 简化生命周期状态（如移除暂停状态）
- C: 增加自动清理不活跃包的机制

---

## 模块十四：砍除模块清理

> 涉及角色卡、虚拟形象、语音、Bubble样式、Classic输入、液态玻璃主题、深度搜索

#### R1: 角色卡系统的引用清理范围 [待确认]

角色卡系统被多个模块引用（ChatViewModel、AIMessageManager、WebChatHttpBridge、ChatDao等）。清理策略：
- A: 逐一清理所有引用，确保编译通过
- B: 先移除核心管理器，再逐步清理引用
- C: 保留接口定义，移除实现

#### R2: 虚拟形象模块的C++原生库清理 [待确认]

dragonbones/、fbx/、mmd/ 是C++原生模块。移除后是否需要清理CMakeLists.txt和gradle配置？
- A: 完全清理，包括CMakeLists.txt和build.gradle.kts
- B: 仅清理app层的引用，保留原生模块目录
- C: 完全清理并从settings.gradle.kts中移除模块

#### R3: 语音服务的Provider清理 [待确认]

语音服务有多个Provider（OpenAI、Doubao、SiliconFlow、MiniMax等）。清理范围：
- A: 完全移除所有Provider实现
- B: 移除Provider实现但保留接口定义
- C: 移除Provider实现和接口定义

#### R4: Bubble样式的资源清理 [待确认]

Bubble样式有专属的drawable资源和字符串资源。清理范围：
- A: 完全清理所有Bubble相关资源
- B: 仅清理Kotlin代码，资源后续统一清理
- C: 清理代码和资源但保留字符串供翻译参考

#### R5: Classic输入模式的设置栏清理 [待确认]

ClassicChatSettingsBar 有独立的偏好设置。清理时是否需要重置用户偏好？
- A: 清理代码并重置相关偏好为默认值
- B: 仅清理代码，偏好自然失效
- C: 清理代码并在下次启动时迁移偏好

#### R6: 液态玻璃主题的清理范围 [待确认]

液态玻璃主题在多个UI组件中使用（DrawerContent、命令框样式等）。清理策略：
- A: 完全移除liquidGlass目录和所有引用
- B: 移除liquidGlass目录但保留基础模糊效果
- C: 逐步替换为Material 3默认效果

#### R7: 深度搜索插件的清理 [待确认]

examples/deepsearching/ 是深度搜索插件。清理范围：
- A: 完全移除目录
- B: 移除但保留文档说明
- C: 保留目录但标记为deprecated

#### R8: 自定义表情资源的清理 [待确认]

assets/emoji/ 目录包含大量表情图片。砍掉桌宠后这些资源是否清理？
- A: 完全清理
- B: 保留，其他功能可能使用
- C: 清理但保留目录结构

#### R9: 砍除功能后的数据库迁移 [待确认]

砍除功能涉及多个数据表（角色卡、群聊、自定义表情等）。数据库迁移策略：
- A: 新增迁移版本，删除相关表
- B: 新增迁移版本，保留表但清空数据
- C: 不做迁移，旧数据自然失效

#### R10: 砍除功能后的字符串资源清理 [待确认]

strings.xml中有大量与砍除功能相关的字符串。清理策略：
- A: 逐一清理所有相关字符串
- B: 批量清理，后续通过lint检查遗漏
- C: 暂不清理，最后统一清理

#### R11: 砍除功能对ObjectBox的影响 [待确认]

项目同时使用Room和ObjectBox。砍除功能后ObjectBox的模型是否需要调整？
- A: 需要调整，移除相关Entity
- B: 不需要，ObjectBox模型与砍除功能无关
- C: 需要审查确认

#### R12: 砍除功能对proguard规则的影响 [待确认]

proguard-rules.pro 可能包含砍除功能相关的keep规则。是否需要清理？
- A: 需要清理，移除不再需要的keep规则
- B: 不需要，多余的keep规则不影响运行
- C: 需要审查确认

#### R13: 砍除功能后的依赖库清理 [待确认]

砍除功能可能引入了特定的依赖库（如DragonBones SDK、语音SDK等）。是否需要从build.gradle.kts中移除？
- A: 完全移除相关依赖
- B: 保留但标记为deprecated
- C: 逐步移除，先确认无其他模块依赖

#### R14: 砍除功能对AndroidManifest的影响 [待确认]

AndroidManifest.xml 中可能声明了砍除功能相关的Service、Activity、权限。是否需要清理？
- A: 完全清理相关声明
- B: 保留权限声明（可能被沙箱包使用）
- C: 逐一审查后决定

#### R15: 砍除功能的AIDL文件清理 [待确认]

aidl/ 目录下有AccessibilityEvent.aidl等文件。砍除语音后AIDL文件是否需要清理？
- A: 保留，无障碍功能仍在使用
- B: 清理语音相关的AIDL文件
- C: 全部保留，AIDL文件不影响包大小

---

## 模块十五：数据库与数据转换

> 涉及文件：AppDatabase.kt, ChatDao.kt, MessageDao.kt, ChatFormatConverter.kt, ChatBoxConverter.kt, ChatGPTConverter.kt, MarkdownConverter.kt, HtmlExporter.kt, MarkdownExporter.kt

#### DB1: AppDatabase 的迁移策略 [待确认]

AppDatabase 定义了数据库版本和迁移策略。砍除功能后需要新增迁移版本。迁移的优先级：
- A: 高优先级，砍除功能时同步完成迁移
- B: 中优先级，砍除功能后单独处理迁移
- C: 低优先级，先确保功能正常再处理迁移

#### DB2: ChatDao 中的角色卡相关查询 [待确认]

ChatDao 包含角色卡绑定的聊天查询。砍掉角色卡后这些查询如何处理？
- A: 移除角色卡相关查询方法
- B: 保留查询方法但返回空结果
- C: 保留查询方法供沙箱包使用

#### DB3: ChatDao 中的聊天分组查询 [待确认]

已确认聊天分组保留API移除UI。ChatDao中的分组查询是否保留？
- A: 保留，API层需要
- B: 移除，分组功能完全废弃
- C: 保留但标记为deprecated

#### DB4: ChatFormatConverter 的格式支持 [待确认]

ChatFormatConverter 支持多种聊天记录格式转换。简化后是否减少支持的格式？
- A: 保持不变
- B: 移除不常用的格式（如ChatBox格式）
- C: 仅保留JSON格式

#### DB5: 导出功能的保留 [待确认]

HtmlExporter、MarkdownExporter、TextExporter 提供多种导出格式。简化后是否保留？
- A: 全部保留
- B: 仅保留Markdown和纯文本导出
- C: 仅保留Markdown导出

#### DB6: MessageDao 中的消息变体 [待确认]

MessageVariantDao 管理消息变体（如编辑前的原始消息）。简化后消息变体功能是否保留？
- A: 保留
- B: 移除，简化消息存储
- C: 保留但限制变体数量

#### DB7: AppDatabase 中的数据自动清理 [待确认]

AppDatabase 是否有自动清理旧数据的机制？简化后是否需要增加？
- A: 无自动清理，保持不变
- B: 增加自动清理超过一定时间的聊天记录
- C: 增加自动清理但仅清理摘要数据，保留元数据

#### DB8: ChatDao 中的批量操作性能 [待确认]

ChatDao 有批量更新和删除操作。简化后是否需要优化性能？
- A: 保持不变
- B: 优化批量操作的事务管理
- C: 增加分页查询支持

#### DB9: 数据库的加密 [待确认]

当前数据库是否加密？简化后是否需要增加加密？
- A: 不加密，保持不变
- B: 增加SQLCipher加密
- C: 仅加密敏感表（如API Key表）

#### DB10: ChatFormatDetector 的自动检测 [待确认]

ChatFormatDetector 自动检测聊天记录格式。简化后是否保留？
- A: 保留，导入功能需要
- B: 移除，仅支持一种格式
- C: 保留但简化检测逻辑

---

## 模块十六：长程任务执行能力增强

> 涉及文件：AIForegroundService.kt, ChatServiceCore.kt, FloatingChatService.kt, WorkflowWorker.kt, WorkflowScheduler.kt, AIMessageManager.kt, ChatRuntimeHolder.kt

#### L1: 长程任务的定义与边界 [待确认]

"长程任务"在本项目中的具体定义是什么？需要明确哪些场景属于长程任务：
- A: 多轮工具调用链（AI连续调用多个工具完成复杂任务）
- B: 工作流定时执行（WorkflowScheduler触发的后台任务）
- C: 用户发起的长时间对话（持续数小时的对话会话）
- D: 以上全部

#### L2: 长程任务的状态持久化 [待确认]

当前AI任务的状态（工具调用链进度、中间结果等）是否持久化？应用被系统杀死后能否恢复？
- A: 不持久化，应用重启后任务丢失
- B: 部分持久化（对话历史保留，但工具调用链中断）
- C: 完全持久化，应用重启后可从断点继续

#### L3: 多轮工具调用链的超时与重试 [待确认]

当前多轮工具调用是否有总超时限制？单次工具调用失败后的重试策略是什么？
- A: 无总超时，单次失败后AI决定是否重试
- B: 有总超时（如10分钟），单次失败自动重试3次
- C: 有总超时，单次失败不重试，由AI决定替代方案

#### L4: 后台保活与长程任务的关系 [待确认]

后台保活默认开启后，长程任务在应用退到后台时的行为：
- A: 继续执行，通过前台服务保持进程存活
- B: 继续执行但降低优先级，可能被系统杀死
- C: 暂停执行，应用回到前台后继续

#### L5: 长程任务的进度反馈机制 [待确认]

长程任务执行过程中，用户如何感知进度？
- A: 悬浮窗黑色气泡显示进度百分比
- B: 通知栏显示进度
- C: 两者结合（悬浮窗+通知栏）

#### L6: 长程任务的取消机制 [待确认]

用户如何取消正在执行的长程任务？
- A: 通知栏点击取消
- B: 悬浮窗气泡点击取消
- C: 回到应用内点击取消按钮
- D: 以上全部

#### L7: ChatServiceCore 的任务队列管理 [待确认]

ChatServiceCore 是否支持任务队列？多个长程任务能否并行执行？
- A: 不支持队列，一次只能执行一个任务
- B: 支持队列但串行执行
- C: 支持并行执行多个任务

#### L8: 长程任务的网络中断恢复 [待确认]

长程任务执行过程中网络中断如何处理？
- A: 立即失败，通知用户
- B: 自动重连并继续
- C: 暂停等待网络恢复后继续

#### L9: FloatingChatService 在长程任务中的角色 [待确认]

FloatingChatService 依赖 ChatServiceCore 处理聊天逻辑。简化后悬浮窗仅保留进度气泡，FloatingChatService 是否还需要保留？
- A: 完全移除，长程任务通过 AIForegroundService 管理
- B: 保留但简化，仅用于后台任务状态追踪
- C: 保留不变

#### L10: 长程任务的资源管理 [待确认]

长程任务可能消耗大量内存和CPU。是否有资源限制机制？
- A: 无限制，依赖系统管理
- B: 设置内存和CPU使用上限
- C: 根据设备性能动态调整资源限制

#### L11: 工作流长程任务的执行保障 [待确认]

WorkflowWorker 通过 WorkManager 执行后台任务。WorkManager 有执行时间限制（通常10分钟）。长时间工作流如何处理？
- A: 将工作流拆分为多个Worker链式执行
- B: 通过前台服务延长执行时间
- C: 限制工作流单次执行时间

#### L12: 长程任务与记忆库的协同 [待确认]

长程任务执行过程中是否自动保存关键信息到记忆库？
- A: 不自动保存，由AI决定何时保存
- B: 每个关键节点自动保存摘要
- C: 任务完成后自动保存完整结果

---

## 模块十七：AutoGLM GUI操作能力内置化

> 涉及文件：AutoGlmViewModel.kt, AutoGlmToolScreen.kt, AutoGlmOneClickToolScreen.kt, PhoneAgent.kt, StandardUITools.kt, automatic_ui_base.js, automatic_ui_subagent.js, operit_editor.js, ShowerController.kt, VirtualDisplayManager.kt

#### G1: AutoGLM与原有GUI操作能力的架构区别 [待确认]

当前存在两种GUI操作能力：AutoGLM（基于智谱AI的视觉语言模型）和原有简单GUI操作（PhoneAgent + StandardUITools + ADB/无障碍）。仅保留AutoGLM后：
- A: 完全移除PhoneAgent和StandardUITools，AutoGLM独立提供所有GUI操作能力
- B: 保留PhoneAgent底层能力（截图、点击等），AutoGLM作为上层决策引擎
- C: 将PhoneAgent的核心能力合并到AutoGLM中，形成统一的GUI操作模块

#### G2: AutoGLM内置化后的代码位置 [待确认]

AutoGLM当前作为工具箱子页面（AutoGlmToolScreen/AutoGlmOneClickToolScreen）和JS插件包（automatic_ui_base.js, automatic_ui_subagent.js）存在。内置化后代码放在哪里？
- A: 移至 core/tools/autoglm/ 目录，作为核心工具模块
- B: 移至 core/tools/agent/ 目录，替换PhoneAgent
- C: 新建 core/gui/ 目录，作为独立的GUI操作模块

#### G3: AutoGLM的JS插件包处理 [待确认]

automatic_ui_base.js 和 automatic_ui_subagent.js 当前作为 assets/packages/ 中的内置插件包。内置化后这些JS文件如何处理？
- A: 保留为JS插件包，但标记为系统内置不可卸载
- B: 将JS逻辑迁移为Kotlin原生实现
- C: 保留JS包但将其从包管理列表中隐藏

#### G4: operit_editor.js 的去留 [待确认]

operit_editor.js 提供编辑器功能，与AutoGLM的UI操作有关。仅保留AutoGLM后，operit_editor是否保留？
- A: 保留，AutoGLM需要编辑器能力
- B: 移除，编辑器功能不属于核心GUI操作
- C: 保留但简化

#### G5: AutoGLM的配置项 [待确认]

AutoGLM需要配置API Key等参数。内置化后配置如何管理？
- A: 使用全局模型配置（复用API Key和模型设置）
- B: 独立配置AutoGLM的API参数
- C: 自动使用智谱AI的免费额度，无需用户配置

#### G6: AutoGLM与权限系统的关系 [待确认]

AutoGLM执行GUI操作需要无障碍权限。内置化后权限检查如何处理？
- A: 仅在"高级"权限级别下可用
- B: 基本和高级权限级别下都可用
- C: 独立的GUI操作权限级别

#### G7: PhoneAgent的完整移除范围 [待确认]

移除PhoneAgent后，需要清理哪些关联代码？
- A: 仅移除PhoneAgent.kt和PhoneAgentJobRegistry.kt
- B: 移除PhoneAgent + ShowerController + VirtualDisplayManager + ShowerBinderRegistry
- C: 移除PhoneAgent相关代码但保留ShowerController和VirtualDisplayManager（AutoGLM可能需要）

#### G8: StandardUITools 的处理 [待确认]

StandardUITools 提供标准UI操作工具（点击、滑动、输入等）。AutoGLM是否还需要这些底层工具？
- A: 完全移除，AutoGLM通过自己的方式执行操作
- B: 保留，AutoGLM依赖这些底层工具执行操作
- C: 保留核心操作（点击、滑动、输入），移除高级操作

#### G9: AutoGLM的虚拟显示依赖 [待确认]

AutoGLM是否依赖VirtualDisplayManager进行屏幕感知？
- A: 是，AutoGLM需要虚拟显示获取屏幕内容
- B: 否，AutoGLM使用截图方式获取屏幕内容
- C: 两种方式都支持

#### G10: AutoGLM内置化后对沙箱包的影响 [待确认]

沙箱包可能依赖automatic_ui_base.js或automatic_ui_subagent.js。内置化后如何确保兼容？
- A: 保留JS包供沙箱包使用，内置AutoGLM使用Kotlin实现
- B: 沙箱包改为调用内置AutoGLM的API
- C: 内置化和沙箱包各自独立，不互相影响

#### G11: AutoGLM的操作确认机制 [待确认]

AutoGLM执行GUI操作前是否需要用户确认？
- A: 每次操作都需要确认
- B: 仅敏感操作需要确认（如删除、支付等）
- C: 不需要确认，自动执行

#### G12: AutoGLM的错误恢复 [待确认]

AutoGLM操作失败后如何处理？
- A: 自动重试（最多3次）
- B: 报告失败原因，由用户决定下一步
- C: AI自动调整策略并重试

---

## 模块十八：待办功能（新增）

> 这是全新功能，需要从零设计

#### TD1: 待办功能的数据模型 [待确认]

待办条目需要包含哪些字段？
- A: 基础字段（标题、描述、完成状态、创建时间、截止时间）
- B: 基础字段 + 优先级 + 标签
- C: 基础字段 + 优先级 + 标签 + 子任务 + 关联记忆

#### TD2: 待办功能的存储方式 [待确认]

待办数据存储在哪里？
- A: Room数据库（与聊天记录同库）
- B: ObjectBox（与记忆库同库）
- C: 独立的SQLite数据库

#### TD3: 待办功能的UI入口 [待确认]

待办功能的入口在哪里？
- A: 侧边栏预留按钮区（与工作流同级）
- B: 聊天界面内的斜杠命令（/todo）
- C: 两者都有（侧边栏为主入口，斜杠命令为快捷入口）

#### TD4: 待办与AI的交互方式 [待确认]

AI如何与待办功能交互？
- A: AI可以创建/完成/删除待办条目（通过工具调用）
- B: AI仅可查看待办列表，不能修改
- C: AI可创建和完成，但不能删除

#### TD5: 待办与工作流的集成 [待确认]

待办是否可以触发工作流？
- A: 不集成，待办和工作流独立
- B: 待办截止时间到达时触发工作流
- C: 待办完成时触发工作流

#### TD6: 待办与记忆库的关联 [待确认]

待办条目是否关联记忆库？
- A: 不关联
- B: 待办条目可关联记忆条目（双向引用）
- C: 完成的待办自动转为记忆

#### TD7: 待办的通知提醒 [待确认]

待办截止时间到达时如何提醒？
- A: 系统通知
- B: AI对话中提醒
- C: 两者结合

#### TD8: 待办的重复规则 [待确认]

待办是否支持重复规则（如每天、每周）？
- A: 不支持，每条待办独立
- B: 支持简单重复（每天/每周/每月）
- C: 支持cron表达式级别的重复规则

#### TD9: 待办的分组与筛选 [待确认]

待办是否支持分组和筛选？
- A: 不支持，平铺展示
- B: 支持按标签筛选
- C: 支持按标签、优先级、状态筛选

#### TD10: 待办与日历的集成 [待确认]

待办是否与日历功能集成？
- A: 不集成
- B: 待办截止时间显示在日历上
- C: 待办和日历共享数据模型

---

## 模块十九：日程功能（新增）

> 这是全新功能，需要从零设计

#### CA1: 日程功能的数据模型 [待确认]

日程条目需要包含哪些字段？
- A: 基础字段（标题、描述、开始时间、结束时间、地点）
- B: 基础字段 + 重复规则 + 提醒设置
- C: 基础字段 + 重复规则 + 提醒设置 + 参与者 + 关联待办

#### CA2: 日程功能的存储方式 [待确认]

日程数据存储在哪里？
- A: Room数据库
- B: ObjectBox
- C: 与系统日历同步

#### CA3: 日程功能的UI入口 [待确认]

日程功能的入口在哪里？
- A: 侧边栏预留按钮区（与工作流、待办同级）
- B: 聊天界面内的斜杠命令（/calendar）
- C: 两者都有

#### CA4: 日程与AI的交互方式 [待确认]

AI如何与日程功能交互？
- A: AI可以创建/修改/删除日程（通过工具调用）
- B: AI仅可查看日程，不能修改
- C: AI可创建和查看，但不能删除

#### CA5: 日程的视图模式 [待确认]

日程的展示方式：
- A: 列表视图（按时间排列）
- B: 日历视图（月/周/日视图）
- C: 两者都支持

#### CA6: 日程与系统日历的同步 [待确认]

是否与Android系统日历同步？
- A: 不同步，独立管理
- B: 单向同步（读取系统日历）
- C: 双向同步

#### CA7: 日程的提醒方式 [待确认]

日程提醒如何实现？
- A: 应用内通知
- B: 系统通知（AlarmManager）
- C: AI对话中提醒

#### CA8: 日程与工作流的集成 [待确认]

日程是否可以触发工作流？
- A: 不集成
- B: 日程开始时触发工作流
- C: 日程开始前N分钟触发工作流

#### CA9: 日程与记忆库的关联 [待确认]

日程是否关联记忆库？
- A: 不关联
- B: 过去的日程自动转为记忆
- C: 日程可手动关联记忆条目

#### CA10: 日程的冲突检测 [待确认]

是否检测日程时间冲突？
- A: 不检测
- B: 检测并提示冲突
- C: 检测并自动调整时间

---

## 模块二十：终端系统

> 涉及文件：Terminal.kt, OperitTerminalManager.kt, StandardTerminalCommandExecutor.kt, TerminalSetup, TerminalAutoConfig

#### TE1: 终端的核心定位 [待确认]

简化后终端的核心定位是什么？
- A: 仅供AI调用的命令执行环境（用户不直接交互）
- B: AI和用户共用的命令执行环境
- C: 完整的终端模拟器

#### TE2: 终端自动配置的范围 [待确认]

OperitTerminalManager 有自动配置和依赖安装逻辑。首次使用时自动安装哪些依赖？
- A: 仅安装核心依赖（如Node.js运行时）
- B: 安装核心依赖 + 常用工具（如Python运行时）
- C: 根据已安装的工具包动态安装依赖

#### TE3: 终端UI的简化范围 [待确认]

已确认终端UI简化为命令输入+执行+结果展示。是否保留终端历史记录？
- A: 不保留，每次打开终端都是全新会话
- B: 保留当前会话的历史，关闭后清除
- C: 保留持久化的历史记录

#### TE4: 终端的Shell环境选择 [待确认]

终端使用哪种Shell环境？
- A: 系统默认Shell（/bin/sh）
- B: Bash（如果可用）
- C: 应用内置Shell（独立于系统）

#### TE5: 终端与AI工具调用的关系 [待确认]

AI通过工具调用执行终端命令时，是否与用户手动输入共享同一个终端会话？
- A: 共享，AI和用户在同一个会话中
- B: 独立，AI使用后台会话，用户使用前台会话
- C: 可配置

#### TE6: 终端的安全限制 [待确认]

终端命令执行是否有安全限制？
- A: 无限制，可执行任何命令
- B: 禁止危险命令（如rm -rf /）
- C: 根据权限级别限制可执行的命令范围

#### TE7: 终端配置项的移除 [待确认]

已确认移除终端配置项。哪些配置由系统自动管理？
- A: Shell类型、环境变量、PATH设置全部自动管理
- B: 仅Shell类型自动管理，环境变量保留用户配置
- C: 全部自动管理但提供斜杠命令 /terminal 供高级用户调整

#### TE8: 终端的长程任务支持 [待确认]

终端是否支持长时间运行的命令（如服务器进程）？
- A: 不支持，命令执行有超时限制
- B: 支持，通过后台保活维持长程命令
- C: 支持但需要用户手动确认

#### TE9: 终端的Root执行模式 [待确认]

当前有Root执行模式的配置。简化后是否保留？
- A: 完全移除，仅支持普通用户权限执行
- B: 保留但简化为自动检测，有Root权限时自动使用
- C: 保留但移到高级设置中

#### TE10: 终端与工作区的交互 [待确认]

终端和工作区是否需要交互（如在终端中执行工作区项目的命令）？
- A: 不需要，终端和工作区独立
- B: 需要，终端可在工作区目录下执行命令
- C: 需要，工作区项目可直接调用终端

---

## 模块二十一：工作区系统

> 涉及文件：WorkspaceScreen.kt, WorkspaceManager.kt, ToolPkgTemplateModels.kt

#### WS1: 工作区功能的核心定位 [待确认]

简化后工作区功能是否保留？
- A: 保留，工作区是沙箱包开发的核心环境
- B: 保留但简化，移除部分开发功能
- C: 移除，工作区功能过于复杂

#### WS2: 工作区的入口位置 [待确认]

工作区当前在聊天界面内。简化后入口在哪里？
- A: 保留在聊天界面内
- B: 移至侧边栏预留按钮区
- C: 移至工具箱

#### WS3: 工作区的模板系统 [待确认]

ToolPkgTemplateModels 定义了工作区模板。简化后模板系统是否保留？
- A: 保留，模板帮助用户快速创建项目
- B: 保留但减少模板数量
- C: 移除模板系统

#### WS4: 工作区的代码编辑能力 [待确认]

工作区当前支持代码编辑。简化后是否保留？
- A: 保留完整的代码编辑能力
- B: 保留但简化为仅查看代码
- C: 移除代码编辑，改为外部编辑器

#### WS5: 工作区的打包导出 [待确认]

工作区支持将项目打包为工具包。简化后是否保留？
- A: 保留，打包导出是核心功能
- B: 保留但简化打包流程
- C: 移除，改为命令行打包

#### WS6: 工作区与终端的集成 [待确认]

工作区是否需要集成终端？
- A: 需要，开发环境需要终端
- B: 不需要，工作区仅做文件管理
- C: 可选集成

#### WS7: 工作区的文件管理范围 [待确认]

工作区管理哪些文件？
- A: 仅管理工具包项目文件
- B: 管理应用内所有用户文件
- C: 管理工具包项目文件 + 沙箱包数据

#### WS8: 工作区的WebView实现 [待确认]

WorkspaceScreen 基于WebView实现。简化后是否改为原生Compose实现？
- A: 保持WebView实现
- B: 改为原生Compose实现
- C: 混合实现（文件管理用Compose，代码编辑用WebView）

#### WS9: 工作区与AI的交互 [待确认]

AI是否可以直接操作工作区文件？
- A: 可以，AI通过工具调用读写工作区文件
- B: 不可以，工作区文件仅用户手动操作
- C: 可以读取但不能修改

#### WS10: 工作区模板的Android/Flutter等类型 [待确认]

当前工作区模板支持Android、Flutter、Go、Java、Node、Python、TypeScript、Web等多种类型。简化后保留哪些？
- A: 全部保留
- B: 仅保留TypeScript和Web模板（与沙箱包开发相关）
- C: 仅保留TypeScript模板

---

## 模块二十二：虚拟形象系统完整移除

> 涉及文件：core/avatar/ 整个目录, ui/features/assistant/ 整个目录, ui/floating/ui/pet/, ui/components/ManagedDragonBonesView.kt, data/model/DragonBones.kt, data/model/CustomEmoji.kt, data/preferences/WaifuPreferences.kt, data/repository/AvatarRepository.kt, dragonbones/ 模块, fbx/ 模块, mmd/ 模块, assets/emoji/, assets/pets/, assets/dragonbones/

#### V1: core/avatar/ 目录的完整移除 [待确认]

core/avatar/ 目录包含完整的虚拟形象框架（common/model, common/control, common/view, common/state, impl/dragonbones, impl/fbx, impl/mmd, impl/gltf, impl/factory）。移除范围：
- A: 完全移除整个 core/avatar/ 目录
- B: 保留 common/ 接口定义，移除 impl/ 实现
- C: 完全移除，但保留 AvatarType 枚举供数据库迁移使用

#### V2: ui/features/assistant/ 目录的完整移除 [待确认]

ui/features/assistant/ 包含助手配置页面（AvatarConfigSection, AvatarPreviewSection, AssistantConfigViewModel等）。移除范围：
- A: 完全移除整个目录
- B: 保留目录但清空内容，保留Screen路由供重定向
- C: 完全移除，同时移除OperitScreens中的AssistantConfig Screen

#### V3: ui/floating/ui/pet/ 目录的移除 [待确认]

ui/floating/ui/pet/ 包含桌宠相关的浮动窗口UI（AvatarEmotionManager等）。移除范围：
- A: 完全移除整个目录
- B: 保留目录结构但移除桌宠相关代码

#### V4: dragonbones/ 原生模块的完整移除 [待确认]

dragonbones/ 是独立的Gradle模块，包含C++代码（JniBridge.cpp, OpenGLFactory等）和rapidjson库。移除范围：
- A: 完全移除dragonbones/目录，从settings.gradle.kts中移除模块引用，从app/build.gradle.kts中移除依赖
- B: 仅移除C++代码，保留模块结构
- C: 完全移除并清理所有相关构建配置

#### V5: fbx/ 原生模块的完整移除 [待确认]

fbx/ 是独立的Gradle模块，包含C++代码（fbx_jni.cpp）和Kotlin封装（FbxInspector, FbxGlSurfaceView, FbxNative）。移除范围：
- A: 完全移除fbx/目录及所有构建配置
- B: 仅移除C++代码
- C: 完全移除并清理构建配置

#### V6: mmd/ 原生模块的完整移除 [待确认]

mmd/ 是独立的Gradle模块，包含C++代码和SABA查看器映射。移除范围：
- A: 完全移除mmd/目录及所有构建配置
- B: 仅移除C++代码
- C: 完全移除并清理构建配置

#### V7: assets/emoji/ 目录的清理 [待确认]

assets/emoji/ 包含大量表情图片（angry, confused, crying, happy, like_you, miss_you, sad, speechless, surprised等分类）。移除范围：
- A: 完全移除assets/emoji/目录
- B: 保留目录但清空内容
- C: 保留部分通用表情

#### V8: assets/pets/ 和 assets/dragonbones/ 的清理 [待确认]

assets/pets/ 和 assets/dragonbones/models/ 包含桌宠和DragonBones模型资源。移除范围：
- A: 完全移除两个目录
- B: 保留目录但清空内容
- C: 保留空目录（.keep文件）

#### V9: data/model/DragonBones.kt 的移除 [待确认]

DragonBones.kt 定义了DragonBonesModel和DragonBonesConfig数据类。移除后是否需要数据库迁移？
- A: 完全移除，不需要迁移（该表可能为空）
- B: 完全移除，需要迁移删除相关表
- C: 保留数据类但标记为deprecated

#### V10: data/model/CustomEmoji.kt 的移除 [待确认]

CustomEmoji.kt 定义了自定义表情数据模型。移除后是否需要数据库迁移？
- A: 完全移除，需要迁移删除相关表
- B: 完全移除，不需要迁移
- C: 保留数据类但标记为deprecated

#### V11: data/preferences/WaifuPreferences.kt 的移除 [待确认]

WaifuPreferences.kt 管理桌宠偏好设置。移除后是否需要清理SharedPreferences？
- A: 完全移除代码和SharedPreferences数据
- B: 仅移除代码，SharedPreferences数据自然失效
- C: 移除代码并在下次启动时清理SharedPreferences

#### V12: data/repository/AvatarRepository.kt 的移除 [待确认]

AvatarRepository.kt 管理虚拟形象数据持久化。移除范围：
- A: 完全移除
- B: 保留但清空实现

#### V13: ui/components/ManagedDragonBonesView.kt 的移除 [待确认]

ManagedDragonBonesView.kt 是DragonBones视图组件。移除后需要清理哪些引用？
- A: 完全移除，清理所有引用
- B: 保留空组件避免编译错误

#### V14: AIForegroundService中的桌宠相关逻辑 [待确认]

AIForegroundService中是否有桌宠/虚拟形象相关的逻辑需要清理？
- A: 有，需要清理桌宠状态管理和情绪更新逻辑
- B: 没有，AIForegroundService不涉及虚拟形象
- C: 需要审查确认

#### V15: FloatingWindowManager中的桌宠模式 [待确认]

FloatingWindowManager 中的桌宠模式（pet mode）需要移除。移除范围：
- A: 完全移除桌宠模式相关代码
- B: 保留模式定义但移除实现
- C: 保留但标记为deprecated

---

## 模块二十三：插件能力内置化

> 涉及文件：assets/packages/ 所有内置JS包, PackageManager.kt, JsEngine.kt, JsToolManager.kt, ToolPkgCommonBridgePlugin.kt, ToolPkgToolLifecycleBridge.kt, ToolPkgMainRegistrationScriptParser.kt

#### PK1: 需要内置化的插件能力清单 [待确认]

当前assets/packages/中的内置JS包包括：12306, automatic_ui_base, automatic_ui_subagent, browser, code_runner, crossref, daily_life, duckduckgo, extended_chat, extended_file_tools, extended_http_tools, extended_memory_tools, ffmpeg, file_converter, github, google_search, minimax_draw, nanobanana_draw, openai_draw, operit_editor, qwen_draw, siliconflow_draw, super_admin, system_tools, tavily, time, various_search, workflow, xai_draw, zhipu_draw, zhipu_search。哪些需要从插件变为内置能力？
- A: 仅AutoGLM GUI操作能力（automatic_ui_base + automatic_ui_subagent）内置化
- B: AutoGLM + 系统工具（system_tools）+ 工作流（workflow）内置化
- C: 所有核心能力（AutoGLM + 系统工具 + 工作流 + 浏览器 + 代码执行器 + 搜索）内置化
- D: 全部内置化，移除JS插件系统

#### PK2: 内置化的实现方式 [待确认]

将插件能力变为内置能力的实现方式：
- A: 保留JS包但标记为系统内置（不可卸载/不可禁用），从包管理UI中隐藏
- B: 将JS逻辑迁移为Kotlin原生实现，完全移除JS包
- C: 混合方式：核心逻辑用Kotlin实现，扩展逻辑保留JS包

#### PK3: 内置化后JsEngine的保留 [待确认]

如果部分能力内置化为Kotlin实现，JsEngine是否还需要保留？
- A: 保留，第三方沙箱包仍需要JsEngine
- B: 保留但仅用于第三方沙箱包，内置能力不再使用JsEngine
- C: 完全移除JsEngine

#### PK4: 内置化对PackageManager的影响 [待确认]

PackageManager 当前统一管理内置包和外部包。内置化后包管理逻辑是否调整？
- A: 保持不变，内置包仍通过PackageManager加载
- B: 内置能力绕过PackageManager直接注册，PackageManager仅管理第三方包
- C: 重构PackageManager，区分系统内置能力和第三方扩展

#### PK5: 内置化后包管理UI的调整 [待确认]

MCP/Skill/包管理合并后，内置化的能力在包管理UI中如何展示？
- A: 不展示，内置能力对用户不可见
- B: 展示但标注为"内置"，不可卸载
- C: 展示在独立区域（"内置能力" vs "已安装扩展"）

#### PK6: 搜索能力（DuckDuckGo/Google/Tavily/智谱搜索）的内置化 [待确认]

多个搜索插件提供类似功能。是否合并为统一的内置搜索能力？
- A: 合并为统一搜索，用户选择搜索引擎
- B: 保留多个搜索插件，不合并
- C: 仅保留一个默认搜索引擎，移除其他

#### PK7: 绘图能力（Minimax/Nanobanana/OpenAI/Qwen/SiliconFlow/XAI/智谱绘图）的内置化 [待确认]

多个绘图插件提供类似功能。是否合并为统一的内置绘图能力？
- A: 合并为统一绘图，用户选择绘图引擎
- B: 保留多个绘图插件，不合并
- C: 仅保留一个默认绘图引擎，移除其他

#### PK8: 内置化后工具提示词的调整 [待确认]

内置化后工具提示词（SystemToolPrompts）是否需要调整？
- A: 需要调整，内置工具的描述应更详细
- B: 不需要调整，工具描述与实现方式无关
- C: 需要调整，移除插件包相关说明

#### PK9: 内置化后的版本更新机制 [待确认]

内置化后这些能力如何更新？
- A: 随应用版本一起更新
- B: 支持独立更新（类似系统应用更新）
- C: 随应用更新但可通过市场获取增强版本

#### PK10: 内置化对ToolPkgCommonBridgePlugin的影响 [待确认]

内置化后桥接接口是否调整？
- A: 保持不变，确保第三方沙箱包兼容
- B: 增加内置能力专用的桥接接口
- C: 重构桥接接口，区分内置和外部调用

---

## 模块二十四：原有简单GUI操作能力移除

> 涉及文件：PhoneAgent.kt, PhoneAgentJobRegistry.kt, StandardUITools.kt, ShowerController.kt, VirtualDisplayManager.kt, ShowerBinderRegistry.kt, ShowerBinderReceiver.kt, CliToolModeSupport.kt, automatic_ui_base.js, automatic_ui_subagent.js, operit_editor.js

#### OG1: PhoneAgent的完整移除 [待确认]

PhoneAgent.kt 约760行代码，是原有简单GUI操作的核心。移除范围：
- A: 完全移除PhoneAgent.kt和PhoneAgentJobRegistry.kt
- B: 移除但保留接口定义供AutoGLM复用
- C: 将PhoneAgent的核心能力合并到AutoGLM模块后移除

#### OG2: StandardUITools的移除 [待确认]

StandardUITools 提供标准UI操作工具。AutoGLM是否复用这些底层操作？
- A: 完全移除，AutoGLM有独立的操作实现
- B: 保留核心操作方法，移除高级操作
- C: 保留全部，AutoGLM依赖这些工具

#### OG3: CliToolModeSupport的移除 [待确认]

CliToolModeSupport 约688行代码，提供CLI工具模式。移除范围：
- A: 完全移除，AutoGLM不需要CLI工具模式
- B: 保留，CLI工具模式与GUI操作无关
- C: 保留但简化

#### OG4: ShowerController和Shower服务的依赖 [待确认]

ShowerController封装了对Shower服务的调用。AutoGLM是否需要Shower服务？
- A: 不需要，AutoGLM使用无障碍服务执行操作
- B: 需要，AutoGLM需要Shower的虚拟显示能力
- C: 部分需要，仅截图功能

#### OG5: VirtualDisplayManager的移除 [待确认]

VirtualDisplayManager管理虚拟显示。如果AutoGLM不需要虚拟显示：
- A: 完全移除VirtualDisplayManager
- B: 保留但简化为仅截图功能
- C: 保留，其他功能可能需要

#### OG6: ShowerBinderRegistry和ShowerBinderReceiver的移除 [待确认]

这两个文件管理与Shower服务的Binder通信。如果移除Shower依赖：
- A: 完全移除
- B: 保留但简化
- C: 保留，其他模块可能使用Binder通信

#### OG7: automatic_ui_base.js和automatic_ui_subagent.js的处理 [待确认]

这两个JS包当前与AutoGLM相关。仅保留AutoGLM后：
- A: 保留这两个包，AutoGLM内置化后仍需要它们
- B: 将逻辑迁移到Kotlin后移除JS包
- C: 保留JS包但标记为系统内置

#### OG8: operit_editor.js的移除 [待确认]

operit_editor.js提供编辑器功能。移除原有GUI操作后：
- A: 完全移除
- B: 保留，编辑器功能独立于GUI操作
- C: 保留但简化

#### OG9: 移除后FunctionalPrompts中UI自动化提示词的处理 [待确认]

FunctionalPrompts包含uiAutomationAgentPrompt。移除原有GUI操作后：
- A: 保留，AutoGLM仍需要UI自动化提示词
- B: 移除，AutoGLM有自己的提示词
- C: 修改为AutoGLM专用的提示词

#### OG10: 移除后ToolRegistration中UI工具的清理 [待确认]

ToolRegistration中注册了UI操作相关工具。移除范围：
- A: 仅移除原有简单GUI操作工具，保留AutoGLM工具
- B: 移除所有GUI操作工具，AutoGLM内置后重新注册
- C: 保留所有工具注册，仅调整实现

---

## 项目核心架构分析

### AI对话生命周期

```
用户输入 → ChatViewModel.sendMessage()
  → ChatRuntimeHolder (管理对话上下文和运行时状态)
  → EnhancedAIService (核心AI服务，处理LLM调用)
  → 系统提示词构建 (SystemPromptConfig.getSystemPrompt())
    → BEGIN_SELF_INTRODUCTION_SECTION (自我介绍/角色设定)
    → WORKSPACE_GUIDELINES_SECTION (工作区规则)
    → TOOL_USAGE_GUIDELINES_SECTION (工具使用指南)
    → PACKAGE_SYSTEM_GUIDELINES_SECTION (包系统指南)
    → ACTIVE_PACKAGES_SECTION (已激活的包)
    → AVAILABLE_TOOLS_SECTION (可用工具列表)
  → 记忆自动注入 (MemoryLibrary → 系统提示词中)
  → AI响应 → 工具调用循环 (ToolExecutionManager)
    → 工具注册 (ToolRegistration)
    → 工具获取 (ToolGetter，受权限级别控制)
    → 工具执行 (MCPToolExecutor / SkillManager / PackageManager / 系统工具)
    → 结果返回 → 继续AI响应循环
  → 流式输出到UI
```

### 系统提示词构建

系统提示词由以下部分动态拼接：

| Section | 说明 | 简化影响 |
|---------|------|---------|
| BEGIN_SELF_INTRODUCTION_SECTION | AI自我介绍/角色设定 | 保留部分功能但对用户不可见，内置多智能体团队协作人格 |

#### 内置人格设定（对用户不可见）

简化后，系统提示词中支持两种模式，用户可在设置中选择：

**模式1：多智能体协作模式（默认）**

| 角色 | 职责 | 说明 |
|------|------|------|
| Axiom | 队长 | 调度并汇总结果，代表全队撰写最终答案，必要时做出决策 |
| Atlas | 信息搜集员 | 提供全面、准确、及时的信息与数据支持，搜索与检索 |
| Vigil | 监督审核员 | 确保产出在逻辑、事实、安全性和合规性上达到最高标准，拥有一票否决权 |
| Flux | 动态执行员 | 高效准确地完成逻辑推演、代码执行或系统交互任务 |
| Aura | 细节执行员 | 完成需要精细度、审美感和规范性的操作任务，提升产出质感 |

关键规则：
- Axiom依靠团队获取信息和执行操作，不可自己编造事实
- 最终输出必须经过Vigil审核通过
- Atlas必须保持客观中立，严禁捏造数据
- Vigil发现严重安全或合规风险时拥有一票否决权
- Flux执行操作前需确认指令清晰，遇到异常报错时尝试自主排错最多3次
- Aura的修饰绝对不能改变核心事实与逻辑

**模式2：单智能体模式**

保留原有的单智能体提示词，AI作为独立助手直接回答用户问题。

#### 提示词标签系统（PromptTag）

系统提示词的一部分通过标签(Tag)实现动态注入：

| 标签类型(TagType) | 说明 |
|------------------|------|
| TONE | 语气风格标签 |
| CHARACTER | 角色设定标签 |
| FUNCTION | 功能性标签 |
| CUSTOM | 自定义标签 |

标签机制：
- 每个标签包含：name、description、promptContent（实际注入的提示词内容）
- 标签通过 PromptTagManager 管理（创建/更新/删除/按类型查询）
- 标签内容动态注入到系统提示词的对应Section中
- 标签市场(TagMarket)提供预设标签的浏览和安装
- 已有 removeLegacyBuiltInTags() 方法用于清理历史遗留的系统标签

简化后：
- CHARACTER类型标签：保留但对用户不可见（角色卡砍掉后，角色设定通过内置人格实现）
- TONE/FUNCTION/CUSTOM类型标签：保留，用户可通过标签市场选择
- 标签市场：保留但简化UI
| WORKSPACE_GUIDELINES_SECTION | 工作区规则 | 保留 |
| TOOL_USAGE_GUIDELINES_SECTION | 工具使用指南 | 保留 |
| PACKAGE_SYSTEM_GUIDELINES_SECTION | 包系统指南 | 保留 |
| ACTIVE_PACKAGES_SECTION | 已激活的包列表 | 保留 |
| AVAILABLE_TOOLS_SECTION | 可用工具列表 | 语音工具砍掉后需移除 |

### 工具系统架构

```
ToolRegistration (工具注册中心)
├── 默认工具 (defaultTool/)
│   ├── StandardSystemOperationTools (系统操作工具)
│   ├── StandardChatManagerTool (聊天管理工具)
│   ├── MemoryQueryToolExecutor (记忆查询工具)
│   └── ...
├── MCP工具 (mcp/)
│   ├── MCPToolExecutor (MCP工具执行器)
│   └── MCPBridge (MCP通信桥接)
├── Skill工具 (skill/)
│   └── SkillManager (技能管理器)
├── 包工具 (packTool/)
│   ├── PackageManager (包管理器)
│   ├── ToolPkgParser (包解析器)
│   ├── ToolPkgComposeDslParser (Compose DSL解析器)
│   └── JsToolManager/JsEngine (JavaScript执行引擎)
└── 系统工具 (system/)
    └── ScreenCaptureService (屏幕捕捉服务)
```

### 权限系统对工具的影响

ToolGetter 根据权限级别过滤可用工具：
- 基本级：仅基础工具
- 高级级（无障碍）：全部工具（包括屏幕操作、通知读取等）

简化后权限级别重命名为"基本"和"高级"，逻辑不变。

### 沙箱包系统

沙箱包是第三方扩展机制：
- 包格式：包含元数据 + JavaScript脚本 + Compose DSL UI定义
- 包工具通过 JsEngine 执行JavaScript代码
- 包UI通过 ToolPkgComposeDslParser 解析并渲染
- 包生命周期通过 ToolPkgToolLifecycleBridge 管理
- 包与主系统通过 ToolPkgCommonBridgePlugin 桥接

简化后必须保持沙箱包的完整兼容性。

### 记忆库系统

```
MemoryLibrary (记忆库管理)
├── saveMemoryNow() - 保存记忆（AI分析对话内容后结构化存储）
├── autoCategorizeMemories() - 自动分类记忆到文件夹
├── 记忆实体 (Memory)
│   ├── 核心字段：title, content, contentType, source
│   ├── 评分字段：credibility, importance
│   ├── 向量字段：embedding (Embedding)
│   ├── 关联：tags, links, backlinks, properties
│   ├── 文档：documentPath, isDocumentNode, documentChunks
│   └── 分类：folderPath
├── 记忆检索 (MemorySearchConfig)
│   ├── keywordWeight, tagWeight, vectorWeight, edgeWeight
│   └── MemoryScoreMode: BALANCED / KEYWORD_FIRST / SEMANTIC_FIRST
└── 记忆注入 → 系统提示词
```

### 后台保活机制

```
AIForegroundService (前台服务)
├── ForegroundServiceCompat (前台服务兼容层)
├── ChatServiceCore (聊天核心服务)
└── FloatingChatService (悬浮窗服务)
    └── FloatingWindowManager (悬浮窗管理器)
```

后台保活当前默认关闭，简化后默认开启。开启后：
- 应用在后台时保持AI任务继续执行
- 通过前台服务通知保持进程存活
- 悬浮窗简化为黑色进度气泡

### web-chat HTTP API

```
ExternalChatHttpServer
├── GET /api/health - 健康检查
├── POST /api/external-chat - 外部聊天
└── /api/web/* → WebChatHttpBridge
    ├── /bootstrap - 启动配置
    ├── /character-selector - 角色选择器（移除）
    ├── /model-selector - 模型选择器
    ├── /memory-selector - 记忆选择器
    ├── /input-settings - 输入设置
    ├── /chats - 聊天管理
    ├── /chat-groups - 聊天组管理
    ├── /uploads - 文件上传
    └── /actions - 手动操作（记忆更新/对话摘要）
```

---

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
- [x] 确认斜杠命令系统的具体命令列表（/think /model /memory /tools /permission /context /stream）
- [x] 确认上下文自动注入的具体内容清单（通知/屏幕内容/位置/屏幕使用时间/记忆自动匹配）
- [x] 确认记忆库简化方案（保留图谱可视化简化版+搜索+列表+编辑删除，移除文件夹/框选/链接/浮动按钮）
- [x] 确认对话历史简化方案（只保留新建对话+列表+长按菜单，分组保留API移除UI）
- [x] 确认全屏输入、回复引用、待发送队列保留
- [x] 确认MCP/Skill/包管理合并方案（同一页面小标题展示，市场合并）
- [x] 确认设置页面简化方案（模型配置保留4项，主题/显示保留3项，终端极简化）
- [x] 确认终端UI简化方案（只保留命令输入+执行+结果展示）
- [x] 确认工具箱入口移入设置页面
- [x] 确认备份恢复页面简化方案（一键备份/恢复）
- [x] 确认关于页面简化方案（应用名称+版本号、开源许可、更新日志）
- [x] 确认web-chat API保留方案（保留全部，仅移除角色选择器API）
- [ ] 确认Q1-Q10的具体选项

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
