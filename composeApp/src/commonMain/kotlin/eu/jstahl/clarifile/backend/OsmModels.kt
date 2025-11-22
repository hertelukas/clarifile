package eu.jstahl.clarifile.backend

import kotlinx.serialization.Serializable

@Serializable
data class OsmResponse(
    val address: OsmAddress
)

@Serializable
data class OsmAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val country: String? = null
)

data class GeoLocation(val latitude: Double, val longitude: Double)