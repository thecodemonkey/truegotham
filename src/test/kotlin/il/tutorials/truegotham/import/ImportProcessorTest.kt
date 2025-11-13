package il.tutorials.truegotham.import

import il.tutorials.truegotham.import.steps.ImportStepBase
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.entity.incident.Incident
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.repository.StatementRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class ImportProcessorTest {

    private lateinit var importProcessor: ImportProcessor
    private lateinit var flow: ImportFlow
    private lateinit var importRepo: ImportRepository
    private lateinit var statementRepo: StatementRepository
    private lateinit var incidentRepo: IncidentRepository

    @BeforeEach
    fun setup() {
        flow = mockk()
        importRepo = mockk()
        statementRepo = mockk()
        incidentRepo = mockk()

        importProcessor = ImportProcessor(flow, importRepo, statementRepo, incidentRepo)
    }

    @Test
    fun `run should process next raw statement successfully`() {
        // Given
        val rawStatementId = UUID.randomUUID()
        val rawStatement = mockk<RawStatement>(relaxed = true) {
            every { id } returns rawStatementId
            every { status } returns ImportStatus.IMPORTED
        }
        val statement = mockk<Statement>(relaxed = true)
        val incident = mockk<Incident>(relaxed = true)

        val context = ImportContext(statement, rawStatement, incident)

        val mockStep = mockk<ImportStepBase>(relaxed = true) {
            every { status() } returns ImportStatus.IMPORTED
            every { run( context ) } just Runs
        }

        every { importRepo.findTopByOrderByUnixtsAsc() } returns rawStatement
        every { statementRepo.getReferenceById(rawStatementId) } returns statement
        every { incidentRepo.getReferenceById(rawStatementId) } returns incident
        every { flow.getStep(ImportStatus.IMPORTED) } returns mockStep
        every { flow.getNextStatus(ImportStatus.IMPORTED) } returns ImportStatus.CRIME_CLASSIFICATION
        every { importRepo.save(any()) } returns rawStatement

        // When
        importProcessor.runSingleStep()

        // Then
        verify(exactly = 1) { mockStep.run(any()) }
        verify(exactly = 1) {
            importRepo.save(match {
                it.status == ImportStatus.CRIME_CLASSIFICATION
            })
        }
//        verify(exactly = 0) {
//            importRepo.save(match {
//                //it.errored
//            })
//        }
    }

    @Test
    fun `run should not process when no raw statement available`() {
        // Given
        every { importRepo.findTopByOrderByUnixtsAsc() } returns null

        // When
        importProcessor.runSingleStep()

        // Then
        verify(exactly = 0) { flow.getStep(any()) }
        verify(exactly = 0) { importRepo.save(any()) }
    }

//    @Test
//    fun `run should not process when step is not found`() {
//        // Given
//        val rawStatement = mockk<RawStatement>(relaxed = true) {
//            every { status } returns ImportStatus.INITIAL
//        }
//
//        every { importRepo.findTopByOrderByUnixtsAsc() } returns rawStatement
//        every { flow.getStep(ImportStatus.INITIAL) } returns null
//
//        // When
//        importProcessor.run()
//
//        // Then
//        verify(exactly = 0) { importRepo.save(any()) }
//    }
//
//    @Test
//    fun `run should mark raw statement as errored when step throws exception`() {
//        // Given
//        val rawStatementId = UUID.randomUUID()
//        val rawStatement = mockk<RawStatement>(relaxed = true) {
//            every { id } returns rawStatementId
//            every { status } returns ImportStatus.INITIAL
//        }
//        val statement = mockk<Statement>(relaxed = true)
//        val incident = mockk<Incident>(relaxed = true)
//
//        val mockStep = mockk<ImportStepBase>(relaxed = true) {
//            every { status() } returns ImportStatus.INITIAL
//            every { run(any()) } throws RuntimeException("Test exception")
//        }
//
//        every { importRepo.findTopByOrderByUnixtsAsc() } returns rawStatement
//        every { statementRepo.getReferenceById(rawStatementId) } returns statement
//        every { incidentRepo.getReferenceById(rawStatementId) } returns incident
//        every { flow.getStep(ImportStatus.INITIAL) } returns mockStep
//        every { importRepo.save(any()) } returns rawStatement
//
//        // When
//        importProcessor.run()
//
//        // Then
//        verify(exactly = 1) { mockStep.run(any()) }
//        verify(exactly = 1) {
//            importRepo.save(match {
//                it.errored == true
//            })
//        }
//        verify(exactly = 0) {
//            importRepo.save(match {
//                it.status == ImportStatus.CRIME_CLASSIFICATION
//            })
//        }
//    }
//
//    @Test
//    fun `run should pass correct context to step`() {
//        // Given
//        val rawStatementId = UUID.randomUUID()
//        val rawStatement = mockk<RawStatement>(relaxed = true) {
//            every { id } returns rawStatementId
//            every { status } returns ImportStatus.INITIAL
//        }
//        val statement = mockk<Statement>(relaxed = true)
//        val incident = mockk<Incident>(relaxed = true)
//
//        val capturedContext = slot<ImportContext>()
//        val mockStep = mockk<ImportStepBase>(relaxed = true) {
//            every { status() } returns ImportStatus.INITIAL
//            every { run(capture(capturedContext)) } just Runs
//        }
//
//        every { importRepo.findTopByOrderByUnixtsAsc() } returns rawStatement
//        every { statementRepo.getReferenceById(rawStatementId) } returns statement
//        every { incidentRepo.getReferenceById(rawStatementId) } returns incident
//        every { flow.getStep(ImportStatus.INITIAL) } returns mockStep
//        every { flow.genNextStatus(ImportStatus.INITIAL) } returns ImportStatus.CRIME_CLASSIFICATION
//        every { importRepo.save(any()) } returns rawStatement
//
//        // When
//        importProcessor.run()
//
//        // Then
//        assert(capturedContext.captured.rawStatement == rawStatement)
//        assert(capturedContext.captured.statement == statement)
//        assert(capturedContext.captured.incident == incident)
//    }
//
//    @Test
//    fun `run should update status to next step after successful execution`() {
//        // Given
//        val rawStatement = mockk<RawStatement>(relaxed = true) {
//            every { id } returns UUID.randomUUID()
//            every { status } returns ImportStatus.CRIME_CLASSIFICATION
//        }
//        val statement = mockk<Statement>(relaxed = true)
//        val incident = mockk<Incident>(relaxed = true)
//
//        val mockStep = mockk<ImportStepBase>(relaxed = true) {
//            every { status() } returns ImportStatus.CRIME_CLASSIFICATION
//            every { run(any()) } just Runs
//        }
//
//        every { importRepo.findTopByOrderByUnixtsAsc() } returns rawStatement
//        every { statementRepo.getReferenceById(any()) } returns statement
//        every { incidentRepo.getReferenceById(any()) } returns incident
//        every { flow.getStep(ImportStatus.CRIME_CLASSIFICATION) } returns mockStep
//        every { flow.genNextStatus(ImportStatus.CRIME_CLASSIFICATION) } returns ImportStatus.SUMMARY_CREATION
//        every { importRepo.save(any()) } returns rawStatement
//
//        // When
//        importProcessor.run()
//
//        // Then
//        verify(exactly = 1) {
//            importRepo.save(match {
//                it.status == ImportStatus.SUMMARY_CREATION
//            })
//        }
//    }
//
//    @Test
//    fun `run should handle multiple steps in sequence when called multiple times`() {
//        // Given - First call
//        val rawStatement1 = mockk<RawStatement>(relaxed = true) {
//            every { id } returns UUID.randomUUID()
//            every { status } returns ImportStatus.INITIAL
//        }
//        val mockStep1 = mockk<ImportStepBase>(relaxed = true) {
//            every { status() } returns ImportStatus.INITIAL
//            every { run(any()) } just Runs
//        }
//
//        // Given - Second call
//        val rawStatement2 = mockk<RawStatement>(relaxed = true) {
//            every { id } returns UUID.randomUUID()
//            every { status } returns ImportStatus.CRIME_CLASSIFICATION
//        }
//        val mockStep2 = mockk<ImportStepBase>(relaxed = true) {
//            every { status() } returns ImportStatus.CRIME_CLASSIFICATION
//            every { run(any()) } just Runs
//        }
//
//        every { importRepo.findTopByOrderByUnixtsAsc() } returnsMany listOf(rawStatement1, rawStatement2)
//        every { statementRepo.getReferenceById(any()) } returns mockk(relaxed = true)
//        every { incidentRepo.getReferenceById(any()) } returns mockk(relaxed = true)
//        every { flow.getStep(ImportStatus.INITIAL) } returns mockStep1
//        every { flow.getStep(ImportStatus.CRIME_CLASSIFICATION) } returns mockStep2
//        every { flow.genNextStatus(ImportStatus.INITIAL) } returns ImportStatus.CRIME_CLASSIFICATION
//        every { flow.genNextStatus(ImportStatus.CRIME_CLASSIFICATION) } returns ImportStatus.SUMMARY_CREATION
//        every { importRepo.save(any()) } returns mockk(relaxed = true)
//
//        // When
//        importProcessor.run()
//        importProcessor.run()
//
//        // Then
//        verify(exactly = 1) { mockStep1.run(any()) }
//        verify(exactly = 1) { mockStep2.run(any()) }
//    }
}