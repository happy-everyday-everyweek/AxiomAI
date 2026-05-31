package com.ai.assistance.operit.ui.features.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.GitHubAuthPreferences
import com.ai.assistance.operit.data.preferences.UserPreferencesManager
import com.ai.assistance.operit.ui.features.github.GitHubLoginWebViewDialog
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color

private val SettingsScreenScrollPosition = mutableStateOf(0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        navigateToModelConfig: () -> Unit,
        navigateToGlobalDisplaySettings: () -> Unit,
        navigateToToolbox: () -> Unit,
        navigateToChatBackupSettings: () -> Unit,
        navigateToAbout: () -> Unit
) {
        val context = LocalContext.current
        val userPreferences = remember { UserPreferencesManager.getInstance(context) }
        val githubAuth = remember { GitHubAuthPreferences.getInstance(context) }
        val scope = rememberCoroutineScope()
        var showGitHubLogin by remember { mutableStateOf(false) }

        val isGitHubLoggedIn = githubAuth.isLoggedInFlow.collectAsState(initial = false).value
        val gitHubUser = githubAuth.userInfoFlow.collectAsState(initial = null).value

        val scrollState = rememberScrollState(SettingsScreenScrollPosition.value)

        LaunchedEffect(scrollState) {
                snapshotFlow { scrollState.value }.collect { position ->
                        SettingsScreenScrollPosition.value = position
                }
        }

        val hasBackgroundImage = userPreferences.useBackgroundImage.collectAsState(initial = false).value

        val cardContainerColor = if (hasBackgroundImage) {
                MaterialTheme.colorScheme.surface
        } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }

        Column(
                modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(scrollState)
        ) {
                // ======= Model + API Config =======
                SettingsSection(
                        title = stringResource(id = R.string.settings_section_ai_model),
                        icon = Icons.Default.Api,
                        containerColor = cardContainerColor
                ) {
                        CompactSettingsItem(
                                title = stringResource(id = R.string.settings_model_parameters),
                                subtitle = stringResource(id = R.string.settings_model_params_subtitle),
                                icon = Icons.Default.Api,
                                onClick = navigateToModelConfig
                        )
                }

                // ======= Display Settings =======
                SettingsSection(
                        title = stringResource(id = R.string.settings_section_personalization),
                        icon = Icons.Default.Palette,
                        containerColor = cardContainerColor
                ) {
                        CompactSettingsItem(
                                title = stringResource(R.string.settings_global_display),
                                subtitle = stringResource(R.string.settings_global_display_subtitle),
                                icon = Icons.Default.Visibility,
                                onClick = navigateToGlobalDisplaySettings
                        )
                }

                // ======= Toolbox =======
                SettingsSection(
                        title = stringResource(id = R.string.settings_data_permissions),
                        icon = Icons.Default.Extension,
                        containerColor = cardContainerColor
                ) {
                        CompactSettingsItem(
                                title = stringResource(id = R.string.settings_tool_permissions),
                                subtitle = stringResource(id = R.string.settings_tool_permissions_subtitle),
                                icon = Icons.Default.Extension,
                                onClick = navigateToToolbox
                        )
                }

                // ======= Backup + About =======
                SettingsSection(
                        title = stringResource(id = R.string.settings_data_backup),
                        icon = Icons.Default.Info,
                        containerColor = cardContainerColor
                ) {
                        CompactSettingsItem(
                                title = stringResource(id = R.string.settings_data_backup),
                                subtitle = stringResource(id = R.string.settings_data_backup_desc),
                                icon = Icons.Default.CloudUpload,
                                onClick = navigateToChatBackupSettings
                        )

                        CompactSettingsItem(
                                title = stringResource(id = R.string.about),
                                subtitle = stringResource(id = R.string.settings_section_about_subtitle),
                                icon = Icons.Default.Info,
                                onClick = navigateToAbout
                        )

                        if (isGitHubLoggedIn) {
                                CompactSettingsItem(
                                        title = stringResource(R.string.logout),
                                        subtitle = "@${gitHubUser?.login ?: ""}",
                                        icon = Icons.Default.Logout,
                                        onClick = {
                                                scope.launch { githubAuth.logout() }
                                        }
                                )
                        } else {
                                CompactSettingsItem(
                                        title = stringResource(R.string.login_github),
                                        subtitle = stringResource(R.string.github_account_login_desc),
                                        icon = Icons.Default.Login,
                                        onClick = { showGitHubLogin = true }
                                )
                        }
                }

                if (showGitHubLogin) {
                        GitHubLoginWebViewDialog(
                                onDismissRequest = { showGitHubLogin = false }
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))
        }
}

@Composable
private fun SettingsSection(
        title: String,
        icon: ImageVector,
        containerColor: Color,
        content: @Composable ColumnScope.() -> Unit
) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                // 分组标题
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                        )
                }
                
                // 内容区域
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                                containerColor = containerColor
                        )
                ) {
                        Column(
                                modifier = Modifier.padding(12.dp),
                                content = content
                        )
                }
        }
}

@Composable
private fun CompactSettingsItem(
        title: String,
        subtitle: String,
        icon: ImageVector,
        onClick: () -> Unit
) {
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onClick() }
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                        Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                }
                
                Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                )
        }
}


