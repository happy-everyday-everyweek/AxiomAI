package com.ai.assistance.operit.ui.features.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.R

@Composable
fun ChatHeader(
        showChatHistorySelector: Boolean,
        onToggleChatHistorySelector: () -> Unit,
        modifier: Modifier = Modifier,
        onLaunchFloatingWindow: () -> Unit = {},
        isFloatingMode: Boolean = false,
        historyIconColor: Int? = null,
        pipIconColor: Int? = null,
        runningTaskCount: Int = 0
) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = modifier
        ) {
                if (runningTaskCount >= 2) {
                        Surface(
                                onClick = onToggleChatHistorySelector,
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                        ) {
                                Row(
                                        modifier = Modifier.height(32.dp).padding(start = 6.dp, end = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription =
                                                        if (showChatHistorySelector) stringResource(R.string.hide_history) else stringResource(R.string.show_history),
                                                tint =
                                                        historyIconColor?.let { Color(it) }
                                                                ?: MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                                text = runningTaskCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                maxLines = 1
                                        )
                                }
                        }
                } else {
                        Box(
                                modifier =
                                        Modifier.size(32.dp)
                                                .background(
                                                        color =
                                                                if (showChatHistorySelector)
                                                                        MaterialTheme.colorScheme.primary
                                                                                .copy(alpha = 0.15f)
                                                                else Color.Transparent,
                                                        shape = CircleShape
                                                )
                        ) {
                                IconButton(
                                        onClick = onToggleChatHistorySelector,
                                        modifier = Modifier.matchParentSize()
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription =
                                                        if (showChatHistorySelector) stringResource(R.string.hide_history) else stringResource(R.string.show_history),
                                                tint =
                                                        historyIconColor?.let { Color(it) }
                                                                ?: if (showChatHistorySelector)
                                                                        MaterialTheme.colorScheme.primary
                                                                else
                                                                        MaterialTheme.colorScheme.onSurface
                                                                                .copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                        )
                                }
                        }
                }

                Box(
                        modifier =
                                Modifier.size(32.dp)
                                        .background(
                                                color =
                                                        if (isFloatingMode)
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.15f)
                                                        else Color.Transparent,
                                                shape = CircleShape
                                        )
                ) {
                        IconButton(
                                onClick = onLaunchFloatingWindow,
                                modifier = Modifier.matchParentSize()
                        ) {
                                Icon(
                                        imageVector = Icons.Default.PictureInPicture,
                                        contentDescription =
                                                if (isFloatingMode) stringResource(R.string.close_floating_window) else stringResource(R.string.open_floating_window),
                                        tint =
                                                pipIconColor?.let { Color(it) }
                                                        ?: if (isFloatingMode)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme.onSurface
                                                                        .copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}
