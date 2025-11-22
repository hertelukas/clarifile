package eu.jstahl.clarifile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import eu.jstahl.clarifile.backend.FileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class AndroidFileStorage(private val context: Context) : FileStorage {

    override suspend fun saveFile(sourcePath: String, id: Long) {
        withContext(Dispatchers.IO) {
            // 1. Create the folder: /data/user/0/eu.jstahl.clarifile/files/<id>/
            val destFolder = File(context.filesDir, id.toString())
            if (!destFolder.exists()) {
                destFolder.mkdirs()
            }

            val uri = Uri.parse(sourcePath)

            if (uri.scheme == "content") {
                // Case A: Content URI (e.g. picked from System File Picker)
                val fileName = getFileNameFromUri(uri)
                val destFile = File(destFolder, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw FileNotFoundException("Could not open stream for URI: $sourcePath")

            } else {
                // Case B: Raw File Path (e.g. from cache or temp file)
                // Strip "file://" prefix if present
                val cleanPath = if (sourcePath.startsWith("file://")) uri.path else sourcePath
                val sourceFile = File(cleanPath ?: sourcePath)

                if (sourceFile.exists()) {
                    val destFile = File(destFolder, sourceFile.name)
                    sourceFile.copyTo(destFile, overwrite = true)
                } else {
                    throw FileNotFoundException("Source file not found at: $sourcePath")
                }
            }
        }
    }

    override fun getAbsolutePath(id: Long): String {
        val folder = File(context.filesDir, id.toString())

        // Find the first file in the folder (ignoring subdirectories)
        val firstFile = folder.listFiles()?.firstOrNull { it.isFile }
            ?: throw FileNotFoundException("No file found for ID $id")

        return firstFile.absolutePath
    }

    // Helper to extract the real filename (e.g. "document.pdf") from a Content URI
    private fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }
}