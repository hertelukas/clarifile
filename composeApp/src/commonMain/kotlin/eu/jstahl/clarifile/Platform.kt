package eu.jstahl.clarifile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform