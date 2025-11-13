package il.tutorials.truegotham.model.entity.incident

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.util.*

@Entity
data class IncidentOffence(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(columnDefinition = "TEXT")
    val text: String,

    val paragraph: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @JsonIgnore
    val incident: Incident
)
