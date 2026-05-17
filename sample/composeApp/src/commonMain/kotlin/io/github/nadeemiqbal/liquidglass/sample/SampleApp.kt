package io.github.nadeemiqbal.liquidglass.sample

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nadeemiqbal.liquidglass.GlassBottomSheet
import io.github.nadeemiqbal.liquidglass.GlassButton
import io.github.nadeemiqbal.liquidglass.GlassCard
import io.github.nadeemiqbal.liquidglass.GlassDialog
import io.github.nadeemiqbal.liquidglass.GlassNavBar
import io.github.nadeemiqbal.liquidglass.LiquidGlassDefaults
import io.github.nadeemiqbal.liquidglass.LiquidGlassQuality
import io.github.nadeemiqbal.liquidglass.liquidGlassSource
import io.github.nadeemiqbal.liquidglass.rememberDynamicSheen
import io.github.nadeemiqbal.liquidglass.rememberLiquidGlassState
import io.github.nadeemiqbal.liquidglass.rememberPlatformLiquidGlassQuality
import kotlin.math.cos
import kotlin.math.sin

private enum class QualitySelection { Auto, Full, Medium, Fallback }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleApp() {
    var darkTheme by remember { mutableStateOf(false) }
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colors) {
        var selection by remember { mutableStateOf(QualitySelection.Auto) }
        val autoQuality = rememberPlatformLiquidGlassQuality()
        val effectiveQuality = when (selection) {
            QualitySelection.Auto -> autoQuality
            QualitySelection.Full -> LiquidGlassQuality.Full
            QualitySelection.Medium -> LiquidGlassQuality.Medium
            QualitySelection.Fallback -> LiquidGlassQuality.Fallback
        }

        var blurDp by remember { mutableStateOf<Float?>(null) }
        var saturation by remember { mutableStateOf<Float?>(null) }
        var tintAlpha by remember { mutableStateOf<Float?>(null) }
        var grain by remember { mutableStateOf(0f) }
        var refraction by remember { mutableStateOf(0f) }
        var dynamicSheen by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }
        var showSheet by remember { mutableStateOf(false) }

        // Rebuild state when quality changes so the underlying GraphicsLayer is reallocated.
        key(effectiveQuality) {
            val state = rememberLiquidGlassState(effectiveQuality)
            val tier = LiquidGlassDefaults.forQuality(effectiveQuality)
            val resolvedBlur = (blurDp ?: tier.blurRadius.value).dp
            val resolvedSaturation = saturation ?: tier.saturation
            val baseTint = LiquidGlassDefaults.tintFor(darkTheme)
            val resolvedTint = baseTint.copy(alpha = tintAlpha ?: baseTint.alpha)
            val resolvedSheen: Brush =
                if (dynamicSheen) rememberDynamicSheen(state) else LiquidGlassDefaults.borderBrush()

            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                ColorfulBackdrop(
                    modifier = Modifier
                        .fillMaxSize()
                        .liquidGlassSource(state),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    GlassNavBar(
                        state = state,
                        blurRadius = resolvedBlur,
                        saturation = resolvedSaturation,
                        tint = resolvedTint,
                        borderHighlight = resolvedSheen,
                        grain = grain,
                        refraction = refraction,
                    ) {
                        Text(
                            "Liquid Glass",
                            color = if (darkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                        )
                    }

                    Spacer(Modifier.height(60.dp))

                    GlassCard(
                        state = state,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        blurRadius = resolvedBlur,
                        saturation = resolvedSaturation,
                        tint = resolvedTint,
                        borderHighlight = resolvedSheen,
                        grain = grain,
                        refraction = refraction,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "iOS-26 flavored glass",
                                color = if (darkTheme) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            )
                            Text(
                                "Drop a Modifier on any composable and you get backdrop blur, " +
                                    "chroma lift, edge sheen, optional grain and SkSL refraction. " +
                                    "Auto-tiers itself to keep low-end Android and older iOS happy.",
                                color = if (darkTheme) Color(0xCCFFFFFF) else Color(0xCC000000),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                GlassButton(
                                    state = state,
                                    onClick = { showDialog = true },
                                    blurRadius = resolvedBlur,
                                    saturation = resolvedSaturation,
                                    tint = resolvedTint,
                                    borderHighlight = resolvedSheen,
                                    grain = grain,
                                    refraction = refraction,
                                ) {
                                    Text(
                                        "Show Dialog",
                                        color = if (darkTheme) Color.White else Color.Black,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                GlassButton(
                                    state = state,
                                    onClick = { showSheet = true },
                                    blurRadius = resolvedBlur,
                                    saturation = resolvedSaturation,
                                    tint = resolvedTint,
                                    borderHighlight = resolvedSheen,
                                    grain = grain,
                                    refraction = refraction,
                                ) {
                                    Text(
                                        "Show Sheet",
                                        color = if (darkTheme) Color.White else Color.Black,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Card(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        ),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                "Quality (auto resolved: $autoQuality)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val choices = QualitySelection.entries
                                choices.forEachIndexed { index, choice ->
                                    SegmentedButton(
                                        selected = selection == choice,
                                        onClick = { selection = choice },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = choices.size,
                                        ),
                                    ) { Text(choice.name) }
                                }
                            }

                            val controlsEnabled = effectiveQuality != LiquidGlassQuality.Fallback
                            LabeledSlider(
                                label = "Blur (${(blurDp ?: tier.blurRadius.value).toInt()}dp)",
                                value = blurDp ?: tier.blurRadius.value,
                                onValueChange = { blurDp = it },
                                range = 0f..60f,
                                enabled = controlsEnabled,
                            )
                            LabeledSlider(
                                label = "Saturation (${niceFloat(saturation ?: tier.saturation)})",
                                value = saturation ?: tier.saturation,
                                onValueChange = { saturation = it },
                                range = 0.5f..2f,
                                enabled = controlsEnabled,
                            )
                            LabeledSlider(
                                label = "Tint alpha (${niceFloat(tintAlpha ?: baseTint.alpha)})",
                                value = tintAlpha ?: baseTint.alpha,
                                onValueChange = { tintAlpha = it },
                                range = 0f..0.8f,
                                enabled = true,
                            )
                            LabeledSlider(
                                label = "Grain (${niceFloat(grain)})",
                                value = grain,
                                onValueChange = { grain = it },
                                range = 0f..0.15f,
                                enabled = true,
                            )
                            LabeledSlider(
                                label = "Refraction (${niceFloat(refraction)})",
                                value = refraction,
                                onValueChange = { refraction = it },
                                range = 0f..1f,
                                enabled = controlsEnabled,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Dynamic edge sheen")
                                Switch(
                                    checked = dynamicSheen,
                                    onCheckedChange = { dynamicSheen = it },
                                    enabled = controlsEnabled,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Dark theme")
                                Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
                            }
                        }
                    }
                }

                if (showDialog) {
                    GlassDialog(
                        onDismissRequest = { showDialog = false },
                        quality = effectiveQuality,
                        blurRadius = resolvedBlur,
                        saturation = resolvedSaturation,
                        tint = resolvedTint,
                        borderHighlight = resolvedSheen,
                        grain = grain,
                        refraction = refraction,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "GlassDialog",
                                color = if (darkTheme) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            )
                            Text(
                                "Material 3 Dialog with a liquid-glass surface. Hosts its own " +
                                    "LiquidGlassState — the glass samples the dialog's own " +
                                    "composition (system scrim), not the host activity behind it.",
                                color = if (darkTheme) Color(0xCCFFFFFF) else Color(0xCC000000),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                "Tap outside to dismiss.",
                                color = if (darkTheme) Color(0x99FFFFFF) else Color(0x99000000),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (showSheet) {
                    GlassBottomSheet(
                        onDismissRequest = { showSheet = false },
                        quality = effectiveQuality,
                        blurRadius = resolvedBlur,
                        saturation = resolvedSaturation,
                        tint = resolvedTint,
                        borderHighlight = resolvedSheen,
                        grain = grain,
                        refraction = refraction,
                    ) {
                        Text(
                            "GlassBottomSheet",
                            color = if (darkTheme) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Material 3 ModalBottomSheet with a liquid-glass surface.",
                            color = if (darkTheme) Color(0xCCFFFFFF) else Color(0xCC000000),
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(16.dp))
                        listOf("Settings", "Privacy", "About").forEach { item ->
                            Text(
                                item,
                                color = if (darkTheme) Color.White else Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
) {
    Column {
        Text(label, fontSize = 11.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
        )
    }
}

@Composable
private fun ColorfulBackdrop(modifier: Modifier) {
    val infinite = rememberInfiniteTransition(label = "backdrop")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 2f * 3.1415927f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "backdrop-phase",
    )
    val density = LocalDensity.current
    Box(
        modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6F61),
                        Color(0xFFFFA000),
                        Color(0xFFFFCA28),
                        Color(0xFFEC407A),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
            )
            .drawBehind {
                val centers = listOf(
                    0.2f to 0.3f,
                    0.7f to 0.2f,
                    0.3f to 0.75f,
                    0.8f to 0.7f,
                    0.5f to 0.5f,
                )
                val radii = listOf(160f, 200f, 140f, 180f, 120f)
                val colors = listOf(
                    Color(0xCC7E57C2),
                    Color(0xAA26C6DA),
                    Color(0xAA66BB6A),
                    Color(0xCCAB47BC),
                    Color(0xBBFFCA28),
                )
                centers.forEachIndexed { i, (fx, fy) ->
                    val driftX = (sin((phase + i.toFloat() * 0.9f).toDouble()) * 60.0).toFloat()
                    val driftY = (cos((phase + i.toFloat() * 1.3f).toDouble()) * 40.0).toFloat()
                    val center = Offset(size.width * fx + driftX, size.height * fy + driftY)
                    val radius = radii[i] + with(density) { 24.dp.toPx() } * i / 5f
                    drawCircle(
                        color = colors[i],
                        radius = radius,
                        center = center,
                        style = Fill,
                    )
                }
            }
    )
}

private fun niceFloat(v: Float): String {
    val rounded = (v * 100f).toInt() / 100f
    return rounded.toString()
}
