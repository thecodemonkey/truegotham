package il.tutorials.truegotham.model.entity

import jakarta.persistence.*
import java.util.*

@Entity
class Image(
    @Id
    val id: UUID,

    val mime: String,

    @Lob
    @Column(columnDefinition = "BLOB")
    val data: ByteArray? = null
) {
    constructor() : this(UUID.randomUUID(), "image/png") { }
}
