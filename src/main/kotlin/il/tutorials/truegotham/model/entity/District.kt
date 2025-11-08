package il.tutorials.truegotham.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.*
import java.util.*

@Entity
class District(
    @Id
    val id: UUID,

    val city: String? = null,
    val name: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "image_id")
    val imageId: UUID? = null
)
{
    constructor() : this(UUID.randomUUID())
}
