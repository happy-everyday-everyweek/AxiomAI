package com.ai.assistance.operit.ui.features.demo.state

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.ai.assistance.operit.util.AppLogger
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.ai.assistance.operit.data.repository.UIHierarchyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.ai.assistance.operit.core.tools.system.Terminal
import com.ai.assistance.operit.data.mcp.plugins.MCPSharedSession
import com.ai.assistance.operit.R

private const val TAG = "DemoStateManager"

class DemoStateManager(private val context: Context, private val coroutineScope: CoroutineScope) : ViewModel() {
    private val _uiState = MutableStateFlow(DemoScreenState())
    val uiState: StateFlow<DemoScreenState> = _uiState.asStateFlow()

    val isPnpmInstalled = mutableStateOf(false)
    val isPythonInstalled = mutableStateOf(false)
    val isNodejsPythonEnvironmentReady = mutableStateOf(false)

    init {
        coroutineScope.launch {
            refreshAllStates()
        }
    }

    fun initialize() {
        coroutineScope.launch {
            AppLogger.d(TAG, "初始化状态...")
            refreshStatusAsync()
        }
    }

    fun refreshStatus() {
       coroutineScope.launch {
           refreshStatusAsync()
       }
    }

    fun updateRootStatus(isDeviceRooted: Boolean, hasRootAccess: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                    isDeviceRooted = mutableStateOf(isDeviceRooted),
                    hasRootAccess = mutableStateOf(hasRootAccess)
            )
        }
    }

    fun updateOutputText(text: String) {
    }

    fun showResultDialog(title: String, content: String) {
        _uiState.update { currentState ->
            currentState.copy(
                    resultDialogTitle = mutableStateOf(title),
                    resultDialogContent = mutableStateOf(content),
                    showResultDialogState = mutableStateOf(true)
            )
        }
    }

    fun hideResultDialog() {
        _uiState.update { currentState ->
            currentState.copy(showResultDialogState = mutableStateOf(false))
        }
    }

    fun toggleShizukuWizard() {
        _uiState.update { currentState ->
            currentState.copy(
                    showShizukuWizard = mutableStateOf(!currentState.showShizukuWizard.value)
            )
        }
    }

    fun toggleOperitTerminalWizard() {
        _uiState.update { currentState ->
            currentState.copy(
                showOperitTerminalWizard = mutableStateOf(!currentState.showOperitTerminalWizard.value)
            )
        }
    }

    fun toggleRootWizard() {
        _uiState.update { currentState ->
            currentState.copy(showRootWizard = mutableStateOf(!currentState.showRootWizard.value))
        }
    }

    fun toggleAccessibilityWizard() {
        _uiState.update { currentState ->
            currentState.copy(
                showAccessibilityWizard = mutableStateOf(!currentState.showAccessibilityWizard.value)
            )
        }
    }

    fun toggleAdbCommandExecutor() {
        _uiState.update { currentState ->
            currentState.copy(
                    showAdbCommandExecutor =
                            mutableStateOf(!currentState.showAdbCommandExecutor.value)
            )
        }
    }

    fun toggleSampleCommands() {
        _uiState.update { currentState ->
            currentState.copy(
                    showSampleCommands = mutableStateOf(!currentState.showSampleCommands.value)
            )
        }
    }

    fun updateCommandText(text: String) {
        _uiState.update { currentState -> currentState.copy(commandText = mutableStateOf(text)) }
    }

    fun updateResultText(text: String) {
        _uiState.update { currentState -> currentState.copy(resultText = mutableStateOf(text)) }
    }

    fun cleanup() {
    }

    suspend fun refreshAllStates() {
        refreshNodejsPythonEnvironment()
    }

    fun refreshAllStatesPublic() {
        coroutineScope.launch {
            refreshAllStates()
        }
    }

    private fun registerStateChangeListeners() {
    }

    fun setLoading(isLoading: Boolean) {
        _uiState.update { currentState -> currentState.copy(isLoading = mutableStateOf(isLoading)) }
    }

    suspend fun initializeAsync() {
        AppLogger.d(TAG, "异步初始化状态...")
        refreshStatusAsync()
    }

    private suspend fun refreshStatusAsync() {
        _uiState.update { currentState -> currentState.copy(isRefreshing = mutableStateOf(true)) }

        try {
            refreshPermissionsAndStatus(
                    context = context,
                    updateShizukuInstalled = { _uiState.value.isShizukuInstalled.value = it },
                    updateShizukuRunning = { _uiState.value.isShizukuRunning.value = it },
                    updateShizukuPermission = { _uiState.value.hasShizukuPermission.value = it },
                    updateOperitTerminalInstalled = { _uiState.value.isOperitTerminalInstalled.value = it },
                    updateOperitTerminalRunning = { isOperitTerminalRunning -> },
                    updateStoragePermission = { _uiState.value.hasStoragePermission.value = it },
                    updateLocationPermission = { _uiState.value.hasLocationPermission.value = it },
                    updateOverlayPermission = { _uiState.value.hasOverlayPermission.value = it },
                    updateBatteryOptimizationExemption = {
                        _uiState.value.hasBatteryOptimizationExemption.value = it
                    },
                    updateAccessibilityProviderInstalled = {
                        _uiState.value.isAccessibilityProviderInstalled.value = it
                    },
                    updateAccessibilityServiceEnabled = {
                        _uiState.value.hasAccessibilityServiceEnabled.value = it
                    }
            )

            refreshNodejsPythonEnvironment()

            delay(300)
        } catch (e: Exception) {
            AppLogger.e(TAG, "刷新权限状态时出错: ${e.message}", e)
        } finally {
            _uiState.update { currentState ->
                currentState.copy(isRefreshing = mutableStateOf(false))
            }
        }
    }

    suspend fun refreshNodejsPythonEnvironment() {
        try {
            val sessionId = MCPSharedSession.getOrCreateSharedSession(context)
            if (sessionId == null) {
                isPnpmInstalled.value = false
                isPythonInstalled.value = false
                isNodejsPythonEnvironmentReady.value = false
                return
            }

            val terminal = Terminal.getInstance(context)
            
            val pnpmResult = terminal.executeCommand(sessionId, "command -v pnpm")
            isPnpmInstalled.value = pnpmResult != null && pnpmResult.contains("pnpm")
            
            val pythonResult = terminal.executeCommand(sessionId, "command -v python")
            var hasPython = pythonResult != null && (pythonResult.contains("python") || pythonResult.contains("/python"))
            
            if (!hasPython) {
                val python3Result = terminal.executeCommand(sessionId, "command -v python3")
                hasPython = python3Result != null && (python3Result.contains("python3") || python3Result.contains("/python3"))
            }

            var hasPip = false
            if (hasPython) {
                val pipResult = terminal.executeCommand(sessionId, "command -v pip")
                hasPip = pipResult != null && pipResult.contains("pip")
                
                if (!hasPip) {
                    val pip3Result = terminal.executeCommand(sessionId, "command -v pip3")
                    hasPip = pip3Result != null && pip3Result.contains("pip3")
                }
            }

            isPythonInstalled.value = hasPython && hasPip

            isNodejsPythonEnvironmentReady.value = isPnpmInstalled.value && isPythonInstalled.value
            
            AppLogger.d(TAG, "NodeJS环境检查 - pnpm: ${isPnpmInstalled.value}, python: $hasPython, pip: $hasPip, python环境: ${isPythonInstalled.value}, 整体ready: ${isNodejsPythonEnvironmentReady.value}")
            
        } catch (e: Exception) {
            AppLogger.e(TAG, "检查NodeJS和Python环境时出错", e)
            isPnpmInstalled.value = false
            isPythonInstalled.value = false
            isNodejsPythonEnvironmentReady.value = false
        }
    }
}

