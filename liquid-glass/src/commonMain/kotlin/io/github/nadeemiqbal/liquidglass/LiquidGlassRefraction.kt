package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RenderEffect

/**
 * Builds a `RenderEffect` that distorts the backdrop with a small sinusoidal pixel offset —
 * the "light bending through glass" feel.
 *
 * Returns `null` when refraction is disabled (`strength <= 0`) or unsupported (Android API < 33).
 * On Skia-backed targets (Desktop, iOS, Web) this uses SkSL via Skiko's `RuntimeEffect`.
 * On Android API 33+ this uses AGSL via `android.graphics.RuntimeShader`.
 *
 * @param strength 0..1. Typical values: 0.2 (subtle), 0.5 (clear ripple), 1.0 (heavy distortion).
 * @param size The size of the surface the effect will be applied to, in pixels. Used to compute
 *   per-fragment UV coordinates inside the shader.
 */
internal expect fun refractionRenderEffect(strength: Float, size: Size): RenderEffect?
