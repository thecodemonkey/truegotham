package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component

@Component
class SummaryCreationImportStep(
    val ai: AIPromptService,
    val statementRepo: StatementRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.SUMMARY_CREATION

    override fun run(context: ImportContext) {
        val summaryResult = ai.generateStatementSummary(context.statement.content!!)
        context.statement.summary = summaryResult.result;
        statementRepo.save(context.statement)
    }
}