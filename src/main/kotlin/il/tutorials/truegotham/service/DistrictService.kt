package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.ai.PromptRequest
import il.tutorials.truegotham.model.ai.promptRequest
import il.tutorials.truegotham.model.ai.structured.CrimeClassificationResult
import il.tutorials.truegotham.model.entity.District
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.utils.ValueContent
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.util.*

@Service
class DistrictService(
    val districtRepository: DistrictRepository,
    val imageRepo: ImageRepository,

    val aiService: AIService,
    val ai: AIPromptService
) {

    @ValueContent("classpath:prompts/district.image.prompt.txt")
    lateinit var DISTRICT_IMAGE_PROMPT: String

    fun getDistrict(city:String, name:String) : District? {
        var district = districtRepository.findFirsByCityAndName(city, name)
        if (district == null) {
            district = createDistrictImage(city, name)
        }

        return district;
    }

    fun createDistrictImage(city: String, name: String): District {
        val raw = ai.generateDistrictRawDescription(city, name)
        val final = ai.generateDistrictDescription(city, raw.toString())

        //val desc = aiService.prompt("gib mir eine Beschreibung des Stadtteils $name in der stadt $city")
        val imageData = generateDistrictImage(name, final.result)

        val image = imageRepo.save(Image(UUID.randomUUID(), "image/jpeg", imageData))

        val district = District(
            UUID.randomUUID(),
            city,
            name,
            final.result,
            image.id
        )

        return districtRepository.save(district)
    }

    fun generateDistrictImage(district: String, imageDescription: String) =
            aiService.generateImage(
                DISTRICT_IMAGE_PROMPT.replace("[IMG_DESC]", imageDescription))



}
