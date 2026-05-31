# P3新功能与P4收尾打磨 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现待办功能、日程功能、上下文自动注入三个全新功能模块，并完成字符串资源清理、聊天格式精简、Compose DSL渲染器LiquidGlass移除、性能优化等收尾打磨工作，最后进行系统规则自动化和验证清理。

**Architecture:** 待办和日程均采用ObjectBox持久化 + AI工具调用集成 + 侧边栏入口的统一架构模式，新建独立数据层（Entity/Repository）和工具注册，通过AIToolHandler注册为AI可调用工具，侧边栏预留按钮区激活入口。上下文自动注入采用优先级调度器，在每次对话请求前按优先级（记忆>通知>屏幕>位置>使用时间）组装上下文片段，总Token上限500。P4收尾工作按文件维度逐一清理。

**Tech Stack:** Kotlin, Jetpack Compose, ObjectBox

---

### Task 1: 待办功能 - ObjectBox实体与数据层

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/data/model/TodoEntity.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/data/repository/TodoRepository.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/data/db/ObjectBox.kt` (确认MyObjectBox自动生成包含新Entity)

- [ ] **Step 1: 创建TodoEntity ObjectBox实体**

新建 `TodoEntity.kt`，定义待办数据模型：

```kotlin
package com.ai.assistance.operit.data.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.Date

@Entity
data class TodoEntity(
    @Id var id: Long = 0,
    @Index var uuid: String = "",
    var title: String = "",
    var description: String = "",
    var status: String = STATUS_PENDING,
    var priority: String = PRIORITY_NORMAL,
    var dueDate: Date? = null,
    var completedAt: Date? = null,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var source: String = SOURCE_MANUAL,
    var chatId: String? = null,
    var tags: String = ""
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
        const val PRIORITY_LOW = "low"
        const val PRIORITY_NORMAL = "normal"
        const val PRIORITY_HIGH = "high"
        const val PRIORITY_URGENT = "urgent"
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_AI = "ai"
        const val SOURCE_CHAT = "chat"
    }
}
```

- [ ] **Step 2: 创建TodoRepository**

新建 `TodoRepository.kt`，封装ObjectBox CRUD操作：

```kotlin
package com.ai.assistance.operit.data.repository

import android.content.Context
import com.ai.assistance.operit.data.db.ObjectBoxManager
import com.ai.assistance.operit.data.model.TodoEntity_
import com.ai.assistance.operit.data.model.TodoEntity
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import java.util.Date
import java.util.UUID

class TodoRepository(private val context: Context, private val profileId: String = "default") {
    private val box: Box<TodoEntity>
        get() = ObjectBoxManager.get(context, profileId).boxFor(TodoEntity::class.java)

    fun getAll(): List<TodoEntity> = box.all.sortedByDescending { it.createdAt }

    fun getByStatus(status: String): List<TodoEntity> =
        box.query().equal(TodoEntity_.status, status).orderDesc(TodoEntity_.createdAt).build().find()

    fun getByUuid(uuid: String): TodoEntity? =
        box.query().equal(TodoEntity_.uuid, uuid).build().findFirst()

    fun insert(entity: TodoEntity): Long {
        if (entity.uuid.isBlank()) entity.uuid = UUID.randomUUID().toString()
        entity.updatedAt = Date()
        return box.put(entity)
    }

    fun update(entity: TodoEntity) {
        entity.updatedAt = Date()
        box.put(entity)
    }

    fun delete(id: Long) = box.remove(id)

    fun deleteByUuid(uuid: String) {
        val entity = getByUuid(uuid) ?: return
        box.remove(entity)
    }

    fun complete(uuid: String) {
        val entity = getByUuid(uuid) ?: return
        entity.status = TodoEntity.STATUS_COMPLETED
        entity.completedAt = Date()
        entity.updatedAt = Date()
        box.put(entity)
    }

    fun getPendingWithDueDate(): List<TodoEntity> =
        box.query()
            .equal(TodoEntity_.status, TodoEntity.STATUS_PENDING)
            .notNull(TodoEntity_.dueDate)
            .order(TodoEntity_.dueDate)
            .build().find()

