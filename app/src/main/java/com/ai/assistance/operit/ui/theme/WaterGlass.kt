package com.ai.assistance.operit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalWaterGlassState = compositionLocalOf<Any?> { null }

fun isWaterGlassSupported(): Boolean = false

@Composable
fun Modifier.waterGlass(
    enabled: Boolean,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    containerColor: androidx.compose.ui.graphics.Color,
    shadowElevation: androidx.compose.ui.unit.Dp = 14.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    overlayAlphaBoost: Float = 0f,
): Modifier = this
