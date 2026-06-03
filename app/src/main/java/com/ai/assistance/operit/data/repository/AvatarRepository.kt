package com.ai.assistance.operit.data.repository

import android.net.Uri
import com.ai.assistance.operit.core.avatar.common.model.AvatarModel
import com.ai.assistance.operit.core.avatar.common.state.AvatarCustomMoodDefinition
import com.ai.assistance.operit.core.avatar.common.state.AvatarEmotion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Stub avatar repository for compilation.
 * This was previously provided by a removed avatar repository module.
 */
class AvatarRepository {

    private val _configs = MutableStateFlow<List<AvatarConfig>>(emptyList())
    val configs: Flow<List<AvatarConfig>> = _configs

    private val _currentAvatarId = MutableStateFlow<String?>(null)
    val currentAvatar: Flow<AvatarModel?> = _currentAvatarId.map { null }

    private val _instanceSettings = MutableStateFlow<Map<String, AvatarInstanceSettings>>(emptyMap())
    val instanceSettings: Flow<Map<String, AvatarInstanceSettings>> = _instanceSettings

    private val _settings = MutableStateFlow(AvatarSettings())
    val settings: Flow<AvatarSettings> = _settings

    suspend fun switchAvatar(modelId: String) {}

    suspend fun updateAvatarSettings(avatarId: String, settings: AvatarInstanceSettings) {}

    fun updateAvatarEmotionAnimationMapping(avatarId: String, mapping: Map<AvatarEmotion, String>) {}

    fun updateAvatarMoodAnimationMapping(avatarId: String, mapping: Map<String, String>) {}

    fun updateAvatarMoodConfig(avatarId: String, definitions: List<AvatarCustomMoodDefinition>, mapping: Map<String, String>) {}

    fun updateVoiceCallAvatarEnabled(enabled: Boolean) {}

    suspend fun deleteAvatar(modelId: String): Boolean = false

    suspend fun renameAvatar(modelId: String, newName: String): Boolean = false

    suspend fun importAvatarFromUri(uri: Uri): Boolean = false

    companion object {
        fun getInstance(): AvatarRepository = AvatarRepository()
    }
}
