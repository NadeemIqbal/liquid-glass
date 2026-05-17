package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-tier defaults used by `Modifier.liquidGlass` and the `Glass*` composables.
 *
 * Override individual parameters at the call site to customize per-surface; the tier defaults
 * are the sensible starting point that auto-scale with [LiquidGlassQuality].
 */
object LiquidGlassDefaults {

    /** Default glass shape — rounded rectangle, matches iOS card corner radius. */
    val shape: Shape = RoundedCornerShape(20.dp)

    /** Tint for use over light backgrounds. Semi-transparent white. */
    val lightTint: Color = Color(0x4DFFFFFF)

    /** Tint for use over dark backgrounds. Semi-transparent black. */
    val darkTint: Color = Color(0x33000000)

    /** Picks the right tint for the current theme. */
    fun tintFor(isDark: Boolean): Color = if (isDark) darkTint else lightTint

    /** Near-opaque white surface for use over light backgrounds. */
    val lightOpaqueTint: Color = Color(0xF2FFFFFF)

    /** Near-opaque dark surface for use over dark backgrounds. */
    val darkOpaqueTint: Color = Color(0xF21A1A1A)

    /**
     * Near-opaque tint suitable for stand-alone glass surfaces that have no host-composition
     * backdrop to sample — typically the inside of a [GlassDialog] or [GlassBottomSheet] where
     * the blur step is a no-op. Use [tintFor] for surfaces that DO sit over a blurred backdrop
     * (the regular tint is intentionally transparent so the blurred content shows through).
     */
    fun opaqueTintFor(isDark: Boolean): Color = if (isDark) darkOpaqueTint else lightOpaqueTint

    /**
     * Default edge-sheen brush — a top-down white gradient that gives the surface a subtle
     * specular highlight along its border. Use the [Brush] as the `borderHighlight` parameter
     * on `Modifier.liquidGlass`.
     */
    fun borderBrush(): Brush =
        Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = 0.5f),
            1.0f to Color.White.copy(alpha = 0.05f),
        )

    /** Per-quality-tier defaults. */
    data class Tier(
        val blurRadius: Dp,
        val saturation: Float,
        val downsampleFactor: Float,
    )

    private val full = Tier(blurRadius = 24.dp, saturation = 1.4f, downsampleFactor = 1.0f)
    private val medium = Tier(blurRadius = 16.dp, saturation = 1.2f, downsampleFactor = 0.5f)
    private val fallback = Tier(blurRadius = 0.dp, saturation = 1.0f, downsampleFactor = 1.0f)

    /** Returns the [Tier] for [quality]. */
    fun forQuality(quality: LiquidGlassQuality): Tier = when (quality) {
        LiquidGlassQuality.Full -> full
        LiquidGlassQuality.Medium -> medium
        LiquidGlassQuality.Fallback -> fallback
    }
}
