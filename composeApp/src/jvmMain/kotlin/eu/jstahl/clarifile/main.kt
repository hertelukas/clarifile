package eu.jstahl.clarifile

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.frontend.App
import eu.jstahl.clarifile.database.getRoomDatabase // Import this!

private fun configureHiDpiScaling() {
    // Only apply if running under Wayland/Sway and no explicit override is set
    val isWayland = System.getenv("XDG_SESSION_TYPE")?.equals("wayland", ignoreCase = true) == true ||
            System.getenv("WAYLAND_DISPLAY") != null ||
            System.getenv("SWAYSOCK") != null

    if (!isWayland) return

    // Respect user-provided JVM properties
    val uiScaleProp = System.getProperty("sun.java2d.uiScale")
    val skikoScaleProp = System.getProperty("skiko.render.scale")

    // Try to infer scale from common toolkit env vars
    fun parseScale(varName: String): Double? = System.getenv(varName)?.trim()?.takeIf { it.isNotEmpty() }?.let {
        it.toDoubleOrNull()
    }

    // GDK_SCALE is integer most of the time, QT_SCALE_FACTOR can be fractional
    val candidates = listOf(
        parseScale("GDK_SCALE"),
        parseScale("GDK_DPI_SCALE"), // sometimes fractional adjustment
        parseScale("QT_SCALE_FACTOR"),
        parseScale("QT_SCREEN_SCALE_FACTORS") // if single monitor, may be a single value
    ).filterNotNull()

    val inferred = candidates.maxOrNull()?.takeIf { it > 0.0 }

    if (inferred != null) {
        if (uiScaleProp.isNullOrBlank()) {
            System.setProperty("sun.java2d.uiScale", inferred.toString())
        }
        if (skikoScaleProp.isNullOrBlank()) {
            System.setProperty("skiko.render.scale", inferred.toString())
        }
    }
}

fun main() = application {
    // Configure HiDPI scaling early for Wayland/Sway if needed
    configureHiDpiScaling()
    val database = remember {
        // 1. Get the platform-specific builder (knows where the file is)
        val builder = getDatabaseBuilder()

        // 2. Pass it to common code to attach the Driver and build it
        getRoomDatabase(builder)
    }

    val dao = remember { database.fileDao() }

    val storage = Storage(dao, DesktopFileStorage())

    Window(onCloseRequest = ::exitApplication, title = "Clarifile") {
        App(storage)
    }
}