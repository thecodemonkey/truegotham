package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.extensions.toDataUrl
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.service.AIPromptService
import il.tutorials.truegotham.service.DistrictService
import io.swagger.v3.oas.annotations.Parameter
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
class DistrictController(
        val districtService: DistrictService,
        val districtRepository: DistrictRepository,
        val imageRepo: ImageRepository,
        val ai: AIPromptService
) {
    @GetMapping("/api/districts/{city}/{name}")
    fun loadDistrict(@PathVariable city: String, @PathVariable name: String) =
            districtService.getDistrict(city, name)

    @PostMapping("/api/districts/{city}/{name}/image")
    fun generateImage(
            @PathVariable city: String,
            @PathVariable name: String,
            @RequestBody description: String,
    ) = districtService.generateDistrictImage(name, description).toDataUrl("image/jpeg")

    @GetMapping("/api/districts/{id}/image")
    fun getDistrictImage(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val image =
                imageRepo.findById(id).orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "District Image not found")
                }

        return ResponseEntity.ok().contentType(MediaType.valueOf(image.mime)).body(image.data)
    }

    @PostMapping("/api/districts/{city}/{name}/description")
    fun generateDistrictRawDescription(@PathVariable city: String, @PathVariable name: String) =
            ai.generateDistrictRawDescription(city, name)

    @PostMapping("/api/districts/{city}/description/final")
    fun generateDistrictFinalDescription(
            @PathVariable city: String,
            @RequestBody description: String
    ) = ai.generateDistrictDescription(city, description)

    @PostMapping("/api/districts/{id}/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateDistrictImage(@PathVariable id: UUID, @RequestPart("file") file: MultipartFile) =
            districtService.updateDistrictImage(id, file)

    @PostMapping(
            "/api/districts/{city}/{name}/image",
            consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun updateDistrictImage(
            @Parameter(description = "City name", example = "Dortmund") @PathVariable city: String,
            @PathVariable name: String,
            @RequestPart("file") file: MultipartFile
    ) = districtService.updateDistrictImage(city, name, file)
}
