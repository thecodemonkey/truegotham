package il.tutorials.truegotham.model.entity.incident

import com.fasterxml.jackson.annotation.JsonIgnore
import il.tutorials.truegotham.model.Gender
import jakarta.persistence.*
import java.util.*

@Entity
data class IncidentOffenderProfile(
    @Id
    val id: UUID = UUID.randomUUID(),

    val age: Int,

    val location: String,

    @Enumerated(EnumType.STRING)
    val gender: Gender,

    val hair: String,

    @Column(columnDefinition = "TEXT")
    val behaviour: String,

    val drugTest: Boolean = false,

    val alcoholTest: Boolean = false,

    @Column(columnDefinition = "TEXT")
    val look: String,


    @Column(columnDefinition = "TEXT")
    val summary: String,

    @Column(columnDefinition = "TEXT")
    val motive: String,

    @Column(name = "image_id")
    var imageId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @JsonIgnore
    val incident: Incident
)
