package com.ai.assistance.operit.ui.floating.ui.fullscreen.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import com.ai.assistance.operit.R
import com.ai.assistance.operit.core.avatarstate.AvatarEmotion
import com.ai.assistance.operit.core.avatarstate.AvatarMoodTypes
import com.ai.assistance.operit.data.model.ChatMessage
import com.ai.assistance.operit.data.model.InputProcessingState
import com.ai.assistance.operit.data.model.PromptFunctionType
import com.ai.assistance.operit.api.voice.VoiceServiceFactory
import com.ai.assistance.operit.api.voice.VoiceService
import com.ai.assistance.operit.ui.floating.FloatContext
import com.ai.assistance.operit.ui.floating.ui.fullscreen.XmlTextProcessor
import com.ai.assistance.operit.util.AppLogger
import com.ai.assistance.operit.util.TtsSegmenter
import com.ai.assistance.operit.util.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
    var aiMessage by mutableStateOf(context.getString(R.string.floating_type_message))

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
    private var ttsSpeakJob: Job? = null

    private val voiceService: VoiceService by lazy { VoiceServiceFactory.getInstance(context.applicationContext) }

    private var voiceAvatarSequence: Long = 0L
    private var lastHandledVoiceAvatarMessageKey: String? = null
    private var hasInitializedVoiceAvatarFromSnapshot: Boolean = false

    fun toggleStreamingTtsMuted() {
        isStreamingTtsMuted = !isStreamingTtsMuted
        if (isStreamingTtsMuted) {
            stopCurrentTtsPlayback()
        }
    }

    private fun stopCurrentTtsPlayback() {
        ttsSpeakJob?.cancel()
        ttsSpeakJob = null
        coroutineScope.launch { voiceService.stop() }
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

        stopCurrentTtsPlayback()

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
        var isFirstSentence = true
        var isFirstChar = true
        XmlTextProcessor.processStreamToText(stream).collect { char ->
            if (isFirstChar) {
                aiMessage = ""
                isFirstChar = false
            }
            aiMessage += char
            sb.append(char)

            val cutIdx = TtsSegmenter.nextSegmentEnd(sb)
            if (cutIdx >= 0) {
                val segment = sb.substring(0, cutIdx)
                if (trySpeak(segment, isFirstSentence, cleaners)) {
                    isFirstSentence = false
                    sb.delete(0, cutIdx)
                }
            }
        }
        trySpeak(sb.toString(), isFirstSentence, cleaners)
    }

    private fun handleStaticResponse(content: String) {
        val plainContent = stripVoiceAvatarTags(content)
        aiMessage = plainContent
    }

    private fun trySpeak(
        text: String,
        interrupt: Boolean,
        cleaners: List<String>
    ): Boolean {
        val cleanText = cleanTextForTts(text.trim(), cleaners)
        if (cleanText.isNotEmpty()) {
            if (isStreamingTtsMuted) {
                return true
            }
            enqueueSpeak(cleanText, interrupt)
            return true
        }
        return false
    }

    private fun cleanTextForTts(text: String, cleaners: List<String>): String {
        var result = text
        for (regex in cleaners) {
            try {
                result = result.replace(Regex(regex), "")
            } catch (_: Exception) {
            }
        }
        return result.trim()
    }

    private fun enqueueSpeak(text: String, interrupt: Boolean) {
        val previousJob = if (interrupt) {
            ttsSpeakJob?.cancel()
            null
        } else {
            ttsSpeakJob
        }

        ttsSpeakJob =
            coroutineScope.launch {
                try {
                    previousJob?.join()
                    voiceService.speak(text, interrupt)
                } catch (_: kotlinx.coroutines.CancellationException) {
                } catch (e: Exception) {
                    AppLogger.e(TAG, "TTS playback failed", e)
                }
            }
    }

    suspend fun initialize(autoEnterVoiceChat: Boolean = false, wakeLaunched: Boolean = false) {
        isInitialLoad.value = true
        isWaveActive = false
        showBottomControls = true
        hasInitializedVoiceAvatarFromSnapshot = false
        lastHandledVoiceAvatarMessageKey = null
        resetVoiceAvatarToIdle()
        exitEditMode()

        aiMessage = context.getString(R.string.floating_type_message)
    }

    fun cleanup() {
        ttsSpeakJob?.cancel()
        ttsSpeakJob = null

        aiStreamJob?.cancel()
        aiStreamJob = null
        activeAiStreamIdentity = null

        hasInitializedVoiceAvatarFromSnapshot = false
        lastHandledVoiceAvatarMessageKey = null
        resetVoiceAvatarToIdle()
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
        aiMessage = context.getString(R.string.floating_type_message)
    }

    fun sendEditedMessage() {
        if (editableText.isNotBlank()) {
            startVoiceAvatarThinking()
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

                val triggerName = extractMoodTagValue(message.content)
                if (!triggerName.isNullOrBlank()) {
                    pushVoiceAvatarMotion(
                        emotion = analyzeEmotion(message.content),
                        triggerName = triggerName,
                        playOnce = true
                    )
                    return
                }

                val emotion = analyzeEmotion(message.content)
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
        return stripXmlLikeTags(content)
    }

    private fun extractMoodTagValue(text: String): String? {
        return try {
            val regex = Regex("<mood>([^<]+)</mood>", RegexOption.IGNORE_CASE)
            val all = regex.findAll(text).toList()
            if (all.isEmpty()) return null
            AvatarMoodTypes.normalizeKey(all.last().groupValues[1])
                .takeIf { it.isNotBlank() }
        } catch (_: Exception) { null }
    }

    private fun analyzeEmotion(text: String): AvatarEmotion {
        val parsedMood = extractMoodTagValue(text)
        if (!parsedMood.isNullOrBlank()) {
            val emotion = AvatarMoodTypes.builtInFallbackEmotion(parsedMood)
            if (emotion != null) return emotion
        }
        return inferEmotionFromText(text)
    }

    private fun inferEmotionFromText(text: String): AvatarEmotion {
        val t = text.lowercase()
        val happyKeywords = listOf("开心", "高兴", "不错", "棒", "太好了", "赞")
        val angryKeywords = listOf("生气", "愤怒", "气死", "讨厌", "糟糕", "怒")
        val cryKeywords = listOf("难过", "伤心", "沮丧", "忧伤", "哭")
        val shyKeywords = listOf("害羞", "羞", "脸红", "不好意思", "///")

        fun containsAny(keys: List<String>): Boolean =
            keys.any { t.contains(it) || text.contains(it) }

        return when {
            containsAny(happyKeywords) -> AvatarEmotion.HAPPY
            containsAny(angryKeywords) -> AvatarEmotion.SAD
            containsAny(cryKeywords) -> AvatarEmotion.SAD
            containsAny(shyKeywords) -> AvatarEmotion.CONFUSED
            else -> AvatarEmotion.IDLE
        }
    }

    private fun stripXmlLikeTags(text: String): String {
        var s = text
        val paired = Regex(
            pattern = "<([A-Za-z][A-Za-z0-9:_-]*)(\\s[^>]*)?>[\\s\\S]*?</\\1>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        repeat(5) { _ ->
            val updated = s.replace(paired, "")
            if (updated == s) return@repeat
            s = updated
        }
        s = s.replace(
            Regex("<[A-Za-z][A-Za-z0-9:_-]*(\\s[^>]*)?/\\s*>", RegexOption.IGNORE_CASE),
            ""
        )
        s = s.replace(
            Regex("</?[^>]+>", RegexOption.IGNORE_CASE),
            ""
        )
        return s.trim()
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
