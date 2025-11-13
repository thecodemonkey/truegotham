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
class EvidenceAndToolsStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.EVIDENCE_RECOGNITION

    override fun run(context: ImportContext) {
        val res = ai.extractEvidenceAndTools(context.rawStatement.content!!)

        if(res.weapons.isNotEmpty())  context.incident.tools.addAll(res.weapons);
        if(res.evidence.isNotEmpty())  context.incident.evidence.addAll(res.evidence);

        incidentRepo.save(context.incident)
    }
}