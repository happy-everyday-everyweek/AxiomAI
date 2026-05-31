package com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import com.ai.assistance.operit.R
import com.ai.assistance.operit.core.avatar.common.state.AvatarEmotion
import com.ai.assistance.operit.data.model.ChatMessage
import com.ai.assistance.operit.data.model.InputProcessingState
import com.ai.assistance.operit.data.model.PromptFunctionType

import com.ai.assistance.operit.ui.floating.FloatContext
import com.ai.assistance.operit.ui.floating.ui.fullscreen.XmlTextProcessor
import com.ai.assistance.operit.ui.floating.ui.pet.AvatarEmotionManager

import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.util.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "FloatingFullscreenViewModel"

data class VoiceAvatarMotionRequest(
    val emotion: AvatarEmotion = AvatarEmotion.IDLE,
    val triggerName: String? = null,
    val playOnce: Boolean = false,
    val sequence: Long = 0L
)

class FloatingFullscreenModeViewModel(
    private val context: Context,
    private val floatContext: FloatContext,
    private val coroutineScope: CoroutineScope,
    initialWaveActive: Boolean
) {
    var aiMessage by mutableStateOf(context.getString(R.string.floating_hold_microphone_to_speak))

    var isWaveActive by mutableStateOf(initialWaveActive)
    var showBottomControls by mutableStateOf(true)
    var isEditMode by mutableStateOf(false)
    var editableText by mutableStateOf("")
    var inputText by mutableStateOf("")
    var showDragHints by mutableStateOf(false)

    var attachScreenContent by mutableStateOf(false)
    var attachNotifications by mutableStateOf(false)
    var attachLocation by mutableStateOf(false)
    var hasOcrSelection by mutableStateOf(false)
    var isStreamingTtsMuted by mutableStateOf(false)
    var voiceAvatarMotionRequest by mutableStateOf(VoiceAvatarMotionRequest())
        private set

    val isInitialLoad = mutableStateOf(true)

    private var aiStreamJob: Job? = null
    private var activeAiStreamIdentity: Int? = null
    private var activeAiMessageTimestamp: Long? = null

    private var inactivityJob: Job? = null
    private var lastVoiceActivityAtMs: Long = 0L
    private var waveModeAutoTimeoutEnabled: Boolean = false
    var isVoiceCapturePausedForAi by mutableStateOf(false)
    private var voiceAvatarSequence: Long = 0L
    private var lastHandledVoiceAvatarMessageKey: String? = null
    private var hasInitializedVoiceAvatarFromSnapshot: Boolean = false

    val isRecording: Boolean get() = false
    val isProcessingSpeech: Boolean get() = false
    val userMessage: String get() = ""
    val hasFocus: Boolean get() = false

    fun toggleStreamingTtsMuted() {
        isStreamingTtsMuted = !isStreamingTtsMuted
    }

    private fun isAiBusyOrSpeaking(): Boolean {
        return isAiBusy()
    }

    private fun shouldInterceptCenterAvatarClick(): Boolean {
        return isVoiceCapturePausedForAi || isAiBusyOrSpeaking()
    }

    private fun prepareVoiceCaptureForAiTurn() {
        if (!isWaveActive) return
        isVoiceCapturePausedForAi = true
    }

    private fun cancelPendingVoiceCaptureResume() {
        isVoiceCapturePausedForAi = false
    }

    fun processAndSpeakAiMessage(lastMessage: ChatMessage?, ttsCleanerRegexs: List<String>) {
        val message = lastMessage ?: return

         if (activeAiMessageTimestamp != null && activeAiMessageTimestamp != message.timestamp) {
             aiStreamJob?.cancel()
             aiStreamJob = null
             activeAiStreamIdentity = null
         }
         activeAiMessageTimestamp = message.timestamp

        if (isInitialLoad.value) {
            isInitialLoad.value = false
            if (message.sender == "ai") aiMessage = stripVoiceAvatarTags(message.content)
            return
        }

        when (message.sender) {
            "think" -> {
                aiStreamJob?.cancel()
                aiStreamJob = null
                activeAiStreamIdentity = null
                aiMessage = context.getString(R.string.floating_thinking)
            }
            "ai" -> {
                val stream = message.contentStream
                if (stream != null) {
                    val streamIdentity = System.identityHashCode(stream)
                    if (aiStreamJob?.isActive == true && activeAiStreamIdentity == streamIdentity) {
                        return
                    }
                    aiStreamJob?.cancel()
                    aiStreamJob = null
                    activeAiStreamIdentity = streamIdentity

                    aiStreamJob = coroutineScope.launch {
                        handleStreamResponse(stream, ttsCleanerRegexs)
                    }
                } else {
                    aiStreamJob?.cancel()
                    aiStreamJob = null
                    activeAiStreamIdentity = null
                    handleStaticResponse(message.content)
                }
            }
        }
    }

    private suspend fun handleStreamResponse(stream: Stream<String>, cleaners: List<String>) {
        val sb = StringBuilder()
        var isFirstChar = true
        XmlTextProcessor.processStreamToText(stream).collect { char ->
            if (isFirstChar) {
                aiMessage = ""
                isFirstChar = false
            }
            aiMessage += char
            sb.append(char)
        }
    }

    private fun handleStaticResponse(content: String) {
        val plainContent = stripVoiceAvatarTags(content)
        aiMessage = plainContent
    }

    fun startVoiceCapture() {
        val lastMessage = floatContext.messages.lastOrNull()
        val isAiWorking = lastMessage?.sender == "think" ||
                          (lastMessage?.sender == "ai" && lastMessage.contentStream != null)

        if (isAiWorking) {
            floatContext.onCancelMessage?.invoke()
        }
    }

    fun stopVoiceCapture(isCancel: Boolean) {
    }

    fun enterWaveMode(
        wakeLaunched: Boolean = false,
        enableAutoTimeout: Boolean = false
    ) {
        isWaveActive = true
        waveModeAutoTimeoutEnabled = enableAutoTimeout
        inactivityJob?.cancel()
        inactivityJob = null

        startVoiceCapture()

        if (waveModeAutoTimeoutEnabled) {
            lastVoiceActivityAtMs = System.currentTimeMillis()
            startInactivityMonitor()
        }
    }

    fun exitWaveMode() {
        cancelPendingVoiceCaptureResume()
        waveModeAutoTimeoutEnabled = false
        stopVoiceCapture(true)
        isWaveActive = false
        showBottomControls = true
        inactivityJob?.cancel()
        inactivityJob = null
        resetVoiceAvatarToIdle()
    }

    fun onCenterAvatarClick() {
        if (isWaveActive && shouldInterceptCenterAvatarClick()) {
            val shouldCancelAiTurn = isAiBusy()
            cancelPendingVoiceCaptureResume()
            if (shouldCancelAiTurn) {
                floatContext.onCancelMessage?.invoke()
            }
            return
        }

        if (isWaveActive) {
            exitWaveMode()
        } else {
            enterWaveMode(enableAutoTimeout = false)
        }
    }

    fun handleRecognitionResult(resultText: String, isFinal: Boolean) {
        if (isWaveActive && resultText.isNotBlank()) {
            lastVoiceActivityAtMs = System.currentTimeMillis()
        }
    }

    suspend fun initialize(autoEnterVoiceChat: Boolean = false, wakeLaunched: Boolean = false) {
        cancelPendingVoiceCaptureResume()
        isInitialLoad.value = true
        isWaveActive = autoEnterVoiceChat
        showBottomControls = true
        hasInitializedVoiceAvatarFromSnapshot = false
        lastHandledVoiceAvatarMessageKey = null
        resetVoiceAvatarToIdle()
        exitEditMode()

        aiMessage = context.getString(R.string.floating_hold_microphone_to_speak)

        if (autoEnterVoiceChat) {
            enterWaveMode(wakeLaunched = wakeLaunched, enableAutoTimeout = true)
        }
    }

    fun cleanup() {
        cancelPendingVoiceCaptureResume()

        inactivityJob?.cancel()
        inactivityJob = null

        aiStreamJob?.cancel()
        aiStreamJob = null
        activeAiStreamIdentity = null

        hasInitializedVoiceAvatarFromSnapshot = false
        lastHandledVoiceAvatarMessageKey = null
        resetVoiceAvatarToIdle()
    }

    private fun startInactivityMonitor() {
        inactivityJob?.cancel()
        inactivityJob = coroutineScope.launch {
            while (isActive && isWaveActive) {
                val timeoutMs = 30L * 1000L
                val elapsed = System.currentTimeMillis() - lastVoiceActivityAtMs
                val remaining = timeoutMs - elapsed
                if (remaining <= 0L) {
                    if (isAiBusy()) {
                        while (isActive && isWaveActive && isAiBusy()) {
                            delay(250L)
                        }
                        lastVoiceActivityAtMs = System.currentTimeMillis()
                        continue
                    }

                    exitWaveMode()
                    if (floatContext.chatService?.isWakeLaunched() == true) {
                        floatContext.onClose()
                    }
                    return@launch
                }
                delay(minOf(remaining, 500L))
            }
        }
    }

    private fun isAiBusy(): Boolean {
        val state = floatContext.inputProcessingState.value
        val stateBusy =
            state !is InputProcessingState.Idle &&
                state !is InputProcessingState.Completed &&
                state !is InputProcessingState.Error

        val lastMessage = floatContext.messages.lastOrNull()
        val streamBusy =
            lastMessage?.sender == "think" ||
                (lastMessage?.sender == "ai" && lastMessage.contentStream != null)

        return stateBusy || streamBusy
    }

    fun enterEditMode(text: String) {
        editableText = text
        isEditMode = true
        aiMessage = context.getString(R.string.floating_edit_your_message)
    }

    fun exitEditMode() {
        isEditMode = false
        editableText = ""
        aiMessage = context.getString(R.string.floating_hold_microphone_to_speak)
    }

    fun sendEditedMessage() {
        if (editableText.isNotBlank()) {
            startVoiceAvatarThinking()
            prepareVoiceCaptureForAiTurn()
            floatContext.onSendMessage?.invoke(editableText, PromptFunctionType.VOICE)
            isEditMode = false
            editableText = ""
            aiMessage = context.getString(R.string.floating_thinking)
        }
    }

    fun sendInputMessage() {
        val text = inputText.trim()
        if (text.isEmpty() && !attachScreenContent && !attachNotifications && !attachLocation && !hasOcrSelection) return

        val shouldCaptureScreen = attachScreenContent
        val shouldCaptureNotifications = attachNotifications
        val shouldCaptureLocation = attachLocation

        inputText = ""
        attachScreenContent = false
        attachNotifications = false
        attachLocation = false
        hasOcrSelection = false
        aiMessage = context.getString(R.string.floating_thinking)

        startVoiceAvatarThinking()
        prepareVoiceCaptureForAiTurn()

        coroutineScope.launch {
            try {
                val attachmentDelegate = floatContext.chatService?.getChatCore()?.getAttachmentDelegate()
                if (shouldCaptureScreen) {
                    attachmentDelegate?.captureScreenContent()
                }
                if (shouldCaptureNotifications) {
                    attachmentDelegate?.captureNotifications()
                }
                if (shouldCaptureLocation) {
                    attachmentDelegate?.captureLocation()
                }
            } catch (_: Exception) {
            }

            floatContext.onSendMessage?.invoke(text, PromptFunctionType.VOICE)
        }
    }

    fun handleVoiceAvatarMessage(message: ChatMessage?) {
        if (!hasInitializedVoiceAvatarFromSnapshot) {
            hasInitializedVoiceAvatarFromSnapshot = true
            if (message?.sender == "ai" && message.contentStream == null) {
                lastHandledVoiceAvatarMessageKey = buildVoiceAvatarMessageKey(message)
                return
            }
        }

        when (message?.sender) {
            "think" -> startVoiceAvatarThinking()
            "ai" -> {
                if (message.contentStream != null) {
                    startVoiceAvatarThinking()
                    return
                }

                val messageKey = buildVoiceAvatarMessageKey(message)
                if (lastHandledVoiceAvatarMessageKey == messageKey) {
                    return
                }
                lastHandledVoiceAvatarMessageKey = messageKey

                val triggerName = AvatarEmotionManager.extractMoodTagValue(message.content)
                if (!triggerName.isNullOrBlank()) {
                    pushVoiceAvatarMotion(
                        emotion = AvatarEmotionManager.analyzeEmotion(message.content),
                        triggerName = triggerName,
                        playOnce = true
                    )
                    return
                }

                val emotion = AvatarEmotionManager.analyzeEmotion(message.content)
                if (emotion == AvatarEmotion.IDLE) {
                    resetVoiceAvatarToIdle()
                } else {
                    pushVoiceAvatarMotion(emotion = emotion, playOnce = true)
                }
            }
        }
    }

    fun syncVoiceAvatarWithProcessingState(
        state: InputProcessingState,
        latestMessage: ChatMessage?
    ) {
        val shouldResetThinking =
            (state is InputProcessingState.Idle || state is InputProcessingState.Error) &&
                voiceAvatarMotionRequest.triggerName.isNullOrBlank() &&
                voiceAvatarMotionRequest.emotion == AvatarEmotion.THINKING
        if (!shouldResetThinking) {
            return
        }

        val hasCompletedAiMessage =
            latestMessage?.sender == "ai" && latestMessage.contentStream == null
        if (!hasCompletedAiMessage) {
            resetVoiceAvatarToIdle()
        }
    }

    private fun buildVoiceAvatarMessageKey(message: ChatMessage): String {
        return "${message.sender}:${message.timestamp}:${message.content.hashCode()}:${message.contentStream == null}"
    }

    private fun pushVoiceAvatarMotion(
        emotion: AvatarEmotion,
        triggerName: String? = null,
        playOnce: Boolean
    ) {
        voiceAvatarSequence += 1
        voiceAvatarMotionRequest = VoiceAvatarMotionRequest(
            emotion = emotion,
            triggerName = triggerName,
            playOnce = playOnce,
            sequence = voiceAvatarSequence
        )
    }

    private fun startVoiceAvatarThinking() {
        pushVoiceAvatarMotion(emotion = AvatarEmotion.THINKING, playOnce = false)
    }

    private fun resetVoiceAvatarToIdle() {
        pushVoiceAvatarMotion(emotion = AvatarEmotion.IDLE, playOnce = false)
    }

    private fun stripVoiceAvatarTags(content: String): String {
        return AvatarEmotionManager.stripXmlLikeTags(content)
    }
}

@Composable
 fun rememberFloatingFullscreenModeViewModel(
     context: Context,
     floatContext: FloatContext,
     coroutineScope: CoroutineScope,
     initialWaveActive: Boolean
 ) = remember(context, initialWaveActive) {
     FloatingFullscreenModeViewModel(context, floatContext, coroutineScope, initialWaveActive)
 }
