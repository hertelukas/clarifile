package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import eu.jstahl.clarifile.database.FileEntity

class Storage(private val dao: FileDao) {
    suspend fun addFile(path: String): File {
        val id = dao.insertFile(FileEntity(name = path))
        return File(id, dao)
    }

    fun getFile(fileRequest: FileRequest): List<File> {
        return emptyList()
    }

    fun getTags(): List<String> {
        return listOf("a", "b", "c")
    }
}