package io.github.nadeemiqbal.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Shared state between a `Modifier.liquidGlassSource` (the backdrop) and one or more
 * `Modifier.liquidGlass` (the glass surfaces drawn on top).
 *
 * Carries the [GraphicsLayer] that records the backdrop pixels and the [quality] tier that
 * decides how the surfaces sample it. When [quality] is [LiquidGlassQuality.Fallback], [backdrop]
 * is `null` — no offscreen layer is allocated.
 *
 * Construct with [rememberLiquidGlassState].
 */
class LiquidGlassState internal constructor(
    val quality: LiquidGlassQuality,
    val backdrop: GraphicsLayer?,
    val downsample: Float,
)

/**
 * Remembers a [LiquidGlassState] keyed to [quality].
 *
 * Pass an explicit [quality] to override the platform default — useful for matching brand
 * preferences (e.g. always [LiquidGlassQuality.Full] on a flagship-only app) or to demo the
 * tiers in a settings screen.
 *
 * For [LiquidGlassQuality.Fallback] no [GraphicsLayer] is allocated; the state still drives the
 * tint + sheen path so call sites do not need to branch.
 */
@Composable
fun rememberLiquidGlassState(
    quality: LiquidGlassQuality = rememberPlatformLiquidGlassQuality(),
): LiquidGlassState {
    val tier = LiquidGlassDefaults.forQuality(quality)
    val layer = if (quality == LiquidGlassQuality.Fallback) null else rememberGraphicsLayer()
    return remember(quality, layer) {
        LiquidGlassState(quality = quality, backdrop = layer, downsample = tier.downsampleFactor)
    }
}
