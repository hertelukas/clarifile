package eu.jstahl.clarifile.backend

object Storage {
    fun addFile(path: String): File {
        return File("Fyl", emptyList())
    }

    fun getFile(fileRequest: FileRequest): List<File> {
        return emptyList()
    }
}