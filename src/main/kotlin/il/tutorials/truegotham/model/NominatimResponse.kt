package il.tutorials.truegotham.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NominatimResponse(
    @JsonProperty("place_id")
    val placeId: Long,

    val licence: String,

    @JsonProperty("osm_type")
    val osmType: String,

    @JsonProperty("osm_id")
    val osmId: Long,

    val lat: String,
    val lon: String,

    @JsonProperty("class")
    val clazz: String,  // "class" ist ein Keyword in Kotlin

    val type: String,

    @JsonProperty("place_rank")
    val placeRank: Int,

    val importance: Double,
    val addresstype: String,
    val name: String,

    @JsonProperty("display_name")
    val displayName: String,

    val boundingbox: List<String>
)