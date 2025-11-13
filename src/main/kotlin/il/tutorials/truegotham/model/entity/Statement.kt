package il.tutorials.truegotham.model.entity

import il.tutorials.truegotham.repository.converter.StringListConverter
import jakarta.persistence.*
import java.util.UUID

@Entity
data class Statement(
    @Id
    val id: UUID,
    val unixts: Long,
    @Column(columnDefinition = "TEXT")
    val title: String,
    val url: String? = null,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Convert(converter = StringListConverter::class)
    var categories: List<String>? = mutableListOf(),
    var crime: Boolean = false,

    var address: String? = null,
    @Column(columnDefinition = "TEXT")
    var addressVerbose: String? = null,
    var district: String? = null,
    var city: String? = null,

    @Column(name = "image_id")
    var imageId: UUID? = null,
    @Column(columnDefinition = "TEXT")
    var summary: String? = null,

    @Column(columnDefinition = "boolean default false")
    var active: Boolean = false
){
    constructor() : this(UUID.randomUUID(), 0, "")
    constructor(id: UUID, unixts: Long) : this(id, unixts, "")
}