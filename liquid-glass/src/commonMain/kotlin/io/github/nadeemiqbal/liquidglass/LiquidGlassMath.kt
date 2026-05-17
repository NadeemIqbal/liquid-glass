package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap

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
    /**
     * Returns the average color of [bitmap] sampled on an [downsampleGrid]×[downsampleGrid]
     * uniform grid. Cheap (256 pixel reads at the default 16×16 grid) and sufficient for driving
     * a tint or sheen brush. Returns [Color.Transparent] if the bitmap is empty.
     */
    fun averageColor(bitmap: ImageBitmap, downsampleGrid: Int = 16): Color {
        if (bitmap.width <= 0 || bitmap.height <= 0) return Color.Transparent
        val pixelMap = bitmap.toPixelMap()
        val grid = downsampleGrid.coerceAtLeast(1)
        var r = 0f
        var g = 0f
        var b = 0f
        var count = 0
        for (gy in 0 until grid) {
            for (gx in 0 until grid) {
                val px = (gx.toFloat() / grid * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
                val py = (gy.toFloat() / grid * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                val c = pixelMap[px, py]
                r += c.red
                g += c.green
                b += c.blue
                count++
            }
        }
        if (count == 0) return Color.Transparent
        return Color(r / count, g / count, b / count, 1f)
    }

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
