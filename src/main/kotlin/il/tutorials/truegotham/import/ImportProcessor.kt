package il.tutorials.truegotham.import

import il.tutorials.truegotham.import.steps.ImportStepBase
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.entity.incident.Incident
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.utils.DateUtils
import il.tutorials.truegotham.utils.DateUtils.elapsedFormatted
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ImportProcessor(
    val flow: ImportFlow,
    val importRepo: ImportRepository,
    val statementRepo: StatementRepository,
    val incidentRepo: IncidentRepository
) {

    fun run() {
        val start = System.currentTimeMillis()

        var status: ImportStatus? = ImportStatus.IMPORTED;

        println("IMPORT STARTED...")

        while (status != null && status != ImportStatus.FINISHED) {
            val nextRawStatement = importRepo.findTopByOrderByUnixtsAsc();
            status = runSingleStep(nextRawStatement)
        }

        if (status == null) {
            println("IMPORT ERRORED:     ${elapsedFormatted(start)}")
        } else {
            println("IMPORT FINISHED:    ${elapsedFormatted(start)}")
        }
    }

    fun runSingleStep(nextRawStatement: RawStatement?): ImportStatus? {
        var lastStatus: ImportStatus? = null;

        nextRawStatement?.let {
            val nextStep = flow.getStep(it.status!!)
            val statement = statementRepo.findByIdOrNull(it.id)
            val incident = incidentRepo.findByIdOrNull(it.id)

            nextStep?.let {
                lastStatus = runStep(nextStep, ImportContext(
                    rawStatement = nextRawStatement,
                    statement = statement ?: Statement(nextRawStatement.id, nextRawStatement.unixts!!),
                    incident = incident ?: Incident(nextRawStatement.id)
                ));
            }
        }

        return lastStatus
    }

    private fun runStep(step: ImportStepBase, context: ImportContext): ImportStatus? {
        println("step started:                   ${step.status()}")
        val start = System.currentTimeMillis()
        try {

            step.run(context);
            println("step finished:      ${elapsedFormatted(start).padEnd(10)}  ${step.status()} ")

            if (context.rawStatement.status == ImportStatus.REMOVE_IRRELEVANT) {
                println("irrelevant statement recognized. remove it.")
                importRepo.delete(context.rawStatement)
                statementRepo.delete(context.statement)
                incidentRepo.delete(context.incident)
                return ImportStatus.FINISHED;
            }

            //move process to nex step by updating the status...
            context.rawStatement.status = flow.getNextStatus(step.status())

            if (context.rawStatement.status != ImportStatus.FINISHED)
                importRepo.save(context.rawStatement)

            return context.rawStatement.status
        } catch (e: Exception) {
            println("step errored:  ${step.status()} in ${elapsedFormatted(start)} : ")
            e.printStackTrace()

            context.rawStatement.errored = true;
            importRepo.save(context.rawStatement)
            return null
        }
    }

    fun runNextFrom(unixts: Long) {

        println("START BATCH IMPORT FROM ${DateUtils.formatUnixTimestamp(unixts)}")
        val statements = importRepo.findAllByUnixtsGreaterThan(unixts)
        println("BATCH ITEMS FOUND: ${statements.size}")

        statements.forEach { nextRaw ->


            val start = System.currentTimeMillis()

            var status: ImportStatus? = ImportStatus.IMPORTED;

            println("START WHOLE IMPORT OF SINGLE ITEM: ${nextRaw.title}  ...")

            while (status != null && status != ImportStatus.FINISHED) {
                status = runSingleStep(nextRaw)
            }

            if (status == null) {
                println("FINISHED WHOLE IMPORT WITH ERRORS: ${elapsedFormatted(start)}")
            } else {
                println("FINISHED SUCCESSFUL THE WHOLE IMPORT: ${elapsedFormatted(start)}")
            }

        }

        println("FINISh BATCH IMPORT FROM.")
    }

}