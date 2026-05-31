package com.ai.assistance.operit.core.tools.autoglm

import android.content.Context
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.core.tools.ToolExecutor
import com.ai.assistance.operit.core.tools.defaultTool.standard.StandardUITools
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolResult
import com.ai.assistance.operit.ui.common.displays.VirtualDisplayOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AutoGLMSubAgentExecutor(private val context: Context) : ToolExecutor {

    override fun invoke(tool: AITool): ToolResult {
        return when (tool.name) {
            "run_subagent_main" -> runSubAgentMain(tool)
            "run_subagent_virtual" -> runSubAgentVirtual(tool)
            "run_subagent_parallel_virtual" -> runSubAgentParallelVirtual(tool)
            "close_all_virtual_displays" -> closeAllVirtualDisplays(tool)
            else -> ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Unsupported AutoGLM tool: ${tool.name}"
            )
        }
    }

    private fun runSubAgentMain(tool: AITool): ToolResult {
        val intent = tool.parameters.find { it.name == "intent" }?.value
        val maxSteps = tool.parameters.find { it.name == "max_steps" }?.value
        val targetApp = tool.parameters.find { it.name == "target_app" }?.value

        if (intent.isNullOrBlank()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Missing required parameter: intent"
            )
        }

        val subAgentTool = AITool(
            name = "run_ui_subagent",
            parameters = buildList {
                add(com.ai.assistance.operit.data.model.ToolParameter(name = "intent", value = intent))
                add(com.ai.assistance.operit.data.model.ToolParameter(name = "agent_id", value = "default"))
                if (!maxSteps.isNullOrBlank()) {
                    add(com.ai.assistance.operit.data.model.ToolParameter(name = "max_steps", value = maxSteps))
                }
                if (!targetApp.isNullOrBlank()) {
                    add(com.ai.assistance.operit.data.model.ToolParameter(name = "target_app", value = targetApp))
                }
            }
        )

        val uiTools = StandardUITools(context)
        return runBlocking(Dispatchers.IO) { uiTools.runUiSubAgent(subAgentTool) }
    }

    private fun runSubAgentVirtual(tool: AITool): ToolResult {
        val intent = tool.parameters.find { it.name == "intent" }?.value
        val maxSteps = tool.parameters.find { it.name == "max_steps" }?.value
        val agentId = tool.parameters.find { it.name == "agent_id" }?.value?.trim()
        val targetApp = tool.parameters.find { it.name == "target_app" }?.value

        if (intent.isNullOrBlank()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Missing required parameter: intent"
            )
        }

        if (agentId.isNullOrBlank() || agentId.equals("default", ignoreCase = true)) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Virtual screen mode requires a non-'default' agent_id. Please provide an explicit agent_id to use a virtual display session."
            )
        }

        val subAgentTool = AITool(
            name = "run_ui_subagent",
            parameters = buildList {
                add(com.ai.assistance.operit.data.model.ToolParameter(name = "intent", value = intent))
                add(com.ai.assistance.operit.data.model.ToolParameter(name = "agent_id", value = agentId))
                if (!maxSteps.isNullOrBlank()) {
                    add(com.ai.assistance.operit.data.model.ToolParameter(name = "max_steps", value = maxSteps))
                }
                if (!targetApp.isNullOrBlank()) {
                    add(com.ai.assistance.operit.data.model.ToolParameter(name = "target_app", value = targetApp))
                }
            }
        )

        val uiTools = StandardUITools(context)
        return runBlocking(Dispatchers.IO) { uiTools.runUiSubAgent(subAgentTool) }
    }

    private fun runSubAgentParallelVirtual(tool: AITool): ToolResult {
        val slots = listOf(1, 2, 3, 4)
        val activeSlots = slots.mapNotNull { i ->
            val intentVal = tool.parameters.find { it.name == "intent_$i" }?.value?.trim()
            if (intentVal.isNullOrBlank()) return@mapNotNull null
            Triple(i, intentVal, tool.parameters.find { it.name == "target_app_$i" }?.value?.trim() ?: "")
        }

        if (activeSlots.isEmpty()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "At least one intent (intent_1) is required."
            )
        }

        val missingTargets = activeSlots.filter { it.second.isNotBlank() && it.third.isBlank() }
        if (missingTargets.isNotEmpty()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Parallel parameter error: intent_${missingTargets.joinToString(", intent_") { it.first.toString() }} missing target_app_${missingTargets.joinToString(", target_app_") { it.first.toString() }}."
            )
        }

        val missingAgentIds = activeSlots.mapNotNull { (i, _, _) ->
            val aid = tool.parameters.find { it.name == "agent_id_$i" }?.value?.trim()
            if (aid.isNullOrBlank() || aid.equals("default", ignoreCase = true)) i else null
        }
        if (missingAgentIds.isNotEmpty()) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Parallel parameter error: virtual screen parallel mode requires non-'default' agent_id for each branch. Missing/invalid: ${missingAgentIds.joinToString(", ") { "#$it" }}."
            )
        }

        val targetApps = mutableMapOf<String, Int>()
        for ((i, _, targetApp) in activeSlots) {
            if (targetApp.isNotBlank()) {
                val key = targetApp.lowercase()
                val prev = targetApps[key]
                if (prev != null) {
                    return ToolResult(
                        toolName = tool.name,
                        success = false,
                        result = StringResultData(""),
                        error = "Parallel parameter error: target_app_$prev and target_app_$i conflict (same target app \"$targetApp\"). The same app cannot be operated in parallel across two virtual displays / agent_ids."
                    )
                }
                targetApps[key] = i
            }
        }

        val uiTools = StandardUITools(context)
        val results = runBlocking(Dispatchers.IO) {
            activeSlots.map { (i, intentVal, _) ->
                try {
                    val maxSteps = tool.parameters.find { it.name == "max_steps_$i" }?.value
                    val agentId = tool.parameters.find { it.name == "agent_id_$i" }?.value?.trim() ?: ""
                    val targetApp = tool.parameters.find { it.name == "target_app_$i" }?.value?.trim()

                    val subAgentTool = AITool(
                        name = "run_ui_subagent",
                        parameters = buildList {
                            add(com.ai.assistance.operit.data.model.ToolParameter(name = "intent", value = intentVal))
                            add(com.ai.assistance.operit.data.model.ToolParameter(name = "agent_id", value = agentId))
                            if (!maxSteps.isNullOrBlank()) {
                                add(com.ai.assistance.operit.data.model.ToolParameter(name = "max_steps", value = maxSteps))
                            }
                            if (!targetApp.isNullOrBlank()) {
                                add(com.ai.assistance.operit.data.model.ToolParameter(name = "target_app", value = targetApp))
                            }
                        }
                    )
                    val result = uiTools.runUiSubAgent(subAgentTool)
                    mapOf("index" to i, "success" to result.success, "result" to (result.result?.toString() ?: ""), "error" to (result.error ?: ""))
                } catch (e: Exception) {
                    mapOf("index" to i, "success" to false, "error" to (e.message ?: "Unknown error"))
                }
            }
        }

        val okCount = results.count { it["success"] == true }
        return ToolResult(
            toolName = tool.name,
            success = true,
            result = StringResultData("Parallel UI subagent execution completed: success $okCount / total ${results.size}")
        )
    }

    private fun closeAllVirtualDisplays(tool: AITool): ToolResult {
        return try {
            VirtualDisplayOverlay.hideAll()
            ToolResult(
                toolName = tool.name,
                success = true,
                result = StringResultData("All virtual displays closed.")
            )
        } catch (e: Exception) {
            ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = e.message
            )
        }
    }
}
