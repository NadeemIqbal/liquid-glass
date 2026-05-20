package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Material 3-style dialog whose surface samples the host composition's backdrop for a real
 * frosted-glass effect — same blur + chroma lift + sheen + grain pipeline as `Modifier.liquidGlass`.
 *
 * Pass the SAME [state] that you used in the host with `Modifier.liquidGlassSource(state)`.
 * The dialog content does **not** record into the layer (no recursive recording — the host is
 * still the only source); it only reads from the layer to render its glass surface.
 *
 * **Platform notes.** On iOS the dialog and host live in the same UIWindow, so the shared
 * `GraphicsLayer` is sampled cleanly. On Android the system Dialog opens in a separate window
 * and may not be able to draw a graphics layer recorded in the host window; in practice this
 * shows as an empty/black backdrop inside the dialog. If that's an issue, place a `GlassCard`
 * over a `Popup` (which stays in-window) instead.
 */
@Composable
fun GlassDialog(
    state: LiquidGlassState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Box(
            modifier.liquidGlass(
                state = state,
                shape = shape,
                blurRadius = blurRadius,
                saturation = saturation,
                tint = tint,
                borderHighlight = borderHighlight,
                grain = grain,
                grainSeed = grainSeed,
                refraction = refraction,
            ),
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}
