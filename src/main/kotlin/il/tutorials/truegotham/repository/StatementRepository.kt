package il.tutorials.truegotham.repository

import org.springframework.data.domain.Pageable
import il.tutorials.truegotham.model.entity.Statement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface StatementRepository : JpaRepository<Statement, UUID>, JpaSpecificationExecutor<Statement> {
    fun findAllByOrderByUnixtsDesc(pageable: Pageable): List<Statement>
}