package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A top app-bar-shaped glass surface. Pinned to the top of a screen, draws backdrop blur over
 * scrolling content beneath it.
 *
 * Apply this above a [Modifier.liquidGlassSource]-marked scrolling backdrop and pass the same
 * [state] you used for the backdrop. The bar fills the available width, applies [windowInsets]
 * padding (so it does not collide with the status bar), and renders a thin hairline divider
 * along its bottom edge for separation.
 */
@Composable
fun GlassNavBar(
    state: LiquidGlassState,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    height: Dp = 56.dp,
    shape: Shape = RectangleShape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val hairlineColor = Color.Black.copy(alpha = 0.08f)
    Box(
        modifier = modifier
            .fillMaxWidth()
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
            .drawWithCache {
                val strokePx = 0.5.dp.toPx()
                onDrawWithContent {
                    drawContent()
                    drawLine(
                        color = hairlineColor,
                        start = Offset(0f, size.height - strokePx),
                        end = Offset(size.width, size.height - strokePx),
                        strokeWidth = strokePx,
                    )
                }
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                .fillMaxWidth()
                .height(height)
                .padding(contentPadding),
            content = content,
        )
    }
}
