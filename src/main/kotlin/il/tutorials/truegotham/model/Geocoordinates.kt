package il.tutorials.truegotham.model

import jakarta.persistence.Embeddable

@Embeddable
data class Geocoordinates(
    var lat: Double,
    var lon: Double
) {
    constructor() : this(0.0, 0.0) {

    }
}
