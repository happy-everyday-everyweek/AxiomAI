# 权限系统简化 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将权限系统从五级（STANDARD/ACCESSIBILITY/DEBUGGER/ADMIN/ROOT）简化为两级（BASIC/ADVANCED），移除Root、ADB(Shizuku)、Admin相关代码，并将无障碍服务从辅助APK迁移到主程序内置。
**Architecture:** 权限枚举从五级缩减为BASIC和ADVANCED两级，所有Shell执行器和ActionListener仅保留Standard和Accessibility两种实现。无障碍服务从独立APK+AIDL跨进程通信改为主程序内直接方法调用，消除辅助APK安装逻辑。
**Tech Stack:** Kotlin, Android

---

## 任务1：简化 AndroidPermissionLevel 枚举

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/AndroidPermissionLevel.kt`

**步骤：**
- [ ] 将枚举值从 `STANDARD, ACCESSIBILITY, DEBUGGER, ADMIN, ROOT` 改为 `BASIC, ADVANCED`
- [ ] 更新 `fromString()` 方法：`"BASIC"` -> `BASIC`，`"ADVANCED"` -> `ADVANCED`，旧值 `"STANDARD"` 兼容映射到 `BASIC`，`"ACCESSIBILITY"` 兼容映射到 `ADVANCED`，其余默认 `BASIC`
- [ ] 更新枚举文档注释为两级描述

**预期结果：**
```kotlin
enum class AndroidPermissionLevel {
    BASIC,
    ADVANCED;

    companion object {
        fun fromString(value: String?): AndroidPermissionLevel {
            return when(value?.uppercase()) {
                "BASIC" -> BASIC
                "ADVANCED" -> ADVANCED
                "STANDARD" -> BASIC
                "ACCESSIBILITY" -> ADVANCED
                else -> BASIC
            }
        }
    }
}
```

---

## 任务2：简化 AndroidPermissionPreferences

**文件：** `app/src/main/java/com/ai/assistance/operit/data/preferences/AndroidPermissionPreferences.kt`

**步骤：**
- [ ] 删除 `RootCommandExecutionMode` 枚举（`AUTO`, `FORCE_LIBSU`, `FORCE_EXEC`）
- [ ] 删除 `ROOT_EXECUTION_MODE` 和 `CUSTOM_SU_COMMAND` 两个 DataStore key
- [ ] 删除 `rootExecutionModeFlow`、`customSuCommandFlow` 属性
- [ ] 删除 `saveRootExecutionMode()`、`saveCustomSuCommand()`、`getRootExecutionMode()`、`getCustomSuCommand()` 方法
- [ ] 删除 `normalizeSuCommand()` 私有方法
- [ ] 删除 `DEFAULT_SU_COMMAND` 常量
- [ ] `preferredPermissionLevelFlow` 和 `savePreferredPermissionLevel()` 逻辑不变，但存储的枚举值将自动使用新的 BASIC/ADVANCED

---

## 任务3：简化 ShellExecutorFactory

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/shell/ShellExecutorFactory.kt`

**步骤：**
- [ ] `getExecutor()` 的 `when` 分支仅保留 `BASIC -> StandardShellExecutor(context)` 和 `ADVANCED -> AccessibilityShellExecutor(context)`
- [ ] `getHighestAvailableExecutor()` 的 `levels` 列表改为 `listOf(AndroidPermissionLevel.ADVANCED, AndroidPermissionLevel.BASIC)`，优先尝试ADVANCED
- [ ] 回退逻辑改为 `AndroidPermissionLevel.BASIC`
- [ ] `getUserPreferredExecutor()` 中回退级别改为 `AndroidPermissionLevel.BASIC`
- [ ] `getAvailableExecutors()` 遍历 `AndroidPermissionLevel.values()` 自动适配新枚举

---

