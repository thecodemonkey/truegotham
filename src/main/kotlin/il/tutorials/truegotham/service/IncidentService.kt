package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.CrimeCase
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class IncidentService(
    val incidentRepository: IncidentRepository,
    val geocodingService: GeocodingService
) {
    fun loadIncident(id: UUID) =
        incidentRepository.findByIdOrNull(id)



    @Transactional
    fun geocodeLocationsWithoutCoordinates() {
        val incidents = incidentRepository.findDistinctByLocationsCoordinatesIsNull()

        incidents.forEach { i ->
            i.locations.forEach { loc ->
                loc.addressFormal?.let {
                    println("try to geocode address: $it")
                    Thread.sleep(2000)
                    geocodingService.geocode(it)?.let { coords ->
                        println("geocoding result: $coords. \n update...")
                        incidentRepository.updateLocationCoordinates(
                            loc.id,
                            coords.lat,
                            coords.lon
                        )
                    }
                }


            }
        }
    }
}
