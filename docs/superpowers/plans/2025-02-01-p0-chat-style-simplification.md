# 聊天样式简化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将聊天样式精简为仅保留"命令框"(Cursor)样式和"智能体模式"(Agent)输入模式，移除所有Bubble样式、Classic输入模式、以及命令框/输入框的全部样式配置项（液态玻璃、水玻璃、自定义颜色、透明、浮动等），统一使用系统默认。
**Architecture:** 删除Bubble样式和Classic输入模式的整个目录及所有引用；将ChatStyle枚举精简为仅CURSOR（移除BUBBLE分支）；将输入模式硬编码为Agent；移除所有cursor/bubble/input的液态玻璃、水玻璃、自定义颜色、透明、浮动等配置项的偏好键、Flow属性和UI设置项；在ChatScreenContent和ChatArea中硬编码Cursor样式，移除所有样式分支逻辑。
**Tech Stack:** Kotlin, Jetpack Compose

---

## Task 1: 删除Bubble样式目录及Classic输入模式目录

- [ ] 删除 `ui/features/chat/components/style/bubble/` 整个目录（包含 BubbleStyleChatMessage.kt、BubbleUserMessageComposable.kt、BubbleAiMessageComposable.kt、BubbleImageBackgroundSurface.kt）
- [ ] 删除 `ui/features/chat/components/style/input/classic/` 整个目录（包含 ClassicChatInputSection.kt、ClassicChatSettingsBar.kt）

**Files:**
- 删除: `ui/features/chat/components/style/bubble/BubbleStyleChatMessage.kt`
- 删除: `ui/features/chat/components/style/bubble/BubbleUserMessageComposable.kt`
- 删除: `ui/features/chat/components/style/bubble/BubbleAiMessageComposable.kt`
- 删除: `ui/features/chat/components/style/bubble/BubbleImageBackgroundSurface.kt`
- 删除: `ui/features/chat/components/style/input/classic/ClassicChatInputSection.kt`
- 删除: `ui/features/chat/components/style/input/classic/ClassicChatSettingsBar.kt`

**Steps:**
1. 删除 `bubble/` 目录下全部4个文件
2. 删除 `classic/` 目录下全部2个文件
3. 确认目录已清空后删除空目录

---

## Task 2: 精简ChatStyle枚举，移除BUBBLE分支

- [ ] 将 `ChatArea.kt` 中的 `ChatStyle` 枚举精简为仅保留 `CURSOR`
- [ ] 移除 `ChatArea.kt` 中所有 `ChatStyle.BUBBLE` 分支逻辑（消息渲染、加载指示器等）

**Files:**
- `ui/features/chat/components/ChatArea.kt`（第147-150行 ChatStyle枚举，第553行、第781行等 BUBBLE分支）

**Steps:**
1. 修改 `ChatStyle` 枚举，移除 `BUBBLE` 值，仅保留 `CURSOR`
2. 在 `ChatArea` composable 中，移除 `chatStyle` 参数（不再需要动态切换），将所有 `when(chatStyle)` 分支替换为直接使用 `CursorStyleChatMessage` 的逻辑
3. 移除加载指示器中的 `ChatStyle.BUBBLE` 分支（第553-567行），仅保留 CURSOR 分支的加载指示器逻辑
4. 移除 `ChatArea` 签名中所有 bubble 相关参数：`bubbleUserBubbleLiquidGlass`、`bubbleUserBubbleWaterGlass`、`bubbleAiBubbleLiquidGlass`、`bubbleAiBubbleWaterGlass`、`bubbleUserImageStyle`、`bubbleAiImageStyle`、`bubbleUserRoundedCornersEnabled`、`bubbleAiRoundedCornersEnabled`、`bubbleUserContentPaddingLeft`、`bubbleUserContentPaddingRight`、`bubbleAiContentPaddingLeft`、`bubbleAiContentPaddingRight`
5. 移除 `ChatArea` 签名中 `cursorUserBubbleLiquidGlass`、`cursorUserBubbleWaterGlass` 参数
6. 移除 `ChatArea` 内部对 `BubbleStyleChatMessage` 的 import
7. 将 `CursorStyleChatMessage` 调用中的 `userMessageLiquidGlassEnabled` 和 `userMessageWaterGlassEnabled` 参数硬编码为 `false`

---

## Task 3: 硬编码ChatScreenContent为Cursor样式

