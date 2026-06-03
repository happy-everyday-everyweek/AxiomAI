package com.ai.assistance.operit.ui.features.assistant.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ai.assistance.operit.ui.components.CustomScaffold
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.operit.R
import com.ai.assistance.operit.ui.features.assistant.components.AvatarConfigSection
import com.ai.assistance.operit.ui.features.assistant.components.AvatarPreviewSection
import com.ai.assistance.operit.core.avatar.impl.factory.AvatarControllerFactoryImpl
import com.ai.assistance.operit.ui.features.assistant.viewmodel.AssistantConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantConfigScreen() {
    val context = LocalContext.current
    val viewModel: AssistantConfigViewModel =
        viewModel(factory = AssistantConfigViewModel.Factory(context))
    val uiState by viewModel.uiState.collectAsState()
    val avatarControllerFactory = remember { AvatarControllerFactoryImpl() }
    val sharedAvatarController =
        uiState.currentAvatarModel?.let { model ->
            avatarControllerFactory.createController(model)
        }

    var isAvatarPreviewCollapsed by rememberSaveable { mutableStateOf(false) }

    val zipFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.importAvatarFromZip(uri)
                }
            }
        }

    val openZipFilePicker = {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "application/zip",
                        "application/x-zip-compressed",
                        "model/vnd.autodesk.fbx",
                        "model/fbx",
                        "application/fbx",
                        "model/gltf-binary",
                        "model/gltf+json",
                        "video/mp4",
                        "application/octet-stream"
                    )
                )
            }
        zipFileLauncher.launch(intent)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState(initial = uiState.scrollPosition)

    val operationSuccessString = context.getString(R.string.operation_success)
    val errorOccurredString = context.getString(R.string.error_occurred_simple)

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }.collect { position ->
            viewModel.updateScrollPosition(position)
        }
    }

    LaunchedEffect(uiState.operationSuccess, uiState.errorMessage) {
        if (uiState.operationSuccess) {
            snackbarHostState.showSnackbar(operationSuccessString)
            viewModel.clearOperationSuccess()
        } else if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage ?: errorOccurredString)
            viewModel.clearErrorMessage()
        }
    }

    CustomScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp)
            ) {
                if (!isAvatarPreviewCollapsed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        AvatarPreviewSection(
                            modifier = Modifier.fillMaxSize(),
                            uiState = uiState,
                            avatarController = sharedAvatarController
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { isAvatarPreviewCollapsed = !isAvatarPreviewCollapsed }) {
                        Icon(
                            imageVector =
                                if (isAvatarPreviewCollapsed) Icons.Default.ExpandMore
                                else Icons.Default.ExpandLess,
                            contentDescription = stringResource(
                                if (isAvatarPreviewCollapsed) R.string.model_config_expand
                                else R.string.model_config_collapse
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AvatarConfigSection(
                            viewModel = viewModel,
                            uiState = uiState,
                            avatarController = sharedAvatarController,
                            onImportClick = { openZipFilePicker() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (uiState.isLoading || uiState.isImporting) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface
                                    .copy(alpha = 0.7f)
                            ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text =
                                if (uiState.isImporting) stringResource(R.string.importing_model)
                                else stringResource(R.string.processing),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
