package il.tutorials.truegotham.model.entity.incident

import il.tutorials.truegotham.model.Geocoordinates
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
class IncidentLocation(
    @Id
    val id: UUID,

    val unixts: Long,

    val statementID: UUID? = null,

    @Column(columnDefinition = "TEXT")
    var address: String? = null,

    @Column(columnDefinition = "TEXT")
    var addressFormal: String? = null,

    @Embedded
    val coordinates: Geocoordinates? = null,

    var main: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,
) {
    constructor() : this(UUID.randomUUID(), 0) {

    }
}