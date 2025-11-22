package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import eu.jstahl.clarifile.database.FileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Storage(private val dao: FileDao, private val fileStorage: FileStorage) {

    suspend fun addFile(path: String): File {
        val extension = path.substringAfterLast(".", "")

        val id = dao.insertFile(FileEntity(name = path, extension = extension))
        fileStorage.saveFile(path, id)

        return File(id, dao, fileStorage)
    }

    fun getFiles(fileRequest: FileRequest): Flow<List<File>> {
        val flow: Flow<List<Long>> = when {
            fileRequest.tags.isEmpty() -> dao.searchFilesByName(fileRequest.searchString)

            fileRequest.tagOperator == LogicalOperator.Or ->
                dao.getFilesByAnyTag(fileRequest.searchString, fileRequest.tags)

            fileRequest.tagOperator == LogicalOperator.And ->
                dao.getFilesByAllTags(fileRequest.searchString, fileRequest.tags, fileRequest.tags.size)

            else -> dao.getAllFiles() // Fallback
        }

        return flow.map { files -> files.map { id -> File(id, dao, fileStorage) } }
    }

    fun getTags(): Flow<List<String>> {
        return dao.getAllTags()
    }

    fun getExtensions(): List<String> {
        return listOf("pdf", "txt", "jpg")
    }
}