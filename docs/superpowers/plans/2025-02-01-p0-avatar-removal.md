# 虚拟形象系统移除 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完整移除虚拟形象（Avatar）系统，包括核心渲染引擎、桌宠逻辑、助手配置UI、C++原生模块及相关资源文件，同时保留沙箱包API的方法签名以维持向后兼容。

**Architecture:** 采用分层移除策略：先清理顶层UI和路由入口，再移除数据层和仓库层，最后删除C++原生模块和资源文件。沙箱包桥接插件中的虚拟形象API保留方法签名但标注@Deprecated并返回空值/默认值，确保外部沙箱包不会因方法缺失而崩溃。所有引用清理按依赖关系从上到下进行。

**Tech Stack:** Kotlin, Jetpack Compose, Gradle, CMake, Android NDK

---

### Task 1: 移除导航入口和路由定义

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/common/NavItem.kt:28-29`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt:28,658-675`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/screens/ScreenRouteRegistry.kt:112-119`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/OperitApp.kt:309`

- [ ] **Step 1: Write the failing test**

创建测试文件验证AssistantConfig导航项已被移除：

```kotlin
// tests/ui/common/NavItemTest.kt
package com.ai.assistance.operit.ui.common

import org.junit.Test
import org.junit.Assert.*

class NavItemTest {
    @Test
    fun `AssistantConfig nav item should not exist in nav items list`() {
        val allItems = listOf(
            NavItem.AiChat,
            NavItem.Settings,
            NavItem.Packages,
            NavItem.MemoryBase,
            NavItem.Toolbox,
            NavItem.ShizukuCommands,
            NavItem.Workflow,
            NavItem.Help,
            NavItem.About
        )
        allItems.forEach { item ->
            assertNotEquals("assistant_config", item.route)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.common.NavItemTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 在 `NavItem.kt` 中删除 `AssistantConfig` 对象：

```kotlin
// 删除 NavItem.kt 第28-29行:
//     object AssistantConfig :
//             NavItem("assistant_config", R.string.nav_assistant_config, Icons.Default.Tune)
```

2. 在 `OperitScreens.kt` 中删除 `AssistantConfig` Screen 定义和import：

```kotlin
// 删除第28行:
// import com.ai.assistance.operit.ui.features.assistant.screens.AssistantConfigScreen

// 删除第658-675行:
//     data object AssistantConfig :
//             Screen(
//                     navItem = NavItem.AssistantConfig,
//                     participatesInCrossfadeTransition = false
//             ) {
//         @Composable
//         override fun Content(...) {
//             AssistantConfigScreen()
//         }
//     }
```

3. 在 `ScreenRouteRegistry.kt` 中删除 `main.assistant_config` 路由注册：

```kotlin
// 删除第112-119行:
//             hostEntryDefinition(
//                 entryId = "main.assistant_config",
//                 screen = Screen.AssistantConfig,
//                 surface = NavigationSurface.MAIN_SIDEBAR_AI,
//                 launchNavItem = NavItem.AssistantConfig,
//                 icon = NavItem.AssistantConfig.icon,
//                 order = 20
//             ),
```

4. 在 `OperitApp.kt` 中从 navItems 列表移除 `NavItem.AssistantConfig`：

```kotlin
// 修改第307-318行，删除 NavItem.AssistantConfig:
    val navItems = listOf(
        NavItem.AiChat,
        NavItem.Packages,
        NavItem.MemoryBase,
        NavItem.Toolbox,
        NavItem.ShizukuCommands,
        NavItem.Workflow,
        NavItem.Settings,
        NavItem.Help,
        NavItem.About
    )
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.common.NavItemTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(nav): remove AssistantConfig navigation entry and route"
```

---

### Task 2: 删除助手配置UI目录

**Files:**
- Delete: `app/src/main/java/com/ai/assistance/operit/ui/features/assistant/` (整个目录)
- Delete: `app/src/main/java/com/ai/assistance/operit/ui/components/ManagedDragonBonesView.kt`

- [ ] **Step 1: Write the failing test**

验证assistant目录下的类不再被编译：

```kotlin
// tests/ui/features/assistant/AssistantRemovalTest.kt
package com.ai.assistance.operit.ui.features.assistant

