package io.github.nadeemiqbal.liquidglass.sample.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.nadeemiqbal.liquidglass.sample.SampleApp
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        SampleApp()
    }
}
