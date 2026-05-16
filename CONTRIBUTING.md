# Contributing to LiquidGlass

Thanks for your interest in improving this library! Contributions of all kinds are welcome —
bug reports, feature requests, docs, and code.

## Project layout

```
liquid-glass/                The published Kotlin Multiplatform library.
  src/commonMain/            Public API + implementation.
    LiquidGlassQuality.kt     Tier enum + `expect` platform resolver.
    LiquidGlassState.kt       State + `rememberLiquidGlassState`.
    LiquidGlassDefaults.kt    Per-tier defaults, tints, edge brush.
    LiquidGlassModifier.kt    `Modifier.liquidGlassSource` + `Modifier.liquidGlass`.
    LiquidGlassMath.kt        Pure helpers (saturation matrix).
    GlassCard.kt / GlassButton.kt / GlassNavBar.kt   Drop-in composables.
  src/androidMain/            `actual` resolver — SDK_INT + isLowRamDevice.
  src/iosMain/                `actual` resolver — UIDevice.systemVersion.
  src/desktopMain/            `actual` resolver — always Full.
  src/wasmJsMain/             `actual` resolver — always Full.
  src/commonTest/             Pure-logic tests — every target including Android unit tests.
  src/skikoTest/              Compose UI tests — Desktop and iOS test targets.
sample/composeApp/            Shared sample UI with a quality picker + live sliders.
sample/androidApp/            Android launcher.
sample/desktopApp/            Desktop (JVM) launcher.
sample/webApp/                Web (wasmJs) launcher.
sample/iosApp/                iOS launcher (standalone Xcode project).
```

## Building & testing

```bash
./gradlew build                                  # build + test everything
./gradlew allTests                               # run tests on all targets
./gradlew :liquid-glass:desktopTest              # fastest feedback (commonTest + skikoTest)
./gradlew :liquid-glass:testDebugUnitTest        # Android unit tests
./gradlew :sample:desktopApp:run                 # run the desktop sample
```

The saturation color matrix and the per-tier defaults are covered by pure-logic tests in
`LiquidGlassMathTest` and `LiquidGlassDefaultsTest`. The Compose-side wiring (Fallback skips
the GraphicsLayer alloc, Full records the backdrop, Medium downsamples) is verified in
`LiquidGlassUiTest` against the Skiko backend.

The Fallback tier is non-negotiable — it must never allocate a `GraphicsLayer`. Any change
that breaks `fallbackQuality_doesNotAllocateBackdropLayer` is breaking behavior the library
exists to preserve.

## Conventions

- Public API gets KDoc.
- Add or update tests for every behavior change — both pure logic and UI wiring.
- Update the sample app when you change a public API.
- Add a `CHANGELOG.md` entry under `## Unreleased`.

## Releasing

Releases are tag-driven: pushing a `v*` tag runs `.github/workflows/publish.yml`, which publishes
to Maven Central via the `com.vanniktech.maven.publish` plugin and creates a GitHub Release.
