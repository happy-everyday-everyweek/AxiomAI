package com.ai.assistance.operit.ui.features.chat.components.part

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ai.assistance.operit.R
import kotlinx.coroutines.delay

@Composable
fun ToolResultDisplay(
        toolName: String,
        result: String,
        isSuccess: Boolean = true,
        onCopyResult: () -> Unit = {},
        modifier: Modifier = Modifier,
        enableDialog: Boolean = true,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val hasContent = result.isNotBlank()

    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog && enableDialog) {
        ToolResultDetailDialog(
                toolName = toolName,
                result = result,
                isSuccess = isSuccess,
                onDismiss = { showDetailDialog = false },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(result))
                    onCopyResult()
                }
        )
    }

    val targetColor = if (isSuccess) ToolCallSuccessColor else ToolCallFailureColor
    var feedbackActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(COLOR_FEEDBACK_DURATION_MS.toLong())
        feedbackActive = false
    }

    val animatedColor by animateColorAsState(
        targetValue = if (feedbackActive) targetColor else ToolCallGrayColor,
        animationSpec = tween(durationMillis = 300),
        label = "toolResultColor"
    )

    val statusText = if (isSuccess) {
        context.getString(R.string.execution_success)
    } else {
        context.getString(R.string.execution_failed)
    }

    val displayText = "$toolName $statusText"

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            color = animatedColor,
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .then(
                if (hasContent && enableDialog) {
                    Modifier.clickable { showDetailDialog = true }
                } else {
                    Modifier
                }
            ),
    )
}

/** 工具结果详情弹窗 美观的弹窗显示完整的工具执行结果 */
@Composable
private fun ToolResultDetailDialog(
        toolName: String,
        result: String,
        isSuccess: Boolean,
        onDismiss: () -> Unit,
        onCopy: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题栏
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // 状态图标
                    Icon(
                            imageVector =
                                    if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (isSuccess) context.getString(R.string.success) else context.getString(R.string.failed),
                            tint =
                                    if (isSuccess) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 工具名称
                    Text(
                            text = "$toolName ${if (isSuccess) context.getString(R.string.execution_success) else context.getString(R.string.execution_failed)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // 复制按钮
                    IconButton(onClick = onCopy) {
                        Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = context.getString(R.string.copy_result),
                                tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 分隔线
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(16.dp))

                // 结果内容
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .heightIn(min = 50.dp, max = 300.dp)
                                        .verticalScroll(rememberScrollState())
                                        .background(
                                                color =
                                                        if (isSuccess)
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant.copy(
                                                                        alpha = 0.5f
                                                                )
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .errorContainer.copy(
                                                                        alpha = 0.2f
                                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                ) {
                    Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 关闭按钮
                Button(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.End),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) { Text(context.getString(R.string.close)) }
            }
        }
    }
}