- [ ] 移除 `ChatScreenContent.kt` 中的 `chatStyle` 参数及所有bubble/cursor样式配置参数
- [ ] 移除 `ChatScreenContent` 传递给 `ChatArea` 的所有bubble相关参数

**Files:**
- `ui/features/chat/components/ChatScreenContent.kt`（第119-139行参数签名，第283行、第399行、第772行 chatStyle传递）

**Steps:**
1. 从 `ChatScreenContent` 函数签名中移除 `chatStyle: ChatStyle` 参数
2. 移除签名中所有 bubble 相关参数：`bubbleUserBubbleLiquidGlass`、`bubbleUserBubbleWaterGlass`、`bubbleAiBubbleLiquidGlass`、`bubbleAiBubbleWaterGlass`、`bubbleUserImageStyle`、`bubbleAiImageStyle`、`bubbleUserRoundedCornersEnabled`、`bubbleAiRoundedCornersEnabled`、`bubbleUserContentPaddingLeft`、`bubbleUserContentPaddingRight`、`bubbleAiContentPaddingLeft`、`bubbleAiContentPaddingRight`
3. 移除签名中 `cursorUserBubbleLiquidGlass`、`cursorUserBubbleWaterGlass` 参数
4. 在所有向 `ChatArea` 传递参数的调用点，移除 `chatStyle` 和所有已删除的 bubble/cursor 样式参数
5. 移除对 `BubbleImageStyleConfig` 的 import

---

## Task 4: 硬编码AIChatScreen为Cursor样式和Agent输入模式

- [ ] 移除 `AIChatScreen.kt` 中对 chatStyle/inputStyle 的动态读取和分支逻辑
- [ ] 移除所有 bubble/cursor/input 样式配置的 collectAsState 读取
- [ ] 移除 ClassicChatInputSection 和 ClassicChatSettingsBar 的引用

**Files:**
- `ui/features/chat/screens/AIChatScreen.kt`（第59-65行 import，第171-288行样式状态读取，第597-625行颜色分支，第1054-1055行 Classic分支，第1893-1982行 输入模式分支）

**Steps:**
1. 移除 `ClassicChatInputSection` 和 `ClassicChatSettingsBar` 的 import（第60行、第65行）
2. 移除 `BubbleImageStyleConfig` 的 import（第67行）
3. 移除所有 chatStyle 相关的状态读取（第191-195行 `chatStyleSetting`/`chatStyle`），硬编码为 Cursor
4. 移除所有 inputStyle 相关的状态读取（第198-201行），硬编码为 Agent
5. 移除所有 bubble 样式配置的 collectAsState 读取（第209-288行，包括 bubbleUserBubbleLiquidGlass、bubbleUserBubbleWaterGlass、bubbleAiBubbleLiquidGlass、bubbleAiBubbleWaterGlass、bubbleUserBubbleColorValue、bubbleAiBubbleColorValue、bubbleUserTextColorValue、bubbleAiTextColorValue、bubbleUserUseImage、bubbleAiUseImage、bubbleUserImageUri、bubbleAiImageUri、所有 bubbleImageCrop/Repeat/Scale 参数、bubbleImageRenderMode、bubbleUserRoundedCornersEnabled、bubbleAiRoundedCornersEnabled、bubbleContentPadding 等）
6. 移除 cursor 样式配置的 collectAsState 读取（第202-208行 cursorUserBubbleFollowTheme、cursorUserBubbleLiquidGlass、cursorUserBubbleWaterGlass，第221-222行 cursorUserBubbleColorValue）
7. 移除 chatInput 样式配置的 collectAsState 读取（第171-177行 chatInputTransparent、chatInputFloating、chatInputLiquidGlass、chatInputWaterGlass）
8. 简化消息颜色逻辑（第597-625行），移除 `ChatStyle.BUBBLE` 分支，统一使用 Cursor 样式的默认颜色
9. 移除 `ClassicChatSettingsBar` 调用（第1054-1055行），移除 inputStyle 的 Classic 分支判断
10. 将输入区域硬编码为 `AgentChatInputSection`，移除 Classic 分支（第1893-1982行）
11. 移除向 `ChatScreenContent` 传递的 `chatStyle` 和所有已删除的 bubble/cursor 样式参数
12. 移除向 `AgentChatInputSection` 传递的 `chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass` 参数

---

