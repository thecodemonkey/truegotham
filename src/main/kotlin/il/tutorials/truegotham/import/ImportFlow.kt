package il.tutorials.truegotham.import

import il.tutorials.truegotham.import.steps.*
import il.tutorials.truegotham.model.ImportStatus
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component


@Component
class ImportFlow (
    val initialStep: InitialImportStep,
    val crimeClassificationStep: CrimeClassificationImportStep,
    val summaryStep: SummaryCreationImportStep,
    val coverImageStep: CoverImageGenerationImportStep,
    val locationsStep: LocationExtractionImportStep,
    val offenderProfileStep: OffenderProfileStep,
    val offenderProfileImageStep: OffenderProfileImageStep,
    val offencesStep: CriminalOffencesStep,
    val evidenceStep: EvidenceAndToolsStep,
    val motivationStep: IncidentMotivationStep,
    val finalStep: FinalStep
) {
    var steps: MutableList<ImportStepBase> = mutableListOf()

    @PostConstruct
    fun init() {
        steps.add(initialStep)
        steps.add(crimeClassificationStep)
        steps.add(summaryStep)
        steps.add(coverImageStep)
        steps.add(locationsStep)
        steps.add(offenderProfileStep)
        steps.add(offenderProfileImageStep)
        steps.add(offencesStep)
        steps.add(evidenceStep)
        steps.add(motivationStep)
        steps.add(finalStep)
    }

    fun getStep(status: ImportStatus) =
        steps.find { it.status() == status }


    fun getNextStatus(status: ImportStatus): ImportStatus {
        val s = steps.find { it.status() == status }

        if (s == null) throw Exception("STEP WITH STATUS '$status' NOT FOUND!")

        val i = steps.indexOf(s);

        return if (i == (steps.size-1))
                    ImportStatus.FINISHED
                else
                    steps[i+1].status()
    }
}