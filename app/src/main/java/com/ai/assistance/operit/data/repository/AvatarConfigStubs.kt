package com.ai.assistance.operit.data.repository

import com.ai.assistance.operit.core.avatar.common.model.AvatarType
import com.ai.assistance.operit.core.avatar.common.state.AvatarCustomMoodDefinition
import com.ai.assistance.operit.core.avatar.common.state.AvatarEmotion

/**
 * Stub data classes for avatar configuration.
 * These were previously provided by a removed avatar repository module.
 */

data class AvatarConfig(
    val id: String,
    val name: String,
    val type: AvatarType = AvatarType.WEBP,
    val emotionAnimationMapping: Map<AvatarEmotion, String> = emptyMap(),
    val moodAnimationMapping: Map<String, String> = emptyMap(),
    val customMoodDefinitions: List<AvatarCustomMoodDefinition> = emptyList()
)

fun AvatarConfig.getEmotionAnimationMapping(): Map<AvatarEmotion, String> = emotionAnimationMapping

fun AvatarConfig.getMoodAnimationMapping(): Map<String, String> = moodAnimationMapping

fun AvatarConfig.getCustomMoodDefinitions(): List<AvatarCustomMoodDefinition> = customMoodDefinitions

data class AvatarInstanceSettings(
    val scale: Float = 1.0f,
    val translateX: Float = 0.0f,
    val translateY: Float = 0.0f,
    val customSettings: Map<String, Float> = emptyMap()
)

data class AvatarSettings(
    val isVoiceCallAvatarEnabled: Boolean = false
)
