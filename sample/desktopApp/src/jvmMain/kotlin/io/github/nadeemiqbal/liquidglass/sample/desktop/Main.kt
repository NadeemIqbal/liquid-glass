package io.github.nadeemiqbal.liquidglass.sample.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.nadeemiqbal.liquidglass.sample.SampleApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LiquidGlass Sample",
        state = rememberWindowState(width = 480.dp, height = 800.dp),
    ) {
        SampleApp()
    }
}
