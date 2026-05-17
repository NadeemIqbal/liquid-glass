# LiquidGlass

**iOS 26-style frosted glass surfaces for Compose Multiplatform.** A `Modifier.liquidGlass()`
plus `GlassCard`, `GlassButton`, and `GlassNavBar` composables that produce backdrop blur,
chroma lift, and an edge sheen — with built-in quality tiers that degrade gracefully on
low-end Android and older iOS devices.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.nadeemiqbal/liquid-glass)](https://central.sonatype.com/artifact/io.github.nadeemiqbal/liquid-glass)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Build](https://github.com/NadeemIqbal/liquid-glass/actions/workflows/build.yml/badge.svg)](https://github.com/NadeemIqbal/liquid-glass/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-7F52FF?logo=kotlin)](https://kotlinlang.org)
![Android](https://img.shields.io/badge/Android-24%2B-3DDC84?logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-x64%20%7C%20arm64%20%7C%20simulator-000000?logo=apple&logoColor=white)
![Desktop](https://img.shields.io/badge/Desktop-JVM-007396?logo=openjdk&logoColor=white)
![Web](https://img.shields.io/badge/Web-wasmJs-654FF0?logo=webassembly&logoColor=white)

<p align="center">
  <img src="docs/hero.gif" alt="LiquidGlass — Modifier.liquidGlass over a colorful backdrop, with a quality-tier picker" width="320">
</p>

## Why this library

Compose's `Modifier.blur` blurs a composable's **own** content, not the backdrop behind it.
Chris Banes's [`haze`](https://github.com/chrisbanes/haze) library solves the backdrop-blur
problem beautifully, but it doesn't auto-degrade for memory-constrained devices — and the
iOS 26 "liquid glass" effect is heavy enough that Apple disables it on older hardware.

`liquid-glass` is an opinionated, iOS-26-flavored take on glassmorphism with three explicit
quality tiers and platform auto-detection:

| Tier      | Blur radius | Saturation | Backdrop layer       | Auto-picked on                              |
|-----------|------------:|-----------:|----------------------|---------------------------------------------|
| Full      |       24.dp |       1.4× | Full-res             | Android 12+ (non-low-RAM), iOS 17+, Desktop, Web |
| Medium    |       16.dp |       1.2× | 0.5× downsampled     | iOS 15–16 (opt-in elsewhere)                |
| Fallback  |        0.dp |       1.0× | **None — zero alloc**| Android < 12 or `isLowRamDevice`, iOS < 15  |

`Fallback` allocates **zero offscreen buffers** and skips the blur entirely — so the same
code runs without OOMing on a 2 GB Android 11 device and still looks reasonable.

## Platform support

| Platform | Supported | Auto-tier                                                |
|----------|:---------:|----------------------------------------------------------|
| Android  |     ✅     | Full on API 31+ (non low-RAM), Fallback otherwise         |
| iOS      |     ✅     | Full on iOS 17+, Medium on iOS 15–16, Fallback on < 15    |
| Desktop  |     ✅     | Full                                                     |
| Web      |     ✅     | Full                                                     |

## Installation

`gradle/libs.versions.toml`:

```toml
[libraries]
liquid-glass = { module = "io.github.nadeemiqbal:liquid-glass", version = "0.2.1" }
```

`commonMain` dependencies:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.liquid.glass)
        }
    }
}
```

## Quick start

```kotlin
@Composable
fun Screen() {
    val state = rememberLiquidGlassState()    // auto-picks the tier for the device

    Box(Modifier.fillMaxSize()) {
        // 1) Anything inside this box becomes the backdrop that the glass samples from.
        Image(
            painter = painterResource(R.drawable.scenery),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().liquidGlassSource(state),
        )

        // 2) A floating glass card on top, sampling the backdrop above.
        GlassCard(
            state = state,
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
        ) {
            Text("Frosted, light-refracting surface — drop-in")
        }
    }
}
```

That's it. Same code on Android, iOS, Desktop, and Web — and the same code on a low-RAM
Android 11 device will quietly fall back to a flat tint + edge sheen with **no GraphicsLayer
allocation**.

## Customization

Override individual parameters at the call site; the per-tier values from
`LiquidGlassDefaults.forQuality` are the sensible defaults:

```kotlin
GlassCard(
    state = state,
    shape = RoundedCornerShape(28.dp),
    blurRadius = 30.dp,
    saturation = 1.6f,
    tint = Color.White.copy(alpha = 0.35f),
    borderHighlight = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0.6f),
        1f to Color.Transparent,
    ),
    grain = 0.04f,         // subtle frosted texture (v0.2.0+)
    refraction = 0.4f,     // SkSL/AGSL pixel-offset distortion (v0.2.0+)
) { /* … */ }
```

### Dynamic-color edge sheen

Derive the sheen color from the captured backdrop, instead of the static white gradient:

```kotlin
GlassCard(
    state = state,
    borderHighlight = rememberDynamicSheen(state),
) { /* … */ }
```

The library polls the backdrop layer every 500 ms (configurable), averages its pixels, and
produces a vertical-gradient brush from that color. Falls back to the static brush on platforms
where backdrop sampling isn't available (e.g. wasmJs).

### Dialogs and bottom sheets

```kotlin
if (showDialog) {
    GlassDialog(onDismissRequest = { showDialog = false }) {
        Text("Material 3 Dialog with a liquid-glass surface.")
    }
}

