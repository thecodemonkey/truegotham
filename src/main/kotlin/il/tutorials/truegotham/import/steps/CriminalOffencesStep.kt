package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.incident.IncidentOffence
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component
import java.util.*

@Component
class CriminalOffencesStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.OFFENSES_EXTRACTION

    override fun run(context: ImportContext) {
        val offencesResult = ai.extractCriminalOffences(context.rawStatement.content!!)

        offencesResult.crimes.forEach {
                context.incident.offences.add(
                    IncidentOffence(
                        id = UUID.randomUUID(),
                        text = it.crime,
                        paragraph = it.paragraph_of_penal_code,
                        incident = context.incident
                    )
                )
        }

        incidentRepo.save(context.incident)
    }
}