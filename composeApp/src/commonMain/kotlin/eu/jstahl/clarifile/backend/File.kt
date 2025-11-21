package eu.jstahl.clarifile.backend

class File(private var name: String, private var tags: List<String>, private val id: Int = 0) {

    fun setName(name: String) {}

    fun getName(): String = name

    fun getTags(): List<String> = tags

    fun addTag(tag: String) {}
}