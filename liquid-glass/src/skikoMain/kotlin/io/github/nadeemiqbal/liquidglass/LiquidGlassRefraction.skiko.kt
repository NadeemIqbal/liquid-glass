package io.github.nadeemiqbal.liquidglass

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

private const val REFRACTION_SKSL = """
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

private val refractionEffect: RuntimeEffect by lazy { RuntimeEffect.makeForShader(REFRACTION_SKSL) }

internal actual fun refractionRenderEffect(strength: Float, size: Size): RenderEffect? {
    if (strength <= 0f) return null
    if (size.width <= 0f || size.height <= 0f) return null

    val builder = RuntimeShaderBuilder(refractionEffect).apply {
        uniform("size", size.width, size.height)
        uniform("strength", strength.coerceIn(0f, 1f))
    }
    val imageFilter = ImageFilter.makeRuntimeShader(
        runtimeShaderBuilder = builder,
        shaderName = "content",
        input = null,
    )
    return imageFilter.asComposeRenderEffect()
}
