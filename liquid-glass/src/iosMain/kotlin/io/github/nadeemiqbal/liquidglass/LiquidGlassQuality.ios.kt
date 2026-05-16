package io.github.nadeemiqbal.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIDevice

/**
 * iOS resolver based on the system version.
 * - iOS 17+ → [LiquidGlassQuality.Full]
 * - iOS 15–16 → [LiquidGlassQuality.Medium]
 * - iOS < 15 → [LiquidGlassQuality.Fallback]
 */
@Composable
actual fun rememberPlatformLiquidGlassQuality(): LiquidGlassQuality {
    return remember { resolveIosQuality() }
}

private fun resolveIosQuality(): LiquidGlassQuality {
    val majorVersion = UIDevice.currentDevice.systemVersion.substringBefore('.').toIntOrNull() ?: 0
    return when {
        majorVersion >= 17 -> LiquidGlassQuality.Full
        majorVersion >= 15 -> LiquidGlassQuality.Medium
        else -> LiquidGlassQuality.Fallback
    }
}
