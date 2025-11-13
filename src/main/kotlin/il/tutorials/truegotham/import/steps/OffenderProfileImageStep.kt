package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.ai.structured.OffenderProfileResult
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class OffenderProfileImageStep(
    val ai: AIPromptService,
    val imageRepository: ImageRepository,
    val incidentRepo: IncidentRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.OFFENDER_PROFILE_IMAGE_GENERATION

    override fun run(context: ImportContext) {
        var profiles: List<OffenderProfileResult> =
                context.incident.offenderProfiles.map {
                    OffenderProfileResult(
                        age = it.age,
                        housing_situation = it.location,
                        gender = it.gender.toString(),
                        hair = it.hair,
                        look = it.look,
                        drugTest = it.drugTest,
                        alkoholTest = it.alcoholTest,
                        psychological_assessment = it.summary
                    )
                }

        val multiple = profiles.size > 1

        val profilesJSON = JsonUtils.toJSON(
            if (multiple) profiles else profiles.first()
        )

        val primgbtarray = ai.generateOffenderImage(profilesJSON, multiple)

        val imgProfileEntity = imageRepository.save(
            Image(UUID.randomUUID(), "image/jpeg", primgbtarray)
        )

        context.incident.offenderProfiles.firstOrNull()?.let {
            it.imageId = imgProfileEntity.id;
        }

        incidentRepo.save(context.incident)
    }


}