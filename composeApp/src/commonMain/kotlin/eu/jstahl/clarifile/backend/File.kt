package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao

class File(private val id: Long, private val dao: FileDao) {

    fun setName(name: String) {}

    fun getName(): String {
        return ""
    }

    fun getTags(): List<String> {
        return emptyList()
    }

    fun addTag(tag: String) {}
}