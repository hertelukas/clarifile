package eu.jstahl.clarifile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import eu.jstahl.clarifile.backend.FileStorage
import eu.jstahl.clarifile.backend.GeoLocation
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

    override fun open(id: Long) {
        try {
            val file = File(getAbsolutePath(id))

            // Generate a content:// URI that grants temporary access
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Must match Manifest authorities
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMimeType(file: File): String {
        val ext = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
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

    override fun getGpsLocation(id: Long): GeoLocation? {
        return try {
            val file = File(getAbsolutePath(id))
            // Metadata-extractor works with standard Java Files
            val metadata = ImageMetadataReader.readMetadata(file)
            val gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            val location = gpsDir?.geoLocation

            println("GPS location: $location")

            if (location != null && !location.isZero) {
                GeoLocation(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            // Fails silently if file is not an image or has no metadata
            null
        }
    }
}