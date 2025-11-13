package il.tutorials.truegotham.model.entity.incident

import com.fasterxml.jackson.annotation.JsonIgnore
import il.tutorials.truegotham.model.Geocoordinates
import jakarta.persistence.*
import java.util.*

@Entity
class IncidentLocation(
    @Id
    val id: UUID,

    val unixts: Long,

    @Column(columnDefinition = "TEXT")
    var address: String? = null,

    @Column(columnDefinition = "TEXT")
    var addressFormal: String? = null,

    var city: String? = null,
    var street: String? = null,
    var street2: String? = null,
    var place: String? = null,

    @Embedded
    val coordinates: Geocoordinates? = null,

    var main: Boolean = false,

    @Column(columnDefinition = "TEXT")
    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @JsonIgnore
    val incident: Incident
)