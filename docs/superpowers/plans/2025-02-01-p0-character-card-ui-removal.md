# 角色卡系统UI移除 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 移除角色卡系统在客户端UI中的所有入口和交互组件，同时完整保留后端数据层、ViewModel状态和API端点，确保后端功能不受影响。
**Architecture:** 本任务仅涉及UI层的裁剪，不修改后端逻辑。移除范围涵盖四个入口文件：OperitScreens路由、DrawerContent侧边栏、AIChatScreen选择器面板、ChatScreenHeader切换按钮。保留所有Manager、DAO字段、API端点和ViewModel中的角色卡状态管理代码。
**Tech Stack:** Kotlin, Jetpack Compose

---

## Task 1: 移除 ChatScreenHeader 中的角色卡切换按钮

**Files:**
- `ui/features/chat/components/ChatScreenHeader.kt`
- `ui/features/chat/components/ChatHeader.kt`
- `ui/features/chat/components/ChatScreenContent.kt`

**Steps:**
- [ ] 在 `ChatHeader.kt` 中，移除 `activeCharacterName`、`activeCharacterAvatarUri`、`onCharacterClick` 三个参数，以及函数体内 "Character Switcher" 区域的整个 `Row` 组件（第153-196行，包含头像Box和角色名Text）
- [ ] 在 `ChatScreenHeader.kt` 中，移除 `onCharacterSwitcherClick` 参数；移除 `characterCardManager`、`characterGroupCardManager`、`activePromptManager`、`userPreferencesManager` 的初始化；移除 `activePrompt`、`activeCharacterCard`、`activeCharacterGroup`、`activeCardAvatarUri`、`activeGroupAvatarUri`、`activeGroupFallbackMemberCardId`、`activeGroupFallbackMemberAvatarUri`、`activeCharacterAvatarUri` 等状态收集代码；移除传递给 `ChatHeader` 的 `activeCharacterName`、`activeCharacterAvatarUri`、`onCharacterClick` 三个参数
- [ ] 在 `ChatScreenContent.kt` 中，移除 `ChatScreenContent` 函数签名中的 `showCharacterSelector`、`onShowCharacterSelectorChange`、`onSwitchCharacter`、`onOpenCharacterSettings` 四个参数；移除两处 `ChatScreenHeader` 调用中的 `onCharacterSwitcherClick = { onShowCharacterSelectorChange(true) }` 参数
- [ ] 清理 `ChatScreenHeader.kt` 中因移除角色卡逻辑而不再需要的 import（如 `CharacterCardManager`、`CharacterGroupCardManager`、`ActivePromptManager`、`ActivePrompt`、`UserPreferencesManager`、`flowOf` 等）
- [ ] 清理 `ChatHeader.kt` 中不再需要的 import（如 `coil.compose.rememberAsyncImagePainter`、`android.net.Uri`、`Icons.Rounded.Person` 等）

## Task 2: 移除 AIChatScreen 中的角色卡选择器面板

**Files:**
- `ui/features/chat/screens/AIChatScreen.kt`

**Steps:**
- [ ] 移除 `showCharacterSelector` 状态变量声明（第915行 `var showCharacterSelector by remember { mutableStateOf(false) }`）
- [ ] 移除 `onSwitchCharacter` 回调定义（第783-787行）
- [ ] 移除 `CharacterSelectorPanel` 组件调用（第1189-1194行）
- [ ] 移除传递给 `ChatScreenContent` 的 `showCharacterSelector`、`onShowCharacterSelectorChange`、`onSwitchCharacter`、`onOpenCharacterSettings` 参数（第1038-1040行附近）
- [ ] 清理不再需要的 import（如 `CharacterSelectorPanel`、`CharacterSelectorTarget`）

## Task 3: 移除 DrawerContent 中的角色卡选择入口

**Files:**
- `ui/main/components/DrawerContent.kt`

**Steps:**
- [ ] 检查 `DrawerContent.kt` 中是否存在角色卡相关的导航入口或快捷操作项；如存在，移除对应的 `NavItem` 引用和UI组件
- [ ] 移除任何与角色卡选择相关的点击处理逻辑和导航调用
- [ ] 清理因移除角色卡入口而不再需要的 import

## Task 4: 移除 OperitScreens 中的角色卡管理页面路由

**Files:**
- `ui/main/screens/OperitScreens.kt`

**Steps:**
- [ ] 在 `ModelPromptsSettings` Screen 的 `Content` 中，移除 `onNavigateToPersonaGeneration` 回调及其 `navigateTo(PersonaCardGeneration)` 调用（第951行）
- [ ] 移除 `PersonaCardGeneration` Screen 对象定义（第860行附近），包括其整个 `data object` 块和 `Content` 实现
- [ ] 移除其他 Screen 中对 `PersonaCardGeneration` 的导航引用（如第564行 `navigateToPersonaCardGeneration = { navigateTo(PersonaCardGeneration) }`）
- [ ] 清理 `PersonaCardGenerationScreen` 的 import 语句
- [ ] 在 `ModelPromptsSettingsScreen` 调用处，移除 `onNavigateToPersonaGeneration` 参数传递