## 任务4：简化 ActionListenerFactory

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/action/ActionListenerFactory.kt`

**步骤：**
- [ ] `getListener()` 的 `when` 分支仅保留 `ADVANCED -> AccessibilityActionListener(context)` 和 `BASIC -> StandardActionListener(context)`
- [ ] `getHighestAvailableListener()` 的 `levels` 列表改为 `listOf(AndroidPermissionLevel.ADVANCED, AndroidPermissionLevel.BASIC)`
- [ ] `getAvailableListeners()` 遍历 `AndroidPermissionLevel.values()` 自动适配新枚举

---

## 任务5：简化 ToolPermissionSystem

**文件：** `app/src/main/java/com/ai/assistance/operit/ui/permissions/ToolPermissionSystem.kt`

**步骤：**
- [ ] 搜索文件中所有对 `AndroidPermissionLevel.DEBUGGER`、`AndroidPermissionLevel.ADMIN`、`AndroidPermissionLevel.ROOT` 的引用
- [ ] 将三级以上的权限检查逻辑简化为两级：BASIC 和 ADVANCED
- [ ] 移除与已删除权限级别相关的条件分支

---

## 任务6：简化 AndroidShellExecutor

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/AndroidShellExecutor.kt`

**步骤：**
- [ ] `getPermissionLevelLabel()` 方法仅保留 `BASIC -> "BASIC"` 和 `ADVANCED -> "ADVANCED"` 两个分支
- [ ] 搜索并移除所有对 `AndroidPermissionLevel.DEBUGGER`、`ADMIN`、`ROOT` 的引用
- [ ] 更新 `buildStrictUnavailableReason()` 等方法中的权限级别判断逻辑

---

## 任务7：简化 ToolGetter

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/ToolGetter.kt`

**步骤：**
- [ ] 删除 `import com.ai.assistance.operit.core.tools.defaultTool.admin.*`
- [ ] 删除 `import com.ai.assistance.operit.core.tools.defaultTool.debugger.*`
- [ ] 删除 `import com.ai.assistance.operit.core.tools.defaultTool.root.*`
- [ ] 所有 `when` 分支简化为：`BASIC -> StandardXxxTools(context)`，`ADVANCED -> AccessibilityXxxTools(context)`，`null -> StandardXxxTools(context)`
- [ ] 涉及方法：`getFileSystemTools()`、`getUITools()`、`getSystemOperationTools()`、`getDeviceInfoToolExecutor()`

---

## 任务8：删除 Root/ADB/Admin 授权器和安装器

**文件：**
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/RootAuthorizer.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/ShizukuAuthorizer.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/ShizukuInstaller.kt`

**步骤：**
- [ ] 删除 `RootAuthorizer.kt`（428行，依赖libsu库的Root权限管理器）
- [ ] 删除 `ShizukuAuthorizer.kt`（419行，Shizuku/ADB权限授权器）
- [ ] 删除 `ShizukuInstaller.kt`（297行，内置Shizuku APK安装管理器）

---

## 任务9：删除 Root/Admin/Debugger Shell执行器

**文件：**
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/shell/RootShellExecutor.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/shell/AdminShellExecutor.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/shell/DebuggerShellExecutor.kt`

**步骤：**
- [ ] 删除 `RootShellExecutor.kt`（601行，基于libsu的Root Shell执行器）
- [ ] 删除 `AdminShellExecutor.kt`（217行，基于设备管理员的Shell执行器）
- [ ] 删除 `DebuggerShellExecutor.kt`（645行，基于Shizuku的Shell执行器）

---

## 任务10：删除 Root/Admin/Debugger ActionListener

**文件：**
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/action/RootActionListener.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/action/AdminActionListener.kt`
- `app/src/main/java/com/ai/assistance/operit/core/tools/system/action/DebuggerActionListener.kt`

**步骤：**
- [ ] 删除 `RootActionListener.kt`
- [ ] 删除 `AdminActionListener.kt`
- [ ] 删除 `DebuggerActionListener.kt`

---

## 任务11：删除 root/admin/debugger 工具目录

**文件：**
- `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/root/` 整个目录（4个文件：RootDeviceInfoToolExecutor.kt、RootFileSystemTools.kt、RootUITools.kt、RootSystemOperationTools.kt）
- `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/admin/` 整个目录（4个文件：AdminDeviceInfoToolExecutor.kt、AdminFileSystemTools.kt、AdminUITools.kt、AdminSystemOperationTools.kt）
- `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/debugger/` 整个目录（4个文件：DebuggerDeviceInfoToolExecutor.kt、DebuggerFileSystemTools.kt、DebuggerUITools.kt、DebuggerSystemOperationTools.kt）

**步骤：**
- [ ] 删除 `root/` 目录及其全部4个文件
- [ ] 删除 `admin/` 目录及其全部4个文件
- [ ] 删除 `debugger/` 目录及其全部4个文件

