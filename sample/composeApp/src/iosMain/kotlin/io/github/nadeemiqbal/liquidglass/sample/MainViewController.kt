package io.github.nadeemiqbal.liquidglass.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** iOS entry point — consumed by the sample/iosApp Xcode project. */
fun MainViewController(): UIViewController = ComposeUIViewController { SampleApp() }
