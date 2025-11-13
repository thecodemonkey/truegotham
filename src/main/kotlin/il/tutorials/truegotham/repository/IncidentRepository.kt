package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.incident.Incident
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface IncidentRepository : JpaRepository<Incident, UUID> {

    fun findDistinctByLocationsCoordinatesIsNull(): List<Incident>

    @Transactional
    @Modifying
    @Query("""
        UPDATE IncidentLocation l 
        SET l.coordinates.lat = :latitude, l.coordinates.lon = :longitude 
        WHERE l.id = :locationId
    """)
    fun updateLocationCoordinates(locationId: UUID, latitude: Double, longitude: Double): Int


}