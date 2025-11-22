package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao

class File(private val id: Long, private val dao: FileDao) {

    suspend fun setName(name: String) {
        return dao.updateNameByID(id, name)
    }

    suspend fun getName(): String {
        return dao.getFileNameByID(id)
    }

    fun getTags(): List<String> {
        return emptyList()
    }

    fun addTag(tag: String) {}
}