if (showSheet) {
    GlassBottomSheet(onDismissRequest = { showSheet = false }) {
        Text("Material 3 ModalBottomSheet with a liquid-glass surface.")
        // …list items, etc.
    }
}
```

Both dialogs and bottom sheets host their own `LiquidGlassState`, so the glass samples the
dialog's own composition (typically just the system scrim), not the host activity behind it.
This matches how `Modifier.blur` and `haze` behave with dialogs.

Force a specific tier (e.g. for a brand-mandated "Full everywhere" experience):

```kotlin
val state = rememberLiquidGlassState(LiquidGlassQuality.Full)
```

…or downgrade for low-end shells without waiting for auto-detection:

```kotlin
val state = rememberLiquidGlassState(LiquidGlassQuality.Fallback)
```

## API at a glance

| Symbol                          | Purpose                                                          |
|---------------------------------|------------------------------------------------------------------|
| `LiquidGlassQuality`            | `Full` / `Medium` / `Fallback`                                    |
| `rememberPlatformLiquidGlassQuality()` | Platform-detected tier (`@Composable expect`)              |
| `rememberLiquidGlassState(quality)` | Holds the shared backdrop `GraphicsLayer` (or `null` for Fallback) |
| `Modifier.liquidGlassSource(state)` | Marks the backdrop composable                                |
| `Modifier.liquidGlass(state, …)`    | Marks the glass surface — supports blur, saturation, tint, sheen, **grain**, **refraction** |
| `GlassCard(state, …, content)`  | `Modifier.liquidGlass` wrapped around a padded `Box`             |
| `GlassButton(state, onClick, …)`| Pill-shaped clickable glass surface                              |
| `GlassNavBar(state, …, content)`| `Modifier.liquidGlass` over a status-bar-padded top bar          |
| `GlassDialog(onDismissRequest, …, content)` | Material 3 `Dialog` with a glass surface             |
| `GlassBottomSheet(onDismissRequest, …, content)` | Material 3 `ModalBottomSheet` with a glass surface |
| `rememberDynamicSheen(state, …)`| Edge-sheen `Brush` sampled from the captured backdrop's average color |
| `rememberGlassNoiseTile(seed, size)` | Procedural noise `ImageBitmap` for the `grain` parameter    |
| `LiquidGlassDefaults`           | Per-tier `blurRadius`, `saturation`, `downsampleFactor`, tints   |

## Comparison

| Library                                                       | Backdrop blur | Per-device tiers | Zero-alloc fallback | API style                |
|---------------------------------------------------------------|:-------------:|:----------------:|:-------------------:|--------------------------|
| `Modifier.blur`                                               |       ❌       |        ❌         |          —          | Blurs own content only   |
| Material 3 `Surface` with `tonalElevation`                    |       ❌       |        ❌         |          —          | Flat tint, no blur       |
| [haze](https://github.com/chrisbanes/haze)                    |       ✅       |        ❌         |          ❌          | Generic, very flexible   |
| **liquid-glass**                                              |       ✅       |        ✅         |          ✅          | Opinionated iOS-26 look  |

## Roadmap

- [x] `GlassDialog` and `GlassBottomSheet` Material 3 wrappers — *shipped in v0.2.0*
- [x] Dynamic-color edge sheen (sample from the captured backdrop) — *shipped in v0.2.0*
- [x] Android-only single-pass chained `RenderEffect` (blur + saturation) on API 31+ — *shipped in v0.2.0*
- [x] Optional grain / noise overlay — *shipped in v0.2.0*
- [x] Refraction shader (SkSL on Skia targets, AGSL on Android API 33+) — *shipped in v0.2.0*

### Future
- Opt-in `Modifier.iosNativeGlass()` interop using `UIVisualEffectView` / `UIGlassEffect` on iOS 26+
- Animated refraction (time-driven uniforms)
- Vary the refraction pattern (caustics, droplets) via additional SkSL shader sources

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[Apache 2.0](LICENSE).
