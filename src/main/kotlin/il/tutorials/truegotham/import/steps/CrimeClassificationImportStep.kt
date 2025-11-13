package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.service.AIPromptService
import org.springframework.stereotype.Component

@Component
class CrimeClassificationImportStep(
    val ai: AIPromptService,
    val importRepo: ImportRepository,
    val statementRepo: StatementRepository
) : ImportStepBase() {
    override fun status() = ImportStatus.CRIME_CLASSIFICATION
    val NOT_CRIME_CATEGORIES = listOf("sonstiges", "Unfall", "Informationsmitteilung")

    override fun run(context: ImportContext) {
        val result = ai.classifyStatement(context.rawStatement.content!!);

        context.statement.categories = result.categories;
        context.statement.crime =
            result.categories.all { c -> NOT_CRIME_CATEGORIES.contains(c) }.not();

        if (!context.statement.crime) {

            println("statement is not a crime: ${context.statement.categories}");
            context.rawStatement.status = ImportStatus.REMOVE_IRRELEVANT;
        }

    }
}