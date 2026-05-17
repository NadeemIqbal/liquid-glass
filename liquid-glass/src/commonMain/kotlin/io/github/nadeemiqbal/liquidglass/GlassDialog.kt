package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Material 3-style dialog whose surface looks like an iOS frosted-glass card.
 *
 * **Visual contract.** Dialogs render in a separate system overlay window (Android) or
 * window-level layer (other platforms), so there is no host-composition content for a real
 * blur to sample. This composable therefore paints a near-opaque [tint] (so the dialog reads
 * as a solid sheet, not a see-through ghost), plus the edge sheen and optional [grain]. The
 * default [tint] is [LiquidGlassDefaults.opaqueTintFor] — pass `Color.Unspecified` to keep it,
 * or override for a custom translucency.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = LiquidGlassDefaults.shape,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        val resolvedTint = if (tint == Color.Unspecified) {
            LiquidGlassDefaults.opaqueTintFor(isSystemInDarkTheme())
        } else {
            tint
        }
        // Fallback-quality state: zero offscreen alloc, draws tint + sheen + grain only.
        // Applying `liquidGlassSource` here would recursively record the glass surface's own
        // draw output (same node = both source AND surface) and blow the Skia draw stack.
        val state = rememberLiquidGlassState(LiquidGlassQuality.Fallback)
        Box(
            modifier.liquidGlass(
                state = state,
                shape = shape,
                tint = resolvedTint,
                borderHighlight = borderHighlight,
                grain = grain,
                grainSeed = grainSeed,
            ),
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}
