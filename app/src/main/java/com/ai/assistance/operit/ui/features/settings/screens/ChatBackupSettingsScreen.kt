package com.ai.assistance.operit.ui.features.settings.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.backup.RawSnapshotBackupManager
import com.ai.assistance.operit.ui.main.MainActivity
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

enum class SimpleBackupState {
    IDLE,
    IN_PROGRESS,
    SUCCESS,
    FAILED
}

@Composable
fun ChatBackupSettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var backupState by remember { mutableStateOf(SimpleBackupState.IDLE) }
    var restoreState by remember { mutableStateOf(SimpleBackupState.IDLE) }
    var statusMessage by remember { mutableStateOf("") }
    var showRestartDialog by remember { mutableStateOf(false) }

    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    restoreState = SimpleBackupState.IN_PROGRESS
                    statusMessage = context.getString(R.string.backup_raw_snapshot_progress_preparing)
                    try {
                        try {
                            context.contentResolver.takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {
                        }
                        RawSnapshotBackupManager.restoreFromBackupUri(
                            context = context,
                            uri = uri,
                            onProgress = { progress ->
                                statusMessage = when (progress) {
                                    RawSnapshotBackupManager.RestoreProgress.PREPARING ->
                                        context.getString(R.string.backup_raw_snapshot_progress_preparing)
                                    RawSnapshotBackupManager.RestoreProgress.READING_ZIP ->
                                        context.getString(R.string.backup_raw_snapshot_progress_reading_zip)
                                    RawSnapshotBackupManager.RestoreProgress.EXTRACTING ->
                                        context.getString(R.string.backup_raw_snapshot_progress_extracting)
                                    RawSnapshotBackupManager.RestoreProgress.REPLACING_FILES ->
                                        context.getString(R.string.backup_raw_snapshot_progress_replacing_files)
                                    RawSnapshotBackupManager.RestoreProgress.REPLACING_EXTERNAL_FILES ->
                                        context.getString(R.string.backup_raw_snapshot_progress_replacing_external_files)
                                    RawSnapshotBackupManager.RestoreProgress.REPLACING_SHARED_PREFS ->
                                        context.getString(R.string.backup_raw_snapshot_progress_replacing_shared_prefs)
                                    RawSnapshotBackupManager.RestoreProgress.REPLACING_DATASTORE ->
                                        context.getString(R.string.backup_raw_snapshot_progress_replacing_datastore)
                                    RawSnapshotBackupManager.RestoreProgress.REPLACING_DATABASES ->
                                        context.getString(R.string.backup_raw_snapshot_progress_replacing_databases)
                                    RawSnapshotBackupManager.RestoreProgress.FINALIZING ->
                                        context.getString(R.string.backup_raw_snapshot_progress_finalizing)
                                }
                            }
                        )
                        restoreState = SimpleBackupState.SUCCESS
                        statusMessage = context.getString(R.string.backup_import_success)
                        showRestartDialog = true
                    } catch (e: Exception) {
                        restoreState = SimpleBackupState.FAILED
                        statusMessage = context.getString(
                            R.string.backup_import_failed_with_reason,
                            e.localizedMessage ?: e.toString()
                        )
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_data_backup),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.backup_raw_snapshot_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    backupState = SimpleBackupState.IN_PROGRESS
                                    statusMessage = context.getString(R.string.backup_raw_snapshot_progress_preparing)
                                    try {
                                        val outFile = RawSnapshotBackupManager.exportToBackupDir(
                                            context = context,
                                            onProgress = { progress ->
                                                statusMessage = when (progress.stage) {
                                                    RawSnapshotBackupManager.ExportProgress.PREPARING ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_preparing)
                                                    RawSnapshotBackupManager.ExportProgress.SCANNING_FILES ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_scanning_files)
                                                    RawSnapshotBackupManager.ExportProgress.ZIPPING_FILES ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_zipping_files)
                                                    RawSnapshotBackupManager.ExportProgress.ZIPPING_EXTERNAL_FILES ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_zipping_external_files)
                                                    RawSnapshotBackupManager.ExportProgress.ZIPPING_SHARED_PREFS ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_zipping_shared_prefs)
                                                    RawSnapshotBackupManager.ExportProgress.ZIPPING_DATASTORE ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_zipping_datastore)
                                                    RawSnapshotBackupManager.ExportProgress.ZIPPING_DATABASES ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_zipping_databases)
                                                    RawSnapshotBackupManager.ExportProgress.FINALIZING ->
                                                        context.getString(R.string.backup_raw_snapshot_progress_finalizing)
                                                }
                                            }
                                        )
                                        backupState = SimpleBackupState.SUCCESS
                                        statusMessage = context.getString(
                                            R.string.backup_export_result_success,
                                            outFile.absolutePath
                                        )
                                    } catch (e: Exception) {
                                        backupState = SimpleBackupState.FAILED
                                        statusMessage = context.getString(
                                            R.string.backup_export_failed_with_reason,
                                            e.localizedMessage ?: e.toString()
                                        )
                                    }
                                }
                            },
                            enabled = backupState != SimpleBackupState.IN_PROGRESS,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.backup_raw_snapshot_backup_now))
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip"))
                                }
                                restoreFilePickerLauncher.launch(intent)
                            },
                            enabled = restoreState != SimpleBackupState.IN_PROGRESS,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.backup_raw_snapshot_restore_from_file))
                        }
                    }

                    if (backupState == SimpleBackupState.IN_PROGRESS || restoreState == SimpleBackupState.IN_PROGRESS) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (backupState == SimpleBackupState.SUCCESS || backupState == SimpleBackupState.FAILED ||
                        restoreState == SimpleBackupState.SUCCESS || restoreState == SimpleBackupState.FAILED
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    backupState == SimpleBackupState.FAILED || restoreState == SimpleBackupState.FAILED ->
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (backupState == SimpleBackupState.FAILED || restoreState == SimpleBackupState.FAILED) {
                                        Icons.Default.Info
                                    } else {
                                        Icons.Default.CloudUpload
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (backupState == SimpleBackupState.FAILED || restoreState == SimpleBackupState.FAILED) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = statusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (backupState == SimpleBackupState.FAILED || restoreState == SimpleBackupState.FAILED) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.backup_raw_snapshot_restart_title)) },
            text = { Text(stringResource(R.string.backup_raw_snapshot_restart_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        val intent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        context.startActivity(intent)
                        exitProcess(0)
                    }
                ) {
                    Text(stringResource(R.string.backup_raw_snapshot_restart_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.backup_raw_snapshot_restart_later))
                }
            }
        )
    }
}
