package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.DistrictService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DistrictController(
    val districtService: DistrictService
) {
    @GetMapping("/api/districts/{city}/{name}")
    fun loadDistrict(
        @PathVariable city: String,
        @PathVariable name: String
    ) = districtService.getDistrict(city, name)

    @GetMapping("/api/districts/{city}/{name}/image")
    fun generateImage(
        @PathVariable city: String,
        @PathVariable name: String
    ) = districtService.generateDistrictImage(name)

}