package eu.jstahl.clarifile

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.GpsDirectory
import eu.jstahl.clarifile.backend.FileStorage
import eu.jstahl.clarifile.backend.GeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.*
import kotlin.io.path.*

class DesktopFileStorage : FileStorage {

    private val dataFolder: Path by lazy {
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()

        val root = when {
            os.contains("win") -> Paths.get(System.getenv("APPDATA"))
            os.contains("mac") -> Paths.get(userHome, "Library", "Application Support")
            else -> Paths.get(userHome, ".local", "share")
        }
        root.resolve("Clarifile").apply { createDirectories() }
    }

    override suspend fun saveFile(sourcePath: String, id: Long) {
        val source = Paths.get(sourcePath)
        val fileFolder = dataFolder.resolve(id.toString())

        val dest = fileFolder.resolve(source.fileName)

        withContext(Dispatchers.IO) {
            if (!fileFolder.exists()) {
                Files.createDirectories(fileFolder)
            }
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun getAbsolutePath(id: Long): String {
        val folder = dataFolder.resolve(id.toString())


        return Files.list(folder).use { stream ->
            stream.findFirst()
                .map { it.toAbsolutePath().toString() }
                .orElseThrow { FileNotFoundException("No file found for ID $id") }
        }
    }

    override fun open(id: Long) {
        try {
            val path = getAbsolutePath(id)
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(File(path))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getGpsLocation(id: Long): GeoLocation? {
        println("Getting GPS location for ID $id")
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