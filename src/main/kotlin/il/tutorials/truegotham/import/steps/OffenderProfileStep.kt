package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.Gender
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.incident.IncidentOffenderProfile
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component
import java.util.*

@Component
class OffenderProfileStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.OFFENDER_PROFILE_EXTRACTION

    override fun run(context: ImportContext) {
        val profilesResult = ai.extractOffenderProfiles(context.rawStatement.content!!)

        profilesResult.profiles.forEach { l ->
            context.incident.offenderProfiles.add(
                IncidentOffenderProfile(
                    id = UUID.randomUUID(),
                    age = l.age ?: -1,
                    location = l.housing_situation ?: "",
                    gender = Gender.MALE, //if (l.gender.isNullOrBlank()) Gender.MALE else Gender.FEMALE,
                    hair = l.hair ?: "",
                    behaviour = "",
                    drugTest = l.drugTest ?: false,
                    alcoholTest = l.alkoholTest ?: false,
                    summary = l.psychological_assessment ?: "",
                    motive = "",
                    look = l.look ?: "",
                    incident = context.incident
                )
            )
        }

        incidentRepo.save(context.incident)
    }


}