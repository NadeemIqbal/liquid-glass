import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.vanniktech.maven.publish)
}

// Release version is driven by the git tag on CI: tag `v0.2.0` publishes `0.2.0`.
// Override locally with `-Pversion=...`; otherwise builds use the literal below.
// Note: `findProperty("version")` returns Gradle's default `"unspecified"` when unset, so it
// is filtered out explicitly here.
val libVersion: String =
    (System.getenv("RELEASE_VERSION") ?: findProperty("version") as String?)
        ?.removePrefix("v")
        ?.takeUnless { it.isBlank() || it == "unspecified" }
        ?: "0.1.0"

group = "io.github.nadeemiqbal"
version = libVersion

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        // Compose UI tests use `compose.uiTest` (Skiko-backed) which has no Android
        // plain-JVM unit-test implementation. Keeping them in a dedicated `skikoTest`
        // source set — wired only into Desktop and iOS test targets — lets `androidUnitTest`
        // (which inherits `commonTest`) keep compiling. Pure-logic tests live in `commonTest`.
        val skikoTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                @OptIn(ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }
        listOf("desktopTest", "iosX64Test", "iosArm64Test", "iosSimulatorArm64Test").forEach { name ->
            named(name) { dependsOn(skikoTest) }
        }
    }
}

android {
    namespace = "io.github.nadeemiqbal.liquidglass"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Wasm is verified compile-only. The wasm test bundle transitively pulls in Skiko (via Compose
// UI), which cannot load under Node, and the browser runner needs a local Chrome install. The
// pure-logic tests and Compose UI tests run on the Desktop, Android and iOS targets instead.
tasks.matching { it.name == "wasmJsBrowserTest" || it.name == "wasmJsNodeTest" }
    .configureEach { enabled = false }

mavenPublishing {
    publishToMavenCentral()

    if (
        project.hasProperty("signingInMemoryKey") ||
        project.hasProperty("signing.keyId")
    ) {
        signAllPublications()
    }

    coordinates("io.github.nadeemiqbal", "liquid-glass", libVersion)

    pom {
        name.set("LiquidGlass")
        description.set(
            "iOS 26-style frosted glass surfaces for Compose Multiplatform — backdrop blur, " +
                "chroma lift, and edge sheen as a Modifier, with auto-tiered memory-friendly " +
                "fallbacks for low-end Android and older iOS devices.",
        )
        inceptionYear.set("2026")
        url.set("https://github.com/NadeemIqbal/liquid-glass")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("NadeemIqbal")
                name.set("Nadeem Iqbal")
                email.set("mr_nadeem_iqbal@yahoo.com")
                url.set("https://github.com/NadeemIqbal")
            }
        }
        scm {
            url.set("https://github.com/NadeemIqbal/liquid-glass")
            connection.set("scm:git:git://github.com/NadeemIqbal/liquid-glass.git")
            developerConnection.set("scm:git:ssh://git@github.com/NadeemIqbal/liquid-glass.git")
        }
    }
}
