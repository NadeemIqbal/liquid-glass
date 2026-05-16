import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "liquidGlassSample"
        browser {
            commonWebpackConfig {
                outputFileName = "liquidGlassSample.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":sample:composeApp"))
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material3)
            }
        }
    }
}
