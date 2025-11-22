package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import kotlinx.coroutines.flow.Flow

class File(private val id: Long, private val dao: FileDao) {

    suspend fun setName(name: String) {
        return dao.updateNameByID(id, name)
    }

    suspend fun getName(): String {
        return dao.getFileNameByID(id)
    }

    fun getTags(): Flow<List<String>> {
        return dao.getFileTags(id)
    }

    suspend fun addTag(tag: String) {
        dao.addTagToFile(id, tag)
    }
}