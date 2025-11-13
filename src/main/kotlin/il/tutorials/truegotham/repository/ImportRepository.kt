package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.RawStatement
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ImportRepository : JpaRepository<RawStatement, UUID> {
    fun findTopByOrderByUnixtsDesc(): RawStatement?
    fun findTopByOrderByUnixtsAsc(): RawStatement?

    fun findTopByStatusOrderByUnixtsAsc(status: ImportStatus): RawStatement?

    // find first element where the status IS NOT equal to the passed status
    fun findTopByStatusNotOrderByUnixtsAsc(status: ImportStatus): RawStatement?
}