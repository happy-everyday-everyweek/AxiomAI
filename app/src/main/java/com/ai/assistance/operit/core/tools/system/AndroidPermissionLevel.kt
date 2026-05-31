package com.ai.assistance.operit.core.tools.system

enum class AndroidPermissionLevel {
    STANDARD,
    ACCESSIBILITY;

    companion object {
        fun fromString(value: String?): AndroidPermissionLevel {
            return when(value?.uppercase()) {
                "STANDARD" -> STANDARD
                "ACCESSIBILITY" -> ACCESSIBILITY
                "BASIC" -> STANDARD
                "ADVANCED" -> ACCESSIBILITY
                "ROOT", "ADMIN", "DEBUGGER", "ADB" -> ACCESSIBILITY
                else -> STANDARD
            }
        }
    }
}
