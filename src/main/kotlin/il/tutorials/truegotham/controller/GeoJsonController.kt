package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.GeoJson
import il.tutorials.truegotham.service.GeocodingService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GeoJsonController(
    val geocoding: GeocodingService
) {
    @GetMapping("/api/geojson/dortmund/districts")
    fun loadDistricts() = GeoJson.districtNames

    @GetMapping("/api/geocoding")
    fun geocoding(@RequestParam(required = true) address: String) =
        geocoding.geocode(address)

    @GetMapping("/api/geocoding/intersections")
    fun geocodingIntersections(
        @RequestParam(required = true) address: String,
        @RequestParam(required = true) address2: String ) =
        geocoding.geocodeIntersections(address, address2)


}