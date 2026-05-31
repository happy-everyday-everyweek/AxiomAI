package com.ai.assistance.operit.ui.features.demo.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.ai.assistance.operit.util.AppLogger
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.operit.R
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.operit.ui.main.screens.Screen
import com.ai.assistance.operit.ui.main.screens.ScreenNavigationHandler
import com.ai.assistance.operit.ui.features.demo.components.*
import com.ai.assistance.operit.ui.features.demo.viewmodel.ShizukuDemoViewModel
import com.ai.assistance.operit.ui.features.demo.wizards.AccessibilityWizardCard
import com.ai.assistance.operit.ui.features.demo.wizards.OperitTerminalWizardCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuDemoScreen(
        viewModel: ShizukuDemoViewModel =
                viewModel(
                        factory =
                                ShizukuDemoViewModel.Factory(
                                        LocalContext.current.applicationContext as
                                                android.app.Application
                                )
                ),
        navigateTo: ScreenNavigationHandler? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 跟踪当前显示的权限级别
    var currentDisplayedPermissionLevel by remember {
        mutableStateOf(AndroidPermissionLevel.STANDARD)
    }

    // Location permission launcher
    val locationPermissionLauncher =
            rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val fineLocationGranted =
                        permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted =
                        permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                if (fineLocationGranted || coarseLocationGranted) {
                    scope.launch(Dispatchers.IO) { viewModel.refreshStatus(context) }
                }
            }

    // Register state change listeners
    DisposableEffect(Unit) {
        onDispose { }
    }

    // 预先加载一个空的UI状态，避免初始化时的卡顿
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize ViewModel
    LaunchedEffect(Unit) {
        // 显示加载指示器
        viewModel.setLoading(true)

        // 在IO线程上执行所有初始化
        withContext(Dispatchers.IO) {
            // 将初始化任务拆分成多个小任务，避免长时间阻塞
            viewModel.initializeAsync(context)
        }

        // 标记初始化完成
        isInitialized = true
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 加载指示器
        if (uiState.isLoading.value) {
            Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(context.getString(R.string.loading_app_state), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 检查无障碍服务版本状态
        val (accessibilityInstalledVersion, accessibilityBundledVersion, isAccessibilityUpdateNeeded) =
                remember(uiState.isRefreshing.value) {
                    Triple(0, 0, false)
                }

        // 权限管理卡片
        PermissionLevelCard(
                hasStoragePermission = uiState.hasStoragePermission.value,
                hasOverlayPermission = uiState.hasOverlayPermission.value,
                hasBatteryOptimizationExemption = uiState.hasBatteryOptimizationExemption.value,
                hasAccessibilityServiceEnabled = uiState.hasAccessibilityServiceEnabled.value,
                hasLocationPermission = uiState.hasLocationPermission.value,
                isShizukuInstalled = uiState.isShizukuInstalled.value,
                isShizukuRunning = uiState.isShizukuRunning.value,
                hasShizukuPermission = uiState.hasShizukuPermission.value,
                isOperitTerminalInstalled = uiState.isOperitTerminalInstalled.value,
                isDeviceRooted = uiState.isDeviceRooted.value,
                hasRootAccess = uiState.hasRootAccess.value,
                isAccessibilityProviderInstalled = uiState.isAccessibilityProviderInstalled.value,
                isAccessibilityUpdateNeeded = isAccessibilityUpdateNeeded,
                isRefreshing = uiState.isRefreshing.value,
                onRefresh = {
                    scope.launch(Dispatchers.IO) {
                        viewModel.refreshStatus(context)
                    }
                },
                onStoragePermissionClick = {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            // Android 11+: Go to manage all files permission page
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            context.startActivity(intent)
                        } else {
                            // Android 10-: Go to app settings page
                            val intent =
                                    Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:" + context.packageName)
                                    )
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        // Fall back to app settings
                        try {
                            val intent =
                                    Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.parse("package:" + context.packageName)
                                    )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_permission_settings), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onOverlayPermissionClick = {
                    try {
                        val intent =
                                Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.cannot_open_overlay_settings), Toast.LENGTH_SHORT).show()
                    }
                },
                onBatteryOptimizationClick = {
                    try {
                        val intent =
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:" + context.packageName)
                                }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.cannot_open_battery_settings), Toast.LENGTH_SHORT).show()
                    }
                },
                onAccessibilityClick = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.cannot_open_accessibility_settings), Toast.LENGTH_SHORT).show()
                    }
                },
                onInstallAccessibilityProviderClick = {
                    scope.launch(Dispatchers.IO) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.accessibility_provider_installed), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onLocationPermissionClick = {
                    // 请求位置权限
                    locationPermissionLauncher.launch(
                            arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                    )
                },
                onShizukuClick = {
                    // 如果Shizuku未完全设置，则显示向导
                    if (!uiState.isShizukuInstalled.value ||
                                    !uiState.isShizukuRunning.value ||
                                    !uiState.hasShizukuPermission.value
                    ) {
                        viewModel.toggleShizukuWizard()
                    }
                },
                onOperitTerminalClick = {
                    // 点击时总是打开向导
                    viewModel.toggleOperitTerminalWizard()
                },
                onRootClick = {
                    // 处理Root权限
                    if (currentDisplayedPermissionLevel == AndroidPermissionLevel.ROOT) {
                        // 如果当前正在浏览ROOT权限级别，则显示或隐藏Root向导
                        viewModel.toggleRootWizard()
                    }
                },
                onPermissionLevelChange = { level -> currentDisplayedPermissionLevel = level },
                onPermissionLevelSet = { _ ->
                    // 当设置了新的权限级别时，刷新工具
                    scope.launch { viewModel.refreshTools(context) }
                }
        )

        // 组合向导卡片到一个专门的设置区域 - 现在检查NodeJS和Python环境
        val needOperitTerminalSetupGuide = !viewModel.isNodejsPythonEnvironmentReady.value

        // 检查Shizuku版本状态 - 使用remember缓存结果，避免每次重组时重复调用
        val (installedVersion, bundledVersion, isUpdateNeeded) =
                remember(uiState.isRefreshing.value) {
                    Triple(0, 0, false)
                }

        val needShizukuSetupGuide = false

        val needRootSetupGuide = false
    
        val needAccessibilitySetupGuide =
            currentDisplayedPermissionLevel == AndroidPermissionLevel.ACCESSIBILITY &&
                    (!uiState.isAccessibilityProviderInstalled.value ||
                            !uiState.hasAccessibilityServiceEnabled.value ||
                            isAccessibilityUpdateNeeded)


        val needSetupGuide = needOperitTerminalSetupGuide || needShizukuSetupGuide || needRootSetupGuide || needAccessibilitySetupGuide

        if (needSetupGuide) {
            Spacer(modifier = Modifier.height(16.dp))

            // 修改为左对齐带图标的标题样式
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = context.getString(R.string.setup_wizard_icon_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                        text = context.getString(R.string.setup_wizard),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                )
            }

            // 添加分割线
            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // Accessibility向导卡片
            if (needAccessibilitySetupGuide) {
                AccessibilityWizardCard(
                    isProviderInstalled = uiState.isAccessibilityProviderInstalled.value,
                    isServiceEnabled = uiState.hasAccessibilityServiceEnabled.value,
                    showWizard = uiState.showAccessibilityWizard.value,
                    onToggleWizard = { viewModel.toggleAccessibilityWizard() },
                    onInstallProvider = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_accessibility_settings), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenAccessibilitySettings = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_accessibility_settings), Toast.LENGTH_SHORT).show()
                        }
                    },
                    updateNeeded = isAccessibilityUpdateNeeded,
                    installedVersion = accessibilityInstalledVersion,
                    bundledVersion = accessibilityBundledVersion,
                    onUpdateProvider = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_accessibility_settings), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }




            // NodeJS和Python环境配置向导卡片
            if (needOperitTerminalSetupGuide) {
                OperitTerminalWizardCard(
                    isPnpmInstalled = viewModel.isPnpmInstalled.value,
                    isPipInstalled = viewModel.isPythonInstalled.value,
                    isEnvironmentReady = viewModel.isNodejsPythonEnvironmentReady.value,
                    showWizard = uiState.showOperitTerminalWizard.value,
                    onToggleWizard = { viewModel.toggleOperitTerminalWizard() },
                    onOpenTerminalScreen = { 
                        // 跳转到TerminalSetup，直接显示配置界面
                        navigateTo?.invoke(Screen.TerminalSetup)
                    }
                )
            }
        }
    }
}
