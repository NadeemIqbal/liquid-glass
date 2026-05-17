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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Material 3-style dialog whose surface is a liquid-glass card.
 *
 * **Limitation: no backdrop blur.** Dialogs render in a separate system overlay window
 * (Android) or window-level layer (other platforms), so there is no host-composition content
 * for the glass surface to sample. This composable therefore renders **[tint] + edge sheen +
 * (optional) [grain]** only — no blur, no chroma lift, no refraction. The result still reads
 * as glass against the system scrim that `Dialog` paints behind itself.
 *
 * For "glass blurring my app's content," don't use a `GlassDialog` — place a `GlassCard`
 * directly in the host composition with `Modifier.liquidGlassSource` on the backdrop above it.
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
        // Fallback-quality state: zero offscreen alloc, draws tint + sheen + grain only.
        // Applying `liquidGlassSource` here would recursively record the glass's own draw output
        // (same node is both source and surface), blowing the Skia draw stack — so it's omitted.
        val state = rememberLiquidGlassState(LiquidGlassQuality.Fallback)
        Box(
            modifier.liquidGlass(
                state = state,
                shape = shape,
                tint = tint,
                borderHighlight = borderHighlight,
                grain = grain,
                grainSeed = grainSeed,
            ),
        ) {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}
