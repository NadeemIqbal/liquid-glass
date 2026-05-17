package io.github.nadeemiqbal.liquidglass

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

/**
 * Android API 31+: chain blur + saturation `ColorMatrix` into a single `RenderEffect` so the
 * GraphicsLayer composites both in one shader pass. On API < 31, `BlurEffect` itself is already
 * unavailable, so we return `null` and let the Fallback tier path render a flat tint instead.
 */
internal actual fun chainedGlassRenderEffect(
    blurRadiusPx: Float,
    saturation: Float,
): RenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val saturationMatrix = ColorMatrix().apply { setSaturation(saturation) }
    val saturationEffect = AndroidRenderEffect.createColorFilterEffect(
        ColorMatrixColorFilter(saturationMatrix),
    )
    val blurEffect = AndroidRenderEffect.createBlurEffect(
        blurRadiusPx,
        blurRadiusPx,
        Shader.TileMode.CLAMP,
    )
    // Order: saturation first, then blur — matches Compose's default two-step pipeline where
    // colorFilter is applied to the source layer first and renderEffect blurs the filtered result.
    val chained = AndroidRenderEffect.createChainEffect(blurEffect, saturationEffect)
    return chained.asComposeRenderEffect()
}

internal actual fun composeRenderEffects(
    outer: RenderEffect?,
    inner: RenderEffect?,
): RenderEffect? {
    if (outer == null) return inner
    if (inner == null) return outer
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return outer
    val chained = AndroidRenderEffect.createChainEffect(
        outer.asAndroidRenderEffect(),
        inner.asAndroidRenderEffect(),
    )
    return chained.asComposeRenderEffect()
}
