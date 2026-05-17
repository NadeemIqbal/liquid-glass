package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A pill-shaped, tappable liquid-glass surface. Use this where you'd reach for a Material 3
 * `Button`, but want the iOS-26 glass look.
 *
 * Defaults to a fully-rounded shape (`RoundedCornerShape(50%)`) so it reads as a chip / pill;
 * pass a [shape] to use a square card or any other outline.
 */
@Composable
fun GlassButton(
    state: LiquidGlassState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(percent = 50),
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
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
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}
