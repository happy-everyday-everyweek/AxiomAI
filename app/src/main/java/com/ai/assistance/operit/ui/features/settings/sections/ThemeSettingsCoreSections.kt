package com.ai.assistance.operit.ui.features.settings.sections

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ai.assistance.operit.R
import com.ai.assistance.operit.data.preferences.UserPreferencesManager
import com.ai.assistance.operit.ui.features.settings.components.ColorSelectionItem
import com.ai.assistance.operit.ui.features.settings.components.ThemeModeOption

internal typealias SaveThemeSettingsAction = (suspend () -> Unit) -> Unit

@Composable
internal fun ThemeSettingsCharacterBindingInfoCard(
    aiAvatarUri: String?,
    activeCharacterName: String?,
    isGroupTarget: Boolean,
    cardColors: CardColors,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = cardColors,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (aiAvatarUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(aiAvatarUri)),
                        contentDescription = stringResource(R.string.character_avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription =
                            stringResource(R.string.character_card_default_avatar),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text =
                        if (isGroupTarget) {
                            stringResource(R.string.current_character_group, activeCharacterName ?: "")
                        } else {
                            stringResource(R.string.current_character, activeCharacterName ?: "")
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text =
                        if (isGroupTarget) {
                            stringResource(R.string.theme_auto_bind_character_group)
                        } else {
                            stringResource(R.string.theme_auto_bind_character_card)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                Icons.Default.Link,
                contentDescription = stringResource(R.string.bind),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun ThemeSettingsThemeModeSection(
    cardColors: CardColors,
    useSystemThemeInput: Boolean,
    onUseSystemThemeInputChange: (Boolean) -> Unit,
    themeModeInput: String,
    onThemeModeInputChange: (String) -> Unit,
    saveThemeSettingsWithCharacterCard: SaveThemeSettingsAction,
    preferencesManager: UserPreferencesManager,
) {
    ThemeSettingsSectionTitle(
        title = stringResource(id = R.string.theme_title_mode),
        icon = Icons.Default.Brightness4,
    )

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = cardColors) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.theme_system_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.theme_follow_system),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.theme_follow_system_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Switch(
                    checked = useSystemThemeInput,
                    onCheckedChange = {
                        onUseSystemThemeInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(useSystemTheme = it)
                        }
                    },
                )
            }

            if (!useSystemThemeInput) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(id = R.string.theme_select),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeModeOption(
                        title = stringResource(id = R.string.theme_light),
                        selected = themeModeInput == UserPreferencesManager.THEME_MODE_LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onThemeModeInputChange(UserPreferencesManager.THEME_MODE_LIGHT)
                            saveThemeSettingsWithCharacterCard {
                                preferencesManager.saveThemeSettings(
                                    themeMode = UserPreferencesManager.THEME_MODE_LIGHT,
                                )
                            }
                        },
                    )

                    ThemeModeOption(
                        title = stringResource(id = R.string.theme_dark),
                        selected = themeModeInput == UserPreferencesManager.THEME_MODE_DARK,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onThemeModeInputChange(UserPreferencesManager.THEME_MODE_DARK)
                            saveThemeSettingsWithCharacterCard {
                                preferencesManager.saveThemeSettings(
                                    themeMode = UserPreferencesManager.THEME_MODE_DARK,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ThemeSettingsChatStyleSection(
    cardColors: CardColors,
    chatStyleInput: String,
    onChatStyleInputChange: (String) -> Unit,
    inputStyleInput: String,
    onInputStyleInputChange: (String) -> Unit,
    bubbleShowAvatarInput: Boolean,
    onBubbleShowAvatarInputChange: (Boolean) -> Unit,
    bubbleWideLayoutEnabledInput: Boolean,
    onBubbleWideLayoutEnabledInputChange: (Boolean) -> Unit,
    cursorUserBubbleFollowThemeInput: Boolean,
    onCursorUserBubbleFollowThemeInputChange: (Boolean) -> Unit,
    cursorUserBubbleLiquidGlassInput: Boolean,
    onCursorUserBubbleLiquidGlassInputChange: (Boolean) -> Unit,
    cursorUserBubbleWaterGlassInput: Boolean,
    onCursorUserBubbleWaterGlassInputChange: (Boolean) -> Unit,
    bubbleUserBubbleLiquidGlassInput: Boolean,
    onBubbleUserBubbleLiquidGlassInputChange: (Boolean) -> Unit,
    bubbleUserBubbleWaterGlassInput: Boolean,
    onBubbleUserBubbleWaterGlassInputChange: (Boolean) -> Unit,
    bubbleAiBubbleLiquidGlassInput: Boolean,
    onBubbleAiBubbleLiquidGlassInputChange: (Boolean) -> Unit,
    bubbleAiBubbleWaterGlassInput: Boolean,
    onBubbleAiBubbleWaterGlassInputChange: (Boolean) -> Unit,
    cursorUserBubbleColorInput: Int,
    bubbleUserBubbleColorInput: Int,
    bubbleAiBubbleColorInput: Int,
    bubbleUserTextColorInput: Int,
    bubbleAiTextColorInput: Int,
    bubbleUserUseCustomFontInput: Boolean,
    onBubbleUserUseCustomFontInputChange: (Boolean) -> Unit,
    bubbleUserFontTypeInput: String,
    onBubbleUserFontTypeInputChange: (String) -> Unit,
    bubbleUserSystemFontNameInput: String,
    onBubbleUserSystemFontNameInputChange: (String) -> Unit,
    bubbleUserCustomFontPathInput: String?,
    onBubbleUserCustomFontPathInputChange: (String?) -> Unit,
    onPickBubbleUserFont: () -> Unit,
    bubbleAiUseCustomFontInput: Boolean,
    onBubbleAiUseCustomFontInputChange: (Boolean) -> Unit,
    bubbleAiFontTypeInput: String,
    onBubbleAiFontTypeInputChange: (String) -> Unit,
    bubbleAiSystemFontNameInput: String,
    onBubbleAiSystemFontNameInputChange: (String) -> Unit,
    bubbleAiCustomFontPathInput: String?,
    onBubbleAiCustomFontPathInputChange: (String?) -> Unit,
    onPickBubbleAiFont: () -> Unit,
    previewUserAvatarUri: String?,
    previewAiAvatarUri: String?,
    onShowColorPicker: (String) -> Unit,
    bubbleUserUseImageInput: Boolean,
    onBubbleUserUseImageInputChange: (Boolean) -> Unit,
    bubbleAiUseImageInput: Boolean,
    onBubbleAiUseImageInputChange: (Boolean) -> Unit,
    bubbleUserImageUriInput: String?,
    bubbleAiImageUriInput: String?,
    onPickBubbleUserImage: () -> Unit,
    onPickBubbleAiImage: () -> Unit,
    onClearBubbleUserImage: () -> Unit,
    onClearBubbleAiImage: () -> Unit,
    bubbleUserImageCropLeftInput: Float,
    onBubbleUserImageCropLeftInputChange: (Float) -> Unit,
    bubbleUserImageCropTopInput: Float,
    onBubbleUserImageCropTopInputChange: (Float) -> Unit,
    bubbleUserImageCropRightInput: Float,
    onBubbleUserImageCropRightInputChange: (Float) -> Unit,
    bubbleUserImageCropBottomInput: Float,
    onBubbleUserImageCropBottomInputChange: (Float) -> Unit,
    bubbleUserImageRepeatStartInput: Float,
    onBubbleUserImageRepeatStartInputChange: (Float) -> Unit,
    bubbleUserImageRepeatEndInput: Float,
    onBubbleUserImageRepeatEndInputChange: (Float) -> Unit,
    bubbleUserImageRepeatYStartInput: Float,
    onBubbleUserImageRepeatYStartInputChange: (Float) -> Unit,
    bubbleUserImageRepeatYEndInput: Float,
    onBubbleUserImageRepeatYEndInputChange: (Float) -> Unit,
    bubbleUserImageScaleInput: Float,
    onBubbleUserImageScaleInputChange: (Float) -> Unit,
    bubbleAiImageCropLeftInput: Float,
    onBubbleAiImageCropLeftInputChange: (Float) -> Unit,
    bubbleAiImageCropTopInput: Float,
    onBubbleAiImageCropTopInputChange: (Float) -> Unit,
    bubbleAiImageCropRightInput: Float,
    onBubbleAiImageCropRightInputChange: (Float) -> Unit,
    bubbleAiImageCropBottomInput: Float,
    onBubbleAiImageCropBottomInputChange: (Float) -> Unit,
    bubbleAiImageRepeatStartInput: Float,
    onBubbleAiImageRepeatStartInputChange: (Float) -> Unit,
    bubbleAiImageRepeatEndInput: Float,
    onBubbleAiImageRepeatEndInputChange: (Float) -> Unit,
    bubbleAiImageRepeatYStartInput: Float,
    onBubbleAiImageRepeatYStartInputChange: (Float) -> Unit,
    bubbleAiImageRepeatYEndInput: Float,
    onBubbleAiImageRepeatYEndInputChange: (Float) -> Unit,
    bubbleAiImageScaleInput: Float,
    onBubbleAiImageScaleInputChange: (Float) -> Unit,
    bubbleImageRenderModeInput: String,
    onBubbleImageRenderModeInputChange: (String) -> Unit,
    bubbleUserRoundedCornersEnabledInput: Boolean,
    onBubbleUserRoundedCornersEnabledInputChange: (Boolean) -> Unit,
    bubbleAiRoundedCornersEnabledInput: Boolean,
    onBubbleAiRoundedCornersEnabledInputChange: (Boolean) -> Unit,
    bubbleUserContentPaddingLeftInput: Float,
    onBubbleUserContentPaddingLeftInputChange: (Float) -> Unit,
    bubbleUserContentPaddingRightInput: Float,
    onBubbleUserContentPaddingRightInputChange: (Float) -> Unit,
    bubbleAiContentPaddingLeftInput: Float,
    onBubbleAiContentPaddingLeftInputChange: (Float) -> Unit,
    bubbleAiContentPaddingRightInput: Float,
    onBubbleAiContentPaddingRightInputChange: (Float) -> Unit,
    saveThemeSettingsWithCharacterCard: SaveThemeSettingsAction,
    preferencesManager: UserPreferencesManager,
) {
    ThemeSettingsSectionTitle(
        title = stringResource(id = R.string.chat_style_title),
        icon = Icons.Default.ColorLens,
    )

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = cardColors) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.chat_style_desc),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.chat_style_cursor_user_follow_theme),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text =
                            stringResource(id = R.string.chat_style_cursor_user_follow_theme_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = cursorUserBubbleFollowThemeInput,
                    onCheckedChange = {
                        onCursorUserBubbleFollowThemeInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(
                                cursorUserBubbleFollowTheme = it,
                            )
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            stringResource(
                                id = R.string.chat_style_cursor_user_bubble_liquid_glass
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text =
                            stringResource(
                                id = R.string.chat_style_cursor_user_bubble_liquid_glass_desc
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = cursorUserBubbleLiquidGlassInput,
                    onCheckedChange = {
                        onCursorUserBubbleLiquidGlassInputChange(it)
                        if (it) {
                            onCursorUserBubbleWaterGlassInputChange(false)
                        }
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(
                                cursorUserBubbleLiquidGlass = it,
                                cursorUserBubbleWaterGlass = if (it) false else null,
                            )
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text =
                            stringResource(
                                id = R.string.chat_style_cursor_user_bubble_water_glass
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text =
                            stringResource(
                                id = R.string.chat_style_cursor_user_bubble_water_glass_desc
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = cursorUserBubbleWaterGlassInput,
                    onCheckedChange = {
                        onCursorUserBubbleWaterGlassInputChange(it)
                        if (it) {
                            onCursorUserBubbleLiquidGlassInputChange(false)
                        }
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(
                                cursorUserBubbleWaterGlass = it,
                                cursorUserBubbleLiquidGlass = if (it) false else null,
                            )
                        }
                    },
                )
            }

            if (!cursorUserBubbleFollowThemeInput) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ColorSelectionItem(
                        title = stringResource(id = R.string.chat_style_cursor_user_bubble_color),
                        color = Color(cursorUserBubbleColorInput),
                        modifier = Modifier.weight(1f),
                        onClick = { onShowColorPicker("cursorUserBubble") },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ChatStylePreviewCard(
                userColor =
                    if (cursorUserBubbleFollowThemeInput) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color(cursorUserBubbleColorInput)
                    },
            )
        }
    }
}

@Composable
private fun ChatStylePreviewCard(
    userColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(id = R.string.chat_style_preview_title),
                style = MaterialTheme.typography.bodyMedium,
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = userColor,
                tonalElevation = 1.dp,
            ) {
                Text(
                    text = stringResource(id = R.string.chat_style_preview_user_message),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.mcp_command_response),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                )
                Text(
                    text = stringResource(id = R.string.chat_style_preview_ai_message),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun ThemeSettingsDisplayOptionsSection(
    cardColors: CardColors,
    showThinkingProcessInput: Boolean,
    onShowThinkingProcessInputChange: (Boolean) -> Unit,
    showStatusTagsInput: Boolean,
    onShowStatusTagsInputChange: (Boolean) -> Unit,
    showModelProviderInput: Boolean,
    onShowModelProviderInputChange: (Boolean) -> Unit,
    showModelNameInput: Boolean,
    onShowModelNameInputChange: (Boolean) -> Unit,
    showRoleNameInput: Boolean,
    onShowRoleNameInputChange: (Boolean) -> Unit,
    showUserNameInput: Boolean,
    onShowUserNameInputChange: (Boolean) -> Unit,
    showMessageTokenStatsInput: Boolean,
    onShowMessageTokenStatsInputChange: (Boolean) -> Unit,
    showMessageTimingStatsInput: Boolean,
    onShowMessageTimingStatsInputChange: (Boolean) -> Unit,
    showMessageTimestampInput: Boolean,
    onShowMessageTimestampInputChange: (Boolean) -> Unit,
    showInputProcessingStatusInput: Boolean,
    onShowInputProcessingStatusInputChange: (Boolean) -> Unit,
    showChatFloatingDotsAnimationInput: Boolean,
    onShowChatFloatingDotsAnimationInputChange: (Boolean) -> Unit,
    saveThemeSettingsWithCharacterCard: SaveThemeSettingsAction,
    preferencesManager: UserPreferencesManager,
) {
    ThemeSettingsSectionTitle(
        title = stringResource(id = R.string.display_options_title),
        icon = Icons.Default.ColorLens,
    )

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = cardColors) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_thinking_process),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_thinking_process_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showThinkingProcessInput,
                    onCheckedChange = {
                        onShowThinkingProcessInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showThinkingProcess = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_model_provider),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_model_provider_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showModelProviderInput,
                    onCheckedChange = {
                        onShowModelProviderInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showModelProvider = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_model_name),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_model_name_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showModelNameInput,
                    onCheckedChange = {
                        onShowModelNameInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showModelName = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_role_name),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_role_name_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showRoleNameInput,
                    onCheckedChange = {
                        onShowRoleNameInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showRoleName = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_user_name),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_user_name_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showUserNameInput,
                    onCheckedChange = {
                        onShowUserNameInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showUserName = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_message_token_stats),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_message_token_stats_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showMessageTokenStatsInput,
                    onCheckedChange = {
                        onShowMessageTokenStatsInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showMessageTokenStats = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_message_timing_stats),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_message_timing_stats_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showMessageTimingStatsInput,
                    onCheckedChange = {
                        onShowMessageTimingStatsInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showMessageTimingStats = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_message_timestamp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_message_timestamp_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showMessageTimestampInput,
                    onCheckedChange = {
                        onShowMessageTimestampInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showMessageTimestamp = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_status_tags),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_status_tags_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showStatusTagsInput,
                    onCheckedChange = {
                        onShowStatusTagsInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(showStatusTags = it)
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_input_processing_status),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text =
                            stringResource(id = R.string.show_input_processing_status_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showInputProcessingStatusInput,
                    onCheckedChange = {
                        onShowInputProcessingStatusInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(
                                showInputProcessingStatus = it,
                            )
                        }
                    },
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.show_chat_floating_dots_animation),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(id = R.string.show_chat_floating_dots_animation_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showChatFloatingDotsAnimationInput,
                    onCheckedChange = {
                        onShowChatFloatingDotsAnimationInputChange(it)
                        saveThemeSettingsWithCharacterCard {
                            preferencesManager.saveThemeSettings(
                                showChatFloatingDotsAnimation = it,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
internal fun ThemeSettingsSectionTitle(
    title: String,
    icon: ImageVector,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
}
