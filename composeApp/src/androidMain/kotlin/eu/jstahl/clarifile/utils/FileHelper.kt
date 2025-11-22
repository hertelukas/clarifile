package eu.jstahl.clarifile.utils

import io.github.vinceglb.filekit.core.PlatformFile

actual fun PlatformFile.getStoragePath(): String {
    // On Android, PlatformFile wraps a Uri. We need the string representation
    // so AndroidFileStorage can parse it back and use ContentResolver.
    return this.uri.toString()
}