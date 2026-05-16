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
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
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
 * Layers (bottom to top): the recorded backdrop (blurred + saturation-lifted, scaled back to
 * surface size), a translucent [tint], the edge [borderHighlight] stroke, then the composable's
 * own content.
 *
 * [state] must be created with [rememberLiquidGlassState] and shared with the
 * [Modifier.liquidGlassSource] that marks the backdrop. When [state]'s quality is
 * [LiquidGlassQuality.Fallback] this falls through to a zero-allocation tint + sheen — no blur,
 * no graphics layer.
 *
 * @param shape clips the blurred backdrop and the surface content.
 * @param blurRadius defaults to the quality-tier value from [LiquidGlassDefaults.forQuality].
 * @param saturation chroma multiplier for the backdrop — 1.0 is unchanged, > 1.0 boosts colors.
 * @param tint translucent overlay color. Defaults to a light/dark-aware white or black.
 * @param borderHighlight brush used for the 1.dp inner edge sheen. Use
 *   [LiquidGlassDefaults.borderBrush] or any vertical/linear gradient.
 */
fun Modifier.liquidGlass(
    state: LiquidGlassState,
    shape: Shape = LiquidGlassDefaults.shape,
    blurRadius: Dp = LiquidGlassDefaults.forQuality(state.quality).blurRadius,
    saturation: Float = LiquidGlassDefaults.forQuality(state.quality).saturation,
    tint: Color = Color.Unspecified,
    borderHighlight: Brush = LiquidGlassDefaults.borderBrush(),
): Modifier = composed {
    val resolvedTint = if (tint == Color.Unspecified) {
        LiquidGlassDefaults.tintFor(isSystemInDarkTheme())
    } else {
        tint
    }
    this.glassDraw(state, shape, blurRadius, saturation, resolvedTint, borderHighlight)
}

private fun Modifier.glassDraw(
    state: LiquidGlassState,
    shape: Shape,
    blurRadius: Dp,
    saturation: Float,
    tint: Color,
    borderHighlight: Brush,
): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val saturationFilter = ColorFilter.colorMatrix(LiquidGlassMath.saturationMatrix(saturation))
    val blurPx = blurRadius.toPx()
    val needsBlur = state.quality != LiquidGlassQuality.Fallback && blurPx > 0f
    val renderEffect = if (needsBlur) BlurEffect(blurPx, blurPx, TileMode.Clamp) else null
    val strokePx = 1.dp.toPx()

    onDrawWithContent {
        withOutlineClip(outline) {
            drawBackdrop(state, renderEffect, saturationFilter)
            drawOutlineFill(outline, tint)
            drawEdgeSheen(outline, borderHighlight, strokePx)
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
    renderEffect: BlurEffect?,
    colorFilter: ColorFilter,
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
