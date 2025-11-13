package il.tutorials.truegotham.model.entity.incident

import il.tutorials.truegotham.repository.converter.StringListConverter
import jakarta.persistence.*
import java.util.*

@Entity
data class Incident(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "statement_id", length = 36)
    var statementID: UUID = id,

    @OneToMany(
        mappedBy = "incident",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val offenderProfiles: MutableList<IncidentOffenderProfile> = mutableListOf(),

    @OneToMany(
        mappedBy = "incident",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val locations: MutableList<IncidentLocation> = mutableListOf(),

    @Convert(converter = StringListConverter::class)
    val tools: MutableList<String> = mutableListOf(),

    @Convert(converter = StringListConverter::class)
    val evidence: MutableList<String> = mutableListOf(),

    @Column(columnDefinition = "TEXT")
    var motive: String? = null,

    @OneToMany(
        mappedBy = "incident",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val offences: MutableList<IncidentOffence> = mutableListOf()
) {

}
