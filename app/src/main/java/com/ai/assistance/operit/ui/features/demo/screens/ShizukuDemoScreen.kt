package com.ai.assistance.operit.ui.features.demo.screens

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    val uiState by viewModel.uiState.collectAsState()

    var currentDisplayedPermissionLevel by remember {
        mutableStateOf(AndroidPermissionLevel.STANDARD)
    }

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

    DisposableEffect(Unit) {
        onDispose { }
    }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setLoading(true)
        withContext(Dispatchers.IO) {
            viewModel.initializeAsync(context)
        }
        isInitialized = true
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isInitialized || uiState.isLoading.value) {
            Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(context.getString(R.string.loading_app_state), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        val isAccessibilityUpdateNeeded = false

        PermissionLevelCard(
                currentLevel = currentDisplayedPermissionLevel,
                onLevelSelected = { level -> currentDisplayedPermissionLevel = level }
        )

        val needOperitTerminalSetupGuide = !viewModel.isNodejsPythonEnvironmentReady.value

        val needAccessibilitySetupGuide =
            currentDisplayedPermissionLevel == AndroidPermissionLevel.ACCESSIBILITY &&
                    !uiState.hasAccessibilityServiceEnabled.value

        val needSetupGuide = needOperitTerminalSetupGuide || needAccessibilitySetupGuide

        if (needSetupGuide) {
            Spacer(modifier = Modifier.height(16.dp))

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

            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            if (needAccessibilitySetupGuide) {
                AccessibilityWizardCard(
                    isProviderInstalled = true,
                    isServiceEnabled = uiState.hasAccessibilityServiceEnabled.value,
                    showWizard = uiState.showAccessibilityWizard.value,
                    onToggleWizard = { viewModel.toggleAccessibilityWizard() },
                    onInstallProvider = { },
                    onOpenAccessibilitySettings = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_accessibility_settings), Toast.LENGTH_SHORT).show()
                        }
                    },
                    updateNeeded = false,
                    installedVersion = null,
                    bundledVersion = "",
                    onUpdateProvider = { }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (needOperitTerminalSetupGuide) {
                OperitTerminalWizardCard(
                    isPnpmInstalled = viewModel.isPnpmInstalled.value,
                    isPipInstalled = viewModel.isPythonInstalled.value,
                    isEnvironmentReady = viewModel.isNodejsPythonEnvironmentReady.value,
                    showWizard = uiState.showOperitTerminalWizard.value,
                    onToggleWizard = { viewModel.toggleOperitTerminalWizard() },
                    onOpenTerminalScreen = {
                        navigateTo?.invoke(Screen.TerminalSetup)
                    }
                )
            }
        }
    }
}
