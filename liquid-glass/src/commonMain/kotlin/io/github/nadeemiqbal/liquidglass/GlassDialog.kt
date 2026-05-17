package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
 * Material 3-style dialog whose surface is a liquid-glass card. Internally hosts its own
 * `LiquidGlassState` — the glass inside the dialog samples whatever sits **inside the dialog's
 * own composition** (typically the system scrim), not the host activity behind it. This is the
 * same limitation any `Modifier.blur`-style backdrop sampling has on Android, where dialogs
 * render in a separate system overlay window.
 *
 * For "glass over my app's content" you don't want a Dialog — use `GlassCard` placed in the host
 * composition instead.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    quality: LiquidGlassQuality = rememberPlatformLiquidGlassQuality(),
    properties: DialogProperties = DialogProperties(),
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        val state = rememberLiquidGlassState(quality)
        Box(
            modifier
                .liquidGlassSource(state)
                .liquidGlass(
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
