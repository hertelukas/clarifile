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

        return File(id, dao)
    }

    fun getFiles(fileRequest: FileRequest): Flow<List<File>> {
        val flow: Flow<List<Long>> = when {
            fileRequest.tags.isEmpty() -> dao.getAllFiles()

            fileRequest.tagOperator == LogicalOperator.Or ->
                dao.getFilesByAnyTag(fileRequest.tags)

            fileRequest.tagOperator == LogicalOperator.And ->
                dao.getFilesByAllTags(fileRequest.tags, fileRequest.tags.size)

            else -> dao.getAllFiles() // Fallback
        }

        return flow.map { files -> files.map { id -> File(id, dao) } }
    }

    fun getTags(): List<String> {
        return listOf("a", "b", "c")
    }

    fun getExtensions(): List<String> {
        return listOf("pdf", "txt", "jpg")
    }
}