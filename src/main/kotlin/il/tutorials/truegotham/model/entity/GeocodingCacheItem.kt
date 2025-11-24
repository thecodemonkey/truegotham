package il.tutorials.truegotham.model.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id


@Entity
data class GeocodingCacheItem(

    @Id
    @Column(unique = true, columnDefinition = "TEXT")
    val address: String,
    val latitude: Double,
    val longitude: Double
) {

}
