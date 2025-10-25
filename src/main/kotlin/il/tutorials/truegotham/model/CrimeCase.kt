package il.tutorials.truegotham.model

import java.util.*

data class CrimeCase(
    val id: UUID,
    val statementID: UUID,
    val suspicious: List<SuspiciousPerson>,
    val events: List<IncidentEvent>,
    val tools: List<String>,
    val evidence: List<String>,
    val violations: List<Violation>
)

data class SuspiciousPerson(
    val age: Int,
    val location: String,
    val gender: Gender,
    val hair: String,
    val behaviour: String,
    val drugTest: Boolean?,
    val alcoholTest: Boolean?
)

data class IncidentEvent(
    val unixts: Number,
    val address: String,
    val coordinates: Geocoordinates,
    val main: Boolean,
    val title: String,
    val description: String
)

data class Violation(
    val text: String,
    val paragraph: String?
)