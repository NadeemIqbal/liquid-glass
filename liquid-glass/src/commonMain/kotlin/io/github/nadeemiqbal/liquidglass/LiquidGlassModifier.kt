package io.github.nadeemiqbal.liquidglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * Marks this composable as the backdrop that any [Modifier.liquidGlass] surface using the same
 * [state] will sample, blur, and tint.
 *
 * Place this on the composable whose pixels should be "behind the glass" — typically a full-bleed
 * image, gradient, or content list. The modifier records the rendered content into the state's
 * [androidx.compose.ui.graphics.layer.GraphicsLayer] on every frame, then draws the content
 * normally on screen.
 *
 * When [state]'s quality is [LiquidGlassQuality.Fallback] this is a zero-cost identity —
 * no graphics layer is allocated and no recording happens.
 */
fun Modifier.liquidGlassSource(state: LiquidGlassState): Modifier {
    val layer = state.backdrop ?: return this
    val downsample = state.downsample
    return this.drawWithContent {
        val recordedWidth = (size.width * downsample).toInt().coerceAtLeast(1)
        val recordedHeight = (size.height * downsample).toInt().coerceAtLeast(1)
        layer.record(size = IntSize(recordedWidth, recordedHeight)) {
            if (downsample == 1f) {
                this@drawWithContent.drawContent()
            } else {
                scale(downsample, downsample, pivot = Offset.Zero) {
                    this@drawWithContent.drawContent()
                }
            }
        }
        drawContent()
    }
}

/**
 * Draws a frosted-glass surface behind this composable's content.
 *
 * Layers (bottom to top): the recorded backdrop (refracted + blurred + saturation-lifted, scaled
 * back to surface size), a translucent [tint], the edge [borderHighlight] stroke, the [grain]
 * noise tile, then the composable's own content.
 *
 * [state] must be created with [rememberLiquidGlassState] and shared with the
 * [Modifier.liquidGlassSource] that marks the backdrop. When [state]'s quality is
 * [LiquidGlassQuality.Fallback] this falls through to a zero-allocation tint + sheen + optional
 * grain — no blur, no refraction, no graphics layer.
 *
 * @param shape clips the blurred backdrop and the surface content.
 * @param blurRadius defaults to the quality-tier value from [LiquidGlassDefaults.forQuality].
 * @param saturation chroma multiplier for the backdrop — 1.0 is unchanged, > 1.0 boosts colors.
 * @param tint translucent overlay color. Defaults to a light/dark-aware white or black.
 * @param borderHighlight brush used for the 1.dp inner edge sheen. Use
 *   [LiquidGlassDefaults.borderBrush] or [rememberDynamicSheen] for a color sampled from the
 *   captured backdrop.
 * @param grain Noise overlay strength, 0..1. Default 0 (off). A subtle 0.04 matches the iOS-26
 *   frosted-glass feel. Drawn as a tiled procedural pattern; works in every tier including
 *   Fallback (no offscreen allocation).
 * @param grainSeed RNG seed for the noise tile — same seed produces an identical tile pattern.
 * @param refraction Sinusoidal pixel-offset distortion of the backdrop, 0..1. Default 0 (off).
 *   Implemented as a SkSL / AGSL `RuntimeShader` on Skia-backed targets and Android API 33+;
 *   silently no-ops below Android 33 and in the Fallback tier.
 */
fun Modifier.liquidGlass(
    state: LiquidGlassState,
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
    grain: Float = 0f,
    grainSeed: Long = 0L,
    refraction: Float = 0f,
): Modifier = composed {
    val resolvedTint = if (tint == Color.Unspecified) {
        LiquidGlassDefaults.tintFor(isSystemInDarkTheme())
    } else {
        tint
    }
    val noiseTile: ImageBitmap? = if (grain > 0f) rememberGlassNoiseTile(seed = grainSeed) else null
    this.glassDraw(
        state = state,
        shape = shape,
        blurRadius = blurRadius,
        saturation = saturation,
        tint = resolvedTint,
        borderHighlight = borderHighlight,
        grain = grain.coerceIn(0f, 1f),
        noiseTile = noiseTile,
        refraction = refraction.coerceIn(0f, 1f),
    )
}

private fun Modifier.glassDraw(
    state: LiquidGlassState,
    shape: Shape,
    blurRadius: Dp,
    saturation: Float,
    tint: Color,
    borderHighlight: Brush,
    grain: Float,
    noiseTile: ImageBitmap?,
    refraction: Float,
): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val saturationFilter = ColorFilter.colorMatrix(LiquidGlassMath.saturationMatrix(saturation))
    val blurPx = blurRadius.toPx()
    val isFallback = state.quality == LiquidGlassQuality.Fallback
    val needsBlur = !isFallback && blurPx > 0f
    val strokePx = 1.dp.toPx()
    val chained = if (needsBlur) chainedGlassRenderEffect(blurPx, saturation) else null
    val baseEffect: RenderEffect? = chained
        ?: if (needsBlur) BlurEffect(blurPx, blurPx, TileMode.Clamp) else null
    val refractionEffect: RenderEffect? = if (!isFallback && refraction > 0f) {
        refractionRenderEffect(refraction, size)
    } else {
        null
    }
    // Refraction is applied to the source pixels first, then blur+saturation on top of that.
    val layerEffect: RenderEffect? = composeRenderEffects(outer = baseEffect, inner = refractionEffect)
    // When the chained path is in use, saturation is baked into baseEffect — clear the layer's
    // colorFilter so it isn't applied twice.
    val layerColorFilter: ColorFilter? = if (chained != null) null else saturationFilter

    onDrawWithContent {
        withOutlineClip(outline) {
            drawBackdrop(state, layerEffect, layerColorFilter)
            drawOutlineFill(outline, tint)
            drawEdgeSheen(outline, borderHighlight, strokePx)
            if (noiseTile != null && grain > 0f) {
                drawNoiseTile(outline, noiseTile, grain)
            }
            this@onDrawWithContent.drawContent()
        }
    }
}

