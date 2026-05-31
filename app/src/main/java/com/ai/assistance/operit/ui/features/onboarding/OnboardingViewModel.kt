package com.ai.assistance.operit.ui.features.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ai.assistance.operit.api.chat.EnhancedAIService
import com.ai.assistance.operit.core.tools.system.AndroidPermissionLevel
import com.ai.assistance.operit.core.tools.system.OperitTerminalManager
import com.ai.assistance.operit.data.model.ApiProviderType
import com.ai.assistance.operit.data.model.FunctionType
import com.ai.assistance.operit.data.preferences.DisplayPreferencesManager
import com.ai.assistance.operit.data.preferences.FunctionalConfigManager
import com.ai.assistance.operit.data.preferences.GitHubAuthPreferences
import com.ai.assistance.operit.data.preferences.ModelConfigManager
import com.ai.assistance.operit.data.preferences.ThemeStyle
import com.ai.assistance.operit.data.preferences.androidPermissionPreferences
import com.ai.assistance.operit.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep(val index: Int) {
    SELF_INTRODUCTION(0),
    GITHUB_LOGIN(1),
    THEME_SELECTION(2),
    DEEPSEEK_API(3),
    ZHIPU_API(4),
    PERMISSION_GRANT(5),
    COMPLETION(6)
}

