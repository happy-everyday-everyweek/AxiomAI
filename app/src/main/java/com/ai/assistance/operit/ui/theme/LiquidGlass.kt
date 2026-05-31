package com.ai.assistance.operit.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalLiquidGlassBackdrop = compositionLocalOf<Any?> { null }

fun isLiquidGlassSupported(): Boolean = false

@Composable
fun Modifier.liquidGlass(
    enabled: Boolean,
    shape: androidx.compose.foundation.shape.CornerBasedShape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
    containerColor: androidx.compose.ui.graphics.Color,
    shadowElevation: androidx.compose.ui.unit.Dp = 14.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    blurRadius: androidx.compose.ui.unit.Dp = 10.dp,
    overlayAlphaBoost: Float = 0f,
    enableLens: Boolean = true,
): Modifier = this
