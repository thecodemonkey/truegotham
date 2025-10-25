package il.tutorials.truegotham.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import java.util.*

@Entity
data class StatsData(
    @Id
    val id: UUID,
    val name: String,
    @Lob
    val data: String
) {
    constructor() : this(UUID.randomUUID(), "", "") { }
}
