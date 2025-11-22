package eu.jstahl.clarifile

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.application
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.frontend.App
import eu.jstahl.clarifile.database.getRoomDatabase

fun main() = application {
    val database = remember {
        // 1. Get the platform-specific builder (knows where the file is)
        val builder = getDatabaseBuilder()

        // 2. Pass it to common code to attach the Driver and build it
        getRoomDatabase(builder)
    }

    val dao = remember { database.fileDao() }

    val storage = Storage(dao, DesktopFileStorage())

    Window(
        onCloseRequest = ::exitApplication,
        title = "Clarifile",
    ) {
        App(storage)
    }
}