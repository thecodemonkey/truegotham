package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component

@Component
class IncidentMotivationStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.MOTIVATION_CREATION

    override fun run(context: ImportContext) {
        val motive = ai.generateIncidentMotivation(context.statement.content!!)
        context.incident.motive = motive.result;
        incidentRepo.save(context.incident)
    }
}