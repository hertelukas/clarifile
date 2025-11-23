package eu.jstahl.clarifile.utils

import io.github.vinceglb.filekit.core.PlatformFile

/**
 * On iOS, `PlatformFile.path` should resolve to a local file-system path for the selected file
 * (typically a security-scoped URL the library resolves for us). We return that path so the
 * platform-specific `IosFileStorage` can copy it into the app sandbox.
 */
actual fun PlatformFile.getStoragePath(): String {
    return this.path ?: ""
}
