# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.3] - 2026-05-17

### Changed (breaking — but v0.2.0–0.2.2 were unshippable)
- **`GlassDialog` and `GlassBottomSheet` now require a `state: LiquidGlassState` parameter.**
  Pass the same state used by `Modifier.liquidGlassSource(state)` in the host composition.
  The dialog/sheet samples that recorded backdrop and renders a real frosted-glass surface —
  blur + chroma + sheen + grain — instead of the flat opaque card the previous versions
  degraded to.
- Re-introduced `blurRadius`, `saturation`, and `refraction` parameters now that they have a
  visual effect again.

### Platform note
- iOS: dialog and host live in the same `UIWindow`, so the shared GraphicsLayer samples cleanly.
- Android: the system `Dialog` opens in a separate window; the shared GraphicsLayer may not be
  reachable from there, producing an empty/black backdrop inside the dialog. Workaround: use
  `Popup` (in-window) with a `GlassCard` instead. Documented in the KDoc.

### Docs
- README quick-start now uses the CMP-correct `Res.drawable.scenery` from
  `composeResources` instead of the Android-only `R.drawable.scenery`, and notes where
  to place the image file. Reported via Reddit.

[0.2.3]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.2.3

## [0.2.2] - 2026-05-17

### Fixed
- **`GlassDialog` and `GlassBottomSheet` rendered as near-invisible ghosts** in v0.2.1.
  The 0x4D-alpha `tintFor()` default is right for surfaces sitting over a blurred backdrop,
  but inside a dialog/sheet there's no backdrop to blur, so the surface degenerated to a
  barely-tinted rectangle floating over the host UI (with the underlying content visibly
  bleeding through). They now default to the new opaque tint and read as solid iOS-style
  sheets/dialogs.

### Added
- `LiquidGlassDefaults.opaqueTintFor(isDark)` (plus `lightOpaqueTint` / `darkOpaqueTint`
  constants) — near-opaque (0xF2 alpha) surface colors meant for stand-alone glass surfaces
  that have no backdrop to sample. `GlassDialog` and `GlassBottomSheet` default to this.

### Changed
- `GlassBottomSheet` default `shape` is now `RoundedCornerShape(topStart = 28.dp, topEnd =
  28.dp)` — matches iOS sheet styling instead of the previous all-corners-rounded card shape.
- `GlassBottomSheet` brings back the M3 drag handle (toggle via `showDragHandle` parameter,
  defaults to `true`) and bumps the scrim opacity from 0.32 → 0.5 for a more typical sheet feel.

[0.2.2]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.2.2

## [0.2.1] - 2026-05-17

