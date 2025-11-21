package eu.jstahl.clarifile

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import eu.jstahl.clarifile.frontend.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Clarifile",
    ) {
        App()
    }
}