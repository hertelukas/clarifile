package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import eu.jstahl.clarifile.database.FileEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class Storage(private val dao: FileDao, private val fileStorage: FileStorage) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    suspend fun addFile(path: String): File {
        val extension = path.substringAfterLast(".", "")

        val id = dao.insertFile(FileEntity(name = path, extension = extension))
        fileStorage.saveFile(path, id)

        val file = File(id, dao, fileStorage)

        CoroutineScope(Dispatchers.IO).launch {
            autoTagLocation(file)
        }

        return file
    }

    fun getFiles(fileRequest: FileRequest): Flow<List<File>> {
        val flow: Flow<List<Long>> = when {
            fileRequest.tags.isEmpty() -> 
                dao.searchFilesByName(fileRequest.searchString, fileRequest.extension)

            fileRequest.tagOperator == LogicalOperator.Or ->
                dao.getFilesByAnyTag(fileRequest.searchString, fileRequest.tags, fileRequest.extension)

            fileRequest.tagOperator == LogicalOperator.And ->
                dao.getFilesByAllTags(fileRequest.searchString, fileRequest.tags, fileRequest.tags.size, fileRequest.extension)

            else -> dao.getAllFiles() // Fallback
        }

        return flow.map { files -> files.map { id -> File(id, dao, fileStorage) } }
    }

    fun getTags(): Flow<List<String>> {
        return dao.getAllTags()
    }

    fun getExtensions(): Flow<List<String>> {
        return dao.getDistinctExtensions()
    }

    suspend fun autoTagLocation(file: File) {

        try {
            // 1. Extract GPS from file
            val location = file.getGpsLocation() ?: return

            // 2. Query OSM (Nominatim)
            // Note: Nominatim requires a User-Agent identifying the app
            val response = client.get("https://nominatim.openstreetmap.org/reverse") {
                parameter("lat", location.latitude)
                parameter("lon", location.longitude)
                parameter("format", "json")
                header(HttpHeaders.UserAgent, "Clarifile/1.0")
            }.body<OsmResponse>()

            // 3. Add tags
            val tags = listOfNotNull(
                response.address.city,
                response.address.town,
                response.address.village,
                response.address.country
            ).filter { it.isNotBlank() }

            tags.forEach { tag ->
                file.addTag(tag)
            }

        } catch (e: Exception) {
            // Silent failure is acceptable for auto-tagging feature
            e.printStackTrace()
        }
    }
}