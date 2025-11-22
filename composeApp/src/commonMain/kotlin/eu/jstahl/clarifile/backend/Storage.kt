package eu.jstahl.clarifile.backend

import eu.jstahl.clarifile.database.FileDao

class Storage(private val dao: FileDao) {
    fun addFile(path: String): File {
        return File("Fyl", emptyList())
    }

    fun getFile(fileRequest: FileRequest): List<File> {
        return emptyList()
    }

    fun getTags(): List<String> {
        return listOf("a", "b", "c")
    }
}