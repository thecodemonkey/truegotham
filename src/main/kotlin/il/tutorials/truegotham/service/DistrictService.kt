package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.entity.District
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.utils.ValueContent
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Service
class DistrictService(
        val districtRepository: DistrictRepository,
        val imageRepo: ImageRepository,
        val aiService: AIService,
        val ai: AIPromptService
) {

    @ValueContent("classpath:prompts/district.image.prompt.txt")
    lateinit var DISTRICT_IMAGE_PROMPT: String

    fun getDistrict(city: String, name: String): District? {
        val district = districtRepository.findFirstByCityIgnoreCaseAndNameIgnoreCase(city, name)
        if (district == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "District not found")
        }

        return district
    }

    fun updateDistrictFacts(city: String, name: String): District {
        val district =
                districtRepository.findFirstByCityIgnoreCaseAndNameIgnoreCase(city, name)
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "District not found")

        val raw = ai.generateDistrictRawDescription(city, name)

        // Mapping AI Result to District Entity
        district.location = raw.location
        district.migrationBackground = raw.migration_background
        district.socialStructure = raw.social_structure
        district.senseOfSecurity = raw.sense_of_security
        district.ageStructure = raw.ageStructure
        district.population = raw.population

        return districtRepository.save(district)
    }

    fun updateAllMissingDistrictFacts(): List<District> {
        val allDistricts = districtRepository.findAll()
        val toUpdate =
                allDistricts.filter {
                    it.location.isNullOrBlank() || it.socialStructure.isNullOrBlank()
                }

        return toUpdate.map { district ->
            runCatching { updateDistrictFacts(district.city ?: "Dortmund", district.name ?: "") }
                    .getOrElse {
                        println("Failed to update facts for ${district.name}: ${it.message}")
                        district
                    }
        }
    }

    fun createDistrictImage(city: String, name: String): District {
        val raw = ai.generateDistrictRawDescription(city, name)
        val final = ai.generateDistrictDescription(city, raw.toString())

        // val desc = aiService.prompt("gib mir eine Beschreibung des Stadtteils $name in der stadt
        // $city")
        val imageData = generateDistrictImage(name, final.result)

        val image = imageRepo.save(Image(UUID.randomUUID(), "image/jpeg", imageData))

        val district = District(UUID.randomUUID(), city, name, final.result, image.id)

        return districtRepository.save(district)
    }

    fun generateDistrictImage(district: String, imageDescription: String) =
            aiService.generateImage(DISTRICT_IMAGE_PROMPT.replace("[IMG_DESC]", imageDescription))

    fun updateDistrictImage(id: UUID, file: MultipartFile) {
        val district =
                districtRepository.findById(id).orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "District not found")
                }
        updateDistrictImage(district, file)
    }

    fun updateDistrictImage(city: String, name: String, file: MultipartFile) {
        val district =
                districtRepository.findFirstByCityIgnoreCaseAndNameIgnoreCase(city, name)
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "District not found")
        updateDistrictImage(district, file)
    }

    private fun updateDistrictImage(district: District, file: MultipartFile) {
        if (district.imageId == null) {
            throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "District has no image to update"
            ) // "keine neue anlegen"
        }

        val image =
                imageRepo.findById(district.imageId).orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found")
                }

        val updatedImage = Image(image.id, file.contentType ?: "image/jpeg", file.bytes)

        imageRepo.save(updatedImage)
    }
}
