package com.ai.assistance.operit.ui.features.demo.viewmodel

import android.app.Application
import android.content.Context
import com.ai.assistance.operit.util.AppLogger
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ai.assistance.operit.core.tools.AIToolHandler

import com.ai.assistance.operit.R
import com.ai.assistance.operit.ui.features.demo.state.DemoStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** ViewModel for the ShizukuDemoScreen Delegates most state management to DemoStateManager */
class ShizukuDemoViewModel(application: Application) : AndroidViewModel(application) {
    // 初始化时直接创建stateManager
    private val stateManager: DemoStateManager = DemoStateManager(application, viewModelScope)

    // AIToolHandler instance
    private val toolHandler: AIToolHandler = AIToolHandler.getInstance(application)

    // Expose state from the manager
    val uiState: StateFlow<com.ai.assistance.operit.ui.features.demo.state.DemoScreenState> =
            stateManager.uiState

    // Expose NodeJS and Python environment properties
    val isPnpmInstalled
        get() = stateManager.isPnpmInstalled
    val isPythonInstalled
        get() = stateManager.isPythonInstalled
    val isNodejsPythonEnvironmentReady
        get() = stateManager.isNodejsPythonEnvironmentReady

    /** Initialize the ViewModel with context data */
    fun initialize(context: Context) {
        // 只需要调用stateManager的initialize方法
        stateManager.initialize()
    }

    /** Set loading state */
    fun setLoading(isLoading: Boolean) {
        stateManager.setLoading(isLoading)
    }

    /** Initialize the ViewModel with context data (Async version) */
    fun initializeAsync(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stateManager.updateRootStatus(false, false)

                stateManager.initializeAsync()
            } catch (e: Exception) {
                AppLogger.e("ShizukuDemoViewModel", "初始化时出错: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) { setLoading(false) }
            }
        }
    }

    /** Refresh app status */
    fun refreshStatus(context: Context) {
        // 检查Root状态
        checkRootStatus(context)
        stateManager.refreshStatus()
    }

    /** Check root status */
    fun checkRootStatus(context: Context) {
        viewModelScope.launch {
            val isDeviceRooted = false
            val hasRootAccess = false
            stateManager.updateRootStatus(isDeviceRooted, hasRootAccess)
        }
    }

    /** Request root permission */
    fun requestRootPermission(context: Context) {
        viewModelScope.launch {
            Toast.makeText(context, context.getString(R.string.root_permission_denied), Toast.LENGTH_SHORT).show()
            checkRootStatus(context)
        }
    }

    /** Execute root command */
    fun executeRootCommand(command: String, context: Context) {
        viewModelScope.launch {
            Toast.makeText(context, context.getString(R.string.command_execution_failed), Toast.LENGTH_SHORT).show()
            stateManager.updateResultText("${context.getString(R.string.command_execution_failed)}: Root access not available")
        }
    }

    /** Dialog management */
    fun showResultDialog(title: String, content: String) {
        stateManager.showResultDialog(title, content)
    }

    fun hideResultDialog() {
        stateManager.hideResultDialog()
    }

    /** UI visibility toggles */
    fun toggleShizukuWizard() {
        stateManager.toggleShizukuWizard()
    }

    fun toggleOperitTerminalWizard() {
        stateManager.toggleOperitTerminalWizard()
    }

    fun toggleRootWizard() {
        stateManager.toggleRootWizard()
    }

    fun toggleAccessibilityWizard() {
        stateManager.toggleAccessibilityWizard()
    }

    fun toggleAdbCommandExecutor() {
        stateManager.toggleAdbCommandExecutor()
    }

    fun toggleSampleCommands() {
        stateManager.toggleSampleCommands()
    }

    /** Command handling */
    fun updateCommandText(text: String) {
        stateManager.updateCommandText(text)
    }

    /** Refresh all registered tools */
    fun refreshTools(context: Context) {
        AppLogger.d("ShizukuDemoViewModel", "Refreshing all registered tools")
        // First clear the current tool execution state
        toolHandler.reset()

        // Re-register all default tools
        toolHandler.registerDefaultTools()

        // Show a toast notification for feedback
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.all_tools_reregistered), Toast.LENGTH_SHORT).show()
        }
    }

    /** Cleanup when ViewModel is cleared */
    override fun onCleared() {
        super.onCleared()
        stateManager.cleanup()
    }

    /** ViewModelFactory for creating ShizukuDemoViewModel with dependencies */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShizukuDemoViewModel::class.java)) {
                return ShizukuDemoViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
