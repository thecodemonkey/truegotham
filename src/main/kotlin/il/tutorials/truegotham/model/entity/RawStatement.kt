package il.tutorials.truegotham.model.entity

import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.repository.converter.StringListConverter
import jakarta.persistence.*
import java.util.*

@Entity
data class RawStatement(
    @Id
    val id: UUID,

    @Column(unique = true)
    val hash: String,

    val unixts: Long? = null,
    //val timestamp: String? = null,

    @Column(columnDefinition = "TEXT")
    val title: String? = null,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    val lfd_nr: Int? = null,

    val url: String? = null,

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    val topics: List<String>? = mutableListOf(),

    var status: ImportStatus? = null,


) {
    constructor() : this(UUID.randomUUID(), "")
}



