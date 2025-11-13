package il.tutorials.truegotham.import

import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.entity.incident.Incident

data class ImportContext(
    var statement: Statement,
    var rawStatement: RawStatement,
    var incident: Incident
)
