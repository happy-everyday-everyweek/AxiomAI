package com.ai.assistance.operit.core.tools.browser

import android.content.Context
import com.ai.assistance.operit.core.tools.StringResultData
import com.ai.assistance.operit.core.tools.ToolExecutor
import com.ai.assistance.operit.core.tools.defaultTool.standard.StandardBrowserSessionTools
import com.ai.assistance.operit.data.model.AITool
import com.ai.assistance.operit.data.model.ToolResult

class BrowserToolExecutor(private val context: Context) : ToolExecutor {

    private val browserSessionTools = StandardBrowserSessionTools(context)

    private val supportedTools = setOf(
        "browser_click", "browser_close", "browser_close_all",
        "browser_console_messages", "browser_drag", "browser_evaluate",
        "browser_file_upload", "browser_fill_form", "browser_handle_dialog",
        "browser_hover", "browser_navigate", "browser_navigate_back",
        "browser_network_requests", "browser_press_key", "browser_resize",
        "browser_run_code", "browser_select_option", "browser_snapshot",
        "browser_take_screenshot", "browser_tabs", "browser_type", "browser_wait_for"
    )

    override fun invoke(tool: AITool): ToolResult {
        if (tool.name !in supportedTools) {
            return ToolResult(
                toolName = tool.name,
                success = false,
                result = StringResultData(""),
                error = "Unsupported browser tool: ${tool.name}"
            )
        }

        return browserSessionTools.invoke(tool)
    }
}