suspend fun refreshPermissionsAndStatus(
    context: Context,
    updateShizukuInstalled: (Boolean) -> Unit,
    updateShizukuRunning: (Boolean) -> Unit,
    updateShizukuPermission: (Boolean) -> Unit,
    updateOperitTerminalInstalled: (Boolean) -> Unit,
    updateOperitTerminalRunning: (Boolean) -> Unit,
    updateStoragePermission: (Boolean) -> Unit,
    updateLocationPermission: (Boolean) -> Unit,
    updateOverlayPermission: (Boolean) -> Unit,
    updateBatteryOptimizationExemption: (Boolean) -> Unit,
    updateAccessibilityProviderInstalled: (Boolean) -> Unit,
    updateAccessibilityServiceEnabled: (Boolean) -> Unit
) {
    AppLogger.d(TAG, "刷新应用权限状态...")

    updateShizukuInstalled(false)
    updateShizukuRunning(false)
    updateShizukuPermission(false)

    val isNodejsPythonEnvironmentReady = try {
        val sessionId = MCPSharedSession.getOrCreateSharedSession(context)
        if (sessionId != null) {
            val terminal = Terminal.getInstance(context)
            val pnpmResult = terminal.executeCommand(sessionId, "command -v pnpm")
            val isPnpmInstalled = pnpmResult != null && pnpmResult.contains("pnpm")
            
            val pythonResult = terminal.executeCommand(sessionId, "command -v python")
            var hasPython = pythonResult != null && (pythonResult.contains("python") || pythonResult.contains("/python"))
            
            if (!hasPython) {
                val python3Result = terminal.executeCommand(sessionId, "command -v python3")
                hasPython = python3Result != null && (python3Result.contains("python3") || python3Result.contains("/python3"))
            }
            
            var hasPip = false
            if (hasPython) {
                val pipResult = terminal.executeCommand(sessionId, "command -v pip")
                hasPip = pipResult != null && pipResult.contains("pip")
                
                if (!hasPip) {
                    val pip3Result = terminal.executeCommand(sessionId, "command -v pip3")
                    hasPip = pip3Result != null && pip3Result.contains("pip3")
                }
            }
            
            isPnpmInstalled && hasPython && hasPip
        } else {
            false
        }
    } catch (e: Exception) {
        AppLogger.e(TAG, "检查NodeJS和Python环境时出错", e)
        false
    }
    updateOperitTerminalInstalled(isNodejsPythonEnvironmentReady)

    val hasStoragePermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            context.checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    updateStoragePermission(hasStoragePermission)

    val hasLocationPermission =
        context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    updateLocationPermission(hasLocationPermission)

    val hasOverlayPermission = Settings.canDrawOverlays(context)
    updateOverlayPermission(hasOverlayPermission)

    val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    val hasBatteryOptimizationExemption =
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    updateBatteryOptimizationExemption(hasBatteryOptimizationExemption)

    val isAccessibilityServiceEnabled =
        UIHierarchyManager.isAccessibilityServiceEnabled(context)
    updateAccessibilityProviderInstalled(true)
    updateAccessibilityServiceEnabled(isAccessibilityServiceEnabled)
}

