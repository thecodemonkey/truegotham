package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.entity.District
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.utils.ValueContent
import org.springframework.stereotype.Service
import java.util.*

@Service
class DistrictService(
    val districtRepository: DistrictRepository,
    val aiService: AIService,
) {

    @ValueContent("classpath:prompts/district.image.city.txt")
    lateinit var DISTRICT_IMAGE_PROMPT: String

    fun getDistrict(city:String, name:String) : District? {
        var district = districtRepository.findByCityAndName(city, name)
        if (district == null) {
            val desc = aiService.prompt("gib mir eine Beschreibung des Stadtteils $name in der stadt $city")
            val img = generateDistrictImage(name)

            district = District(
                UUID.randomUUID(),
                city,
                name,
                desc,
                img
            )

            districtRepository.save(district)
        }

        return district;
    }

    fun generateDistrictImage(district: String) =
            aiService.generateImage(DISTRICT_IMAGE_PROMPT)

}
