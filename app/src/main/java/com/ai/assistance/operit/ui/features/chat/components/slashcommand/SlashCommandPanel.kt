package com.ai.assistance.operit.ui.features.chat.components.slashcommand

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.assistance.operit.R

@Composable
fun SlashCommandPanel(
    query: String,
    onCommandSelected: (SlashCommand) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val commands = remember { buildSlashCommands(context) }
    val filteredCommands = remember(query, commands) {
        if (query.isBlank() || query == "/") {
            commands
        } else {
            commands.filter { cmd ->
                cmd.name.contains(query.removePrefix("/"), ignoreCase = true) ||
                    cmd.description.contains(query.removePrefix("/"), ignoreCase = true)
            }
        }
    }

    AnimatedVisibility(
        visible = filteredCommands.isNotEmpty(),
        enter = expandVertically(
            animationSpec = tween(150),
            expandFrom = Alignment.Bottom
        ) + fadeIn(animationSpec = tween(150)),
        exit = shrinkVertically(
            animationSpec = tween(150),
            shrinkTowards = Alignment.Bottom
        ) + fadeOut(animationSpec = tween(150)),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = context.getString(R.string.slash_command_title),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.size(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                ) {
                    items(filteredCommands, key = { it.name }) { command ->
                        SlashCommandItem(
                            command = command,
                            query = query,
                            onClick = { onCommandSelected(command) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlashCommandItem(
    command: SlashCommand,
    query: String,
    onClick: () -> Unit,
) {
    val highlightRange = if (query.length > 1) {
        val searchStr = query.removePrefix("/").lowercase()
        val idx = command.name.lowercase().indexOf(searchStr)
        if (idx >= 0) idx to (idx + searchStr.length) else null
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        command.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    append("/")
                    if (highlightRange != null) {
                        val (start, end) = highlightRange
                        if (start > 0) {
                            append(command.name.substring(0, start))
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append(command.name.substring(start, end.coerceAtMost(command.name.length)))
                        }
                        if (end < command.name.length) {
                            append(command.name.substring(end))
                        }
                    } else {
                        append(command.name)
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = command.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun buildSlashCommands(context: android.content.Context): List<SlashCommand> {
    return listOf(
        SlashCommand(
            name = "think",
            description = context.getString(R.string.slash_command_think_desc),
            icon = Icons.Rounded.Psychology,
            action = SlashCommandAction.ToggleThinking(),
        ),
        SlashCommand(
            name = "model",
            description = context.getString(R.string.slash_command_model_desc),
            icon = Icons.Outlined.DataObject,
            action = SlashCommandAction.SelectModel,
        ),
        SlashCommand(
            name = "memory",
            description = context.getString(R.string.slash_command_memory_desc),
            icon = Icons.Rounded.Save,
            action = SlashCommandAction.SelectMemory,
        ),
        SlashCommand(
            name = "tools",
            description = context.getString(R.string.slash_command_tools_desc),
            icon = Icons.Outlined.Psychology,
            action = SlashCommandAction.ToggleTools,
        ),
        SlashCommand(
            name = "permission",
            description = context.getString(R.string.slash_command_permission_desc),
            icon = Icons.Outlined.Security,
            action = SlashCommandAction.SwitchPermission,
        ),
        SlashCommand(
            name = "context",
            description = context.getString(R.string.slash_command_context_desc),
            icon = Icons.Rounded.Memory,
            action = SlashCommandAction.ConfigContext,
        ),
        SlashCommand(
            name = "stream",
            description = context.getString(R.string.slash_command_stream_desc),
            icon = Icons.Outlined.Speed,
            action = SlashCommandAction.ToggleStream,
        ),
    )
}