## Task 5: 移除UserPreferencesManager中的样式配置偏好

- [ ] 移除 `UserPreferencesManager.kt` 中所有 bubble/cursor/input 样式相关的偏好键、Flow属性和写入方法

**Files:**
- `data/preferences/UserPreferencesManager.kt`（第123-233行偏好键定义，第257-261行常量，第524-541行 chatInput Flow，第580-715行 chatStyle/bubble Flow，第1312-1323行写入逻辑，第1472-1483行重置逻辑）

**Steps:**
1. 移除偏好键定义：`CHAT_INPUT_TRANSPARENT`、`CHAT_INPUT_FLOATING`、`CHAT_INPUT_LIQUID_GLASS`、`CHAT_INPUT_WATER_GLASS`（第123-126行）
2. 移除偏好键定义：`CHAT_STYLE`、`INPUT_STYLE`（第149-150行）
3. 移除偏好键定义：`BUBBLE_SHOW_AVATAR`、`BUBBLE_WIDE_LAYOUT_ENABLED`（第152-154行）
4. 移除偏好键定义：`CURSOR_USER_BUBBLE_FOLLOW_THEME`、`CURSOR_USER_BUBBLE_LIQUID_GLASS`、`CURSOR_USER_BUBBLE_WATER_GLASS`、`CURSOR_USER_BUBBLE_COLOR`（第156-161行）
5. 移除偏好键定义：`BUBBLE_USER_BUBBLE_LIQUID_GLASS`、`BUBBLE_USER_BUBBLE_WATER_GLASS`、`BUBBLE_AI_BUBBLE_LIQUID_GLASS`、`BUBBLE_AI_BUBBLE_WATER_GLASS`、`BUBBLE_USER_BUBBLE_COLOR`、`BUBBLE_AI_BUBBLE_COLOR`、`BUBBLE_USER_TEXT_COLOR`、`BUBBLE_AI_TEXT_COLOR`（第163-173行）
6. 移除偏好键定义：所有 bubble 字体相关键（`BUBBLE_USER_USE_CUSTOM_FONT`、`BUBBLE_USER_FONT_TYPE`、`BUBBLE_USER_SYSTEM_FONT_NAME`、`BUBBLE_USER_CUSTOM_FONT_PATH`、`BUBBLE_AI_USE_CUSTOM_FONT`、`BUBBLE_AI_FONT_TYPE`、`BUBBLE_AI_SYSTEM_FONT_NAME`、`BUBBLE_AI_CUSTOM_FONT_PATH`）（第175-187行）
7. 移除偏好键定义：所有 bubble 图片相关键（`BUBBLE_USER_USE_IMAGE`、`BUBBLE_AI_USE_IMAGE`、`BUBBLE_USER_IMAGE_URI`、`BUBBLE_AI_IMAGE_URI`、所有 CROP/REPEAT/SCALE 键）（第188-219行）
8. 移除偏好键定义：`BUBBLE_IMAGE_RENDER_MODE`、`BUBBLE_ROUNDED_CORNERS_ENABLED`、`BUBBLE_AI_ROUNDED_CORNERS_ENABLED`、所有 `BUBBLE_CONTENT_PADDING` 键（第221-233行）
9. 移除常量：`CHAT_STYLE_BUBBLE`、`INPUT_STYLE_CLASSIC`（第258行、第260行）
10. 移除 Flow 属性：`chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass`（第524-541行）
11. 移除 Flow 属性：`chatStyle`、`inputStyle`（第580-587行）
12. 移除 Flow 属性：`bubbleShowAvatar`、`bubbleWideLayoutEnabled`（第590-595行）
13. 移除 Flow 属性：`cursorUserBubbleFollowTheme`、`cursorUserBubbleLiquidGlass`、`cursorUserBubbleWaterGlass`、`cursorUserBubbleColor`（第600-615行）
14. 移除 Flow 属性：所有 bubble LiquidGlass/WaterGlass/Color/TextColor/Font/Image 相关 Flow（第620-715行）
15. 移除 Flow 属性：所有 bubble Image Crop/Repeat/Scale/RenderMode/RoundedCorners/ContentPadding 相关 Flow
16. 在写入/更新方法中，移除对已删除偏好键的写入逻辑（第1312-1323行等）
17. 在重置方法中，移除对已删除偏好键的重置逻辑（第1472-1483行等）
18. 保留 `CHAT_STYLE_CURSOR` 和 `INPUT_STYLE_AGENT` 常量以备向后兼容读取（或一并移除，视调用方清理情况决定）

