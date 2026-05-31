# P1-架构调整 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将AutoGLM子代理和Browser从JS沙箱包迁移为Kotlin原生内置工具，增强长程任务执行能力（断点恢复/超时重试/进度通知），简化模型配置UI仅暴露4项核心参数。

**Architecture:** AutoGLM子代理和Browser内置化采用"新建Kotlin执行器 + 复用现有底层方法 + 注册为内置工具"的模式，保留JS包文件供第三方沙箱包通过JS接口调用。长程任务增强采用ObjectBox持久化任务记录 + 检查点管理 + 指数退避重试的架构。模型配置简化在ModelConfigManager中增加"简化模式"开关，UI层仅暴露4项参数，其余参数由系统根据模型名自动计算默认值。

**Tech Stack:** Kotlin, Android, ObjectBox

---

### Task 1: AutoGLM子代理内置化 - 新建AutoGLMSubAgentExecutor

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/core/tools/autoglm/AutoGLMSubAgentExecutor.kt`

- [ ] **Step 1: 创建AutoGLMSubAgentExecutor类骨架**

新建 `core/tools/autoglm/AutoGLMSubAgentExecutor.kt`，实现 `ToolExecutor` 接口。该类负责将 `automatic_ui_subagent.js` 中的工具调用逻辑迁移为Kotlin原生实现，核心是调用 `PhoneAgent.run()` 方法执行UI自动化子代理循环。

```kotlin
package com.ai.assistance.operit.core.tools.autoglm

