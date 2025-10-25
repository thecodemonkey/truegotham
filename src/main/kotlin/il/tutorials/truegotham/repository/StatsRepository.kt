package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.StatsData
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface StatsRepository : JpaRepository<StatsData, UUID> {
    fun findByName(name: String): StatsData?
}