package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.entity.incident.Incident
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.repository.StatementRepository
import org.springframework.stereotype.Component

@Component
class InitialImportStep(
    val incidentRepo: IncidentRepository,
    val statementRepo: StatementRepository
): ImportStepBase() {
    override fun status() = ImportStatus.IMPORTED

    override fun run(context: ImportContext) {
        val statement = Statement(
            id = context.rawStatement.id,
            unixts = context.rawStatement.unixts!!,
            content = context.rawStatement.content,
            url = context.rawStatement.url,
            title = context.rawStatement.title!!)

        context.statement =  statementRepo.save(statement)
        context.incident = incidentRepo.save(Incident(context.rawStatement.id))
    }
}