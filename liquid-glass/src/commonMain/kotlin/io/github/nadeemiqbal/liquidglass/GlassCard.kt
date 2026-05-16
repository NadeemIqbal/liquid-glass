package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A simple card whose background is a liquid-glass surface — the rest of your composition
 * shows through it with backdrop blur, chroma lift and an edge sheen.
 *
 * Wraps a [Box] with [Modifier.liquidGlass] for the visual effect plus a customizable
 * [contentPadding] for the children.
 */
@Composable
fun GlassCard(
    state: LiquidGlassState,
    modifier: Modifier = Modifier,
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(
            state = state,
            shape = shape,
            blurRadius = blurRadius,
            saturation = saturation,
            tint = tint,
            borderHighlight = borderHighlight,
        ),
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}
