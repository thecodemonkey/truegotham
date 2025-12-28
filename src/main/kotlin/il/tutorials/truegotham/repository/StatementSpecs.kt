package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.Statement
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

object StatementSpecs {

    fun filter(
            search: String?,
            categories: List<String>?,
            districts: List<String>?,
            active: Boolean? = null
    ): Specification<Statement> = Specification { root, query, cb ->
        val predicates = mutableListOf<Predicate>()

        active?.let { predicates += cb.equal(root.get<Boolean>("active"), it) }

        // Volltextsuche (Sicher gegen SQL-Injection durch Verwendung der JPA Criteria API mit
        // Parametern)
        search?.takeIf { it.isNotBlank() }?.let {
            val queryStr = it.trim().lowercase()
            val like = "%$queryStr%"
            predicates +=
                    cb.or(
                            cb.like(cb.lower(root.get("title")), like),
                            cb.like(cb.lower(root.get("content")), like),
                            cb.like(cb.lower(root.get("address")), like),
                            cb.like(cb.lower(root.get("district")), like),
                            cb.like(cb.lower(root.get("addressVerbose")), like)
                    )
        }

        // Kategorien (wenn als CSV-String gespeichert)
        categories?.takeIf { it.isNotEmpty() }?.let {
            val catPredicates =
                    it.map { cat ->
                        cb.like(cb.lower(root.get<String>("categories")), "%${cat.lowercase()}%")
                    }
            predicates += cb.or(*catPredicates.toTypedArray())
        }

        // Districts
        districts?.takeIf { it.isNotEmpty() }?.let {
            val distPredicates =
                    it.map { d -> cb.equal(cb.lower(root.get<String>("district")), d.lowercase()) }
            predicates += cb.or(*distPredicates.toTypedArray())
        }

        query?.orderBy(cb.desc(root.get<Long>("unixts")))
        cb.and(*predicates.toTypedArray())
    }
}
