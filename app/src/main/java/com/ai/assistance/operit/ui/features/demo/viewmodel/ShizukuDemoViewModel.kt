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

class ShizukuDemoViewModel(application: Application) : AndroidViewModel(application) {
    private val stateManager: DemoStateManager = DemoStateManager(application, viewModelScope)

    private val toolHandler: AIToolHandler = AIToolHandler.getInstance(application)

    val uiState: StateFlow<com.ai.assistance.operit.ui.features.demo.state.DemoScreenState> =
            stateManager.uiState

    val isPnpmInstalled
        get() = stateManager.isPnpmInstalled
    val isPythonInstalled
        get() = stateManager.isPythonInstalled
    val isNodejsPythonEnvironmentReady
        get() = stateManager.isNodejsPythonEnvironmentReady

    fun initialize(context: Context) {
        stateManager.initialize()
    }

    fun setLoading(isLoading: Boolean) {
        stateManager.setLoading(isLoading)
    }

    fun initializeAsync(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stateManager.initializeAsync()
            } catch (e: Exception) {
                AppLogger.e("ShizukuDemoViewModel", "初始化时出错: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) { setLoading(false) }
            }
        }
    }

    fun refreshStatus(context: Context) {
        stateManager.refreshStatus()
    }

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

    fun updateCommandText(text: String) {
        stateManager.updateCommandText(text)
    }

    fun refreshTools(context: Context) {
        AppLogger.d("ShizukuDemoViewModel", "Refreshing all registered tools")
        toolHandler.reset()
        toolHandler.registerDefaultTools()

        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.all_tools_reregistered), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stateManager.cleanup()
    }

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
