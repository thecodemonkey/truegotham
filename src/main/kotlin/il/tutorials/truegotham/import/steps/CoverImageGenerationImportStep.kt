package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component
import java.util.*

@Component
class CoverImageGenerationImportStep(
    val ai: AIPromptService,
    val statementRepo: StatementRepository,
    val imageRepo: ImageRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.COVER_IMAGE_GENERATION;

    override fun run(context: ImportContext) {
        val imgDescResult = ai.generateStatementImageDescription(context.statement.content!!)
        val img = ai.generateStatementCoverImage(imgDescResult.result)

        val imgEntity = imageRepo.save(Image(UUID.randomUUID(), "image/jpeg", img))
        context.statement.imageId = imgEntity.id;

        statementRepo.save(context.statement);
    }


}