package il.tutorials.truegotham.model.entity

import jakarta.persistence.*
import jakarta.persistence.Entity
import java.util.*

@Entity
class District(
        @Id val id: UUID,
        val city: String? = null,
        val name: String? = null,
        @Column(columnDefinition = "TEXT") val description: String? = null,
        @Column(name = "image_id") val imageId: UUID? = null,

        // Fakten-Properties
        var population: Int? = null,
        @Column(columnDefinition = "TEXT") var location: String? = null,
        @Column(columnDefinition = "TEXT") var socialStructure: String? = null,
        @Column(columnDefinition = "TEXT") var senseOfSecurity: String? = null,
        @Column(columnDefinition = "TEXT") var migrationBackground: String? = null,
        @Column(columnDefinition = "TEXT") var ageStructure: String? = null
) {
    constructor() : this(UUID.randomUUID())
}