---

## Task 6: 精简ThemePreferenceSnapshot

- [ ] 移除 `ThemePreferenceSnapshot.kt` 中所有 bubble/cursor/input 样式配置字段

**Files:**
- `data/preferences/ThemePreferenceSnapshot.kt`

**Steps:**
1. 移除字段：`chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass`（第18-21行）
2. 移除字段：`chatStyle`、`inputStyle`（第22-23行）
3. 移除字段：`bubbleShowAvatar`、`bubbleWideLayoutEnabled`（第24-25行）
4. 移除字段：`cursorUserBubbleFollowTheme`、`cursorUserBubbleColor`（第26-27行）
5. 移除字段：`bubbleUserBubbleColor`、`bubbleAiBubbleColor`、`bubbleUserTextColor`、`bubbleAiTextColor`（第28-31行）
6. 移除字段：`bubbleUserUseImage`、`bubbleAiUseImage`、`bubbleUserImageUri`、`bubbleAiImageUri`（第32-35行）
7. 移除字段：`bubbleImageRenderMode`、`bubbleUserRoundedCornersEnabled`、`bubbleAiRoundedCornersEnabled`、`bubbleUserContentPaddingLeft`、`bubbleUserContentPaddingRight`、`bubbleAiContentPaddingLeft`、`bubbleAiContentPaddingRight`（第36-42行）
8. 更新所有构造 `ThemePreferenceSnapshot` 的调用点，移除已删除字段的赋值

---

## Task 7: 清理ThemeSettingsScreen中的样式设置UI

- [ ] 移除 `ThemeSettingsScreen.kt` 中所有 chatStyle/inputStyle/bubble 样式配置的状态读取和UI控件
- [ ] 移除 `ThemeSettingsCoreSections.kt` 中 `ThemeSettingsChatStyleSection` 的全部 bubble/cursor/input 样式配置参数和UI

**Files:**
- `ui/features/settings/screens/ThemeSettingsScreen.kt`（第304-412行样式状态读取，第1877-1922行 ChatStyleSectionContent）
- `ui/features/settings/sections/ThemeSettingsCoreSections.kt`（第247-346行 ThemeSettingsChatStyleSection 参数签名）
- `ui/features/settings/sections/ThemeSettingsFontAvatarSections.kt`（第423-435行 ChatStyleOption）
- `ui/features/settings/components/ChatStyleOption.kt`

**Steps:**
1. 在 `ThemeSettingsScreen.kt` 中，移除所有 bubble/cursor/input 样式配置的 collectAsState 读取（第304-412行），包括：chatStyle、inputStyle、bubbleShowAvatar、bubbleWideLayoutEnabled、cursorUserBubbleFollowTheme、cursorUserBubbleLiquidGlass、cursorUserBubbleWaterGlass、bubbleUserBubbleLiquidGlass、bubbleUserBubbleWaterGlass、bubbleAiBubbleLiquidGlass、bubbleAiBubbleWaterGlass、cursorUserBubbleColor、bubbleUserBubbleColor、bubbleAiBubbleColor、bubbleUserTextColor、bubbleAiTextColor、bubbleUserUseCustomFont、bubbleUserFontType、bubbleUserSystemFontName、bubbleUserCustomFontPath、bubbleAiUseCustomFont、bubbleAiFontType、bubbleAiSystemFontName、bubbleAiCustomFontPath、bubbleUserUseImage、bubbleAiUseImage、bubbleUserImageUri、bubbleAiImageUri、所有 bubbleImageCrop/Repeat/Scale、bubbleImageRenderMode、bubbleRoundedCorners、bubbleContentPadding
2. 在 `ThemeSettingsScreen.kt` 中，移除 `ChatStyleSectionContent` 中对已删除参数的传递
3. 在 `ThemeSettingsCoreSections.kt` 中，精简 `ThemeSettingsChatStyleSection` 函数：移除 chatStyle/inputStyle 选择器UI（因为现在只有唯一选项），移除所有 bubble 配置参数和UI（头像、宽布局、液态玻璃、水玻璃、自定义颜色、字体、图片、圆角、内边距等），移除 cursor 配置参数和UI（跟随主题、液态玻璃、水玻璃、自定义颜色）
4. 移除 `ThemeSettingsFontAvatarSections.kt` 中与 bubble 字体相关的 ChatStyleOption
5. 评估是否可以完全移除 `ChatStyleOption.kt` 组件（如果不再有样式选择需求）
6. 移除对 `BubbleImageBackgroundSurface` 和 `BubbleImageStyleConfig` 的 import

