package io.github.nadeemiqbal.liquidglass

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class LiquidGlassNoiseTest {

    @Test
    fun sameSeedProducesIdenticalFirstPixel() {
        val a = firstPixelOfNoiseTile(seed = 42L)
        val b = firstPixelOfNoiseTile(seed = 42L)
        assertEquals(a, b)
    }

    @Test
    fun differentSeedProducesDifferentFirstPixel() {
        val a = firstPixelOfNoiseTile(seed = 0L)
        val b = firstPixelOfNoiseTile(seed = 12345L)
        assertNotEquals(a, b)
    }

    @Test
    fun pixelsAreGrayscale() {
        // For any tile, the first pixel should be a gray (R == G == B).
        val pixel = firstPixelOfNoiseTile(seed = 7L)
        assertEquals(pixel.red, pixel.green)
        assertEquals(pixel.green, pixel.blue)
        assertTrue(pixel.red in 0f..1f)
    }

    @Test
    fun rejectsZeroSize() {
        assertFails { generateNoiseTile(seed = 0L, size = 0) }
    }
}
