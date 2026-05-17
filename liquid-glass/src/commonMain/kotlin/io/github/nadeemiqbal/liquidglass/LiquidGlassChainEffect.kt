package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.graphics.RenderEffect

/**
 * Returns a chained `RenderEffect` that performs the saturation chroma boost AND the blur in a
 * single shader pass. Used by `Modifier.liquidGlass` to skip the extra `layer.colorFilter` round
 * trip when the platform supports chaining cheaply.
 *
 * Returns `null` when chaining isn't worthwhile (e.g. Skia targets where the two-step path is
 * what Skia would emit internally anyway, or pre-API-31 Android where `RenderEffect.createChainEffect`
 * doesn't exist). In that case `Modifier.liquidGlass` falls back to `layer.renderEffect = BlurEffect(...)`
 * + `layer.colorFilter = ColorFilter.colorMatrix(saturationMatrix)`.
 */
internal expect fun chainedGlassRenderEffect(
    blurRadiusPx: Float,
    saturation: Float,
): RenderEffect?

/**
 * Composes two `RenderEffect`s into one. [outer] is applied to the result of [inner] — i.e.
 * `outer(inner(pixels))`. Used by `Modifier.liquidGlass` to chain refraction (inner) → blur (outer)
 * so both effects apply.
 *
 * Returns `outer ?: inner` when only one side is non-null.
 * Returns the platform-specific chained effect when both are non-null:
 * - **Android** API 31+: `RenderEffect.createChainEffect(outer, inner)`.
 * - **Skia targets**: `org.jetbrains.skia.ImageFilter.makeCompose(outer, inner)`.
 * - **Android pre-31**: falls back to [outer] alone (refraction is gated to API 33+ anyway).
 */
internal expect fun composeRenderEffects(
    outer: RenderEffect?,
    inner: RenderEffect?,
): RenderEffect?