data class OnboardingMessage(
    val id: String,
    val text: String,
    val isFromAi: Boolean = true,
    val cardType: CardType? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class CardType {
    GITHUB_LOGIN,
    THEME_SELECTION,
    DEEPSEEK_API,
    ZHIPU_API,
    PERMISSION_GRANT
}

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.SELF_INTRODUCTION,
    val messages: List<OnboardingMessage> = emptyList(),
    val isSaving: Boolean = false,
    val isTerminalInstalled: Boolean = false,
    val isGitHubLoggedIn: Boolean = false,
    val selectedTheme: ThemeStyle = ThemeStyle.BRIGHT,
    val hasDeepSeekApiKey: Boolean = false,
    val hasZhipuApiKey: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasBatteryOptimizationExemption: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val hasNotificationListenerPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel(private val context: Context) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingVM"
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_CURRENT_STEP = "current_step"

        fun isOnboardingCompleted(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        }

        fun getCurrentStepIndex(context: Context): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getInt(KEY_CURRENT_STEP, 0)
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val displayPreferencesManager = DisplayPreferencesManager.getInstance(context)
    private val modelConfigManager = ModelConfigManager(context)
    private val functionalConfigManager = FunctionalConfigManager(context)
    private val githubAuthPreferences = GitHubAuthPreferences.getInstance(context)

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val isTerminalInstalled = OperitTerminalManager.isInstalled(context)
            val isGitHubLoggedIn = githubAuthPreferences.isLoggedIn()
            _uiState.update { it.copy(isTerminalInstalled = isTerminalInstalled, isGitHubLoggedIn = isGitHubLoggedIn) }
        }
        restoreStep()
    }

    private fun restoreStep() {
        val savedStepIndex = prefs.getInt(KEY_CURRENT_STEP, 0)
        val step = OnboardingStep.entries.find { it.index == savedStepIndex } ?: OnboardingStep.SELF_INTRODUCTION
        if (step != OnboardingStep.SELF_INTRODUCTION) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    private fun saveCurrentStep(step: OnboardingStep) {
        prefs.edit().putInt(KEY_CURRENT_STEP, step.index).apply()
    }

    fun startOnboarding() {
        addAiMessage("你好！我是 Operit，你的 AI 助手。我可以帮你完成各种任务：对话、文件管理、代码编写、手机操控等等。接下来我会引导你完成一些基本配置，让我能更好地为你服务。")
        advanceToStep(OnboardingStep.GITHUB_LOGIN)
    }

    private fun addAiMessage(text: String, cardType: CardType? = null) {
        val message = OnboardingMessage(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            isFromAi = true,
            cardType = cardType
        )
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun addUserMessage(text: String) {
        val message = OnboardingMessage(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            isFromAi = false
        )
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun advanceToStep(step: OnboardingStep) {
        _uiState.update { it.copy(currentStep = step) }
        saveCurrentStep(step)

        when (step) {
            OnboardingStep.GITHUB_LOGIN -> {
                addAiMessage("你可以登录 GitHub 来使用代码仓库相关功能。如果你暂时不需要，也可以跳过。", CardType.GITHUB_LOGIN)
            }
            OnboardingStep.THEME_SELECTION -> {
                addAiMessage("选择一个你喜欢的主题风格吧！这会影响整个应用的配色方案。", CardType.THEME_SELECTION)
            }
            OnboardingStep.DEEPSEEK_API -> {
                addAiMessage("接下来配置 DeepSeek API。DeepSeek 提供强大的 AI 对话能力，建议只充值 1 元即可体验。如果暂时不需要，可以跳过。", CardType.DEEPSEEK_API)
            }
            OnboardingStep.ZHIPU_API -> {
                addAiMessage("智谱 API 是必配项，因为 AutoGLM 手机操控功能依赖它。好消息是，智谱提供免费模型，无需充值！\n\n免费模型列表：\n- GLM-4.7-Flash\n- GLM-4.6V-Flash\n- GLM-4V-Flash\n- CogView-3-Flash\n- CogVideoX-Flash", CardType.ZHIPU_API)
            }
            OnboardingStep.PERMISSION_GRANT -> {
                addAiMessage("最后一步！授权一些权限让我能更好地工作。部分权限可以跳过，但建议尽量授权。", CardType.PERMISSION_GRANT)
            }
            OnboardingStep.COMPLETION -> {
                showCompletionMessage()
            }
            OnboardingStep.SELF_INTRODUCTION -> {}
        }
    }

    fun skipGitHubLogin() {
        addUserMessage("暂时跳过 GitHub 登录")
        advanceToStep(OnboardingStep.THEME_SELECTION)
    }

    fun onGitHubLoginSuccess() {
        addUserMessage("GitHub 登录成功！")
        _uiState.update { it.copy(isGitHubLoggedIn = true) }
        advanceToStep(OnboardingStep.THEME_SELECTION)
    }

    fun selectTheme(themeStyle: ThemeStyle) {
        _uiState.update { it.copy(selectedTheme = themeStyle) }
        viewModelScope.launch {
            displayPreferencesManager.saveDisplaySettings(themeStyle = themeStyle)
        }
        addUserMessage("已选择${if (themeStyle == ThemeStyle.BRIGHT) "明快" else "温暖"}主题")
        advanceToStep(OnboardingStep.DEEPSEEK_API)
    }

    fun skipDeepSeekApi() {
        addUserMessage("暂时跳过 DeepSeek API 配置")
        advanceToStep(OnboardingStep.ZHIPU_API)
    }

    fun saveDeepSeekApiKey(apiKey: String) {
        if (apiKey.isBlank()) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                modelConfigManager.initializeIfNeeded()
                functionalConfigManager.initializeIfNeeded()
                val summaries = modelConfigManager.getAllConfigSummaries()
                val existing = summaries.find { it.name == "DeepSeek" }
                if (existing != null) {
                    modelConfigManager.updateModelConfig(
                        existing.id,
                        apiKey,
                        "https://api.deepseek.com/v1/chat/completions",
                        "deepseek-v4-flash",
                        ApiProviderType.DEEPSEEK
                    )
                    functionalConfigManager.setConfigForFunction(FunctionType.CHAT, existing.id)
                } else {
                    val configId = modelConfigManager.createConfig("DeepSeek")
                    modelConfigManager.updateModelConfig(
                        configId,
                        apiKey,
                        "https://api.deepseek.com/v1/chat/completions",
                        "deepseek-v4-flash",
                        ApiProviderType.DEEPSEEK
                    )
                    functionalConfigManager.setConfigForFunction(FunctionType.CHAT, configId)
                }
                EnhancedAIService.refreshAllServices(context)
                _uiState.update { it.copy(isSaving = false, hasDeepSeekApiKey = true) }
                addUserMessage("DeepSeek API Key 已配置")
                advanceToStep(OnboardingStep.ZHIPU_API)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save DeepSeek API key", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun saveZhipuApiKey(apiKey: String) {
        if (apiKey.isBlank()) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                modelConfigManager.initializeIfNeeded()
                functionalConfigManager.initializeIfNeeded()
                val summaries = modelConfigManager.getAllConfigSummaries()
                val existing = summaries.find { it.name == "Zhipu" }
                if (existing != null) {
                    modelConfigManager.updateModelConfig(
                        existing.id,
                        apiKey,
                        "https://open.bigmodel.cn/api/paas/v4/chat/completions",
                        "glm-4-flash",
                        ApiProviderType.ZHIPU
                    )
                    functionalConfigManager.setConfigForFunction(FunctionType.UI_CONTROLLER, existing.id)
                } else {
                    val configId = modelConfigManager.createConfig("Zhipu")
                    modelConfigManager.updateModelConfig(
                        configId,
                        apiKey,
                        "https://open.bigmodel.cn/api/paas/v4/chat/completions",
                        "glm-4-flash",
                        ApiProviderType.ZHIPU
                    )
                    functionalConfigManager.setConfigForFunction(FunctionType.UI_CONTROLLER, configId)
                }
                EnhancedAIService.refreshAllServices(context)
                _uiState.update { it.copy(isSaving = false, hasZhipuApiKey = true) }
                addUserMessage("智谱 API Key 已配置")
                advanceToStep(OnboardingStep.PERMISSION_GRANT)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save Zhipu API key", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
            }
        }
    }

    fun checkPermissions() {
        val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        val hasBatteryOptimizationExemption = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else true

        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        val hasNotificationListenerPermission = isNotificationListenerEnabled()

        val hasAccessibilityPermission = isAccessibilityEnabled()

        _uiState.update {
            it.copy(
                hasStoragePermission = hasStoragePermission,
                hasOverlayPermission = hasOverlayPermission,
                hasBatteryOptimizationExemption = hasBatteryOptimizationExemption,
                hasLocationPermission = hasLocationPermission,
                hasNotificationListenerPermission = hasNotificationListenerPermission,
                hasAccessibilityPermission = hasAccessibilityPermission
            )
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        return try {
            val cn = android.content.ComponentName(context, com.ai.assistance.operit.services.notification.OperitNotificationListenerService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            flat != null && flat.contains(cn.flattenToString())
        } catch (e: Exception) {
            false
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        return try {
            val cn = android.content.ComponentName(context, com.ai.assistance.operit.OperitAccessibilityService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            flat != null && flat.contains(cn.flattenToString())
        } catch (e: Exception) {
            false
        }
    }

    fun completePermissions() {
        val state = _uiState.value
        val permissionLevel = if (state.hasAccessibilityPermission) {
            AndroidPermissionLevel.ACCESSIBILITY
        } else {
            AndroidPermissionLevel.STANDARD
        }
        viewModelScope.launch {
            androidPermissionPreferences.savePreferredPermissionLevel(permissionLevel)
        }
        addUserMessage("权限配置完成")
        advanceToStep(OnboardingStep.COMPLETION)
    }

    private fun showCompletionMessage() {
        val state = _uiState.value
        val message = when {
            state.isTerminalInstalled && state.hasZhipuApiKey && state.hasDeepSeekApiKey -> {
                "其他项目我已经帮你配置完成了，现在可以直接和我对话，使用我的全部能力了！"
            }
            state.isTerminalInstalled && state.hasZhipuApiKey && !state.hasDeepSeekApiKey -> {
                "基本配置已经完成了！目前你使用的是智谱的免费模型。如果后续需要更强大的对话能力，可以在设置中配置DeepSeek API。现在就可以开始和我对话了！"
            }
            else -> {
                "我的一些能力还没有完成配置，但是大部分功能已经可以稳定使用了。可以先开始和我对话体验一下！"
            }
        }
        addAiMessage(message)
        completeOnboarding()
    }

    private fun completeOnboarding() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .putInt(KEY_CURRENT_STEP, OnboardingStep.COMPLETION.index)
            .apply()
        _uiState.update { it.copy(isCompleted = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(context.applicationContext) as T
        }
    }
}
