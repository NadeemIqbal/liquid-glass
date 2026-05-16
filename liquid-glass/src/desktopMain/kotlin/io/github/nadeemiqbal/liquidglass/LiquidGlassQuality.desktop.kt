package io.github.nadeemiqbal.liquidglass

import androidx.compose.runtime.Composable

/** Desktop is always [LiquidGlassQuality.Full] — override manually if you need to downgrade. */
@Composable
actual fun rememberPlatformLiquidGlassQuality(): LiquidGlassQuality = LiquidGlassQuality.Full
