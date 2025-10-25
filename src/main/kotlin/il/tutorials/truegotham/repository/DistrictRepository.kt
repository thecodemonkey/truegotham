package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.District
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface DistrictRepository : JpaRepository<District, UUID> {
    fun findByName(name: String): District?
    fun findByCityAndName(city: String, name: String): District?
}