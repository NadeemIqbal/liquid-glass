package io.github.nadeemiqbal.liquidglass

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect

// AGSL — Android's variant of SkSL. The shader body is the same as the SkSL counterpart;
// `eval()` and `shader` keyword work identically on RuntimeShader on API 33+.
private const val REFRACTION_AGSL = """
uniform shader content;
uniform float2 size;
uniform float strength;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / size;
    float dx = sin(uv.y * 18.0) * strength * 6.0;
    float dy = cos(uv.x * 14.0) * strength * 4.0;
    return content.eval(fragCoord + float2(dx, dy));
}
"""

internal actual fun refractionRenderEffect(strength: Float, size: Size): RenderEffect? {
    if (strength <= 0f) return null
    if (size.width <= 0f || size.height <= 0f) return null
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
    return buildRefractionEffectApi33(strength, size)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun buildRefractionEffectApi33(strength: Float, size: Size): RenderEffect {
    val shader = RuntimeShader(REFRACTION_AGSL).apply {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("strength", strength.coerceIn(0f, 1f))
    }
    val refraction = AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
    return refraction.asComposeRenderEffect()
}
