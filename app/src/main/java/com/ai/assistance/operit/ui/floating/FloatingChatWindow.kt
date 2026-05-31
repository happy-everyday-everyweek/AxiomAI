package com.ai.assistance.operit.ui.floating

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ai.assistance.operit.data.model.AttachmentInfo
import com.ai.assistance.operit.data.model.ChatMessage
import com.ai.assistance.operit.data.model.InputProcessingState
import com.ai.assistance.operit.data.model.PromptFunctionType
import com.ai.assistance.operit.services.FloatingChatService
import com.ai.assistance.operit.services.floating.FloatingWindowState

@Composable
fun FloatingChatWindow(
        messages: List<ChatMessage>,
        width: Dp,
        height: Dp,
        onClose: () -> Unit,
        onResize: (Dp, Dp) -> Unit,
        ballSize: Dp = 48.dp,
        windowScale: Float = 1.0f,
        onScaleChange: (Float) -> Unit = {},
        currentMode: FloatingMode = FloatingMode.BALL,
        previousMode: FloatingMode = FloatingMode.BALL,
        onModeChange: (FloatingMode) -> Unit = {},
        onMove: (Float, Float, Float) -> Unit = { _, _, _ -> },
        snapToEdge: (Boolean) -> Unit = { _ -> },
        isAtEdge: Boolean = false,
        screenWidth: Dp = 1080.dp,
        screenHeight: Dp = 2340.dp,
        currentX: Float = 0f,
        currentY: Float = 0f,
        saveWindowState: (() -> Unit)? = null,
        onSendMessage: ((String, PromptFunctionType) -> Unit)? = null,
        onCancelMessage: (() -> Unit)? = null,
        onAttachmentRequest: ((String) -> Unit)? = null,
        attachments: List<AttachmentInfo> = emptyList(),
        onRemoveAttachment: ((String) -> Unit)? = null,
        onInputFocusRequest: ((Boolean) -> Unit)? = null,
        chatService: FloatingChatService? = null,
        windowState: FloatingWindowState? = null,
        inputProcessingState: androidx.compose.runtime.State<InputProcessingState> = remember { mutableStateOf(InputProcessingState.Idle) }
) {
    SimpleProgressBubble(
        ballSize = ballSize,
        onMove = onMove,
        saveWindowState = saveWindowState
    )
}

@Composable
private fun SimpleProgressBubble(
    ballSize: Dp,
    onMove: (Float, Float, Float) -> Unit,
    saveWindowState: (() -> Unit)?
) {
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(ballSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        isDragging = false
                        saveWindowState?.invoke()
                    },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        isDragging = true
                        onMove(dragAmount.x, dragAmount.y, 1.0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ballSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            drawCircle(
                color = Color.Black,
                radius = radius,
                center = center
            )
        }
    }
}
