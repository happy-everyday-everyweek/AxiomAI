package com.ai.assistance.operit.ui.features.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ai.assistance.operit.ui.features.onboarding.components.ApiKeyConfigCard
import com.ai.assistance.operit.ui.features.onboarding.components.GitHubLoginCard
import com.ai.assistance.operit.ui.features.onboarding.components.PermissionGrantCard
import com.ai.assistance.operit.ui.features.onboarding.components.ThemeSelectionCard

@Composable
fun OnboardingChatScreen(
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory(LocalContext.current)),
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            kotlinx.coroutines.delay(1500)
            onComplete()
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.messages.isEmpty()) {
            viewModel.startOnboarding()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LinearProgressIndicator(
            progress = {
                val stepProgress = when (uiState.currentStep) {
                    OnboardingStep.SELF_INTRODUCTION -> 0f
                    OnboardingStep.GITHUB_LOGIN -> 1f / 7f
                    OnboardingStep.THEME_SELECTION -> 2f / 7f
                    OnboardingStep.DEEPSEEK_API -> 3f / 7f
                    OnboardingStep.ZHIPU_API -> 4f / 7f
                    OnboardingStep.PERMISSION_GRANT -> 5f / 7f
                    OnboardingStep.COMPLETION -> 1f
                }
                stepProgress
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = uiState.messages,
                key = { it.id }
            ) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                ) {
                    if (message.isFromAi) {
                        AiMessageBubble(
                            message = message,
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    } else {
                        UserMessageBubble(message = message)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: OnboardingMessage,
    viewModel: OnboardingViewModel,
    uiState: OnboardingUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(6.dp)
                        .size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (message.cardType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OnboardingCard(
                        cardType = message.cardType,
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: OnboardingMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun OnboardingCard(
    cardType: CardType,
    viewModel: OnboardingViewModel,
    uiState: OnboardingUiState
) {
    when (cardType) {
        CardType.GITHUB_LOGIN -> {
            GitHubLoginCard(
                isLoggedIn = uiState.isGitHubLoggedIn,
                onLoginSuccess = { viewModel.onGitHubLoginSuccess() },
                onSkip = { viewModel.skipGitHubLogin() }
            )
        }

        CardType.THEME_SELECTION -> {
            ThemeSelectionCard(
                selectedTheme = uiState.selectedTheme,
                onThemeSelected = { viewModel.selectTheme(it) }
            )
        }

        CardType.DEEPSEEK_API -> {
            ApiKeyConfigCard(
                title = "配置 DeepSeek API",
                description = "DeepSeek 提供强大的 AI 对话能力。建议只充值 1 元即可体验。如果暂时不需要，可以跳过。",
                apiKeyLinkUrl = "https://platform.deepseek.com/api_keys",
                apiKeyLinkText = "前往 DeepSeek 获取 API Key",
                isSkippable = true,
                isSaving = uiState.isSaving,
                isConfigured = uiState.hasDeepSeekApiKey,
                onSave = { viewModel.saveDeepSeekApiKey(it) },
                onSkip = { viewModel.skipDeepSeekApi() }
            )
        }

        CardType.ZHIPU_API -> {
            ApiKeyConfigCard(
                title = "配置智谱 API（必填）",
                description = "智谱 API 是必配项，AutoGLM 手机操控功能依赖它。智谱提供免费模型，无需充值！",
                apiKeyLinkUrl = "https://open.bigmodel.cn/usercenter/apikeys",
                apiKeyLinkText = "前往智谱获取 API Key",
                isSkippable = false,
                isSaving = uiState.isSaving,
                isConfigured = uiState.hasZhipuApiKey,
                onSave = { viewModel.saveZhipuApiKey(it) },
                onSkip = null
            )
        }

        CardType.PERMISSION_GRANT -> {
            PermissionGrantCard(
                hasStoragePermission = uiState.hasStoragePermission,
                hasOverlayPermission = uiState.hasOverlayPermission,
                hasBatteryOptimizationExemption = uiState.hasBatteryOptimizationExemption,
                hasLocationPermission = uiState.hasLocationPermission,
                hasNotificationListenerPermission = uiState.hasNotificationListenerPermission,
                hasAccessibilityPermission = uiState.hasAccessibilityPermission,
                onRefresh = { viewModel.checkPermissions() },
                onComplete = { viewModel.completePermissions() }
            )
        }
    }
}
