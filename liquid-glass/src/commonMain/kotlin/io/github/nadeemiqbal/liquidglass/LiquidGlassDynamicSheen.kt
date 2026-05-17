package io.github.nadeemiqbal.liquidglass

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

/**
 * Returns a [Brush] for the edge sheen whose color is sampled from the captured backdrop
 * — every [sampleIntervalMs] the library snapshots the backdrop graphics layer, averages its
 * pixels, and produces a top-down gradient from that color. Pass the result to the
 * `borderHighlight` parameter of `Modifier.liquidGlass` / `GlassCard` / `GlassButton`.
 *
 * Falls back to the static [fallback] brush when:
 * - The quality tier is [LiquidGlassQuality.Fallback] (no backdrop layer to sample).
 * - The platform's `GraphicsLayer.toImageBitmap()` throws (most likely on `wasmJs` —
 *   the library catches once and stops polling).
 *
 * @param state The shared liquid-glass state.
 * @param sampleIntervalMs Milliseconds between samples. 500 ms is the iOS-like default;
 *   reduce for snappier color tracking, raise to save battery.
 * @param fallback Brush returned when sampling can't run.
 */
@Composable
fun rememberDynamicSheen(
    state: LiquidGlassState,
    sampleIntervalMs: Long = 500,
    fallback: Brush = LiquidGlassDefaults.borderBrush(),
): Brush {
    val layer = state.backdrop
    if (state.quality == LiquidGlassQuality.Fallback || layer == null) return fallback

    var averagedColor by remember(state) { mutableStateOf<Color?>(null) }
    var samplingFailed by remember(state) { mutableStateOf(false) }

    LaunchedEffect(state, sampleIntervalMs) {
        while (!samplingFailed) {
            delay(sampleIntervalMs)
            try {
                if (layer.size.width <= 0 || layer.size.height <= 0) continue
                val bitmap = layer.toImageBitmap()
                averagedColor = LiquidGlassMath.averageColor(bitmap)
            } catch (e: Throwable) {
                samplingFailed = true
                break
            }
        }
    }

    val color = averagedColor ?: return fallback
    val animated by animateColorAsState(targetValue = color, animationSpec = tween(durationMillis = 300))
    return Brush.verticalGradient(
        0.0f to animated.copy(alpha = 0.55f),
        1.0f to animated.copy(alpha = 0.05f),
    )
}
