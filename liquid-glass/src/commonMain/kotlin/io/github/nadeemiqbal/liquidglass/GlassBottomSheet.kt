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
import androidx.compose.ui.unit.dp

/**
 * Material 3 `ModalBottomSheet` whose surface is a liquid-glass card.
 *
 * Same caveat as [GlassDialog]: bottom sheets paint their own scrim and content area in a
 * separate composition layer, so backdrop sampling of the host composition is not possible.
 * The sheet renders **[tint] + edge sheen + (optional) [grain]** only — no blur, no chroma
 * lift, no refraction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = LiquidGlassDefaults.shape,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
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
        // Fallback-quality state: same reasoning as GlassDialog — there is no backdrop to
        // sample inside the sheet's own composition window, and applying `liquidGlassSource`
        // on the same node as `liquidGlass` would recurse.
        val state = rememberLiquidGlassState(LiquidGlassQuality.Fallback)
        Column(
            modifier
                .fillMaxWidth()
                .liquidGlass(
                    state = state,
                    shape = shape,
                    tint = tint,
                    borderHighlight = borderHighlight,
                    grain = grain,
                    grainSeed = grainSeed,
                ),
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}
