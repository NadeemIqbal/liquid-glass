package io.github.nadeemiqbal.liquidglass

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android resolver: returns [LiquidGlassQuality.Fallback] when:
 * - The device is a low-RAM device (`ActivityManager.isLowRamDevice()` is true), OR
 * - Android API level is below 31 (where `RenderEffect` / `BlurEffect` are unavailable).
 *
 * Otherwise returns [LiquidGlassQuality.Full]. [LiquidGlassQuality.Medium] is opt-in only on
 * Android — auto-picking it from a RAM heuristic on flagship devices would be surprising.
 */
@Composable
actual fun rememberPlatformLiquidGlassQuality(): LiquidGlassQuality {
    val context = LocalContext.current
    return remember(context) { resolveAndroidQuality(context) }
}

private fun resolveAndroidQuality(context: Context): LiquidGlassQuality {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return LiquidGlassQuality.Fallback
    val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (activityManager != null && activityManager.isLowRamDevice) {
        return LiquidGlassQuality.Fallback
    }
    return LiquidGlassQuality.Full
}