import android.content.Context
import com.ai.assistance.operit.api.chat.EnhancedAIService
import com.ai.assistance.operit.core.config.FunctionalPrompts
import com.ai.assistance.operit.core.tools.AutomationExecutionResult
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.core.tools.ToolExecutor
import com.ai.assistance.operit.core.tools.agent.ActionHandler
import com.ai.assistance.operit.core.tools.agent.AgentConfig
import com.ai.assistance.operit.core.tools.agent.PhoneAgent
import com.ai.assistance.operit.core.tools.agent.ShowerController
import com.ai.assistance.operit.core.tools.defaultTool.standard.StandardUITools
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.FunctionType
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.util.LocaleUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AutoGLMSubAgentExecutor(private val context: Context) : ToolExecutor {

    companion object {
        private const val TAG = "AutoGLMSubAgent"
    }

    override fun invoke(tool: AITool): ToolResult {
        throw UnsupportedOperationException("Use suspend invokeSuspend instead")
    }

    suspend fun invokeSuspend(tool: AITool): ToolResult {
        val intent = tool.parameters.find { it.name == "intent" }?.value
        val maxSteps = tool.parameters.find { it.name == "max_steps" }?.value?.toIntOrNull() ?: 20
        val requestedAgentId = tool.parameters.find { it.name == "agent_id" }?.value
        val targetApp = tool.parameters.find { it.name == "target_app" }?.value

        if (intent.isNullOrBlank()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Missing required parameter: intent"
            )
        }

        val uiConfig = EnhancedAIService.getModelConfigForFunction(context, FunctionType.UI_CONTROLLER)
        if (!uiConfig.enableDirectImageProcessing) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "UI controller model does not support image recognition"
            )
        }

        return try {
            val uiService = EnhancedAIService.getAIServiceForFunction(context, FunctionType.UI_CONTROLLER)
            val systemPrompt = buildUiAutomationSystemPrompt()
            val metrics = context.resources.displayMetrics
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels

            val uiTools = StandardUITools(context)
            val agentConfig = AgentConfig(maxSteps = maxSteps)
            val actionHandler = ActionHandler(
                context = context,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                toolImplementations = uiTools
            )

            val agentId = if (!requestedAgentId.isNullOrBlank()) requestedAgentId else "default"
            val agent = PhoneAgent(
                context = context,
                config = agentConfig,
                uiService = uiService,
                actionHandler = actionHandler,
                agentId = agentId,
                cleanupOnFinish = false
            )

            val pausedState = MutableStateFlow(false)
            val finalMessage = agent.run(
                task = intent,
                systemPrompt = systemPrompt,
                isPausedFlow = pausedState,
                targetApp = targetApp
            )

            val displayId = try {
                ShowerController.getDisplayId(agentId)
            } catch (_: Exception) {
                null
            }

            val success = !finalMessage.contains("Max steps reached") && !finalMessage.contains("Error")
            val executionMessage = buildString {
                appendLine("UI automation subagent run summary:")
                appendLine("Intent: $intent")
                appendLine("Steps executed: ${agent.stepCount} / ${agentConfig.maxSteps}")
                appendLine("Finished: $success")
                appendLine("Final message: $finalMessage")
            }

            val resultData = AutomationExecutionResult(
                functionName = "AutoGLMSubAgent",
                providedParameters = buildMap {
                    put("intent", intent)
                    put("max_steps", maxSteps.toString())
                    if (!targetApp.isNullOrBlank()) put("target_app", targetApp)
                    if (!requestedAgentId.isNullOrBlank()) put("agent_id", requestedAgentId)
                },
                agentId = agentId,
                displayId = displayId,
                executionSuccess = success,
                executionMessage = executionMessage,
                executionError = if (!success) finalMessage else null,
                finalState = null,
                executionSteps = agent.stepCount
            )

            ToolResult(toolName = tool.name, success = true, result = resultData, error = "")
        } catch (e: CancellationException) {
            AppLogger.e(TAG, "AutoGLM subagent cancelled", e)
            ToolResult(toolName = tool.name, success = false, result = StringResultData(""), error = "Cancelled: ${e.message}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error running AutoGLM subagent", e)
            ToolResult(toolName = tool.name, success = false, result = StringResultData(""), error = "Error: ${e.message}")
        }
    }

    private fun buildUiAutomationSystemPrompt(): String {
        val useEnglish = LocaleUtils.getCurrentLanguage(context).lowercase().startsWith("en")
        val formattedDate = if (useEnglish) {
            SimpleDateFormat("yyyy-MM-dd EEEE", Locale.ENGLISH).format(Date())
        } else {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            val datePart = sdf.format(Date())
            val weekdayNames = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
            val weekday = weekdayNames[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            "$datePart $weekday"
        }
        return FunctionalPrompts.buildUiAutomationAgentPrompt(formattedDate, useEnglish)
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(autoglm): create AutoGLMSubAgentExecutor with PhoneAgent integration"
```

---

### Task 2: AutoGLM子代理内置化 - 在ToolRegistration中注册内置工具

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/ToolRegistration.kt`

- [ ] **Step 1: 在ToolRegistration中注册autoglm_subagent内置工具**

在 `registerAllTools` 函数中，在 `run_ui_subagent` 工具注册之后，新增 `autoglm_subagent` 工具注册。该工具直接调用 `AutoGLMSubAgentExecutor.invokeSuspend()`，无需经过JS沙箱包。

```kotlin
// 在 run_ui_subagent 注册之后添加:

val autoglmExecutor = AutoGLMSubAgentExecutor(context)

handler.registerTool(
    name = "autoglm_subagent",
    descriptionGenerator = { tool ->
        val intent = tool.parameters.find { it.name == "intent" }?.value ?: ""
        val maxSteps = tool.parameters.find { it.name == "max_steps" }?.value ?: "20"
        val agentId = tool.parameters.find { it.name == "agent_id" }?.value
        buildString {
            append("Run AutoGLM UI subagent: intent=$intent, max_steps=$maxSteps")
            if (!agentId.isNullOrBlank()) {
                append(", agent_id=$agentId")
            }
        }
    },
    executor = { tool -> runBlocking(Dispatchers.IO) { autoglmExecutor.invokeSuspend(tool) } }
)

handler.registerTool(
    name = "autoglm_subagent_parallel",
    descriptionGenerator = { tool ->
        val count = tool.parameters.count { it.name.startsWith("intent_") }
        "Run parallel AutoGLM UI subagents: $count intents"
    },
    executor = { tool -> runBlocking(Dispatchers.IO) { autoglmExecutor.invokeParallelSuspend(tool) } }
)
```

注意：需要在文件顶部添加 `import com.ai.assistance.operit.core.tools.autoglm.AutoGLMSubAgentExecutor`。

- [ ] **Step 2: 在AutoGLMSubAgentExecutor中补充并行执行方法**

在 `AutoGLMSubAgentExecutor.kt` 中新增 `invokeParallelSuspend` 方法，用于并行执行多个子代理意图：

```kotlin
suspend fun invokeParallelSuspend(tool: AITool): ToolResult {
    val intentParams = tool.parameters.filter { it.name.startsWith("intent_") }
        .sortedBy { it.name }
    val targetAppParams = tool.parameters.filter { it.name.startsWith("target_app_") }
        .associate { it.name to it.value }
    val agentIdParams = tool.parameters.filter { it.name.startsWith("agent_id_") }
        .associate { it.name to it.value }
    val maxSteps = tool.parameters.find { it.name == "max_steps" }?.value?.toIntOrNull() ?: 20

    if (intentParams.isEmpty()) {
        return ToolResult(toolName = tool.name, success = false, result = StringResultData(""), error = "No intent parameters provided")
    }

    val results = kotlinx.coroutines.coroutineScope {
        intentParams.mapIndexed { index, param ->
            val suffix = param.name.removePrefix("intent_")
            val targetApp = targetAppParams["target_app_$suffix"]
            val agentId = agentIdParams["agent_id_$suffix"]
            kotlinx.coroutines.async(Dispatchers.IO) {
                val subTool = AITool(
                    name = "autoglm_subagent",
                    parameters = buildList {
                        add(com.ai.assistance.operit.data.model.ToolParameter("intent", param.value))
                        add(com.ai.assistance.operit.data.model.ToolParameter("max_steps", maxSteps.toString()))
                        if (!agentId.isNullOrBlank()) add(com.ai.assistance.operit.data.model.ToolParameter("agent_id", agentId))
                        if (!targetApp.isNullOrBlank()) add(com.ai.assistance.operit.data.model.ToolParameter("target_app", targetApp))
                    }
                )
                invokeSuspend(subTool)
            }
        }.map { it.await() }
    }

    val allSuccess = results.all { it.success }
    val combinedMessage = results.mapIndexed { index, result ->
        "Sub-agent ${index + 1}: ${if (result.success) "SUCCESS" else "FAILED"} - ${result.error ?: "OK"}"
    }.joinToString("\n")

    return ToolResult(
        toolName = tool.name,
        success = allSuccess,
        result = StringResultData(combinedMessage),
        error = if (allSuccess) "" else "One or more sub-agents failed"
    )
}
```

- [ ] **Step 3: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(autoglm): register autoglm_subagent as built-in tool in ToolRegistration"
```

---

### Task 3: AutoGLM子代理内置化 - 在SystemToolPrompts中新增内置工具提示词

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemToolPrompts.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemToolPromptsInternal.kt` (如果存在)

- [ ] **Step 1: 在SystemToolPrompts中新增AutoGLM内置工具提示词分类**

在 `SystemToolPrompts` 对象中新增 `autoglmTools` 和 `autoglmToolsCn` 分类，包含 `autoglm_subagent` 和 `autoglm_subagent_parallel` 工具的结构化定义：

```kotlin
// 在 SystemToolPrompts 对象中添加:

val autoglmTools = SystemToolPromptCategory(
    categoryName = "AutoGLM UI Automation Tools",
    tools = listOf(
        ToolPrompt(
            name = "autoglm_subagent",
            description = "Run a UI automation sub-agent powered by AutoGLM to execute a sequence of UI actions (tap/type/swipe/launch) based on natural-language intent. The sub-agent uses an independent UI-controller model to plan and execute steps. IMPORTANT: each call is stateless - include full context in intent. Reuse agent_id for session continuity. On first use of a new agent_id, start intent with 'Launch XXX app...'.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "intent", type = "string", description = "Natural language description of the UI task. Must be self-contained with context. Template: 'Current progress: ... | Next step: ... | Useful info: ...'", required = true),
                ToolParameterSchema(name = "max_steps", type = "integer", description = "Maximum number of agent steps, default 20", required = false, default = "20"),
                ToolParameterSchema(name = "agent_id", type = "string", description = "Agent session ID. 'default' or omit = main screen; other values = virtual display session", required = false),
                ToolParameterSchema(name = "target_app", type = "string", description = "Target app name for prewarming virtual display", required = false)
            )
        ),
        ToolPrompt(
            name = "autoglm_subagent_parallel",
            description = "Run multiple AutoGLM UI sub-agents in parallel. Each sub-agent operates on an independent virtual display. All target_app_i values must be different across branches. Parallel branch count must not exceed available independent apps.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "intent_1", type = "string", description = "First sub-agent intent", required = true),
                ToolParameterSchema(name = "target_app_1", type = "string", description = "First sub-agent target app", required = false),
                ToolParameterSchema(name = "agent_id_1", type = "string", description = "First sub-agent agent ID", required = false),
                ToolParameterSchema(name = "intent_2", type = "string", description = "Second sub-agent intent", required = false),
                ToolParameterSchema(name = "target_app_2", type = "string", description = "Second sub-agent target app", required = false),
                ToolParameterSchema(name = "agent_id_2", type = "string", description = "Second sub-agent agent ID", required = false),
                ToolParameterSchema(name = "max_steps", type = "integer", description = "Max steps per sub-agent, default 20", required = false, default = "20")
            )
        )
    )
)

val autoglmToolsCn = SystemToolPromptCategory(
    categoryName = "AutoGLM UI自动化工具",
    tools = listOf(
        ToolPrompt(
            name = "autoglm_subagent",
            description = "运行基于AutoGLM的UI自动化子代理，根据自然语言意图执行一系列界面操作（点击/输入/滑动/启动应用）。子代理使用独立的UI控制器模型来规划和执行步骤。重要：每次调用是无状态的，intent必须自带完整上下文。复用agent_id保持会话连续性。首次使用新agent_id时，intent开头必须写"启动XXX应用..."。",
            parametersStructured = listOf(
                ToolParameterSchema(name = "intent", type = "string", description = "UI任务的自然语言描述，必须自包含上下文。模板："当前进度: ... | 下一步: ... | 可用信息: ..."", required = true),
                ToolParameterSchema(name = "max_steps", type = "integer", description = "最大执行步数，默认20", required = false, default = "20"),
                ToolParameterSchema(name = "agent_id", type = "string", description = "代理会话ID。'default'或不传=主屏幕；其他值=虚拟屏会话", required = false),
                ToolParameterSchema(name = "target_app", type = "string", description = "目标应用名，用于预热虚拟屏", required = false)
            )
        ),
        ToolPrompt(
            name = "autoglm_subagent_parallel",
            description = "并行运行多个AutoGLM UI子代理。每个子代理在独立虚拟屏上操作。所有target_app_i必须互不相同。并行分支数不得超过可用独立App数量。",
            parametersStructured = listOf(
                ToolParameterSchema(name = "intent_1", type = "string", description = "第一个子代理意图", required = true),
                ToolParameterSchema(name = "target_app_1", type = "string", description = "第一个子代理目标应用", required = false),
                ToolParameterSchema(name = "agent_id_1", type = "string", description = "第一个子代理agent ID", required = false),
                ToolParameterSchema(name = "intent_2", type = "string", description = "第二个子代理意图", required = false),
                ToolParameterSchema(name = "target_app_2", type = "string", description = "第二个子代理目标应用", required = false),
                ToolParameterSchema(name = "agent_id_2", type = "string", description = "第二个子代理agent ID", required = false),
                ToolParameterSchema(name = "max_steps", type = "integer", description = "每个子代理最大步数，默认20", required = false, default = "20")
            )
        )
    )
)
```

- [ ] **Step 2: 将autoglmTools分类加入getAllCategories方法**

在 `getAIAllCategoriesEn` 和 `getAIAllCategoriesCn` 方法的返回列表中追加 `autoglmTools` / `autoglmToolsCn`：

```kotlin
// getAIAllCategoriesEn 返回值追加:
return listOf(
    basicTools,
    adjustedFileSystemTools,
    httpTools,
    memoryTools,
    autoglmTools
)

// getAIAllCategoriesCn 返回值追加:
return listOf(
    basicToolsCn,
    adjustedFileSystemTools,
    httpToolsCn,
    memoryToolsCn,
    autoglmToolsCn
)
```

- [ ] **Step 3: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(autoglm): add AutoGLM built-in tool prompts to SystemToolPrompts"
```

---

### Task 4: Browser内置化 - 新建BrowserToolExecutor

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/core/tools/browser/BrowserToolExecutor.kt`

- [ ] **Step 1: 创建BrowserToolExecutor类**

新建 `core/tools/browser/BrowserToolExecutor.kt`，实现 `ToolExecutor` 接口。该类作为浏览器工具的Kotlin原生执行器，将 `browser.js` 中的工具调用逻辑直接委托给 `StandardBrowserSessionTools`：

```kotlin
package com.ai.assistance.operit.core.tools.browser

import android.content.Context
import com.ai.assistance.operit.core.tools.ToolExecutor
import com.ai.assistance.operit.core.tools.defaultTool.standard.StandardBrowserSessionTools
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolResult

class BrowserToolExecutor(private val context: Context) : ToolExecutor {

    private val sessionTools = StandardBrowserSessionTools(context)

    override fun invoke(tool: AITool): ToolResult {
        return sessionTools.invoke(tool)
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(browser): create BrowserToolExecutor delegating to StandardBrowserSessionTools"
```

---

### Task 5: Browser内置化 - 在ToolRegistration和SystemToolPrompts中注册

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/ToolRegistration.kt`
- Modify: `app/src/main/java/com/ai/assistance/operit/core/config/SystemToolPrompts.kt`

- [ ] **Step 1: 修改ToolRegistration中browser工具的executor**

当前 `ToolRegistration.kt` 中 browser 系列工具已经通过 `ToolGetter.getBrowserSessionTools(context).invoke(tool)` 调用 `StandardBrowserSessionTools`。内置化后，改为使用 `BrowserToolExecutor`，使调用路径更明确：

```kotlin
// 在 registerAllTools 函数开头创建 BrowserToolExecutor 实例:
val browserExecutor = BrowserToolExecutor(context)

// 将所有 browser_* 工具的 executor 改为:
executor = { tool -> browserExecutor.invoke(tool) }
```

注意：需要添加 `import com.ai.assistance.operit.core.tools.browser.BrowserToolExecutor`。

- [ ] **Step 2: 在SystemToolPrompts中新增Browser内置工具提示词分类**

在 `SystemToolPrompts` 对象中新增 `browserTools` 和 `browserToolsCn` 分类，包含所有 `browser_*` 工具的结构化定义：

```kotlin
val browserTools = SystemToolPromptCategory(
    categoryName = "Browser Automation Tools",
    categoryHeader = "Browser tools for web page automation (aligned to Playwright MCP surface). Always call browser_snapshot first to get element refs before interacting.",
    tools = listOf(
        ToolPrompt(
            name = "browser_navigate",
            description = "Navigate the browser to a URL.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "url", type = "string", description = "Target URL", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_snapshot",
            description = "Capture a browser accessibility snapshot including same-origin iframe content. Returns element refs for interaction.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "selector", type = "string", description = "Optional CSS selector to scope snapshot", required = false),
                ToolParameterSchema(name = "depth", type = "integer", description = "Optional max depth for snapshot tree", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_click",
            description = "Click a browser element by ref or selector.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "ref", type = "string", description = "Element ref from snapshot", required = false),
                ToolParameterSchema(name = "selector", type = "string", description = "CSS selector fallback", required = false),
                ToolParameterSchema(name = "element", type = "string", description = "Human-readable element description", required = false),
                ToolParameterSchema(name = "button", type = "string", description = "Mouse button: left/right/middle", required = false, default = "left"),
                ToolParameterSchema(name = "doubleClick", type = "boolean", description = "Double click", required = false, default = "false"),
                ToolParameterSchema(name = "modifiers", type = "array", description = "Modifier keys: Alt, Control, Meta, Shift", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_type",
            description = "Type text into a browser element by ref.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "ref", type = "string", description = "Element ref from snapshot", required = true),
                ToolParameterSchema(name = "text", type = "string", description = "Text to type", required = true),
                ToolParameterSchema(name = "submit", type = "boolean", description = "Press Enter after typing", required = false, default = "false"),
                ToolParameterSchema(name = "slowly", type = "boolean", description = "Type character by character", required = false, default = "false")
            )
        ),
        ToolPrompt(
            name = "browser_evaluate",
            description = "Evaluate JavaScript on the current page or element.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "function", type = "string", description = "JavaScript function source", required = true),
                ToolParameterSchema(name = "element", type = "string", description = "Optional element description", required = false),
                ToolParameterSchema(name = "ref", type = "string", description = "Optional element ref", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_fill_form",
            description = "Fill multiple form fields at once.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "fields", type = "array", description = "JSON array of {ref, value} objects", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_navigate_back",
            description = "Navigate browser back.",
            parametersStructured = emptyList()
        ),
        ToolPrompt(
            name = "browser_hover",
            description = "Hover over a browser element by ref.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "ref", type = "string", description = "Element ref from snapshot", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_drag",
            description = "Drag between two browser elements.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "startRef", type = "string", description = "Source element ref", required = true),
                ToolParameterSchema(name = "endRef", type = "string", description = "Target element ref", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_select_option",
            description = "Select options in a browser dropdown.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "ref", type = "string", description = "Select element ref", required = true),
                ToolParameterSchema(name = "values", type = "array", description = "JSON array of option values", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_handle_dialog",
            description = "Accept or dismiss the current browser dialog.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "accept", type = "boolean", description = "Whether to accept", required = true),
                ToolParameterSchema(name = "promptText", type = "string", description = "Text for prompt dialog", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_file_upload",
            description = "Upload files to the active file chooser or cancel it.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "paths", type = "array", description = "JSON array of absolute file paths; omit to cancel", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_press_key",
            description = "Press a keyboard key in the browser.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "key", type = "string", description = "Key name (e.g. Enter, Tab, Escape)", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_wait_for",
            description = "Wait for text to appear/disappear or a time duration.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "time", type = "number", description = "Seconds to wait", required = false),
                ToolParameterSchema(name = "text", type = "string", description = "Wait until this text appears", required = false),
                ToolParameterSchema(name = "textGone", type = "string", description = "Wait until this text disappears", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_take_screenshot",
            description = "Take a screenshot of the current browser page.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "type", type = "string", description = "Image format: png or jpeg", required = false, default = "png"),
                ToolParameterSchema(name = "fullPage", type = "boolean", description = "Capture full scrollable page", required = false, default = "false"),
                ToolParameterSchema(name = "ref", type = "string", description = "Optional element ref for element screenshot", required = false),
                ToolParameterSchema(name = "filename", type = "string", description = "Optional output filename", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_console_messages",
            description = "Read browser console messages.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "level", type = "string", description = "Log level: error/warning/info/debug", required = false, default = "info")
            )
        ),
        ToolPrompt(
            name = "browser_network_requests",
            description = "Read browser network request log.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "includeStatic", type = "boolean", description = "Include static resource requests", required = false, default = "false")
            )
        ),
        ToolPrompt(
            name = "browser_resize",
            description = "Resize the browser viewport.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "width", type = "integer", description = "Viewport width in pixels", required = true),
                ToolParameterSchema(name = "height", type = "integer", description = "Viewport height in pixels", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_run_code",
            description = "Run Playwright-like code in the browser.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "code", type = "string", description = "Playwright-like code to execute", required = true)
            )
        ),
        ToolPrompt(
            name = "browser_tabs",
            description = "Manage browser tabs: list, create, select, close.",
            parametersStructured = listOf(
                ToolParameterSchema(name = "action", type = "string", description = "Action: list/create/select/close", required = true),
                ToolParameterSchema(name = "index", type = "integer", description = "Tab index for select/close", required = false)
            )
        ),
        ToolPrompt(
            name = "browser_close",
            description = "Close the current browser tab.",
            parametersStructured = emptyList()
        ),
        ToolPrompt(
            name = "browser_close_all",
            description = "Close all browser tabs.",
            parametersStructured = emptyList()
        )
    ),
    categoryFooter = "Always call browser_snapshot before interacting with elements to get fresh refs. Refs become stale after page navigation."
)

val browserToolsCn = SystemToolPromptCategory(
    categoryName = "浏览器自动化工具",
    categoryHeader = "浏览器自动化工具（对齐Playwright MCP）。操作前务必先调用 browser_snapshot 获取元素ref。",
    tools = listOf(
        ToolPrompt(name = "browser_navigate", description = "导航浏览器到指定URL。", parametersStructured = listOf(ToolParameterSchema(name = "url", type = "string", description = "目标URL", required = true))),
        ToolPrompt(name = "browser_snapshot", description = "捕获浏览器无障碍快照，包括同源iframe内容。返回元素ref供交互使用。", parametersStructured = listOf(ToolParameterSchema(name = "selector", type = "string", description = "可选CSS选择器限定快照范围", required = false), ToolParameterSchema(name = "depth", type = "integer", description = "可选快照树最大深度", required = false))),
        ToolPrompt(name = "browser_click", description = "通过ref或selector点击浏览器元素。", parametersStructured = listOf(ToolParameterSchema(name = "ref", type = "string", description = "快照中的元素ref", required = false), ToolParameterSchema(name = "selector", type = "string", description = "CSS选择器备选", required = false), ToolParameterSchema(name = "element", type = "string", description = "元素的人类可读描述", required = false), ToolParameterSchema(name = "button", type = "string", description = "鼠标按钮: left/right/middle", required = false, default = "left"), ToolParameterSchema(name = "doubleClick", type = "boolean", description = "是否双击", required = false, default = "false"), ToolParameterSchema(name = "modifiers", type = "array", description = "修饰键: Alt, Control, Meta, Shift", required = false))),
        ToolPrompt(name = "browser_type", description = "在浏览器元素中输入文本。", parametersStructured = listOf(ToolParameterSchema(name = "ref", type = "string", description = "快照中的元素ref", required = true), ToolParameterSchema(name = "text", type = "string", description = "要输入的文本", required = true), ToolParameterSchema(name = "submit", type = "boolean", description = "输入后按Enter", required = false, default = "false"), ToolParameterSchema(name = "slowly", type = "boolean", description = "逐字符输入", required = false, default = "false"))),
        ToolPrompt(name = "browser_evaluate", description = "在当前页面或元素上执行JavaScript。", parametersStructured = listOf(ToolParameterSchema(name = "function", type = "string", description = "JavaScript函数源码", required = true), ToolParameterSchema(name = "element", type = "string", description = "可选元素描述", required = false), ToolParameterSchema(name = "ref", type = "string", description = "可选元素ref", required = false))),
        ToolPrompt(name = "browser_fill_form", description = "批量填写表单字段。", parametersStructured = listOf(ToolParameterSchema(name = "fields", type = "array", description = "JSON数组，每项含ref和value", required = true))),
        ToolPrompt(name = "browser_navigate_back", description = "浏览器后退。", parametersStructured = emptyList()),
        ToolPrompt(name = "browser_hover", description = "悬停在浏览器元素上。", parametersStructured = listOf(ToolParameterSchema(name = "ref", type = "string", description = "快照中的元素ref", required = true))),
        ToolPrompt(name = "browser_drag", description = "在两个浏览器元素之间拖拽。", parametersStructured = listOf(ToolParameterSchema(name = "startRef", type = "string", description = "源元素ref", required = true), ToolParameterSchema(name = "endRef", type = "string", description = "目标元素ref", required = true))),
        ToolPrompt(name = "browser_select_option", description = "在浏览器下拉框中选择选项。", parametersStructured = listOf(ToolParameterSchema(name = "ref", type = "string", description = "select元素ref", required = true), ToolParameterSchema(name = "values", type = "array", description = "选项值JSON数组", required = true))),
        ToolPrompt(name = "browser_handle_dialog", description = "接受或关闭当前浏览器对话框。", parametersStructured = listOf(ToolParameterSchema(name = "accept", type = "boolean", description = "是否接受", required = true), ToolParameterSchema(name = "promptText", type = "string", description = "prompt对话框输入文本", required = false))),
        ToolPrompt(name = "browser_file_upload", description = "向当前文件选择器上传文件或取消。", parametersStructured = listOf(ToolParameterSchema(name = "paths", type = "array", description = "绝对路径JSON数组；不传则取消", required = false))),
        ToolPrompt(name = "browser_press_key", description = "在浏览器中按下键盘按键。", parametersStructured = listOf(ToolParameterSchema(name = "key", type = "string", description = "按键名称（如Enter、Tab、Escape）", required = true))),
        ToolPrompt(name = "browser_wait_for", description = "等待文本出现/消失或指定时长。", parametersStructured = listOf(ToolParameterSchema(name = "time", type = "number", description = "等待秒数", required = false), ToolParameterSchema(name = "text", type = "string", description = "等待此文本出现", required = false), ToolParameterSchema(name = "textGone", type = "string", description = "等待此文本消失", required = false))),
        ToolPrompt(name = "browser_take_screenshot", description = "截取当前浏览器页面截图。", parametersStructured = listOf(ToolParameterSchema(name = "type", type = "string", description = "图片格式: png或jpeg", required = false, default = "png"), ToolParameterSchema(name = "fullPage", type = "boolean", description = "截取完整可滚动页面", required = false, default = "false"), ToolParameterSchema(name = "ref", type = "string", description = "可选元素ref用于元素截图", required = false), ToolParameterSchema(name = "filename", type = "string", description = "可选输出文件名", required = false))),
        ToolPrompt(name = "browser_console_messages", description = "读取浏览器控制台消息。", parametersStructured = listOf(ToolParameterSchema(name = "level", type = "string", description = "日志级别: error/warning/info/debug", required = false, default = "info"))),
        ToolPrompt(name = "browser_network_requests", description = "读取浏览器网络请求日志。", parametersStructured = listOf(ToolParameterSchema(name = "includeStatic", type = "boolean", description = "是否包含静态资源请求", required = false, default = "false"))),
        ToolPrompt(name = "browser_resize", description = "调整浏览器视口大小。", parametersStructured = listOf(ToolParameterSchema(name = "width", type = "integer", description = "视口宽度（像素）", required = true), ToolParameterSchema(name = "height", type = "integer", description = "视口高度（像素）", required = true))),
        ToolPrompt(name = "browser_run_code", description = "在浏览器中运行Playwright风格代码。", parametersStructured = listOf(ToolParameterSchema(name = "code", type = "string", description = "要执行的Playwright风格代码", required = true))),
        ToolPrompt(name = "browser_tabs", description = "管理浏览器标签页：列表、创建、选择、关闭。", parametersStructured = listOf(ToolParameterSchema(name = "action", type = "string", description = "操作: list/create/select/close", required = true), ToolParameterSchema(name = "index", type = "integer", description = "标签页索引（select/close时使用）", required = false))),
        ToolPrompt(name = "browser_close", description = "关闭当前浏览器标签页。", parametersStructured = emptyList()),
        ToolPrompt(name = "browser_close_all", description = "关闭所有浏览器标签页。", parametersStructured = emptyList())
    ),
    categoryFooter = "操作元素前务必先调用 browser_snapshot 获取最新ref。页面导航后ref会失效。"
)
```

- [ ] **Step 3: 将browserTools分类加入getAllCategories方法**

在 `getAIAllCategoriesEn` 和 `getAIAllCategoriesCn` 方法的返回列表中追加 `browserTools` / `browserToolsCn`。

- [ ] **Step 4: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(browser): register BrowserToolExecutor and add browser tool prompts to SystemToolPrompts"
```

---

### Task 6: Automatic_ui_base移除

**Files:**
- Delete: `app/src/main/assets/packages/automatic_ui_base.js`
- Delete: `examples/automatic_ui_base/` (整个目录)

- [ ] **Step 1: 删除automatic_ui_base.js包文件**

```bash
rm app/src/main/assets/packages/automatic_ui_base.js
```

- [ ] **Step 2: 删除examples/automatic_ui_base/源码目录**

```bash
rm -rf examples/automatic_ui_base/
```

- [ ] **Step 3: 确认StandardUITools.kt和PhoneAgent.kt保留不动**

验证以下文件未被修改：

```bash
git diff --name-only | grep -E "(StandardUITools|PhoneAgent)" && echo "WARNING: Core files modified" || echo "OK: Core files preserved"
```

`StandardUITools.kt` 保留原因：`AutoGLMSubAgentExecutor` 和 `BrowserToolExecutor` 都依赖它（前者通过 `PhoneAgent` -> `ActionHandler` -> `ToolImplementations` 间接依赖，后者通过 `StandardBrowserSessionTools` 间接依赖）。

`PhoneAgent.kt` 保留原因：`AutoGLMSubAgentExecutor` 直接调用 `PhoneAgent.run()` 方法。

- [ ] **Step 4: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor: remove automatic_ui_base JS package (logic migrated to built-in tools)"
```

---

### Task 7: 长程任务执行能力增强 - 新建core/task/目录和ObjectBox实体

**Files:**
- Create: `app/src/main/java/com/ai/assistance/operit/core/task/TaskRecord.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/core/task/TaskRepository.kt`
- Create: `app/src/main/java/com/ai/assistance/operit/core/task/TaskCheckpointManager.kt`

- [ ] **Step 1: 创建TaskRecord ObjectBox实体**

新建 `core/task/TaskRecord.kt`：

```kotlin
package com.ai.assistance.operit.core.task

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class TaskRecord(
    @Id var id: Long = 0,
    val taskId: String,
    val chatId: String,
    val status: String = TaskStatus.PENDING.name,
    val checkpoint: String = "",
    val retryCount: Int = 0,
    val lastError: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
```

- [ ] **Step 2: 创建TaskRepository任务仓库**

新建 `core/task/TaskRepository.kt`：

```kotlin
package com.ai.assistance.operit.core.task

import android.content.Context
import com.ai.assistance.operit.data.OperitObjectBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(private val context: Context) {

    private val box by lazy {
        OperitObjectBox.boxStore.boxFor(TaskRecord::class.java)
    }

    suspend fun insert(record: TaskRecord): Long = withContext(Dispatchers.IO) {
        box.put(record)
    }

    suspend fun update(record: TaskRecord) = withContext(Dispatchers.IO) {
        val updated = record.copy(updatedAt = System.currentTimeMillis())
        box.put(updated)
    }

    suspend fun getByTaskId(taskId: String): TaskRecord? = withContext(Dispatchers.IO) {
        box.query().equal(TaskRecord_.taskId, taskId).build().findFirst()
    }

    suspend fun getByChatId(chatId: String): List<TaskRecord> = withContext(Dispatchers.IO) {
        box.query().equal(TaskRecord_.chatId, chatId).build().find()
    }

    suspend fun getRunningByChatId(chatId: String): List<TaskRecord> = withContext(Dispatchers.IO) {
        box.query()
            .equal(TaskRecord_.chatId, chatId)
            .equal(TaskRecord_.status, TaskStatus.RUNNING.name)
            .build()
            .find()
    }

    suspend fun updateStatus(taskId: String, status: TaskStatus, lastError: String = "") = withContext(Dispatchers.IO) {
        val record = getByTaskId(taskId) ?: return@withContext
        val updated = record.copy(
            status = status.name,
            lastError = lastError,
            updatedAt = System.currentTimeMillis()
        )
        box.put(updated)
    }

    suspend fun updateCheckpoint(taskId: String, checkpoint: String) = withContext(Dispatchers.IO) {
        val record = getByTaskId(taskId) ?: return@withContext
        val updated = record.copy(
            checkpoint = checkpoint,
            updatedAt = System.currentTimeMillis()
        )
        box.put(updated)
    }

    suspend fun incrementRetryCount(taskId: String) = withContext(Dispatchers.IO) {
        val record = getByTaskId(taskId) ?: return@withContext
        val updated = record.copy(
            retryCount = record.retryCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        box.put(updated)
    }

    suspend fun deleteByTaskId(taskId: String) = withContext(Dispatchers.IO) {
        val record = getByTaskId(taskId) ?: return@withContext
        box.remove(record)
    }
}
```

- [ ] **Step 3: 创建TaskCheckpointManager检查点管理器**

新建 `core/task/TaskCheckpointManager.kt`：

```kotlin
package com.ai.assistance.operit.core.task

import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TaskCheckpoint(
    val taskId: String,
    val chatId: String,
    val stepIndex: Int,
    val toolName: String,
    val toolParameters: Map<String, String> = emptyMap(),
    val partialResult: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class TaskCheckpointManager(private val context: Context) {

    private val repository = TaskRepository(context)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "TaskCheckpoint"
    }

    suspend fun saveCheckpoint(checkpoint: TaskCheckpoint) {
        val checkpointJson = json.encodeToString(checkpoint)
        repository.updateCheckpoint(checkpoint.taskId, checkpointJson)
        AppLogger.d(TAG, "Checkpoint saved for task=${checkpoint.taskId}, step=${checkpoint.stepIndex}")
    }

    suspend fun loadCheckpoint(taskId: String): TaskCheckpoint? {
        val record = repository.getByTaskId(taskId) ?: return null
        if (record.checkpoint.isBlank()) return null
        return try {
            json.decodeFromString<TaskCheckpoint>(record.checkpoint)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to parse checkpoint for task=$taskId", e)
            null
        }
    }

    suspend fun hasCheckpoint(taskId: String): Boolean {
        val record = repository.getByTaskId(taskId) ?: return false
        return record.checkpoint.isNotBlank()
    }

    suspend fun clearCheckpoint(taskId: String) {
        repository.updateCheckpoint(taskId, "")
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat(task): add TaskRecord, TaskRepository, and TaskCheckpointManager for long-running task support"
```

---

### Task 8: 长程任务执行能力增强 - 修改ChatServiceCore增加断点恢复

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/services/ChatServiceCore.kt`

- [ ] **Step 1: 在ChatServiceCore中集成TaskRepository**

在 `ChatServiceCore` 中添加 `TaskRepository` 和 `TaskCheckpointManager` 实例，并在消息处理流程中增加断点恢复逻辑：

```kotlin
// 在 ChatServiceCore 类中添加字段:
private val taskRepository by lazy { TaskRepository(context) }
private val taskCheckpointManager by lazy { TaskCheckpointManager(context) }

// 添加断点恢复方法:
suspend fun resumeTaskFromCheckpoint(chatId: String): Boolean {
    val runningTasks = taskRepository.getRunningByChatId(chatId)
    if (runningTasks.isEmpty()) return false

    val task = runningTasks.first()
    val checkpoint = taskCheckpointManager.loadCheckpoint(task.taskId) ?: return false

    AppLogger.d(TAG, "Resuming task=${task.taskId} from checkpoint at step=${checkpoint.stepIndex}")
    return true
}

// 添加任务创建方法:
suspend fun createTaskRecord(chatId: String, taskId: String): TaskRecord {
    val record = TaskRecord(
        taskId = taskId,
        chatId = chatId,
        status = TaskStatus.PENDING.name
    )
    taskRepository.insert(record)
    return record
}

// 添加任务状态更新方法:
suspend fun updateTaskStatus(taskId: String, status: TaskStatus, lastError: String = "") {
    taskRepository.updateStatus(taskId, status, lastError)
}
```

注意：需要在文件顶部添加 `import com.ai.assistance.operit.core.task.*`。

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(task): integrate TaskRepository into ChatServiceCore for checkpoint recovery"
```

---

### Task 9: 长程任务执行能力增强 - 修改AIToolHandler增加超时重试

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/AIToolHandler.kt`

- [ ] **Step 1: 在AIToolHandler中增加超时重试机制**

在 `AIToolHandler` 中新增带超时重试的工具执行方法。超时120秒，最多3次重试，指数退避（1秒、2秒、4秒）：

```kotlin
// 在 AIToolHandler 类中添加:

companion object {
    private const val TAG = "AIToolHandler"
    private const val TOOL_EXECUTION_TIMEOUT_MS = 120_000L
    private const val MAX_RETRY_COUNT = 3
    private const val INITIAL_RETRY_DELAY_MS = 1_000L
    // ... 保留现有常量
}

suspend fun executeToolWithRetry(
    tool: AITool,
    timeoutMs: Long = TOOL_EXECUTION_TIMEOUT_MS,
    maxRetries: Int = MAX_RETRY_COUNT,
    initialDelayMs: Long = INITIAL_RETRY_DELAY_MS
): ToolResult {
    var lastResult: ToolResult? = null
    var retryCount = 0

    while (retryCount <= maxRetries) {
        try {
            val result = kotlinx.coroutines.withTimeout(timeoutMs) {
                executeTool(tool)
            }
            if (result.success) {
                return result
            }
            lastResult = result
            val isRetryable = isRetryableError(result.error)
            if (!isRetryable || retryCount >= maxRetries) {
                return result
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            AppLogger.w(TAG, "Tool ${tool.name} timed out after ${timeoutMs}ms (attempt ${retryCount + 1}/${maxRetries + 1})")
            lastResult = ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Timeout after ${timeoutMs}ms"
            )
        }

        retryCount++
        if (retryCount <= maxRetries) {
            val delayMs = initialDelayMs * (1L shl (retryCount - 1))
            AppLogger.d(TAG, "Retrying tool ${tool.name} in ${delayMs}ms (attempt $retryCount/$maxRetries)")
            kotlinx.coroutines.delay(delayMs)
        }
    }

    return lastResult ?: ToolResult(
        toolName = tool.name,
        success = false,
        result = StringResultData(""),
        error = "All retries exhausted"
    )
}

private fun isRetryableError(error: String?): Boolean {
    if (error.isNullOrBlank()) return false
    val retryablePatterns = listOf(
        "Timeout",
        "timeout",
        "Connection",
        "connection",
        "Socket",
        "socket",
        "Network",
        "network",
        "503",
        "502",
        "429",
        "rate limit",
        "Rate limit",
        "overloaded",
        "Overloaded"
    )
    return retryablePatterns.any { error.contains(it, ignoreCase = true) }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(tools): add executeToolWithRetry with 120s timeout, 3 retries, exponential backoff"
```

---

### Task 10: 长程任务执行能力增强 - 修改AIForegroundService增强进度通知

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/api/chat/AIForegroundService.kt`

- [ ] **Step 1: 在AIForegroundService中增强进度通知**

在 `AIForegroundService` 中增加任务级别的进度通知更新。当工具执行时，通过 `ToolProgressBus` 监听进度事件并更新前台服务通知：

```kotlin
// 在 AIForegroundService 类中添加字段:
private var progressJob: Job? = null

// 添加进度监听启动方法:
private fun startProgressMonitoring() {
    if (progressJob?.isActive == true) return
    progressJob = serviceScope.launch {
        ToolProgressBus.progress.collect { event ->
            if (event != null) {
                updateProgressNotification(event)
            }
        }
    }
}

private fun stopProgressMonitoring() {
    progressJob?.cancel()
    progressJob = null
}

private fun updateProgressNotification(event: ToolProgressEvent) {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val progressPercent = (event.progress * 100).toInt().coerceIn(0, 100)
    val title = when {
        event.toolName == ToolProgressBus.SUMMARY_PROGRESS_TOOL_NAME -> "Processing..."
        else -> "Running: ${event.toolName}"
    }
    val text = if (event.message.isNotBlank()) event.message else "$progressPercent%"

    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(text)
        .setProgress(100, progressPercent, progressPercent == 0)
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .build()

    notificationManager.notify(NOTIFICATION_ID, notification)
}
```

注意：需要在文件顶部添加 `import com.ai.assistance.operit.core.tools.ToolProgressBus` 和 `import com.ai.assistance.operit.core.tools.ToolProgressEvent`。

- [ ] **Step 2: 在onCreate/onDestroy中启动/停止进度监听**

在 `AIForegroundService.onCreate()` 中调用 `startProgressMonitoring()`，在 `onDestroy()` 中调用 `stopProgressMonitoring()`。

- [ ] **Step 3: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat(service): enhance AIForegroundService with task-level progress notifications"
```

---

### Task 11: 长程任务执行能力增强 - 修改ToolProgressBus增加任务级别进度

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/core/tools/ToolProgressBus.kt`

- [ ] **Step 1: 在ToolProgressBus中增加任务级别进度支持**

扩展 `ToolProgressBus` 以支持任务级别的进度追踪，新增 `taskId` 字段和任务进度查询方法：

```kotlin
// 替换整个 ToolProgressBus.kt:

package com.ai.assistance.operit.core.tools

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ToolProgressEvent(
    val toolName: String,
    val progress: Float,
    val message: String = "",
    val priority: Int = 0,
    val level: Int = 0,
    val taskId: String? = null
)

object ToolProgressBus {
    const val SUMMARY_PROGRESS_TOOL_NAME: String = "__SUMMARY__"

    private val _progress = MutableStateFlow<ToolProgressEvent?>(null)
    val progress: StateFlow<ToolProgressEvent?> = _progress.asStateFlow()

    private val _taskProgress = MutableStateFlow<Map<String, ToolProgressEvent>>(emptyMap())
    val taskProgress: StateFlow<Map<String, ToolProgressEvent>> = _taskProgress.asStateFlow()

    private fun priorityForTool(toolName: String): Int {
        return when (toolName) {
            SUMMARY_PROGRESS_TOOL_NAME -> 1000
            "grep_context" -> 100
            "grep_code" -> 10
            "find_files" -> 5
            else -> 0
        }
    }

    fun update(toolName: String, progress: Float, message: String = "") {
        update(toolName = toolName, progress = progress, message = message, priority = priorityForTool(toolName))
    }

    fun update(toolName: String, progress: Float, message: String = "", priority: Int, level: Int = 0, taskId: String? = null) {
        val next = ToolProgressEvent(
            toolName = toolName,
            progress = progress,
            message = message,
            priority = priority,
            level = level,
            taskId = taskId
        )

        val current = _progress.value
        val shouldReplace =
            current == null ||
                current.toolName == next.toolName ||
                current.progress >= 1f ||
                next.priority > current.priority ||
                (next.priority == current.priority && next.level >= current.level)

        if (shouldReplace) {
            _progress.value = next
        }

        if (taskId != null) {
            _taskProgress.value = _taskProgress.value + (taskId to next)
        }
    }

    fun getTaskProgress(taskId: String): ToolProgressEvent? {
        return _taskProgress.value[taskId]
    }

    fun clearTaskProgress(taskId: String) {
        _taskProgress.value = _taskProgress.value - taskId
    }

    fun clear() {
        _progress.value = null
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(progress): add task-level progress tracking to ToolProgressBus"
```

---

### Task 12: 模型配置简化 - 修改ModelConfigManager

**Files:**
- Modify: `app/src/main/java/com/ai/assistance/operit/data/preferences/ModelConfigManager.kt`

- [ ] **Step 1: 在ModelConfigManager中增加简化模式方法**

在 `ModelConfigManager` 中新增方法，用于获取UI层仅暴露的4项核心参数（apiUrl, apiKey, modelName, temperature），以及根据模型名自动计算其他参数的默认值：

```kotlin
// 在 ModelConfigManager 类中添加:

data class SimplifiedModelConfig(
    val configId: String,
    val configName: String,
    val apiUrl: String,
    val apiKey: String,
    val modelName: String,
    val temperature: Float
)

fun toSimplifiedConfig(config: ModelConfigData): SimplifiedModelConfig {
    return SimplifiedModelConfig(
        configId = config.id,
        configName = config.name,
        apiUrl = config.apiEndpoint,
        apiKey = config.apiKey,
        modelName = config.modelName,
        temperature = config.temperature
    )
}

suspend fun updateSimplifiedConfig(
    configId: String,
    apiUrl: String? = null,
    apiKey: String? = null,
    modelName: String? = null,
    temperature: Float? = null
): ModelConfigData {
    return updateConfigInternal(configId) { current ->
        val updatedModelName = modelName ?: current.modelName
        val autoParams = computeAutoParameters(updatedModelName)

        current.copy(
            apiEndpoint = apiUrl ?: current.apiEndpoint,
            apiKey = apiKey ?: current.apiKey,
            modelName = updatedModelName,
            temperature = temperature ?: current.temperature,
            maxTokens = autoParams.maxTokens,
            topP = autoParams.topP,
            topK = autoParams.topK,
            presencePenalty = autoParams.presencePenalty,
            frequencyPenalty = autoParams.frequencyPenalty,
            repetitionPenalty = autoParams.repetitionPenalty,
            maxTokensEnabled = autoParams.maxTokensEnabled,
            temperatureEnabled = temperature != null,
            topPEnabled = false,
            topKEnabled = false,
            presencePenaltyEnabled = false,
            frequencyPenaltyEnabled = false,
            repetitionPenaltyEnabled = false
        )
    }
}

private data class AutoParameters(
    val maxTokens: Int,
    val topP: Float,
    val topK: Int,
    val presencePenalty: Float,
    val frequencyPenalty: Float,
    val repetitionPenalty: Float,
    val maxTokensEnabled: Boolean
)

private fun computeAutoParameters(modelName: String): AutoParameters {
    val lowered = modelName.lowercase()
    return when {
        lowered.contains("gpt-4") || lowered.contains("gpt4") -> AutoParameters(
            maxTokens = 4096, topP = 1.0f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        lowered.contains("claude-3") || lowered.contains("claude3") -> AutoParameters(
            maxTokens = 4096, topP = 1.0f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        lowered.contains("deepseek") -> AutoParameters(
            maxTokens = 4096, topP = 0.95f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        lowered.contains("qwen") || lowered.contains("qwq") -> AutoParameters(
            maxTokens = 4096, topP = 0.8f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        lowered.contains("llama") -> AutoParameters(
            maxTokens = 4096, topP = 0.9f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        lowered.contains("gemini") -> AutoParameters(
            maxTokens = 8192, topP = 0.95f, topK = 40,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
        else -> AutoParameters(
            maxTokens = 4096, topP = 0.95f, topK = -1,
            presencePenalty = 0f, frequencyPenalty = 0f, repetitionPenalty = 1.0f,
            maxTokensEnabled = true
        )
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | grep -c "error:" || echo "0 errors"
```

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(config): add simplified model config with auto-computed parameters in ModelConfigManager"
```

---

### Task 13: 全量编译验证和最终清理

**Files:**
- Modify: 所有残留编译错误的文件

- [ ] **Step 1: 运行全量编译**

```bash
./gradlew :app:compileDebugKotlin 2>&1 | tail -50
```

- [ ] **Step 2: 修复所有编译错误**

根据编译输出逐一修复残留引用：
- 删除未使用的import语句
- 补充缺失的import语句
- 确保ObjectBox实体正确注册（如果项目使用ObjectBox注解处理器，需要确认 `TaskRecord_` 类能被自动生成）
- 确认 `OperitObjectBox.boxStore` 的初始化方式与项目现有ObjectBox使用模式一致

- [ ] **Step 3: 运行lint检查**

```bash
./gradlew :app:lintDebug 2>&1 | tail -30
```

- [ ] **Step 4: 确认APK可以正常构建**

```bash
./gradlew :app:assembleDebug 2>&1 | tail -20
```

- [ ] **Step 5: 验证JS包文件保留**

确认以下JS包文件仍然存在（供第三方沙箱包通过JS接口调用）：

```bash
test -f app/src/main/assets/packages/automatic_ui_subagent.js && echo "PASS: automatic_ui_subagent.js preserved" || echo "FAIL"
test -f app/src/main/assets/packages/browser.js && echo "PASS: browser.js preserved" || echo "FAIL"
test ! -f app/src/main/assets/packages/automatic_ui_base.js && echo "PASS: automatic_ui_base.js removed" || echo "FAIL"
```

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "refactor: final cleanup and full compilation verification for P1 architecture adjustment"
```
