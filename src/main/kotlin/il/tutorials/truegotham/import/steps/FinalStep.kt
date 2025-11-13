package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.StatementRepository
import org.springframework.stereotype.Component

@Component
class FinalStep(
    val importRepo: ImportRepository,
    val statementRepo: StatementRepository
): ImportStepBase() {
    override fun status() = ImportStatus.FINALIZE

    override fun run(context: ImportContext) {
        importRepo.delete(context.rawStatement)

        context.statement.active = true;
        statementRepo.save(context.statement)
    }

}