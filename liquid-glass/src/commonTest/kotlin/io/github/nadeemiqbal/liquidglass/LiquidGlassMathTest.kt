package io.github.nadeemiqbal.liquidglass

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquidGlassMathTest {

    @Test
    fun saturation_one_is_identity() {
        val m = LiquidGlassMath.saturationMatrix(1f).values
        // R row maps to (1, 0, 0, 0, 0); G to (0, 1, 0, 0, 0); B to (0, 0, 1, 0, 0); A to identity.
        assertCloseTo(1f, m[0]); assertCloseTo(0f, m[1]); assertCloseTo(0f, m[2])
        assertCloseTo(0f, m[5]); assertCloseTo(1f, m[6]); assertCloseTo(0f, m[7])
        assertCloseTo(0f, m[10]); assertCloseTo(0f, m[11]); assertCloseTo(1f, m[12])
        assertCloseTo(1f, m[18])
    }

    @Test
    fun saturation_zero_collapses_to_luminance() {
        val m = LiquidGlassMath.saturationMatrix(0f).values
        // Each RGB output row should be (lumaR, lumaG, lumaB, 0, 0).
        for (rowStart in intArrayOf(0, 5, 10)) {
            assertCloseTo(0.2126f, m[rowStart])
            assertCloseTo(0.7152f, m[rowStart + 1])
            assertCloseTo(0.0722f, m[rowStart + 2])
            assertCloseTo(0f, m[rowStart + 3])
            assertCloseTo(0f, m[rowStart + 4])
        }
    }

    @Test
    fun saturation_one_point_four_boosts_diagonal() {
        val m = LiquidGlassMath.saturationMatrix(1.4f).values
        // Diagonal entries should be > 1.0 (chroma boost), off-diagonals should be negative.
        assertTrue(m[0] > 1f, "R-R should be boosted, got ${m[0]}")
        assertTrue(m[6] > 1f, "G-G should be boosted, got ${m[6]}")
        assertTrue(m[12] > 1f, "B-B should be boosted, got ${m[12]}")
        assertTrue(m[1] < 0f, "R-G should be negative, got ${m[1]}")
    }

    private fun assertCloseTo(expected: Float, actual: Float, eps: Float = 1e-5f) {
        assertTrue(
            abs(expected - actual) < eps,
            "expected $expected ± $eps, got $actual",
        )
    }
}
