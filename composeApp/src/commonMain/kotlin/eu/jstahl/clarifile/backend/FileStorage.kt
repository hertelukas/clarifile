package eu.jstahl.clarifile.backend

interface FileStorage {
    // Returns the relative path or new name of the saved file
    suspend fun saveFile(sourcePath: String, id: Long)

    // To read it back later
    fun getAbsolutePath(id: Long): String
}