---

## Task 8: 清理CursorStyleChatMessage和UserMessageComposable中的液态玻璃/水玻璃参数

- [ ] 移除 `CursorStyleChatMessage.kt` 中的 `userMessageLiquidGlassEnabled` 和 `userMessageWaterGlassEnabled` 参数
- [ ] 移除 `UserMessageComposable.kt` 中的液态玻璃/水玻璃相关逻辑

**Files:**
- `ui/features/chat/components/style/cursor/CursorStyleChatMessage.kt`（第30-31行参数）
- `ui/features/chat/components/style/cursor/UserMessageComposable.kt`（第70-73行 import，第92-93行参数，第236-260行液态玻璃/水玻璃逻辑）

**Steps:**
1. 在 `CursorStyleChatMessage.kt` 中，移除 `userMessageLiquidGlassEnabled` 和 `userMessageWaterGlassEnabled` 参数
2. 在 `CursorStyleChatMessage.kt` 中，调用 `UserMessageComposable` 时不再传递这两个参数
3. 在 `UserMessageComposable.kt` 中，移除 `enableLiquidGlass` 和 `enableWaterGlass` 参数
4. 在 `UserMessageComposable.kt` 中，移除液态玻璃/水玻璃相关的 import（`isLiquidGlassSupported`、`isWaterGlassSupported`、`liquidGlass`、`waterGlass`）
5. 在 `UserMessageComposable.kt` 中，移除 `waterGlassEnabled`/`liquidGlassEnabled` 变量计算逻辑（第236-238行）
6. 在 `UserMessageComposable.kt` 中，移除 `.waterGlass()` 和 `.liquidGlass()` 修饰符调用（第251-260行）
7. 在 `UserMessageComposable.kt` 中，移除液态玻璃/水玻璃条件下的边框/内边距调整逻辑（第271行附近）

---

## Task 9: 清理AgentChatInputSection中的输入框样式配置

- [ ] 移除 `AgentChatInputSection.kt` 中的 `chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass` 参数及相关逻辑

**Files:**
- `ui/features/chat/components/style/input/agent/AgentChatInputSection.kt`（第152-155行 import，第184-187行参数，第509-528行透明逻辑，第651/756/761-819/1098-1119行浮动/液态玻璃/水玻璃逻辑）

**Steps:**
1. 移除 `AgentChatInputSection` 函数签名中的 `chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass` 参数（第184-187行）
2. 移除液态玻璃/水玻璃相关的 import（第152-155行：`isLiquidGlassSupported`、`isWaterGlassSupported`、`liquidGlass`、`waterGlass`）
3. 移除背景颜色选择中的 `chatInputTransparent` 分支逻辑（第509-528行），统一使用不透明默认背景色
4. 移除 `chatInputFloating` 相关的布局逻辑（第651行、第756行等），统一使用非浮动布局
5. 移除 `inputLiquidGlassEnabled`/`inputWaterGlassEnabled` 变量计算（第761-764行）
6. 移除 `.waterGlass()` 和 `.liquidGlass()` 修饰符调用（第800-815行、第1100-1115行）
7. 移除液态玻璃/水玻璃条件下的边框/内边距/阴影调整逻辑

---

## Task 10: 清理MessageImageGenerator中的样式分支

- [ ] 移除 `MessageImageGenerator.kt` 中的 `chatStyle` 参数和 Bubble 分支，硬编码为 Cursor 样式

**Files:**
- `ui/features/chat/util/MessageImageGenerator.kt`（第44-45行 import，第93-99行参数，第266-292行样式分支）