## Task 5: 移除 ModelPromptsSettingsScreen 中的角色卡Tab

**Files:**
- `ui/features/settings/screens/ModelPromptsSettingsScreen.kt`

**Steps:**
- [ ] 移除 `CharacterCardTab` 可组合函数的调用（在Tab布局中移除角色卡Tab页）
- [ ] 移除 `CharacterCardTab` 函数定义（第1951行起）
- [ ] 移除与角色卡Tab相关的状态管理代码（如 `characterCards` 列表、`onAddCharacterCard`、`onEditCharacterCard`、`onDeleteCharacterCard`、`onDuplicateCharacterCard`、`onResetDefaultCharacterCard`、`onSetActiveCharacterCard`、`onImportTavernCard`、`onImportColorQrCode`、`onScanColorQrCode`、`onExportCharacterCard` 等回调）
- [ ] 移除角色卡相关的ViewModel交互逻辑（如加载角色卡列表、切换活跃角色卡等）
- [ ] 清理不再需要的 import（如 `CharacterCard`、`CharacterCardManager`、`CharacterCardSortOption` 等）

## Task 6: 移除 ChatHistorySelector 中的角色卡UI元素

**Files:**
- `ui/features/chat/components/ChatHistorySelector.kt`

**Steps:**
- [ ] 移除 `autoSwitchCharacterCard` 和 `autoSwitchChatOnCharacterSelect` 两个参数及其对应的UI开关（第446-448行参数声明，第1723-1791行UI开关组件）
- [ ] 移除 `onAutoSwitchCharacterCardChange` 和 `onAutoSwitchChatOnCharacterSelectChange` 回调参数
- [ ] 移除角色卡头像显示逻辑（第2042-2061行附近的 `characterCardId` 和 `characterCardAvatarUri` 状态）
- [ ] 简化 `onNewChat`、`onUpdateChatBinding`、`onCreateGroup`、`onUpdateGroupName`、`onDeleteGroup` 等回调中的 `characterCardName` 参数传递（保留参数签名但不显示UI选择器）
- [ ] 清理因移除角色卡UI而不再需要的 import（如 `CharacterCardManager`、`UserPreferencesManager`、`rememberAsyncImagePainter` 等）

## Task 7: 移除 ClassicChatSettingsBar 中的角色卡绑定相关UI

**Files:**
- `ui/features/chat/components/style/input/classic/ClassicChatSettingsBar.kt`

**Steps:**
- [ ] 移除 `characterCardBoundChatModelConfigId`、`characterCardBoundChatModelIndex`、`characterCardBoundMemoryProfileId` 三个参数
- [ ] 移除 `showCharacterCardBindingSwitchConfirm` 状态变量（第148行）
- [ ] 移除角色卡绑定切换确认对话框的调用和逻辑
- [ ] 清理相关的 import（如 `CharacterCardModelBindingSwitchConfirmDialog`、`CharacterCardMemoryBindingSwitchConfirmDialog`）

## Task 8: 移除角色卡专用UI组件文件

**Files:**
- `ui/features/chat/components/CharacterSelectorPanel.kt`
- `ui/features/settings/components/CharacterCardDialog.kt`
- `ui/features/settings/components/CharacterCardAssignDialog.kt`
- `ui/features/chat/components/style/input/common/CharacterCardMemoryBindingSwitchConfirmDialog.kt`
- `ui/features/chat/components/style/input/common/CharacterCardModelBindingSwitchConfirmDialog.kt`

**Steps:**
- [ ] 删除 `CharacterSelectorPanel.kt` 文件（角色卡选择器面板组件，521行）
- [ ] 删除 `CharacterCardDialog.kt` 文件（角色卡编辑对话框组件，1800行）
- [ ] 删除 `CharacterCardAssignDialog.kt` 文件（角色卡分配对话框组件，197行）
- [ ] 删除 `CharacterCardMemoryBindingSwitchConfirmDialog.kt` 文件（角色卡记忆绑定切换确认对话框，33行）
- [ ] 删除 `CharacterCardModelBindingSwitchConfirmDialog.kt` 文件（角色卡模型绑定切换确认对话框，33行）

## Task 9: 编译验证与清理

**Steps:**
- [ ] 执行项目编译（`./gradlew assembleDebug` 或等效命令），确认无编译错误
- [ ] 检查并修复所有因移除角色卡UI而导致的未使用import警告
- [ ] 检查并修复所有因参数签名变更而导致的调用点编译错误
- [ ] 确认保留的后端文件未被修改：`CharacterCardManager.kt`、`CharacterCard.kt`、`PromptTag.kt`、`PromptTagManager.kt`、`ActivePromptManager.kt`、`PromptVersionManager.kt`、`CharacterCardToolAccessResolver.kt`、`CharacterGroupCardManager.kt`、`PersonaCardChatHistoryManager.kt`、`ChatViewModel.kt`（角色卡状态部分）、`ChatDao.kt`（characterId字段）、`WebChatHttpBridge.kt`（角色卡API端点）、`ToolPkgCommonBridgePlugin.kt`（角色卡桥接字段）