import org.junit.Test
import org.junit.Assert.*

class AssistantRemovalTest {
    @Test
    fun `AssistantConfigScreen class should not exist`() {
        val className = "com.ai.assistance.operit.ui.features.assistant.screens.AssistantConfigScreen"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AssistantConfigScreen should be removed", exists)
    }

    @Test
    fun `ManagedDragonBonesView class should not exist`() {
        val className = "com.ai.assistance.operit.ui.components.ManagedDragonBonesView"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("ManagedDragonBonesView should be removed", exists)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.features.assistant.AssistantRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 删除整个助手配置UI目录：

```bash
rm -rf app/src/main/java/com/ai/assistance/operit/ui/features/assistant/
```

2. 删除ManagedDragonBonesView：

```bash
rm app/src/main/java/com/ai/assistance/operit/ui/components/ManagedDragonBonesView.kt
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.features.assistant.AssistantRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(ui): delete assistant config UI and ManagedDragonBonesView"
```

---

### Task 3: 删除桌宠UI目录和AvatarEmotionManager

**Files:**
- Delete: `app/src/main/java/com/ai/assistance/operit/ui/floating/ui/pet/` (整个目录)

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/ui/floating/PetRemovalTest.kt
package com.ai.assistance.operit.ui.floating.ui.pet

import org.junit.Test
import org.junit.Assert.*

class PetRemovalTest {
    @Test
    fun `AvatarEmotionManager class should not exist`() {
        val className = "com.ai.assistance.operit.ui.floating.ui.pet.AvatarEmotionManager"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AvatarEmotionManager should be removed", exists)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.floating.ui.pet.PetRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

```bash
rm -rf app/src/main/java/com/ai/assistance/operit/ui/floating/ui/pet/
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.floating.ui.pet.PetRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(ui): delete pet overlay UI directory"
```

---

### Task 4: 删除核心Avatar模块

**Files:**
- Delete: `app/src/main/java/com/ai/assistance/operit/core/avatar/` (整个目录)

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/core/avatar/AvatarCoreRemovalTest.kt
package com.ai.assistance.operit.core.avatar

import org.junit.Test
import org.junit.Assert.*

class AvatarCoreRemovalTest {
    @Test
    fun `AvatarView class should not exist`() {
        val className = "com.ai.assistance.operit.core.avatar.common.view.AvatarView"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AvatarView should be removed", exists)
    }

    @Test
    fun `AvatarController class should not exist`() {
        val className = "com.ai.assistance.operit.core.avatar.common.control.AvatarController"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AvatarController should be removed", exists)
    }

    @Test
    fun `AvatarType class should not exist`() {
        val className = "com.ai.assistance.operit.core.avatar.common.model.AvatarType"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AvatarType should be removed", exists)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.core.avatar.AvatarCoreRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

```bash
rm -rf app/src/main/java/com/ai/assistance/operit/core/avatar/
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.core.avatar.AvatarCoreRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(core): delete entire avatar module"
```

---

### Task 5: 删除数据层模型和仓库

**Files:**
- Delete: `app/src/main/java/com/ai/assistance/operit/data/model/DragonBones.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/model/CustomEmoji.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/model/CharacterGroupCard.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/repository/AvatarRepository.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/repository/CustomEmojiRepository.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/preferences/WaifuPreferences.kt`
- Delete: `app/src/main/java/com/ai/assistance/operit/data/preferences/CustomEmojiPreferences.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/data/avatar/DataLayerRemovalTest.kt
package com.ai.assistance.operit.data.avatar

import org.junit.Test
import org.junit.Assert.*

class DataLayerRemovalTest {
    private val removedClasses = listOf(
        "com.ai.assistance.operit.data.model.DragonBonesModel",
        "com.ai.assistance.operit.data.model.CustomEmoji",
        "com.ai.assistance.operit.data.model.CharacterGroupCard",
        "com.ai.assistance.operit.data.repository.AvatarRepository",
        "com.ai.assistance.operit.data.repository.CustomEmojiRepository",
        "com.ai.assistance.operit.data.preferences.WaifuPreferences",
        "com.ai.assistance.operit.data.preferences.CustomEmojiPreferences"
    )

    @Test
    fun `all avatar data layer classes should be removed`() {
        removedClasses.forEach { className ->
            val exists = try {
                Class.forName(className)
                true
            } catch (_: ClassNotFoundException) {
                false
            }
            assertFalse("$className should be removed", exists)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.data.avatar.DataLayerRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

```bash
rm app/src/main/java/com/ai/assistance/operit/data/model/DragonBones.kt
rm app/src/main/java/com/ai/assistance/operit/data/model/CustomEmoji.kt
rm app/src/main/java/com/ai/assistance/operit/data/model/CharacterGroupCard.kt
rm app/src/main/java/com/ai/assistance/operit/data/repository/AvatarRepository.kt
rm app/src/main/java/com/ai/assistance/operit/data/repository/CustomEmojiRepository.kt
rm app/src/main/java/com/ai/assistance/operit/data/preferences/WaifuPreferences.kt
rm app/src/main/java/com/ai/assistance/operit/data/preferences/CustomEmojiPreferences.kt
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.data.avatar.DataLayerRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(data): delete avatar data models, repositories and preferences"
```

---

### Task 6: 删除WaifuMessageProcessor

**Files:**
- Delete: `app/src/main/java/com/ai/assistance/operit/util/WaifuMessageProcessor.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/application/OperitApplication.kt:47,59,206-211,262-264`
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/AIForegroundService.kt:53,329`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/floating/voice/SpeechInteractionManager.kt:19,320`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/chat/viewmodel/ChatViewModel.kt:57,64`
- Modify: `app/src/main/java/com/ai/assistance/operit/services/core/MessageProcessingDelegate.kt:22,25`
- Modify: `app/src/main/java/com/ai/assistance/operit/services/FloatingChatService.kt:47`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/standard/StandardChatManagerTool.kt:14,36`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/util/WaifuRemovalTest.kt
package com.ai.assistance.operit.util

import org.junit.Test
import org.junit.Assert.*

class WaifuRemovalTest {
    @Test
    fun `WaifuMessageProcessor class should not exist`() {
        val className = "com.ai.assistance.operit.util.WaifuMessageProcessor"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("WaifuMessageProcessor should be removed", exists)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.util.WaifuRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 删除WaifuMessageProcessor：

```bash
rm app/src/main/java/com/ai/assistance/operit/util/WaifuMessageProcessor.kt
```

2. 修改 `OperitApplication.kt`，移除WaifuMessageProcessor和CustomEmojiRepository相关初始化：

```kotlin
// 删除第47行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository

// 删除第59行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor

// 删除第206-211行（CustomEmojiRepository初始化块）:
//         // 初始化当前活跃角色目标的自定义表情
//         applicationScope.launch {
//             val emojiStartTime = System.currentTimeMillis()
//             CustomEmojiRepository.getInstance(applicationContext).initializeBuiltinEmojis()
//             AppLogger.d(TAG, "【启动计时】当前角色自定义表情初始化完成（异步） - ${System.currentTimeMillis() - emojiStartTime}ms")
//         }

// 删除第262-264行:
//         // Initialize WaifuMessageProcessor
//         WaifuMessageProcessor.initialize(applicationContext)
//         AppLogger.d(TAG, "【启动计时】WaifuMessageProcessor初始化完成 - ${System.currentTimeMillis() - startTime}ms")
```

3. 修改 `AIForegroundService.kt`，移除WaifuMessageProcessor引用：

```kotlin
// 删除第53行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor

// 修改第329行，将:
//     val cleanedReplyContent = WaifuMessageProcessor.cleanContentForWaifu(rawReplyContent)
// 改为:
//     val cleanedReplyContent = rawReplyContent
```

4. 修改 `SpeechInteractionManager.kt`，移除WaifuMessageProcessor引用：

```kotlin
// 删除第19行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor

// 修改第320行，将:
//     return WaifuMessageProcessor.cleanContentForWaifu(TtsCleaner.clean(text, regexs))
// 改为:
//     return TtsCleaner.clean(text, regexs)
```

5. 修改 `ChatViewModel.kt`，移除WaifuMessageProcessor和AvatarEmotionManager引用：

```kotlin
// 删除第57行:
// import com.ai.assistance.operit.ui.floating.ui.pet.AvatarEmotionManager
// 删除第64行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor
```

6. 修改 `MessageProcessingDelegate.kt`，移除WaifuMessageProcessor和WaifuPreferences引用：

```kotlin
// 删除第22行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor
// 删除第25行:
// import com.ai.assistance.operit.data.preferences.WaifuPreferences
```

7. 修改 `FloatingChatService.kt`，移除WaifuMessageProcessor引用：

```kotlin
// 删除第47行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor
```

8. 修改 `StandardChatManagerTool.kt`，移除WaifuMessageProcessor和WaifuPreferences引用：

```kotlin
// 删除第14行:
// import com.ai.assistance.operit.util.WaifuMessageProcessor
// 删除第36行:
// import com.ai.assistance.operit.data.preferences.WaifuPreferences
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.util.WaifuRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: delete WaifuMessageProcessor and clean up all references"
```

---

### Task 7: 清理FloatingFullscreenModeViewModel中的Avatar逻辑

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/floating/ui/fullscreen/viewmodel/FloatingFullscreenModeViewModel.kt:6,13,32-33,61,83-85,97,140,201,264,369,391-392,439-441,476-478,555,581,655-744`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/ui/floating/FloatingFullscreenRemovalTest.kt
package com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel

import org.junit.Test
import org.junit.Assert.*

class FloatingFullscreenRemovalTest {
    @Test
    fun `VoiceAvatarMotionRequest class should not exist`() {
        val className = "com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel.VoiceAvatarMotionRequest"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("VoiceAvatarMotionRequest should be removed", exists)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel.FloatingFullscreenRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

修改 `FloatingFullscreenModeViewModel.kt`：

1. 删除Avatar相关import：

```kotlin
// 删除第6行:
// import com.ai.assistance.operit.core.avatar.common.state.AvatarEmotion
// 删除第13行:
// import com.ai.assistance.operit.ui.floating.ui.pet.AvatarEmotionManager
```

2. 删除 `VoiceAvatarMotionRequest` 数据类（第32-33行）：

```kotlin
// 删除:
// data class VoiceAvatarMotionRequest(
//     val emotion: AvatarEmotion = AvatarEmotion.IDLE,
//     ...
// )
```

3. 删除所有 `voiceAvatarMotionRequest`、`voiceAvatarSequence`、`lastHandledVoiceAvatarMessageKey`、`hasInitializedVoiceAvatarFromSnapshot` 状态字段。

4. 删除所有 `startVoiceAvatarThinking()`、`resetVoiceAvatarToIdle()`、`pushVoiceAvatarMotion()`、`handleVoiceAvatarMessage()`、`syncVoiceAvatarWithProcessingState()`、`stripVoiceAvatarTags()`、`buildVoiceAvatarMessageKey()`、`shouldInterceptCenterAvatarClick()`、`onCenterAvatarClick()` 方法。

5. 将调用 `stripVoiceAvatarTags` 的地方改为直接使用原始content。

6. 将调用 `startVoiceAvatarThinking()` 的地方（如消息处理开始时）删除该调用。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel.FloatingFullscreenRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(floating): remove avatar logic from FloatingFullscreenModeViewModel"
```

---

### Task 8: 清理AIForegroundService中的桌宠逻辑

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/AIForegroundService.kt:53,150,289,329,368-369,712,1247-1248`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/api/chat/AIForegroundServiceRemovalTest.kt
package com.ai.assistance.operit.api.chat

import org.junit.Test
import org.junit.Assert.*

class AIForegroundServiceRemovalTest {
    @Test
    fun `EXTRA_AVATAR_URI should not exist in AIForegroundService`() {
        val fields = AIForegroundService.Companion::class.java.declaredFields
        val hasAvatarUri = fields.any { it.name == "EXTRA_AVATAR_URI" }
        assertFalse("EXTRA_AVATAR_URI should be removed", hasAvatarUri)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.api.chat.AIForegroundServiceRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

修改 `AIForegroundService.kt`：

1. 删除WaifuMessageProcessor import（第53行，已在Task 6处理）。

2. 删除 `EXTRA_AVATAR_URI` 常量（第150行）：

```kotlin
// 删除:
//         const val EXTRA_AVATAR_URI = "extra_avatar_uri"
```

3. 删除 `avatarUri` 字段（第712行）：

```kotlin
// 删除:
//     private var avatarUri: String? = null
```

4. 修改回复通知构建方法（第289行附近），移除avatarUri参数中的头像加载逻辑（第368-369行）。

5. 修改onStartCommand中读取avatarUri的逻辑（第1247-1248行）：

```kotlin
// 删除:
//             avatarUri = it.getStringExtra(EXTRA_AVATAR_URI)
//             AppLogger.d(TAG, "收到通知数据 - 角色: $characterName, 头像: $avatarUri")
```

6. 将第329行的 `WaifuMessageProcessor.cleanContentForWaifu(rawReplyContent)` 替换为 `rawReplyContent`（已在Task 6处理）。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.api.chat.AIForegroundServiceRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(service): remove avatar/pet logic from AIForegroundService"
```

---

### Task 9: 清理FloatingWindowManager中的PET模式

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/services/floating/FloatingWindowState.kt:35`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/floating/FloatingMode.kt:1-10`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/services/floating/PetModeRemovalTest.kt
package com.ai.assistance.operit.services.floating

import org.junit.Test
import org.junit.Assert.*

class PetModeRemovalTest {
    @Test
    fun `FloatingMode should not contain PET mode`() {
        val modeNames = FloatingMode.values().map { it.name }
        assertFalse("PET mode should be removed", modeNames.contains("PET"))
    }

    @Test
    fun `isPetModeLocked should not exist in FloatingWindowState`() {
        val fields = FloatingWindowState::class.java.declaredFields
        val hasPetModeLocked = fields.any { it.name == "isPetModeLocked" }
        assertFalse("isPetModeLocked should be removed", hasPetModeLocked)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.services.floating.PetModeRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 修改 `FloatingMode.kt`，当前枚举中没有PET值（只有WINDOW, BALL, VOICE_BALL, FULLSCREEN, RESULT_DISPLAY, SCREEN_OCR），确认无需删除PET枚举值，但保留验证。

2. 修改 `FloatingWindowState.kt`，删除第35行：

```kotlin
// 删除:
//     var isPetModeLocked = mutableStateOf(false)
```

3. 搜索并清理所有引用 `isPetModeLocked` 的代码。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.services.floating.PetModeRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(floating): remove pet mode lock state from FloatingWindowState"
```

---

### Task 10: 删除C++原生模块和Gradle配置

**Files:**
- Delete: `app/src/main/cpp/dragonbones/` (整个目录)
- Delete: `app/src/main/cpp/fbx/` (整个目录)
- Delete: `app/src/main/cpp/mmd/` (整个目录)
- Modify: `app/build.gradle.kts:183,187-188`
- Modify: `settings.gradle.kts:22,26-27`
- Delete: `dragonbones/` (整个模块目录)
- Delete: `fbx/` (整个模块目录)
- Delete: `mmd/` (整个模块目录)

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/native/NativeModuleRemovalTest.kt
package com.ai.assistance.operit.native

import org.junit.Test
import org.junit.Assert.*

class NativeModuleRemovalTest {
    @Test
    fun `dragonbones module should not be in project dependencies`() {
        val gradleFile = java.io.File("../app/build.gradle.kts").readText()
        assertFalse("dragonbones dependency should be removed", gradleFile.contains("project(\":dragonbones\")"))
    }

    @Test
    fun `fbx module should not be in project dependencies`() {
        val gradleFile = java.io.File("../app/build.gradle.kts").readText()
        assertFalse("fbx dependency should be removed", gradleFile.contains("project(\":fbx\")"))
    }

    @Test
    fun `mmd module should not be in project dependencies`() {
        val gradleFile = java.io.File("../app/build.gradle.kts").readText()
        assertFalse("mmd dependency should be removed", gradleFile.contains("project(\":mmd\")"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.native.NativeModuleRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 删除C++原生模块目录：

```bash
rm -rf app/src/main/cpp/dragonbones/
rm -rf app/src/main/cpp/fbx/
rm -rf app/src/main/cpp/mmd/
```

2. 删除Gradle子模块目录：

```bash
rm -rf dragonbones/
rm -rf fbx/
rm -rf mmd/
```

3. 修改 `app/build.gradle.kts`，删除第183、187-188行：

```kotlin
// 删除:
//     implementation(project(":dragonbones"))
//     implementation(project(":mmd"))
//     implementation(project(":fbx"))
```

4. 修改 `settings.gradle.kts`，删除第22、26-27行：

```kotlin
// 删除:
// include(":dragonbones")
// include(":mmd")
// include(":fbx")
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.native.NativeModuleRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(native): delete dragonbones, fbx, mmd C++ modules and Gradle config"
```

---

### Task 11: 删除资源文件

**Files:**
- Delete: `app/src/main/assets/emoji/` (整个目录)
- Delete: `app/src/main/assets/pets/` (整个目录)
- Delete: `app/src/main/assets/dragonbones/` (整个目录)

- [ ] **Step 1: Write the failing test**

```bash
# 验证资源目录不存在
test ! -d app/src/main/assets/emoji && echo "PASS: emoji dir removed" || echo "FAIL: emoji dir exists"
test ! -d app/src/main/assets/pets && echo "PASS: pets dir removed" || echo "FAIL: pets dir exists"
test ! -d app/src/main/assets/dragonbones && echo "PASS: dragonbones dir removed" || echo "FAIL: dragonbones dir exists"
```

- [ ] **Step 2: Run test to verify it fails**

```bash
bash -c 'test ! -d app/src/main/assets/emoji && echo "PASS" || echo "FAIL"; test ! -d app/src/main/assets/pets && echo "PASS" || echo "FAIL"; test ! -d app/src/main/assets/dragonbones && echo "PASS" || echo "FAIL"'
```

- [ ] **Step 3: Write minimal implementation**

```bash
rm -rf app/src/main/assets/emoji/
rm -rf app/src/main/assets/pets/
rm -rf app/src/main/assets/dragonbones/
```

- [ ] **Step 4: Run test to verify it passes**

```bash
bash -c 'test ! -d app/src/main/assets/emoji && echo "PASS" || echo "FAIL"; test ! -d app/src/main/assets/pets && echo "PASS" || echo "FAIL"; test ! -d app/src/main/assets/dragonbones && echo "PASS" || echo "FAIL"'
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(assets): delete emoji, pets, dragonbones resource directories"
```

---

### Task 12: 沙箱包API保留空壳（@Deprecated）

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/plugins/toolpkg/ToolPkgCommonBridgePlugin.kt`

- [ ] **Step 1: Write the failing test**

当前 `ToolPkgCommonBridgePlugin.kt` 中没有直接的avatar/pet/dragonbones相关API（经搜索确认无匹配），此Task验证桥接插件中不存在虚拟形象相关注册：

```kotlin
// tests/plugins/ToolPkgBridgeTest.kt
package com.ai.assistance.operit.plugins.toolpkg

import org.junit.Test
import org.junit.Assert.*

class ToolPkgBridgeTest {
    @Test
    fun `ToolPkgCommonBridgePlugin should not register avatar-related hooks`() {
        val source = java.io.File(
            "app/src/main/java/com/ai/assistance/operit/plugins/toolpkg/ToolPkgCommonBridgePlugin.kt"
        ).readText()
        assertFalse("Should not reference AvatarEmotion", source.contains("AvatarEmotion"))
        assertFalse("Should not reference AvatarController", source.contains("AvatarController"))
        assertFalse("Should not reference DragonBones", source.contains("DragonBones"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.plugins.toolpkg.ToolPkgBridgeTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

经搜索确认 `ToolPkgCommonBridgePlugin.kt` 中当前没有直接引用avatar/dragonbones/pet相关代码。如果未来沙箱包需要暴露虚拟形象相关API，应按以下模式处理：

```kotlin
// 在 ToolPkgCommonBridgePlugin 中，如果存在虚拟形象相关API方法，按此模式保留:
@Deprecated("虚拟形象系统已移除，此方法始终返回null")
fun getAvatarConfig(): Any? = null

@Deprecated("虚拟形象系统已移除，此方法始终返回空列表")
fun getAvailableAvatars(): List<Any> = emptyList()

@Deprecated("虚拟形象系统已移除，此方法为空操作")
fun setAvatarEmotion(emotion: String) {
    // no-op
}
```

当前无需修改此文件，因为不存在虚拟形象相关API。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.plugins.toolpkg.ToolPkgBridgeTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(plugins): verify no avatar API in ToolPkgCommonBridgePlugin"
```

---

### Task 13: 清理CustomEmojiRepository和WaifuPreferences的外部引用

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/data/preferences/CharacterCardManager.kt:21`
- Modify: `app/src/main/java/com/ai/assistance/operit/data/preferences/CharacterGroupCardManager.kt:21`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/settings/viewmodels/CustomEmojiViewModel.kt:14`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/WaifuModeSettingsScreen.kt:23`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/chat/screens/AIChatScreen.kt:56`
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/EnhancedAIService.kt:79`
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/enhance/ConversationService.kt:28,54`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/data/preferences/ExternalRefCleanupTest.kt
package com.ai.assistance.operit.data.preferences

import org.junit.Test
import org.junit.Assert.*

class ExternalRefCleanupTest {
    @Test
    fun `CharacterCardManager should not import CustomEmojiRepository`() {
        val source = java.io.File(
            "app/src/main/java/com/ai/assistance/operit/data/preferences/CharacterCardManager.kt"
        ).readText()
        assertFalse("CharacterCardManager should not import CustomEmojiRepository",
            source.contains("import com.ai.assistance.operit.data.repository.CustomEmojiRepository"))
    }

    @Test
    fun `ConversationService should not import WaifuPreferences`() {
        val source = java.io.File(
            "app/src/main/java/com/ai/assistance/operit/api/chat/enhance/ConversationService.kt"
        ).readText()
        assertFalse("ConversationService should not import WaifuPreferences",
            source.contains("import com.ai.assistance.operit.data.preferences.WaifuPreferences"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.data.preferences.ExternalRefCleanupTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 修改 `CharacterCardManager.kt`，删除CustomEmojiRepository import及相关调用：

```kotlin
// 删除第21行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository
```

移除所有使用 `CustomEmojiRepository` 的方法体，改为空实现或删除方法。

2. 修改 `CharacterGroupCardManager.kt`，删除CustomEmojiRepository import及相关调用：

```kotlin
// 删除第21行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository
```

3. 删除 `CustomEmojiViewModel.kt`（如果整个ViewModel仅服务于CustomEmoji功能）或移除CustomEmojiRepository引用：

```kotlin
// 删除第14行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository
```

4. 修改 `WaifuModeSettingsScreen.kt`，删除WaifuPreferences import及相关UI：

```kotlin
// 删除第23行:
// import com.ai.assistance.operit.data.preferences.WaifuPreferences
```

移除所有引用WaifuPreferences的Composable代码。

5. 修改 `AIChatScreen.kt`，删除WaifuPreferences import及相关逻辑：

```kotlin
// 删除第56行:
// import com.ai.assistance.operit.data.preferences.WaifuPreferences
```

6. 修改 `EnhancedAIService.kt`，删除CustomEmojiRepository import及相关调用：

```kotlin
// 删除第79行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository
```

7. 修改 `ConversationService.kt`，删除WaifuPreferences和CustomEmojiRepository import：

```kotlin
// 删除第28行:
// import com.ai.assistance.operit.data.preferences.WaifuPreferences
// 删除第54行:
// import com.ai.assistance.operit.data.repository.CustomEmojiRepository
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.data.preferences.ExternalRefCleanupTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: clean up CustomEmojiRepository and WaifuPreferences external references"
```

---

### Task 14: 清理DrawerContent中的助手配置入口

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/ui/main/DrawerContentRemovalTest.kt
package com.ai.assistance.operit.ui.main.components

import org.junit.Test
import org.junit.Assert.*

class DrawerContentRemovalTest {
    @Test
    fun `DrawerContent should not reference AssistantConfig`() {
        val source = java.io.File(
            "app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt"
        ).readText()
        assertFalse("DrawerContent should not reference AssistantConfig",
            source.contains("AssistantConfig"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.main.components.DrawerContentRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

经搜索确认 `DrawerContent.kt` 当前没有直接引用 `AssistantConfig`（搜索结果为空）。此Task验证确认无残留引用。如果发现引用，按以下方式处理：

1. 搜索DrawerContent中所有 `AssistantConfig` 引用并删除对应导航项渲染代码。

2. 确认侧边栏导航项列表不再包含助手配置入口。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.main.components.DrawerContentRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(drawer): verify no AssistantConfig entry in DrawerContent"
```

---

### Task 15: 删除Filament（glTF）依赖和清理AvatarPicker

**Files:**
- Modify: `app/build.gradle.kts:192-195`
- Delete: `app/src/main/java/com/ai/assistance/operit/ui/features/settings/components/AvatarPicker.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// tests/ui/settings/AvatarPickerRemovalTest.kt
package com.ai.assistance.operit.ui.features.settings.components

import org.junit.Test
import org.junit.Assert.*

class AvatarPickerRemovalTest {
    @Test
    fun `AvatarPicker class should not exist`() {
        val className = "com.ai.assistance.operit.ui.features.settings.components.AvatarPicker"
        val exists = try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
        assertFalse("AvatarPicker should be removed", exists)
    }

    @Test
    fun `build.gradle should not contain Filament dependencies`() {
        val gradleFile = java.io.File("app/build.gradle.kts").readText()
        assertFalse("Filament dependency should be removed", gradleFile.contains("filament-android"))
        assertFalse("gltfio dependency should be removed", gradleFile.contains("gltfio-android"))
        assertFalse("filament-utils dependency should be removed", gradleFile.contains("filament-utils-android"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.features.settings.components.AvatarPickerRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 3: Write minimal implementation**

1. 删除AvatarPicker：

```bash
rm app/src/main/java/com/ai/assistance/operit/ui/features/settings/components/AvatarPicker.kt
```

2. 修改 `app/build.gradle.kts`，删除第192-195行Filament依赖：

```kotlin
// 删除:
//     implementation("com.google.android.filament:filament-android:1.69.2")
//     implementation("com.google.android.filament:gltfio-android:1.69.2")
//     implementation("com.google.android.filament:filament-utils-android:1.69.2")
```

3. 搜索并清理所有引用 `AvatarPicker` 的代码。

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:testDebugUnitTest --tests "com.ai.assistance.operit.ui.features.settings.components.AvatarPickerRemovalTest" 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: delete AvatarPicker and remove Filament glTF dependencies"
```

---

### Task 16: 全量编译验证和最终清理

**Files:**
- Modify: 所有残留编译错误的文件

- [ ] **Step 1: Write the failing test**

```bash
# 全量编译验证
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -50
```

- [ ] **Step 3: Write minimal implementation**

1. 运行全量编译，根据编译错误逐一修复残留引用：

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep "e:" | head -30
```

2. 对每个编译错误，移除对应的import语句和代码引用。

3. 典型修复模式：
   - 删除未使用的import语句
   - 将引用已删除类的方法体替换为空实现或删除方法
   - 将引用已删除类的参数替换为可空类型并赋默认值null
   - 删除仅服务于虚拟形象功能的整个方法/类

4. 运行lint检查：

```bash
./gradlew :app:lintDebug 2>&1 | tail -30
```

5. 确认APK可以正常构建：

```bash
./gradlew :app:assembleDebug 2>&1 | tail -20
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" | xargs -I{} bash -c 'if [ {} -eq 0 ]; then echo "BUILD SUCCESS"; else echo "BUILD FAILED: {} errors"; fi'
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: final cleanup and full compilation verification for avatar removal"
```
