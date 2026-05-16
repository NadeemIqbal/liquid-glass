package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Compose UI tests — Skiko-backed, run on Desktop and iOS targets.
 *
 * Tests focus on **state shape** (was a GraphicsLayer allocated? what tier did it pick?) rather
 * than visual pixel verification, since pixel readback differs significantly across Skia
 * backends.
 *
 * Tests marked `@Ignore` exercise the full glass-render pipeline end-to-end. They SIGILL on the
 * Skiko Desktop test runner when `BlurEffect` is chained onto a recorded GraphicsLayer
 * (a known Compose Multiplatform 1.10 + Skiko issue on macOS desktop). They're left here as
 * documentation of intent and to be flipped on once that backend is stable. The same code paths
 * are exercised by the sample app at runtime on every platform.
 */
@OptIn(ExperimentalTestApi::class)
class LiquidGlassUiTest {

    @Test
    fun fallbackQuality_doesNotAllocateBackdropLayer() = runComposeUiTest {
        var capturedState: LiquidGlassState? = null
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Fallback)
                capturedState = state
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1976D2))
                        .liquidGlassSource(state),
                ) {
                    GlassCard(state = state, modifier = Modifier.size(160.dp)) {
                        Text("Fallback glass")
                    }
                }
            }
        }
        waitForIdle()
        val state = assertNotNull(capturedState)
        assertEquals(LiquidGlassQuality.Fallback, state.quality)
        assertNull(state.backdrop, "Fallback must not allocate a backdrop GraphicsLayer")
    }

    @Test
    fun firstFrame_withoutRecordedBackdrop_drawsTintWithoutCrash() = runComposeUiTest {
        // No `liquidGlassSource` upstream — the layer exists but `record { }` is never called,
        // so `layer.size` stays zero. The Modifier should still draw the tint + sheen and the
        // composition must complete cleanly.
        var rendered by mutableStateOf(false)
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Full)
                Box(Modifier.fillMaxSize().background(Color.DarkGray)) {
                    GlassCard(state = state, modifier = Modifier.size(150.dp)) {
                        LaunchedEffect(Unit) { rendered = true }
                        Text("Tint-only")
                    }
                }
            }
        }
        waitForIdle()
        assertTrue(rendered)
    }

    @Test
    fun fullQualityState_allocatesBackdropLayer() = runComposeUiTest {
        var capturedState: LiquidGlassState? = null
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Full)
                capturedState = state
                Box(Modifier.size(50.dp).background(Color.LightGray))
            }
        }
        waitForIdle()
        val state = assertNotNull(capturedState)
        assertNotNull(state.backdrop, "Full must allocate a backdrop GraphicsLayer")
        assertEquals(1.0f, state.downsample)
    }

    @Test
    fun mediumQualityState_allocatesBackdropLayerWithHalfDownsample() = runComposeUiTest {
        var capturedState: LiquidGlassState? = null
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Medium)
                capturedState = state
                Box(Modifier.size(50.dp).background(Color.LightGray))
            }
        }
        waitForIdle()
        val state = assertNotNull(capturedState)
        assertNotNull(state.backdrop)
        assertEquals(0.5f, state.downsample)
    }

    @Ignore
    @Test
    fun glassCard_rendersOverColoredBackground_withoutCrash() = runComposeUiTest {
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Full)
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF3D00))
                        .liquidGlassSource(state),
                ) {
                    GlassCard(state = state, modifier = Modifier.size(200.dp, 120.dp)) {
                        Text("Glass over red")
                    }
                }
            }
        }
        waitForIdle()
    }

    @Ignore
    @Test
    fun fullQuality_recordsBackdropAfterFirstFrame() = runComposeUiTest {
        var capturedState: LiquidGlassState? = null
        setContent {
            MaterialTheme {
                val state = rememberLiquidGlassState(LiquidGlassQuality.Full)
                capturedState = state
                Box(
                    Modifier
                        .size(300.dp)
                        .background(Color(0xFF009688))
                        .liquidGlassSource(state),
                ) {
                    GlassCard(state = state, modifier = Modifier.size(200.dp, 100.dp)) {
                        Text("Recorded")
                    }
                }
            }
        }
        waitForIdle()
        val state = assertNotNull(capturedState)
        val layer = assertNotNull(state.backdrop)
        assertTrue(layer.size.width > 0 && layer.size.height > 0)
    }
}
