package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import eu.jstahl.clarifile.database.FileEntity

class Storage(private val dao: FileDao, private val fileStorage: FileStorage) {

    suspend fun addFile(path: String): File {
        val extension = path.substringAfterLast(".", "")

        val id = dao.insertFile(FileEntity(name = path, extension = extension))
        fileStorage.saveFile(path, id)

        return File(id, dao)
    }

    fun getFile(fileRequest: FileRequest): List<File> {
        return emptyList()
    }

    fun getTags(): List<String> {
        return listOf("a", "b", "c")
    }

    fun getExtensions(): List<String> {
        return listOf("pdf", "txt", "jpg")
    }
}