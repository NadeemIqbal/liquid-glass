package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.ImageFilter

/**
 * Skia-backed targets (Desktop, iOS, Web): no chained-effect optimization is exposed.
 * Compose's default path of `layer.renderEffect = BlurEffect(...)` + `layer.colorFilter = ...`
 * already maps to two Skia `ImageFilter` passes, which is what Skia would do anyway. Return
 * `null` so `Modifier.liquidGlass` falls back to the standard two-step pipeline.
 */
internal actual fun chainedGlassRenderEffect(
    blurRadiusPx: Float,
    saturation: Float,
): RenderEffect? = null

internal actual fun composeRenderEffects(
    outer: RenderEffect?,
    inner: RenderEffect?,
): RenderEffect? {
    if (outer == null) return inner
    if (inner == null) return outer
    val composed = ImageFilter.makeCompose(
        outer = outer.asSkiaImageFilter(),
        inner = inner.asSkiaImageFilter(),
    )
    return composed.asComposeRenderEffect()
}