### Fixed
- **GlassDialog and GlassBottomSheet crash on open** (stack overflow / SIGSEGV "Could not
  determine thread index for stack guard region"). The v0.2.0 implementations applied both
  `Modifier.liquidGlassSource` and `Modifier.liquidGlass` on the same node, so the backdrop
  graphics layer recorded the glass surface's own draw output and recursed unbounded inside
  Skia's `SkDrawable::draw`. The fix omits `liquidGlassSource` inside dialogs and sheets
  entirely — they have no host-composition backdrop to sample anyway.

### Changed (breaking — but v0.2.0 was unshippable)
- `GlassDialog` and `GlassBottomSheet` parameter lists trimmed to only the params that have a
  visual effect inside a system overlay window: `shape`, `tint`, `borderHighlight`, `grain`,
  `grainSeed`, plus the M3 host params (`properties`, `sheetState`). Removed: `quality`,
  `blurRadius`, `saturation`, `refraction` — all were silently no-ops inside an overlay window
  and just confused the call site. Documented limitation.

## [0.2.0] - 2026-05-17

### Added
- `GlassDialog` — Material 3 `Dialog` whose surface is a liquid-glass card. Hosts its own
  `LiquidGlassState`. Documented limitation: the glass samples the dialog's own composition
  (typically the system scrim), not the host activity behind it — dialogs render in a separate
  system overlay window on Android.
- `GlassBottomSheet` — Material 3 `ModalBottomSheet` with a liquid-glass surface; same
  composition-window caveat as `GlassDialog`.
- `rememberDynamicSheen(state, ...)` — derives the edge-sheen `Brush` from the captured backdrop's
  average color. Polls every 500ms (configurable) using `GraphicsLayer.toImageBitmap()` +
  `LiquidGlassMath.averageColor`. Falls back to the static white gradient when the platform
  cannot read the backdrop (e.g. wasmJs) or when the tier is `Fallback`.
- `grain: Float` and `grainSeed: Long` parameters on `Modifier.liquidGlass`, `GlassCard`,
  `GlassButton`, `GlassNavBar`, `GlassDialog`, and `GlassBottomSheet` — tiled procedural noise
  overlay (`rememberGlassNoiseTile`) drawn before the composable's own content. Default `0f`
  (off). Works in every tier including Fallback (no offscreen allocation needed).
- `refraction: Float` parameter — SkSL/AGSL pixel-offset distortion of the backdrop, chained
  before the blur+saturation step.
  - **Skia targets** (Desktop, iOS, Web) — `org.jetbrains.skia.RuntimeEffect` with a small
    SkSL shader (uniform `size`, `strength`).
  - **Android API 33+** — equivalent AGSL via `android.graphics.RuntimeShader`.
  - **Android < 33** and **Fallback tier** — silently no-op.
- `LiquidGlassMath.averageColor(ImageBitmap, downsampleGrid)` — public helper used internally
  by `rememberDynamicSheen` and unit-tested.
- Internal: single-pass chained `RenderEffect` (blur + saturation) on Android API 31+ via
  `RenderEffect.createChainEffect`; cross-platform `composeRenderEffects(outer, inner)`
  expect/actual chains refraction → blur, implemented with `createChainEffect` on Android and
  `ImageFilter.makeCompose` on Skia.

### Changed
- `skikoMain` intermediate source set added to the library's Gradle config, shared across
  Desktop, iOS, and wasmJs for SkSL shader code and Skia-only chained effects.
- Existing call sites of `Modifier.liquidGlass(state)`, `GlassCard`, etc. render identically to
  v0.1.0 — every new parameter defaults to `0f` / off / static.

[0.2.0]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.2.0

## [0.1.0] - 2026-05-16

### Added
- Initial release of `LiquidGlass` for Compose Multiplatform — iOS 26-style frosted glass
  surfaces with backdrop blur, chroma lift, and an edge sheen.
- `Modifier.liquidGlassSource(state)` — marks a composable as the backdrop. Records its drawn
  content into a shared `GraphicsLayer` (or no-ops in the Fallback tier).
- `Modifier.liquidGlass(state, shape, blurRadius, saturation, tint, borderHighlight)` — draws
  the recorded backdrop with blur + saturation + tint + edge sheen behind the composable's
  own content.
- `GlassCard`, `GlassButton`, `GlassNavBar` — drop-in composables wrapping `Modifier.liquidGlass`.
- `LiquidGlassState` + `rememberLiquidGlassState(quality)` — holds the shared `GraphicsLayer`,
  honors the per-tier downsample factor.
- `LiquidGlassQuality` (`Full` / `Medium` / `Fallback`) + `rememberPlatformLiquidGlassQuality()`
  — per-platform auto-detection:
  - **Android** — `Build.VERSION.SDK_INT < 31` or `ActivityManager.isLowRamDevice` → Fallback;
    else Full. Medium is opt-in.
  - **iOS** — iOS ≥ 17 → Full, iOS 15–16 → Medium, iOS < 15 → Fallback.
  - **Desktop / Web** — Full by default.
- `LiquidGlassDefaults` — per-tier defaults (`blurRadius`, `saturation`, `downsampleFactor`),
  shape, tint helpers, and edge-sheen brush.
- **Zero-alloc fallback path**: the Fallback tier allocates no `GraphicsLayer` and skips the
  blur entirely, so the library runs safely on low-RAM Android 11 / older iOS hardware.
- Pure-logic helpers (`LiquidGlassMath.saturationMatrix`) covered by `commonTest`.
- Compose UI tests in `skikoTest` verifying allocation behavior across tiers.
- Targets: Android, iOS (x64, arm64, simulatorArm64), Desktop (JVM), Web (wasmJs).

[Unreleased]: https://github.com/NadeemIqbal/liquid-glass/compare/v0.2.3...HEAD
[0.2.2]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.2.2
[0.2.1]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.2.1
[0.1.0]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.1.0
