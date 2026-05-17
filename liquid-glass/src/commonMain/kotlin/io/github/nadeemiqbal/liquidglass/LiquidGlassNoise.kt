package io.github.nadeemiqbal.liquidglass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.toPixelMap
import kotlin.random.Random

/**
 * Procedural noise tile used by `Modifier.liquidGlass(grain = ...)`.
 *
 * Generates a deterministic grayscale `ImageBitmap` from [seed], remembered across recompositions.
 * Drawn tiled (via `TileMode.Repeated`) over the glass surface with alpha = `grain`, the result is
 * the subtle frosted-grain texture iOS 26 uses to break up flat tints. Zero-cost when `grain == 0`.
 *
 * @param seed RNG seed. Same seed → identical tile across runs, useful when you want the same
 *   noise pattern across multiple glass surfaces.
 * @param size Tile edge length in pixels. 64 is the default and is invisible at typical glass-card
 *   sizes; smaller tiles repeat more frequently and look more uniform.
 */
@Composable
fun rememberGlassNoiseTile(seed: Long = 0L, size: Int = 64): ImageBitmap {
    return remember(seed, size) { generateNoiseTile(seed, size) }
}

internal fun generateNoiseTile(seed: Long, size: Int): ImageBitmap {
    require(size > 0) { "noise tile size must be > 0, got $size" }
    val rng = Random(seed)
    val bitmap = ImageBitmap(size, size, config = ImageBitmapConfig.Argb8888, hasAlpha = false)
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    val paint = androidx.compose.ui.graphics.Paint()
    for (y in 0 until size) {
        for (x in 0 until size) {
            val v = rng.nextFloat()
            paint.color = Color(v, v, v, 1f)
            canvas.drawRect(
                left = x.toFloat(),
                top = y.toFloat(),
                right = (x + 1).toFloat(),
                bottom = (y + 1).toFloat(),
                paint = paint,
            )
        }
    }
    return bitmap
}

/** Pure-logic accessor used by tests: returns the first pixel of a freshly-generated tile. */
internal fun firstPixelOfNoiseTile(seed: Long, size: Int = 64): Color =
    generateNoiseTile(seed, size).toPixelMap(0, 0, 1, 1)[0, 0]
