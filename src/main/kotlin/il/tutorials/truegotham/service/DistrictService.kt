package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.entity.District
import il.tutorials.truegotham.repository.DistrictRepository
import il.tutorials.truegotham.utils.ByteUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
class DistrictService(
    val districtRepository: DistrictRepository,
    val aiService: AIService
) {
    fun getDistrict(city:String, name:String) : District {

        val desc = aiService.prompt("gib mir eine Beschreibung des stadtteils $name in der stadt $city")

        return District(
            UUID.randomUUID(),
            "Gotham",
            name,
            desc)

        //districtRepository.findByName(name)
    }

    fun generateDistrictImage(district: String): String? {
        val prompt = """ 
Erstelle eine Photorealistische Schwarz/Weiß-Illustration im düsteren Batman-Cartoon-/Comic-Stil (ohne Panels oder Sprechblasen, nur der Zeichenstil).

Wichtige Vorgaben:

- Schwarz/Weiß-Stil, starke Kontraste, dramatische Schatten.
- Photorealistischer Look
- Batman darf in der szene nicht vorkommen!
- Die Szenen sollen möglichst realistisch sein, also keine fiktiven Handlungen, Carachtere oder Gegenstände enthalten.


Hier ist die Szene:

Inhalt: Szene in Dortmund Hohensyburg mit dem Casino auf dem Berg. Der Blick vom Hengstersee auf das Leuchtende Casino auf dem Berg von Hohensyburg. Im Vordergrund ist ein düsterer See mit gruselligen Bäumen und im Hintegrund der Berg mit dem Leutenden Casino oben drauf. 

Das Casino gebäude sollte möglichst so aussehen wie auf dem Bild. Die Perspektive sollte so sein wie in der Beschreibung!            
        """.trimIndent();

        return ByteUtils.toDataUrl(aiService.generateImage(prompt), "image/jpeg")
    }
}