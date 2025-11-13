package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.IncidentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class IncidentController(val incidentService: IncidentService) {
    @GetMapping("/api/incident/{id}")
    fun loadCrimeCase(@PathVariable id: UUID) = incidentService.loadIncident(id)

    @PostMapping("/api/incident/locations/update")
    fun updateUngeocodedLocations() =
        incidentService.geocodeLocationsWithoutCoordinates()

}