package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.extensions.toDataUrl
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.service.DistrictService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
class DistrictController(
    val districtService: DistrictService,
    val districtRepository: DistrictRepository
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
                       .toDataUrl("image/jpeg")

    @GetMapping("/api/districts/{id}/image")
    fun getDistrictImage(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val district = districtRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "District not found") }

        val image = district.image
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found")

        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(image)
    }

}