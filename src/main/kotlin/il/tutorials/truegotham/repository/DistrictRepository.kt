package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.District
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository

interface DistrictRepository : JpaRepository<District, UUID> {
    fun findByName(name: String): District?
    fun findByCityAndName(city: String, name: String): District?
    fun findFirsByCityAndName(city: String, name: String): District?
    fun findFirstByCityIgnoreCaseAndNameIgnoreCase(city: String, name: String): District?
}
