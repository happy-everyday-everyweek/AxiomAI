package com.ai.assistance.operit.ui.features.chat.components

sealed interface CharacterSelectorTarget {
    data class CharacterCardTarget(val id: String) : CharacterSelectorTarget
    data class CharacterGroupTarget(val id: String) : CharacterSelectorTarget
}
