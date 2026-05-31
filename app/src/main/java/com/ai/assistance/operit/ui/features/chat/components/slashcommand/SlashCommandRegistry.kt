package com.ai.assistance.operit.ui.features.chat.components.slashcommand

import androidx.compose.ui.graphics.vector.ImageVector

data class SlashCommand(
    val name: String,
    val description: String,
    val icon: ImageVector? = null,
    val action: SlashCommandAction,
)

sealed interface SlashCommandAction {
    data class ToggleThinking(val qualityLevel: Int = 2) : SlashCommandAction
    data object SelectModel : SlashCommandAction
    data object SelectMemory : SlashCommandAction
    data object ToggleTools : SlashCommandAction
    data object SwitchPermission : SlashCommandAction
    data object ConfigContext : SlashCommandAction
    data object ToggleStream : SlashCommandAction
}

data class SlashCommandMatch(
    val command: SlashCommand,
    val matchedQuery: String,
)