    fun search(keyword: String): List<TodoEntity> =
        box.query().contains(TodoEntity_.title, keyword, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .or().contains(TodoEntity_.description, keyword, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build().find()
}
```

- [ ] **Step 3: 触发ObjectBox代码生成**

运行Gradle构建以触发ObjectBox注解处理器生成 `MyObjectBox` 类：

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -20
```

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(todo): add TodoEntity ObjectBox model and TodoRepository"
```

---

### Task 2: 待办功能 - AI工具调用集成

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/standard/StandardTodoTools.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/ToolRegistration.kt` (注册待办工具)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemToolPrompts.kt` (添加待办工具提示词)

- [ ] **Step 1: 创建StandardTodoTools**

新建 `StandardTodoTools.kt`，实现AI可调用的待办管理工具：

```kotlin
package com.ai.assistance.operit.core.tools.defaultTool.standard

import android.content.Context
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolParameter
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.data.model.StringResultData
import com.ai.assistance.operit.data.repository.TodoRepository
import com.ai.assistance.operit.data.model.TodoEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class StandardTodoTools(private val context: Context) {
    private val repository by lazy { TodoRepository(context) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun createTodo(tool: AITool): ToolResult {
        val title = tool.parameters.find { it.name == "title" }?.value ?: return errorResult(tool, "title is required")
        val description = tool.parameters.find { it.name == "description" }?.value ?: ""
        val priority = tool.parameters.find { it.name == "priority" }?.value ?: TodoEntity.PRIORITY_NORMAL
        val dueDateStr = tool.parameters.find { it.name == "due_date" }?.value
        val tags = tool.parameters.find { it.name == "tags" }?.value ?: ""

        val entity = TodoEntity(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDateStr?.let { dateFormat.parse(it) },
            source = TodoEntity.SOURCE_AI,
            tags = tags
        )
        val id = repository.insert(entity)
        return ToolResult(
            toolName = tool.name,
            success = true,
            result = StringResultData("Todo created with id=$id, title=$title")
        )
    }

    fun listTodos(tool: AITool): ToolResult {
        val status = tool.parameters.find { it.name == "status" }?.value
        val todos = if (status != null) repository.getByStatus(status) else repository.getAll()
        val arr = JSONArray()
        todos.take(50).forEach { todo ->
            arr.put(JSONObject().apply {
                put("uuid", todo.uuid)
                put("title", todo.title)
                put("description", todo.description)
                put("status", todo.status)
                put("priority", todo.priority)
                put("due_date", todo.dueDate?.let { dateFormat.format(it) } ?: "")
                put("tags", todo.tags)
            })
        }
        return ToolResult(toolName = tool.name, success = true, result = StringResultData(arr.toString()))
    }

    fun completeTodo(tool: AITool): ToolResult {
        val uuid = tool.parameters.find { it.name == "uuid" }?.value
            ?: return errorResult(tool, "uuid is required")
        repository.complete(uuid)
        return ToolResult(toolName = tool.name, success = true, result = StringResultData("Todo $uuid completed"))
    }

    fun deleteTodo(tool: AITool): ToolResult {
        val uuid = tool.parameters.find { it.name == "uuid" }?.value
            ?: return errorResult(tool, "uuid is required")
        repository.deleteByUuid(uuid)
        return ToolResult(toolName = tool.name, success = true, result = StringResultData("Todo $uuid deleted"))
    }

    private fun errorResult(tool: AITool, error: String) = ToolResult(
        toolName = tool.name, success = false, result = StringResultData(""), error = error
    )
}
```

- [ ] **Step 2: 在ToolRegistration.kt中注册待办工具**

在 `registerAllTools` 函数中添加待办工具注册：

```kotlin
val todoTools = StandardTodoTools(context)

handler.registerTool("create_todo") { tool ->
    todoTools.createTodo(tool)
}
handler.registerTool("list_todos") { tool ->
    todoTools.listTodos(tool)
}
handler.registerTool("complete_todo") { tool ->
    todoTools.completeTodo(tool)
}
handler.registerTool("delete_todo") { tool ->
    todoTools.deleteTodo(tool)
}
```

- [ ] **Step 3: 在SystemToolPrompts.kt中添加待办工具提示词**

添加待办工具的系统提示词描述，说明AI可以使用 `create_todo`、`list_todos`、`complete_todo`、`delete_todo` 四个工具来管理用户的待办事项。

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(todo): add AI tool integration for todo management"
```

---

### Task 3: 待办功能 - 侧边栏入口与UI

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/ui/features/todo/screens/TodoScreen.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/ui/features/todo/viewmodel/TodoViewModel.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/common/NavItem.kt` (添加Todo导航项)
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt` (添加Todo Screen)
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt` (侧边栏预留按钮区添加待办入口)

- [ ] **Step 1: 在NavItem.kt中添加Todo导航项**

```kotlin
object Todo : NavItem("todo", R.string.nav_todo, Icons.Default.CheckCircle)
```

同时在字符串资源文件中添加 `nav_todo` 对应的中文字符串。

- [ ] **Step 2: 创建TodoViewModel**

新建 `TodoViewModel.kt`，管理待办列表状态，提供加载、创建、完成、删除等操作，使用 `TodoRepository` 进行数据操作。

- [ ] **Step 3: 创建TodoScreen**

新建 `TodoScreen.kt`，使用Jetpack Compose实现待办列表界面，包含：
- 待办列表展示（按状态分组：待处理/进行中/已完成）
- 新建待办对话框
- 待办项滑动操作（完成/删除）
- 与AI对话联动（从对话中创建待办）

- [ ] **Step 4: 在OperitScreens.kt中添加Todo Screen定义**

添加 `data object Todo : Screen(navItem = NavItem.Todo)` 及其Content实现。

- [ ] **Step 5: 在DrawerContent.kt侧边栏预留按钮区添加待办入口**

在 `NewSidebarTopContent` 的快捷操作区添加待办快捷卡片，与工作流、日历并列。

- [ ] **Step 6: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat(todo): add sidebar entry and todo UI screen"
```

---

### Task 4: 日程功能 - ObjectBox实体与数据层

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/data/model/ScheduleEntity.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/data/repository/ScheduleRepository.kt`

- [ ] **Step 1: 创建ScheduleEntity ObjectBox实体**

新建 `ScheduleEntity.kt`，定义日程数据模型：

```kotlin
package com.ai.assistance.operit.data.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.Date

@Entity
data class ScheduleEntity(
    @Id var id: Long = 0,
    @Index var uuid: String = "",
    var title: String = "",
    var description: String = "",
    var startTime: Date = Date(),
    var endTime: Date? = null,
    var isAllDay: Boolean = false,
    var recurrence: String = "",
    var location: String = "",
    var reminderMinutesBefore: Int = 0,
    var status: String = STATUS_CONFIRMED,
    var createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var source: String = SOURCE_MANUAL,
    var chatId: String? = null,
    var tags: String = ""
) {
    companion object {
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_TENTATIVE = "tentative"
        const val STATUS_CANCELLED = "cancelled"
        const val RECURRENCE_NONE = ""
        const val RECURRENCE_DAILY = "daily"
        const val RECURRENCE_WEEKLY = "weekly"
        const val RECURRENCE_MONTHLY = "monthly"
        const val RECURRENCE_YEARLY = "yearly"
        const val SOURCE_MANUAL = "manual"
        const val SOURCE_AI = "ai"
        const val SOURCE_CHAT = "chat"
    }
}
```

- [ ] **Step 2: 创建ScheduleRepository**

新建 `ScheduleRepository.kt`，封装ObjectBox CRUD操作，包含按日期范围查询、按日查询、即将到来的日程查询等方法。

- [ ] **Step 3: 触发ObjectBox代码生成并验证编译**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(schedule): add ScheduleEntity ObjectBox model and ScheduleRepository"
```

---

### Task 5: 日程功能 - AI工具调用集成

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/core/tools/defaultTool/standard/StandardScheduleTools.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/ToolRegistration.kt` (注册日程工具)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemToolPrompts.kt` (添加日程工具提示词)

- [ ] **Step 1: 创建StandardScheduleTools**

新建 `StandardScheduleTools.kt`，实现AI可调用的日程管理工具：

```kotlin
package com.ai.assistance.operit.core.tools.defaultTool.standard

import android.content.Context
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.data.model.StringResultData
import com.ai.assistance.operit.data.repository.ScheduleRepository
import com.ai.assistance.operit.data.model.ScheduleEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class StandardScheduleTools(private val context: Context) {
    private val repository by lazy { ScheduleRepository(context) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun createSchedule(tool: AITool): ToolResult {
        val title = tool.parameters.find { it.name == "title" }?.value ?: return errorResult(tool, "title is required")
        val description = tool.parameters.find { it.name == "description" }?.value ?: ""
        val startTimeStr = tool.parameters.find { it.name == "start_time" }?.value ?: return errorResult(tool, "start_time is required")
        val endTimeStr = tool.parameters.find { it.name == "end_time" }?.value
        val isAllDay = tool.parameters.find { it.name == "is_all_day" }?.value?.toBoolean() ?: false
        val location = tool.parameters.find { it.name == "location" }?.value ?: ""
        val recurrence = tool.parameters.find { it.name == "recurrence" }?.value ?: ScheduleEntity.RECURRENCE_NONE
        val reminderMinutes = tool.parameters.find { it.name == "reminder_minutes_before" }?.value?.toIntOrNull() ?: 0

        val entity = ScheduleEntity(
            title = title,
            description = description,
            startTime = dateFormat.parse(startTimeStr) ?: return errorResult(tool, "invalid start_time format"),
            endTime = endTimeStr?.let { dateFormat.parse(it) },
            isAllDay = isAllDay,
            location = location,
            recurrence = recurrence,
            reminderMinutesBefore = reminderMinutes,
            source = ScheduleEntity.SOURCE_AI
        )
        val id = repository.insert(entity)
        return ToolResult(toolName = tool.name, success = true, result = StringResultData("Schedule created with id=$id, title=$title"))
    }

    fun listSchedules(tool: AITool): ToolResult {
        val startDateStr = tool.parameters.find { it.name == "start_date" }?.value
        val endDateStr = tool.parameters.find { it.name == "end_date" }?.value
        val schedules = if (startDateStr != null && endDateStr != null) {
            val start = dateFormat.parse(startDateStr)
            val end = dateFormat.parse(endDateStr)
            if (start != null && end != null) repository.getByDateRange(start, end) else repository.getAll()
        } else {
            repository.getAll()
        }
        val arr = JSONArray()
        schedules.take(50).forEach { schedule ->
            arr.put(JSONObject().apply {
                put("uuid", schedule.uuid)
                put("title", schedule.title)
                put("start_time", dateFormat.format(schedule.startTime))
                put("end_time", schedule.endTime?.let { dateFormat.format(it) } ?: "")
                put("is_all_day", schedule.isAllDay)
                put("location", schedule.location)
                put("recurrence", schedule.recurrence)
            })
        }
        return ToolResult(toolName = tool.name, success = true, result = StringResultData(arr.toString()))
    }

    fun deleteSchedule(tool: AITool): ToolResult {
        val uuid = tool.parameters.find { it.name == "uuid" }?.value ?: return errorResult(tool, "uuid is required")
        repository.deleteByUuid(uuid)
        return ToolResult(toolName = tool.name, success = true, result = StringResultData("Schedule $uuid deleted"))
    }

    private fun errorResult(tool: AITool, error: String) = ToolResult(
        toolName = tool.name, success = false, result = StringResultData(""), error = error
    )
}
```

- [ ] **Step 2: 在ToolRegistration.kt中注册日程工具**

在 `registerAllTools` 函数中添加日程工具注册：`create_schedule`、`list_schedules`、`delete_schedule`。

- [ ] **Step 3: 在SystemToolPrompts.kt中添加日程工具提示词**

添加日程工具的系统提示词描述，说明AI可以使用 `create_schedule`、`list_schedules`、`delete_schedule` 三个工具来管理用户的日程。

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(schedule): add AI tool integration for schedule management"
```

---

### Task 6: 日程功能 - 侧边栏入口与UI

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/ui/features/schedule/screens/ScheduleScreen.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/ui/features/schedule/viewmodel/ScheduleViewModel.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/common/NavItem.kt` (添加Schedule导航项)
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/screens/OperitScreens.kt` (添加Schedule Screen)
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/main/components/DrawerContent.kt` (侧边栏预留按钮区添加日程入口)

- [ ] **Step 1: 在NavItem.kt中添加Schedule导航项**

```kotlin
object Schedule : NavItem("schedule", R.string.nav_schedule, Icons.Default.CalendarToday)
```

同时在字符串资源文件中添加 `nav_schedule` 对应的中文字符串。

- [ ] **Step 2: 创建ScheduleViewModel**

新建 `ScheduleViewModel.kt`，管理日程列表状态，提供按日/周/月视图加载、创建、删除等操作。

- [ ] **Step 3: 创建ScheduleScreen**

新建 `ScheduleScreen.kt`，使用Jetpack Compose实现日程界面，包含：
- 日历视图（月/周切换）
- 日程列表（按日期分组）
- 新建日程对话框
- 日程详情查看
- 与AI对话联动（从对话中创建日程）

- [ ] **Step 4: 在OperitScreens.kt中添加Schedule Screen定义**

- [ ] **Step 5: 在DrawerContent.kt侧边栏预留按钮区添加日程入口**

在 `NewSidebarTopContent` 的快捷操作区添加日程快捷卡片，与工作流、待办并列。

- [ ] **Step 6: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat(schedule): add sidebar entry and schedule UI screen"
```

---

### Task 7: 上下文自动注入 - 上下文采集器

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/api/chat/enhance/ContextAutoInjector.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/api/chat/enhance/ContextSource.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/services/notification/OperitNotificationListenerService.kt` (确保通知数据可被采集器读取)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/system/MediaProjectionCaptureManager.kt` (确保屏幕内容可被采集器读取)

- [ ] **Step 1: 创建ContextSource数据类**

新建 `ContextSource.kt`，定义上下文来源枚举和优先级：

```kotlin
package com.ai.assistance.operit.api.chat.enhance

enum class ContextSourcePriority(val order: Int) {
    MEMORY(1),
    NOTIFICATION(2),
    SCREEN(3),
    LOCATION(4),
    USAGE_TIME(5)
}

data class ContextSnippet(
    val source: ContextSourcePriority,
    val content: String,
    val tokenEstimate: Int
)
```

- [ ] **Step 2: 创建ContextAutoInjector**

新建 `ContextAutoInjector.kt`，实现上下文自动注入调度器：

```kotlin
package com.ai.assistance.operit.api.chat.enhance

import android.content.Context
import com.ai.assistance.operit.data.repository.MemoryRepository
import com.ai.assistance.operit.services.notification.OperitNotificationStore
import com.ai.assistance.operit.data.model.NotificationData

class ContextAutoInjector(private val context: Context) {
    companion object {
        const val MAX_CONTEXT_TOKENS = 500
        const val CHARS_PER_TOKEN = 2
    }

    suspend fun buildAutoContext(userMessage: String): List<ContextSnippet> {
        val snippets = mutableListOf<ContextSnippet>()
        var remainingTokens = MAX_CONTEXT_TOKENS

        val candidates = listOf(
            { collectMemoryContext(userMessage) },
            { collectNotificationContext() },
            { collectScreenContext() },
            { collectLocationContext() },
            { collectUsageTimeContext() }
        )

        for (collector in candidates) {
            if (remainingTokens <= 0) break
            val snippet = collector()
            if (snippet != null && snippet.tokenEstimate <= remainingTokens) {
                snippets.add(snippet)
                remainingTokens -= snippet.tokenEstimate
            }
        }

        return snippets.sortedBy { it.source.order }
    }

    private suspend fun collectMemoryContext(userMessage: String): ContextSnippet? {
        val memoryRepo = MemoryRepository(context)
        val results = memoryRepo.search(userMessage, limit = 3)
        if (results.isEmpty()) return null
        val content = results.joinToString("\n") { "- ${it.title}: ${it.content.take(200)}" }
        return ContextSnippet(ContextSourcePriority.MEMORY, content, estimateTokens(content))
    }

    private fun collectNotificationContext(): ContextSnippet? {
        val notifications = OperitNotificationStore.snapshot(limit = 5, includeOngoing = false)
        if (notifications.isEmpty()) return null
        val content = notifications.joinToString("\n") { "- [${it.packageName}] ${it.text.take(100)}" }
        return ContextSnippet(ContextSourcePriority.NOTIFICATION, content, estimateTokens(content))
    }

    private fun collectScreenContext(): ContextSnippet? {
        return null
    }

    private fun collectLocationContext(): ContextSnippet? {
        return null
    }

    private fun collectUsageTimeContext(): ContextSnippet? {
        return null
    }

    private fun estimateTokens(text: String): Int = text.length / CHARS_PER_TOKEN
}
```

- [ ] **Step 3: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(context): add ContextAutoInjector with priority-based context collection"
```

---

### Task 8: 上下文自动注入 - 集成到对话流程

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/enhance/InputProcessor.kt` (在输入处理流程中调用ContextAutoInjector)
- Modify: `app/src/main/java/com/ai/assistance/operit/services/core/MessageProcessingDelegate.kt` (在消息处理前注入上下文)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemPromptConfig.kt` (添加自动注入上下文的系统提示词段落)

- [ ] **Step 1: 在InputProcessor中集成上下文注入**

修改 `InputProcessor.kt`，在处理用户输入时调用 `ContextAutoInjector.buildAutoContext()`，将采集到的上下文片段拼接为附加文本，注入到用户消息之前。

- [ ] **Step 2: 在SystemPromptConfig中添加自动注入段落**

在系统提示词中添加自动注入上下文的说明段落，格式如下：

```
[自动上下文]
以下是自动采集的上下文信息，仅供参考：
{context_snippets}
```

- [ ] **Step 3: 在MessageProcessingDelegate中确保上下文注入在消息发送前完成**

确保上下文注入在消息处理流程的早期阶段完成，不阻塞用户消息发送，注入结果作为系统上下文附加。

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(context): integrate auto-injection into chat input processing flow"
```

---

### Task 9: 上下文自动注入 - 附件区UI展示

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/chat/screens/AIChatScreen.kt` (附件区自动注入展示)
- Modify: `app/src/main/java/com/ai/assistance/operit/services/core/AttachmentDelegate.kt` (附件委托中添加自动注入项)

- [ ] **Step 1: 在附件区添加自动注入项展示**

修改聊天界面附件区域，分为两个区域：
- **自动注入区**（无需用户手动选择）：通知、屏幕内容、位置、屏幕使用时间、记忆，每个项显示为小标签，可点击开关
- **手动选择区**：截图、拍照、文件、工具包

- [ ] **Step 2: 在AttachmentDelegate中添加自动注入项状态管理**

添加自动注入项的开关状态管理，用户可以控制每个自动注入项的启用/禁用。

- [ ] **Step 3: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(context): add auto-injection UI in chat attachment area"
```

---

### Task 10: P4收尾 - 字符串资源清理

**Files:**
- Modify: `app/src/main/res/values/strings.xml` (清理未使用的字符串资源)
- Modify: `app/src/main/res/values-zh/strings.xml` (清理中文未使用字符串资源)

- [ ] **Step 1: 扫描未使用的字符串资源**

使用Android Lint检测未使用的字符串资源：

```bash
./gradlew :app:lintDebug 2>&1 | grep "UnusedResources" | head -50
```

- [ ] **Step 2: 逐一确认并删除未使用的字符串资源**

对每个未使用的字符串资源进行确认，确保确实没有在代码、布局或Manifest中引用后删除。特别注意：
- 不删除模型定价相关的字符串资源
- 不删除待办和日程功能新增的字符串资源
- 保留可能在动态引用中使用的字符串

- [ ] **Step 3: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "chore: clean up unused string resources"
```

---

### Task 11: P4收尾 - 模型定价数据完整保留

**Files:**
- Verify: `app/src/main/java/com/ai/assistance/operit/data/collects/ModelPricingDefaultsCollect.kt`
- Verify: `app/src/main/java/com/ai/assistance/operit/data/collects/ScrapedModelPricingRowsCollect.kt`

- [ ] **Step 1: 验证模型定价数据完整性**

检查 `ScrapedModelPricingRowsCollect.kt` 中的定价数据行数和格式完整性：

```bash
wc -l app/src/main/java/com/ai/assistance/operit/data/collects/ScrapedModelPricingRowsCollect.kt
grep -c "|" app/src/main/java/com/ai/assistance/operit/data/collects/ScrapedModelPricingRowsCollect.kt
```

- [ ] **Step 2: 验证ModelPricingDefaultsCollect逻辑完整性**

确认 `DefaultModelPricingCollect` 的 `getDefaultPricing`、`isDomesticProvider`、`getCurrency` 方法正常工作，所有provider的定价数据均可正确查询。

- [ ] **Step 3: 确认无任何定价数据被误删**

确保在P0-P3阶段的重构中，模型定价数据未被意外删除或截断。如发现缺失，从版本控制中恢复。

- [ ] **Step 4: Commit (如有修复)**

```bash
git add -A && git commit -m "chore: verify model pricing data integrity"
```

---

### Task 12: P4收尾 - 聊天格式导入导出精简

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/data/converter/ChatFormat.kt` (精简导入格式枚举)
- Modify: `app/src/main/java/com/ai/assistance/operit/data/converter/ChatFormatDetector.kt` (精简格式检测)
- Delete: `app/src/main/java/com/ai/assistance/operit/data/converter/ChatBoxConverter.kt` (移除ChatBox导入)
- Delete: `app/src/main/java/com/ai/assistance/operit/data/converter/GenericJsonConverter.kt` (移除GenericJson导入)
- Modify: `app/src/main/java/com/ai/assistance/operit/data/exporter/HtmlExporter.kt` (移除HTML导出)
- Verify: `app/src/main/java/com/ai/assistance/operit/data/converter/ChatGPTConverter.kt` (保留ChatGPT导入)
- Verify: `app/src/main/java/com/ai/assistance/operit/data/converter/MarkdownConverter.kt` (保留Markdown导入)
- Verify: `app/src/main/java/com/ai/assistance/operit/data/exporter/MarkdownExporter.kt` (保留Markdown导出)
- Verify: `app/src/main/java/com/ai/assistance/operit/data/exporter/TextExporter.kt` (保留纯文本导出)

- [ ] **Step 1: 精简ChatFormat枚举**

修改 `ChatFormat.kt`，导入仅保留 `CHATGPT` 和 `MARKDOWN`，导出仅保留 `MARKDOWN` 和 `TXT`：

```kotlin
enum class ChatFormat {
    OPERIT,
    CHATGPT,
    MARKDOWN,
    PLAIN_TEXT,
    UNKNOWN
}

enum class ExportFormat {
    MARKDOWN,
    TXT
}
```

- [ ] **Step 2: 删除ChatBoxConverter和GenericJsonConverter**

```bash
rm app/src/main/java/com/ai/assistance/operit/data/converter/ChatBoxConverter.kt
rm app/src/main/java/com/ai/assistance/operit/data/converter/GenericJsonConverter.kt
```

- [ ] **Step 3: 删除HtmlExporter**

```bash
rm app/src/main/java/com/ai/assistance/operit/data/exporter/HtmlExporter.kt
```

- [ ] **Step 4: 更新ChatFormatDetector**

移除对ChatBox、GenericJson、CSV格式的检测逻辑，仅保留ChatGPT和Markdown检测。

- [ ] **Step 5: 清理所有对已删除Converter/Exporter的引用**

搜索并移除所有对 `ChatBoxConverter`、`GenericJsonConverter`、`HtmlExporter` 的import和使用。

- [ ] **Step 6: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "refactor(converter): simplify chat import to ChatGPT+Markdown, export to Markdown+PlainText"
```

---

### Task 13: P4收尾 - Compose DSL渲染器移除LiquidGlass组件

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/javascript/JsComposeDslBridge.kt` (移除LiquidGlass相关DSL节点)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/javascript/JsComposeDslRuntimeScript.kt` (移除LiquidGlass运行时支持)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/packTool/ToolPkgComposeDslParser.kt` (移除LiquidGlass解析)

- [ ] **Step 1: 搜索Compose DSL中所有LiquidGlass引用**

```bash
grep -rn "liquidGlass\|LiquidGlass\|liquid_glass\|waterGlass\|WaterGlass\|water_glass" \
  app/src/main/java/com/ai/assistance/operit/core/tools/javascript/ \
  app/src/main/java/com/ai/assistance/operit/core/tools/packTool/
```

- [ ] **Step 2: 在JsComposeDslBridge中移除LiquidGlass节点类型**

找到DSL桥接定义中注册 `liquidGlass` 或 `LiquidGlass` 修饰符的代码，将其移除。如果DSL树中存在 `type: "liquidGlass"` 节点定义，删除该节点类型。

- [ ] **Step 3: 在JsComposeDslRuntimeScript中移除LiquidGlass运行时支持**

移除运行时脚本中对 `liquidGlass` 修饰符的处理逻辑，将其降级为空操作（no-op Modifier）。

- [ ] **Step 4: 在ToolPkgComposeDslParser中移除LiquidGlass解析**

移除解析器中对LiquidGlass属性的处理。

- [ ] **Step 5: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor(compose-dsl): remove LiquidGlass component from DSL renderer"
```

---

### Task 14: P4收尾 - 性能优化

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/data/db/ObjectBox.kt` (优化BoxStore缓存)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/AIToolHandler.kt` (优化工具查找性能)
- Modify: `app/src/main/java/com/ai/assistance/operit/ui/features/chat/screens/AIChatScreen.kt` (优化Compose重组)

- [ ] **Step 1: 优化ObjectBox查询性能**

- 为 `TodoEntity` 和 `ScheduleEntity` 的高频查询字段添加 `@Index` 注解（已在实体定义中添加）
- 对频繁查询的方法添加查询缓存（如 `getAll()` 结果缓存，数据变更时失效）

- [ ] **Step 2: 优化AIToolHandler工具查找**

当前 `availableTools` 使用 `ConcurrentHashMap`，工具名查找为O(1)，无需优化。但可优化 `getAllToolNames()` 的排序操作，改为延迟排序或缓存排序结果。

- [ ] **Step 3: 优化聊天界面Compose重组**

- 检查 `AIChatScreen` 中是否存在不必要的重组
- 对稳定的数据类添加 `@Stable` 注解
- 对大列表使用 `key` 参数优化 `LazyColumn` 重组

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "perf: optimize ObjectBox queries, tool lookup, and Compose recomposition"
```

---

### Task 15: 阶段四 - 系统规则自动化

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/data/preferences/ModelConfigManager.kt` (常用配置项改为系统默认值)
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/system/OperitTerminalManager.kt` (终端首次使用自动安装依赖)
- Modify: `app/src/main/java/com/ai/assistance/operit/data/preferences/FunctionalConfigManager.kt` (其他规则自动化处理)

- [ ] **Step 1: 将常用配置项改为系统默认值**

修改 `ModelConfigManager.kt`，将以下参数设为系统自动计算，用户无需手动配置：
- `topP`：根据模型类型自动设置（聊天模型0.9，代码模型0.95）
- `maxTokens`：根据模型上下文长度自动计算（上下文长度的50%）
- `contextLength`：根据模型规格自动设置
- `summaryEnabled`：默认开启
- `summaryThreshold`：自动根据上下文长度计算

- [ ] **Step 2: 终端首次使用自动安装依赖**

修改 `OperitTerminalManager.kt`，在终端首次启动时自动静默安装以下依赖：
- Node.js
- PNPM
- Python环境
- Python链接
- Python虚拟环境
- Pip
- uv

安装过程后台执行，不阻塞用户操作。安装状态持久化，避免重复安装。

- [ ] **Step 3: 其他规则自动化处理**

修改 `FunctionalConfigManager.kt`，将以下配置项设为系统自动管理：
- 工具权限默认策略：自动根据权限级别设置
- 记忆自动保存：默认开启
- 上下文自动注入：默认开启
- 流式输出：默认开启

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(automation): set common config to system defaults, auto-install terminal deps"
```

---

### Task 16: 阶段五 - 清理无用资源文件

**Files:**
- Scan and delete: `app/src/main/res/` 下未使用的资源文件
- Scan and delete: `app/src/main/assets/` 下未使用的资源文件

- [ ] **Step 1: 扫描未使用的资源文件**

使用Android Lint检测未使用的资源：

```bash
./gradlew :app:lintDebug 2>&1 | grep "UnusedResources" | head -100
```

- [ ] **Step 2: 清理未使用的drawable资源**

逐一确认并删除未使用的drawable、mipmap资源文件。

- [ ] **Step 3: 清理未使用的assets资源**

检查 `app/src/main/assets/` 目录，删除已移除功能相关的资源文件（如已删除的沙箱包资源、模板资源等）。

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "chore: clean up unused resource files"
```

---

### Task 17: 阶段五 - 清理无用依赖

**Files:**
- Modify: `app/build.gradle.kts` (移除无用依赖)
- Modify: `settings.gradle.kts` (移除无用子模块)

- [ ] **Step 1: 扫描未使用的Gradle依赖**

使用Gradle依赖分析工具检测未使用的依赖：

```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath 2>&1 | head -100
```

- [ ] **Step 2: 移除已删除功能对应的依赖**

根据P0阶段已删除的功能模块，移除对应的Gradle依赖：
- Filament相关依赖（已在P0移除）
- DragonBones相关依赖（已在P0移除）
- 其他已删除模块的依赖

- [ ] **Step 3: 移除已删除的Gradle子模块**

确认 `settings.gradle.kts` 中不再包含已删除的子模块（showerclient、dragonbones、fbx、mmd、tools/shower）。

- [ ] **Step 4: 验证编译通过**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -20
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "chore: clean up unused Gradle dependencies and submodules"
```

---

### Task 18: 阶段五 - 验证核心功能正常

**Files:**
- All modified files in P3/P4

- [ ] **Step 1: 全量编译验证**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -30
```

- [ ] **Step 2: 运行单元测试**

```bash
./gradlew :app:testDebugUnitTest 2>&1 | tail -30
```

- [ ] **Step 3: 运行Lint检查**

```bash
./gradlew :app:lintDebug 2>&1 | tail -30
```

- [ ] **Step 4: 功能验证清单**

逐一验证以下核心功能：
- [ ] AI对话正常工作
- [ ] 工具调用正常执行
- [ ] 待办功能：创建/列表/完成/删除
- [ ] 日程功能：创建/列表/删除
- [ ] 上下文自动注入：记忆/通知注入
- [ ] 聊天导入：ChatGPT格式、Markdown格式
- [ ] 聊天导出：Markdown格式、纯文本格式
- [ ] 侧边栏导航：待办/日程入口正常
- [ ] 设置页面：模型配置简化正常
- [ ] 终端：首次使用自动安装依赖

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "chore: verify core functionality after P3/P4 implementation"
```

---

### Task 19: 阶段五 - 更新文档

**Files:**
- Modify: `PLAN.md` (标记P3/P4任务为已完成)
- Modify: `README.md` (更新功能列表和状态)

- [ ] **Step 1: 更新PLAN.md**

将PLAN.md中第十章（P3新功能）和第十一章（P4收尾打磨）标记为已完成状态。

- [ ] **Step 2: 更新README.md**

更新README.md中的功能列表，添加待办功能和日程功能的说明，更新当前状态描述。

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "docs: update PLAN and README for P3/P4 completion"
```
