package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertNull

class LiquidGlassRefractionTest {

    @Test
    fun strengthZeroReturnsNull() {
        assertNull(refractionRenderEffect(strength = 0f, size = Size(400f, 300f)))
    }

    @Test
    fun zeroSizeReturnsNull() {
        assertNull(refractionRenderEffect(strength = 0.5f, size = Size.Zero))
    }
}
