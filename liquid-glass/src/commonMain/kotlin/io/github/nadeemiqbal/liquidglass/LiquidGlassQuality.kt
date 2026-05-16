package io.github.nadeemiqbal.liquidglass

import androidx.compose.runtime.Composable

/**
 * Quality tier that decides how aggressively `liquid-glass` renders the glass effect.
 *
 * The library degrades gracefully on weaker hardware: [Fallback] never allocates an offscreen
 * graphics layer and skips the blur entirely, so low-RAM Android devices and older iOS hardware
 * still render a clean, lightweight glass surface without OOMing or stuttering.
 */
enum class LiquidGlassQuality {
    /**
     * Full-resolution backdrop capture, full blur radius and saturation lift. Use this on modern
     * GPUs — Android API 31+, iOS 17+, Desktop, and Web.
     */
    Full,

    /**
     * Backdrop captured at a downsampled resolution (typically 0.5×), with a reduced blur radius
     * and saturation. Roughly 4× cheaper than [Full] in memory and fillrate. Good for mid-range
     * mobile devices and older iOS hardware.
     */
    Medium,

    /**
     * Zero offscreen allocations and zero blur. The glass surface degrades to a translucent tint
     * with an edge sheen — visually consistent with [Full] but safe for low-RAM Android devices
     * and Android API < 31 (where `BlurEffect` is unavailable) and iOS versions where the iOS 26
     * liquid-glass effect is disabled by Apple.
     */
    Fallback,
}

/**
 * Resolves the recommended [LiquidGlassQuality] for the current device.
 *
 * Per-platform behavior:
 * - **Android** — `Build.VERSION.SDK_INT < 31` or `ActivityManager.isLowRamDevice` ⇒ [Fallback];
 *   otherwise [Full]. [Medium] is opt-in only on Android.
 * - **iOS** — iOS ≥ 17 ⇒ [Full], iOS 15–16 ⇒ [Medium], iOS < 15 ⇒ [Fallback].
 * - **Desktop / Web** — always [Full]. Override manually if you need to downgrade.
 *
 * Override the tier by passing an explicit value to [rememberLiquidGlassState].
 */
@Composable
expect fun rememberPlatformLiquidGlassQuality(): LiquidGlassQuality
