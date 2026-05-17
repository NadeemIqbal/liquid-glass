package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 `ModalBottomSheet` whose surface is a liquid-glass card.
 *
 * Hosts its own `LiquidGlassState`. As with `GlassDialog`, the glass effect samples content
 * **inside the sheet's own composition**, not the host activity — `ModalBottomSheet` paints its
 * own scrim and content area, and the glass blurs whatever lives inside the sheet bounds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    quality: LiquidGlassQuality = rememberPlatformLiquidGlassQuality(),
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.32f),
    ) {
        val state = rememberLiquidGlassState(quality)
        Column(
            modifier
                .fillMaxWidth()
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
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}
