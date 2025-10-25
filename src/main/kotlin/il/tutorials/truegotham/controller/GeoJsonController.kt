package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.GeoJson
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GeoJsonController {
    @GetMapping("/api/geojson/dortmund/districts")
    fun loadDistricts() = GeoJson.districtNames
}