---

## 任务12：删除内置 Shizuku APK 资源

**文件：**
- `app/src/main/assets/shizuku.apk`
- `app/src/main/assets/shizuku_version.txt`

**步骤：**
- [ ] 删除 `shizuku.apk`（内置Shizuku安装包）
- [ ] 删除 `shizuku_version.txt`（Shizuku版本信息文件）

---

## 任务13：清理引用已删除类的文件

**文件（需修改引用）：**
- `app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt` — 移除 `ShizukuAuthorizer` import，移除 `DEBUGGER`/`ADMIN`/`ROOT` 分支
- `app/src/main/java/com/ai/assistance/operit/ui/features/demo/state/DemoStateManager.kt` — 移除 `RootAuthorizer`、`ShizukuAuthorizer`、`AccessibilityProviderInstaller` import 及相关逻辑
- `app/src/main/java/com/ai/assistance/operit/ui/features/demo/screens/ShizukuDemoScreen.kt` — 移除 `ShizukuAuthorizer`、`ShizukuInstaller`、`AccessibilityProviderInstaller` import 及 Shizuku 相关UI
- `app/src/main/java/com/ai/assistance/operit/ui/features/demo/viewmodel/ShizukuDemoViewModel.kt` — 移除 `RootAuthorizer` import 及 Root 相关逻辑
- `app/src/main/java/com/ai/assistance/operit/ui/features/demo/wizards/ShizukuWizardCard.kt` — 移除整个 Shizuku 向导卡片组件（532行）
- `app/src/main/java/com/ai/assistance/operit/ui/features/demo/components/PermissionLevelCard.kt` — 移除 `ADMIN`/`DEBUGGER`/`ROOT` 分支，简化为 BASIC/ADVANCED 两级
- `app/src/main/java/com/ai/assistance/operit/ui/features/permission/screens/PermissionGuideScreen.kt` — 移除 `DEBUGGER`/`ROOT` 选项，简化为 BASIC/ADVANCED
- `app/src/main/java/com/ai/assistance/operit/ui/features/settings/screens/GlobalDisplaySettingsScreen.kt` — 移除 `AndroidPermissionLevel.ROOT` 判断
- `app/src/main/java/com/ai/assistance/operit/core/tools/packTool/PackageManager.kt` — 移除 `ShizukuAuthorizer` import，简化 `DEBUGGER`/`ADMIN`/`ROOT` 权限判断为 `ADVANCED`
- `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/standard/StandardShellToolExecutor.kt` — 移除 `ShizukuAuthorizer` import
- `app/src/main/java/com/ai/assistance/operit/core/tools/agent/PhoneAgent.kt` — 移除 `ShizukuAuthorizer` import，简化 `DEBUGGER`/`ADMIN`/`ROOT` 权限判断

**步骤：**
- [ ] 逐文件清理 import 语句，移除对已删除类的引用
- [ ] 将所有 `when` 分支中的 `DEBUGGER`/`ADMIN`/`ROOT` 合并到 `ADVANCED` 或 `BASIC`
- [ ] 移除 ShizukuDemoScreen 和 ShizukuWizardCard 中与 Shizuku 安装/权限请求相关的完整UI逻辑
- [ ] 确保编译不报错

---

