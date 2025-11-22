package eu.jstahl.clarifile.utils

import io.github.vinceglb.filekit.core.PlatformFile

actual fun PlatformFile.getStoragePath(): String {
    // On Desktop, path is a standard filesystem path.
    // We use the Elvis operator just in case, though it shouldn't be null on JVM.
    return this.path ?: ""
}