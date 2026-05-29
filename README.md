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

以下每个问题都提供了具体选项，请逐一确认：

#### Q1: 图谱可视化简化方案 [已确认: B]

只读展示+缩放/平移，移除节点拖拽和图谱编辑。

#### Q2: 记忆自动检索匹配的触发时机 [已确认: A+B]

每次用户发送消息时自动检索 + AI判断需要记忆时也触发。

#### Q3: 记忆检索匹配策略权重配置 [已确认]

- 权重配置：A（语义优先）keywordWeight=5.0, tagWeight=2.0, vectorWeight=8.0, edgeWeight=1.0
- importance/credibility：B（纳入，低权重）importance=1.0, credibility=0.5
- 时间衰减：D（作为一项权重因子，而非必须遵循的规则）

#### Q4: 自动注入的记忆条数上限 [已确认: C]

最多5条。

#### Q5: 砍掉功能后的数据迁移方案 [待重新提问]

此问题需要更具体的描述后重新提问。

#### Q6: 设置页面中工具箱的展示方式 [已确认: A]

作为设置页面的一个子页面"工具"，点击后展示工具列表。

#### Q7: 侧边栏历史对话列表的展示方式 [已确认: A+B结合]

按日期分组展示（今天/昨天/更早），每组内按时间倒序，长按弹出删除/编辑菜单。

#### Q8: 侧边栏预留按钮区的展示方式 [已确认: B，需修正理解]

垂直排列的列表项（带图标+文字）。参考主流AI应用程序，新建对话下方会有模式选择等快捷入口，我们的预留按钮区就是这类功能入口（工作流、日历、待办等）。

#### Q9: 斜杠命令面板的展示方式 [已确认: A]

参考shadcn/ui Command组件，在输入框上方弹出下拉列表，显示命令名称+描述，支持模糊搜索。

#### Q10: MCP/Skill/包管理合并页面的市场展示 [待重新提问]

此问题需要更具体的描述后重新提问。

#### Q11: 后台保活默认开启后的行为 [待重新提问]

此问题需要更具体的描述后重新提问。

#### Q12: 系统默认值调整确认 [待重新提问]

此问题需要更具体的描述后重新提问。

#### Q13: 记忆检索评分模式选择 [待重新提问]

此问题需要更具体的描述后重新提问。

#### Q14: 向量嵌入配置 [已确认: C]

优先本地嵌入模型，本地不可用时回退到云端嵌入API。

#### Q15: 外部HTTP聊天设置是否保留 [已确认: A]

保留，与web-chat API保留策略一致。

---

## 深层功能确认清单

以下问题涉及核心架构层面的简化决策：

#### D1: 系统提示词自我介绍Section [已确认]

保留部分角色/人格功能，但对用户不可见。内置多智能体团队协作人格（Axiom/Atlas/Vigil/Flux/Aura）。

#### D2: 子任务代理(SUBTASK_AGENT) [待确认]

当前有子任务代理提示词模板，用于工作流中的子任务执行。工作流保留后，子任务代理也保留。确认？

#### D3: 工作流触发机制 [待确认]

当前工作流支持定时触发(cron)和语音唤醒触发。语音砍掉后：
- A: 只保留定时触发
- B: 保留定时触发 + 其他触发方式（如应用启动时、特定事件等）

#### D4: 工具提示词中的语音工具 [待确认]

SystemToolPrompts 中包含了语音相关工具的提示词（TTS/STT等），砍掉语音后需要从工具提示词中移除这些描述。确认？

#### D5: 角色卡对聊天管理工具的影响 [待确认]

StandardChatManagerTool 中可能包含角色卡相关的聊天管理功能（如切换角色卡、群聊管理等），砍掉角色卡后需要清理这些功能。确认？

#### D6: 记忆自动保存 [待确认]

当前 MemoryLibrary 有自动保存功能（autoCategorizeMemories + 定时自动保存），简化后是否保持自动保存行为不变？

#### D7: 记忆库的文档节点功能 [待确认]

Memory 实体支持 documentPath/isDocumentNode/documentChunks，这是文档嵌入功能。是否保留？
- A: 保留，文档嵌入是有用功能
- B: 移除，简化记忆库

#### D8: MCP/Skill/包管理的API重定向 [待确认]

合并这三个页面后，原来的独立导航路由需要重定向到新的统一页面。沙箱包通过 ToolPkgCommonBridgePlugin 访问包管理功能，这些桥接接口是否需要保持不变？
- A: 保持桥接接口不变，只改UI路由
- B: 桥接接口也需要调整

#### D9: 上下文自动注入的实现方式 [待确认]

通知、屏幕内容等自动注入到上下文中，是通过什么方式注入？
- A: 系统提示词注入（在SYSTEM_PROMPT中添加上下文信息section）
- B: 作为用户消息的隐藏附件注入
- C: 两者结合

#### D10: 工具调用展示简化后的交互 [待确认]

工具调用展示简化为一行灰/绿/红文字后，用户是否还能点击展开查看工具调用的详细参数和结果？
- A: 可以点击展开查看详情
- B: 不可以，只显示一行状态文字

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