## 任务14：无障碍服务内置化 - 删除辅助APK安装逻辑

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/AccessibilityProviderInstaller.kt`

**步骤：**
- [ ] 删除整个 `AccessibilityProviderInstaller.kt`（160行，管理辅助APK安装和版本检查）
- [ ] 删除 `app/src/main/assets/accessibility.apk`（辅助无障碍服务APK）
- [ ] 删除 `app/src/main/assets/accessibility_version.txt`（辅助APK版本信息，如存在）

---

## 任务15：无障碍服务内置化 - 删除AIDL接口

**文件：**
- `app/src/main/aidl/com/ai/assistance/operit/provider/IAccessibilityProvider.aidl`
- `app/src/main/aidl/com/ai/assistance/operit/provider/IAccessibilityEventCallback.aidl`
- `app/src/main/aidl/android/view/accessibility/AccessibilityEvent.aidl`

**步骤：**
- [ ] 删除 `IAccessibilityProvider.aidl`（定义跨进程无障碍服务接口）
- [ ] 删除 `IAccessibilityEventCallback.aidl`（定义跨进程事件回调接口）
- [ ] 删除 `AccessibilityEvent.aidl`（AIDL所需的parcelable声明）

---

## 任务16：无障碍服务内置化 - 在 AndroidManifest.xml 声明主程序 AccessibilityService

**文件：** `app/src/main/AndroidManifest.xml`

**步骤：**
- [ ] 在 `<application>` 标签内添加 `<service>` 声明：
  ```xml
  <service
      android:name=".YourAccessibilityServiceClassName"
      android:exported="false"
      android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
      <intent-filter>
          <action android:name="android.accessibilityservice.AccessibilityService" />
      </intent-filter>
      <meta-data
          android:name="android.accessibilityservice"
          android:resource="@xml/accessibility_service_config" />
  </service>
  ```
- [ ] 创建 `app/src/main/res/xml/accessibility_service_config.xml` 无障碍服务配置文件
- [ ] 确认主程序中 AccessibilityService 子类的完整类名并填入 `android:name`

---

## 任务17：无障碍服务内置化 - AccessibilityActionListener 改为直接方法调用

**文件：** `app/src/main/java/com/ai/assistance/operit/core/tools/system/action/AccessibilityActionListener.kt`

**步骤：**
- [ ] `handleAccessibilityEvent()` 方法当前接收 `AccessibilityEvent` 参数，从AIDL回调触发；改为直接由主程序内的 AccessibilityService 实例调用
- [ ] 移除 `UIHierarchyManager` 的AIDL远程调用依赖，改为直接引用主程序内的 AccessibilityService
- [ ] 更新 `isAvailable()` 和 `hasPermission()` 方法，直接检查主程序内 AccessibilityService 的运行状态
- [ ] `startListening()` 方法改为注册主程序内 AccessibilityService 的事件监听器

---

## 任务18：无障碍服务内置化 - 清理 UIHierarchyManager 的 AIDL 引用

**文件：** `app/src/main/java/com/ai/assistance/operit/data/repository/UIHierarchyManager.kt`

**步骤：**
- [ ] 移除 `import com.ai.assistance.operit.core.tools.system.AccessibilityProviderInstaller`
- [ ] 移除 `import com.ai.assistance.operit.provider.IAccessibilityProvider`
- [ ] 将 `UIHierarchyManager` 中通过 AIDL `IAccessibilityProvider` 获取 UI 层级、执行点击/滑动等操作的方法，改为直接调用主程序内 AccessibilityService 实例的对应方法
- [ ] 移除 `launchProviderInstall()` 等辅助APK安装相关方法
- [ ] 移除 ServiceConnection 绑定逻辑，改为直接持有主程序内 AccessibilityService 引用

---

## 任务19：清理 Shizuku 依赖

**步骤：**
- [ ] 检查 `app/build.gradle.kts` 或 `app/build.gradle`，移除 Shizuku 相关依赖（`dev.rikka.shizuku:api`、`dev.rikka.shizuku:provider` 等）
- [ ] 检查 `app/build.gradle.kts` 或 `app/build.gradle`，移除 libsu 相关依赖（`com.topjohnwu.libsu:core` 等）
- [ ] 移除 `app/src/main/aidl/moe/shizuku/server/` 目录下的 Shizuku AIDL 文件（如 `IShizukuService.aidl` 等）
- [ ] 检查并移除 `settings.gradle.kts` 中 Shizuku 相关的仓库源配置（如 Maven 仓库）

---

## 任务20：编译验证与最终清理

**步骤：**
- [ ] 执行全项目编译，确保无编译错误
- [ ] 全局搜索 `AndroidPermissionLevel.DEBUGGER`、`AndroidPermissionLevel.ADMIN`、`AndroidPermissionLevel.ROOT`，确认无残留引用
- [ ] 全局搜索 `RootAuthorizer`、`ShizukuAuthorizer`、`ShizukuInstaller`、`RootShellExecutor`、`AdminShellExecutor`、`DebuggerShellExecutor`，确认无残留引用
- [ ] 全局搜索 `IAccessibilityProvider`、`IAccessibilityEventCallback`、`AccessibilityProviderInstaller`，确认无残留引用
- [ ] 全局搜索 `RootCommandExecutionMode`，确认无残留引用
- [ ] 验证 BASIC/ADVANCED 两级权限切换功能正常
