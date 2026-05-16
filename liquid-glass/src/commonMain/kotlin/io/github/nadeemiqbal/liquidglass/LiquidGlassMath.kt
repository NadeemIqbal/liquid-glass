package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.graphics.ColorMatrix

/** Pure-logic helpers used by the glass modifier. Kept testable in `commonTest`. */
internal object LiquidGlassMath {

    private const val LUMA_R = 0.2126f
    private const val LUMA_G = 0.7152f
    private const val LUMA_B = 0.0722f

    /**
     * Builds a saturation [ColorMatrix].
     *
     * - `s == 1.0` → identity (no change)
     * - `s == 0.0` → fully desaturated (Rec.709 luminance)
     * - `s > 1.0` → boosted chroma — this is what the "liquid glass" look uses (typically 1.2–1.4)
     */
    fun saturationMatrix(s: Float): ColorMatrix {
        val invS = 1f - s
        val r = invS * LUMA_R
        val g = invS * LUMA_G
        val b = invS * LUMA_B
        return ColorMatrix(
            floatArrayOf(
                r + s, g,     b,     0f, 0f,
                r,     g + s, b,     0f, 0f,
                r,     g,     b + s, 0f, 0f,
                0f,    0f,    0f,    1f, 0f,
            ),
        )
    }
}
