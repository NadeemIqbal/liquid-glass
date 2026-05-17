package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * `ImageBitmap`-backed math tests live in `skikoTest` because the Android plain-JVM unit-test
 * runtime doesn't ship a Skia backend — `ImageBitmap`/`Canvas.drawRect` throws at runtime there.
 * Desktop and iOS Skiko runtimes have it.
 */
class LiquidGlassImageMathTest {

    @Test
    fun averageColor_solidRedProducesRed() {
        val bitmap = solidColorBitmap(16, Color(1f, 0f, 0f, 1f))
        val avg = LiquidGlassMath.averageColor(bitmap)
        assertCloseTo(1f, avg.red)
        assertCloseTo(0f, avg.green)
        assertCloseTo(0f, avg.blue)
    }

    @Test
    fun averageColor_solidBlueProducesBlue() {
        val bitmap = solidColorBitmap(16, Color(0.1f, 0.2f, 0.9f, 1f))
        val avg = LiquidGlassMath.averageColor(bitmap)
        assertCloseTo(0.1f, avg.red, eps = 0.02f)
        assertCloseTo(0.2f, avg.green, eps = 0.02f)
        assertCloseTo(0.9f, avg.blue, eps = 0.02f)
    }

    private fun solidColorBitmap(size: Int, color: Color): ImageBitmap {
        val bitmap = ImageBitmap(size, size)
        val canvas = Canvas(bitmap)
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), Paint().apply { this.color = color })
        return bitmap
    }

    private fun assertCloseTo(expected: Float, actual: Float, eps: Float = 1e-4f) {
        assertTrue(
            abs(expected - actual) < eps,
            "expected $expected ± $eps, got $actual",
        )
    }
}
