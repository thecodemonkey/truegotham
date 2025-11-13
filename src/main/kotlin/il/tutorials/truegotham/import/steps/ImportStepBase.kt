package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus

abstract class ImportStepBase() {
    abstract fun run(context: ImportContext)

    abstract fun status(): ImportStatus
}