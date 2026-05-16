# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/NadeemIqbal/liquid-glass/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/NadeemIqbal/liquid-glass/releases/tag/v0.1.0