**Steps:**
1. 移除 `BubbleStyleChatMessage` 的 import（第45行）
2. 移除 `ChatStyle` 的 import（第44行）
3. 移除函数签名中的 `chatStyle: ChatStyle` 参数（第93行）
4. 移除函数签名中的 `cursorUserBubbleLiquidGlass`、`cursorUserBubbleWaterGlass`、`bubbleUserBubbleLiquidGlass`、`bubbleUserBubbleWaterGlass`、`bubbleAiBubbleLiquidGlass`、`bubbleAiBubbleWaterGlass` 参数（第94-99行）
5. 将消息渲染逻辑中的 `when(chatStyle)` 分支（第266-292行）替换为直接调用 `CursorStyleChatMessage`，且 `userMessageLiquidGlassEnabled = false`、`userMessageWaterGlassEnabled = false`

---

## Task 11: 清理WebChatHttpBridge中的样式引用

- [ ] 更新 `WebChatModels.kt` 和 `WebChatHttpBridge.kt` 中的 chatStyle/inputStyle 引用

**Files:**
- `integrations/http/WebChatModels.kt`（第13-15行 defaultChatStyle/defaultInputStyle）
- `integrations/http/WebChatHttpBridge.kt`（第274-275行、第2301行、第2315行）

**Steps:**
1. 在 `WebBootstrapResponse` 中，将 `defaultChatStyle` 硬编码为 `"cursor"`（或移除该字段，视API兼容性决定）
2. 在 `WebBootstrapResponse` 中，将 `defaultInputStyle` 硬编码为 `"agent"`（或移除该字段）
3. 在 `WebChatHttpBridge.kt` 中，将 `snapshot.chatStyle` 替换为硬编码 `"cursor"`，将 `snapshot.inputStyle` 替换为硬编码 `"agent"`
4. 如果 `ThemePreferenceSnapshot` 中已移除 `chatStyle`/`inputStyle` 字段，则必须在此处硬编码

---

## Task 12: 清理ChatViewModel中的ChatStyle引用

- [ ] 移除 `ChatViewModel.kt` 中对 `ChatStyle` 的 import 和使用

**Files:**
- `ui/features/chat/viewmodel/ChatViewModel.kt`（第16行 import，第1034行参数，第1080行传递）

**Steps:**
1. 移除 `import com.ai.assistance.operit.ui.features.chat.components.ChatStyle`（第16行）
2. 找到 `ChatViewModel` 中使用 `ChatStyle` 的方法（第1034行参数），将参数类型从 `ChatStyle` 改为移除或硬编码
3. 更新调用点，不再传递 `chatStyle` 参数

---

## Task 13: 清理ChatMessageDisplayMode

- [ ] 确认 `ChatMessageDisplayMode.kt` 的修改需求（注意：当前枚举值为 NORMAL/HIDDEN_PLACEHOLDER，无 CURSOR 值，spec中"仅保留CURSOR"可能指ChatStyle枚举而非此文件）

**Files:**
- `data/model/ChatMessageDisplayMode.kt`

**Steps:**
1. 确认 `ChatMessageDisplayMode` 的 `NORMAL` 和 `HIDDEN_PLACEHOLDER` 仍在使用（用于隐藏用户消息占位符功能，与聊天样式无关）
2. 如果确认与样式简化无关，则保持此文件不变
3. 如果spec确实要求修改，则需评估将 NORMAL 重命名为 CURSOR 的影响范围（涉及 ChatMessage、MessageEntity、ChatArea 等多处引用）

---

## Task 14: 编译验证与清理

- [ ] 执行项目编译，确认无编译错误
- [ ] 检查并移除所有残留的未使用 import

**Files:**
- 全项目

**Steps:**
1. 执行 `./gradlew compileDebugKotlin` 编译项目
2. 修复所有编译错误（未解析的引用、缺少参数等）
3. 搜索残留的 `BubbleStyleChatMessage`、`ClassicChatInputSection`、`ClassicChatSettingsBar`、`BubbleImageStyleConfig`、`ChatStyle.BUBBLE`、`INPUT_STYLE_CLASSIC`、`CHAT_STYLE_BUBBLE` 引用并清理
4. 搜索残留的 `chatInputTransparent`、`chatInputFloating`、`chatInputLiquidGlass`、`chatInputWaterGlass` 引用并清理
5. 搜索残留的 `cursorUserBubbleLiquidGlass`、`cursorUserBubbleWaterGlass`、`cursorUserBubbleFollowTheme`、`cursorUserBubbleColor` 引用并清理
6. 搜索残留的 `bubbleUserBubble`、`bubbleAiBubble`、`bubbleShowAvatar`、`bubbleWideLayout` 引用并清理
7. 移除所有因删除参数而产生的未使用 import
