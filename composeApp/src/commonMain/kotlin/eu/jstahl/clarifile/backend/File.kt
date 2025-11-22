package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao

class File(private val id: Long, private val dao: FileDao) {

    fun setName(name: String) {}

    suspend fun getName(): String {
        return dao.getFileNameByID(id)
    }

    fun getTags(): List<String> {
        return emptyList()
    }

    fun addTag(tag: String) {}
}