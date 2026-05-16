package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquidGlassDefaultsTest {

    @Test
    fun full_tier_has_full_blur_and_chroma_boost() {
        val tier = LiquidGlassDefaults.forQuality(LiquidGlassQuality.Full)
        assertEquals(24.dp, tier.blurRadius)
        assertEquals(1.4f, tier.saturation)
        assertEquals(1.0f, tier.downsampleFactor)
    }

    @Test
    fun medium_tier_is_downsampled_and_softer() {
        val tier = LiquidGlassDefaults.forQuality(LiquidGlassQuality.Medium)
        assertEquals(16.dp, tier.blurRadius)
        assertEquals(1.2f, tier.saturation)
        assertEquals(0.5f, tier.downsampleFactor)
    }

    @Test
    fun fallback_tier_has_zero_blur_and_no_chroma_boost() {
        val tier = LiquidGlassDefaults.forQuality(LiquidGlassQuality.Fallback)
        assertEquals(0.dp, tier.blurRadius)
        assertEquals(1.0f, tier.saturation)
    }

    @Test
    fun default_shape_is_rounded_rectangle() {
        assertTrue(LiquidGlassDefaults.shape is RoundedCornerShape)
    }

    @Test
    fun tint_alphas_are_nontrivial_in_both_themes() {
        assertTrue(LiquidGlassDefaults.tintFor(isDark = false).alpha > 0f)
        assertTrue(LiquidGlassDefaults.tintFor(isDark = true).alpha > 0f)
    }
}
