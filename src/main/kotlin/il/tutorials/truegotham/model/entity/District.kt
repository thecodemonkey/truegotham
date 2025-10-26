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

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "BLOB")
    val image: ByteArray? = null
)
{
    constructor() : this(UUID.randomUUID())
}