private inline fun ContentDrawScope.withOutlineClip(
    outline: Outline,
    block: ContentDrawScope.() -> Unit,
) {
    val cd = this  // capture: clip blocks below have a DrawScope receiver, not ContentDrawScope.
    when (outline) {
        is Outline.Rectangle -> {
            clipRect(
                left = outline.rect.left,
                top = outline.rect.top,
                right = outline.rect.right,
                bottom = outline.rect.bottom,
            ) { cd.block() }
        }
        is Outline.Rounded -> {
            val path = Path().apply { addRoundRect(outline.roundRect) }
            clipPath(path) { cd.block() }
        }
        is Outline.Generic -> {
            clipPath(outline.path) { cd.block() }
        }
    }
}

private fun ContentDrawScope.drawBackdrop(
    state: LiquidGlassState,
    renderEffect: RenderEffect?,
    colorFilter: ColorFilter?,
) {
    val layer = state.backdrop ?: return
    val recorded = layer.size.width > 0 && layer.size.height > 0
    if (!recorded) return

    layer.renderEffect = renderEffect
    layer.colorFilter = colorFilter

    val scaleUp = 1f / state.downsample
    if (scaleUp == 1f) {
        drawLayer(layer)
    } else {
        withTransform({ scale(scaleUp, scaleUp, pivot = Offset.Zero) }) {
            drawLayer(layer)
        }
    }
}

private fun DrawScope.drawOutlineFill(outline: Outline, color: Color) {
    drawOutline(outline, color)
}

private fun DrawScope.drawEdgeSheen(outline: Outline, brush: Brush, strokePx: Float) {
    drawOutline(outline, brush, style = Stroke(width = strokePx))
}

private fun DrawScope.drawNoiseTile(outline: Outline, tile: ImageBitmap, alpha: Float) {
    val tileSize = IntSize(tile.width, tile.height)
    val (origin, dimensions) = outlineBoundsAsInts(outline, size)
    // Tile by drawing the noise bitmap repeatedly across the outline bounds. Cheap enough for the
    // typical glass-surface sizes; if profiling shows hotspots a ShaderBrush with TileMode.Repeated
    // can replace this loop.
    var y = origin.y
    while (y < origin.y + dimensions.height) {
        var x = origin.x
        while (x < origin.x + dimensions.width) {
            val w = minOf(tileSize.width, origin.x + dimensions.width - x)
            val h = minOf(tileSize.height, origin.y + dimensions.height - y)
            drawImage(
                image = tile,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(w, h),
                dstOffset = IntOffset(x, y),
                dstSize = IntSize(w, h),
                alpha = alpha,
                filterQuality = FilterQuality.None,
            )
            x += tileSize.width
        }
        y += tileSize.height
    }
}

private fun outlineBoundsAsInts(outline: Outline, surfaceSize: Size): Pair<IntOffset, IntSize> {
    val rect = when (outline) {
        is Outline.Rectangle -> outline.rect
        is Outline.Rounded -> outline.roundRect.let {
            androidx.compose.ui.geometry.Rect(it.left, it.top, it.right, it.bottom)
        }
        is Outline.Generic -> outline.path.getBounds()
    }
    val left = rect.left.toInt().coerceAtLeast(0)
    val top = rect.top.toInt().coerceAtLeast(0)
    val right = rect.right.toInt().coerceAtMost(surfaceSize.width.toInt())
    val bottom = rect.bottom.toInt().coerceAtMost(surfaceSize.height.toInt())
    return IntOffset(left, top) to IntSize((right - left).coerceAtLeast(0), (bottom - top).coerceAtLeast(0))
}

private fun DrawScope.drawOutline(outline: Outline, color: Color) {
    when (outline) {
        is Outline.Rectangle -> drawRect(
            color = color,
            topLeft = Offset(outline.rect.left, outline.rect.top),
            size = Size(outline.rect.width, outline.rect.height),
        )
        is Outline.Rounded -> {
            val path = Path().apply { addRoundRect(outline.roundRect) }
            drawPath(path, color = color)
        }
        is Outline.Generic -> drawPath(outline.path, color = color)
    }
}

private fun DrawScope.drawOutline(outline: Outline, brush: Brush, style: Stroke) {
    when (outline) {
        is Outline.Rectangle -> drawRect(
            brush = brush,
            topLeft = Offset(outline.rect.left, outline.rect.top),
            size = Size(outline.rect.width, outline.rect.height),
            style = style,
        )
        is Outline.Rounded -> {
            val path = Path().apply { addRoundRect(outline.roundRect) }
            drawPath(path, brush = brush, style = style)
        }
        is Outline.Generic -> drawPath(outline.path, brush = brush, style = style)
    }
}