data class DemoScreenState(
        val isShizukuInstalled: MutableState<Boolean> = mutableStateOf(false),
        val isShizukuRunning: MutableState<Boolean> = mutableStateOf(false),
        val hasShizukuPermission: MutableState<Boolean> = mutableStateOf(false),
        val isOperitTerminalInstalled: MutableState<Boolean> = mutableStateOf(false),
        val hasStoragePermission: MutableState<Boolean> = mutableStateOf(false),
        val hasOverlayPermission: MutableState<Boolean> = mutableStateOf(false),
        val hasBatteryOptimizationExemption: MutableState<Boolean> = mutableStateOf(false),
        val hasAccessibilityServiceEnabled: MutableState<Boolean> = mutableStateOf(false),
        val isAccessibilityProviderInstalled: MutableState<Boolean> = mutableStateOf(false),
        val hasLocationPermission: MutableState<Boolean> = mutableStateOf(false),
        val isDeviceRooted: MutableState<Boolean> = mutableStateOf(false),
        val hasRootAccess: MutableState<Boolean> = mutableStateOf(false),

        val isRefreshing: MutableState<Boolean> = mutableStateOf(false),
        val showHelp: MutableState<Boolean> = mutableStateOf(false),
        val permissionErrorMessage: MutableState<String?> = mutableStateOf(null),
        val showSampleCommands: MutableState<Boolean> = mutableStateOf(false),
        val showAdbCommandExecutor: MutableState<Boolean> = mutableStateOf(false),
        val showShizukuWizard: MutableState<Boolean> = mutableStateOf(false),
        val showOperitTerminalWizard: MutableState<Boolean> = mutableStateOf(false),
        val showRootWizard: MutableState<Boolean> = mutableStateOf(false),
        val showAccessibilityWizard: MutableState<Boolean> = mutableStateOf(false),
        val showResultDialogState: MutableState<Boolean> = mutableStateOf(false),

        val commandText: MutableState<String> = mutableStateOf(""),
        val resultText: MutableState<String> = mutableStateOf(""),
        val resultDialogTitle: MutableState<String> = mutableStateOf(""),
        val resultDialogContent: MutableState<String> = mutableStateOf(""),
        val isLoading: MutableState<Boolean> = mutableStateOf(false)
)

fun getSampleAdbCommands(context: Context) =
        listOf(
                "getprop ro.build.version.release" to context.getString(R.string.demo_cmd_get_android_version),
                "pm list packages" to context.getString(R.string.demo_cmd_list_packages),
                "dumpsys battery" to context.getString(R.string.demo_cmd_check_battery),
                "settings list system" to context.getString(R.string.demo_cmd_list_settings),
                "am start -a android.intent.action.VIEW -d https://www.example.com" to context.getString(R.string.demo_cmd_open_webpage),
                "dumpsys activity activities" to context.getString(R.string.demo_cmd_list_activities),
                "service list" to context.getString(R.string.demo_cmd_list_services),
                "wm size" to context.getString(R.string.demo_cmd_check_resolution)
        )

fun getOperitTerminalSampleCommands(context: Context) =
        listOf(
                "echo 'Hello OperitTerminal'" to context.getString(R.string.demo_cmd_echo_hello),
                "ls -la" to context.getString(R.string.demo_cmd_list_files),
                "whoami" to context.getString(R.string.demo_cmd_show_user),
                "apt update" to context.getString(R.string.demo_cmd_update_package_manager),
                "apt install python3" to context.getString(R.string.demo_cmd_install_python),
                "ip addr" to context.getString(R.string.demo_cmd_show_network)
        )

fun getRootSampleCommands(context: Context) =
        listOf(
                "mount -o rw,remount /system" to context.getString(R.string.demo_cmd_remount_system),
                "cat /proc/version" to context.getString(R.string.demo_cmd_check_kernel),
                "ls -la /data" to context.getString(R.string.demo_cmd_list_data_dir),
                "getenforce" to context.getString(R.string.demo_cmd_check_selinux),
                "ps -A" to context.getString(R.string.demo_cmd_list_processes),
                "cat /proc/meminfo" to context.getString(R.string.demo_cmd_check_memory),
                "pm list features" to context.getString(R.string.demo_cmd_list_features),
                "dumpsys power" to context.getString(R.string.demo_cmd_check_power)
        )
