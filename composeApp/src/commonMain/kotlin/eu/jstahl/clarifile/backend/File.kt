package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao
import kotlinx.coroutines.flow.Flow

class File(private val id: Long, private val dao: FileDao) {

    suspend fun setName(name: String) {
        println("Setting name for file $id to $name")
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

    suspend fun removeAllTags() {
        dao.removeTags(id)
    }

    suspend fun setTags(tags: List<String>) {
        println("Setting tags for file $id to $tags")
        removeAllTags()
        tags.forEach { dao.addTagToFile(id, it) }
    }
}