package il.tutorials.truegotham.model.ai.structured

import il.tutorials.truegotham.model.ai.PromptInfo


@PromptInfo(id = "pmpt_68e2a2d406e881948622f7676db6f14f070b07eb75598604")
data class IncidentEventsResult(
    val locations: List<IncidentLocationResult>
)



data class IncidentLocationResult(
    val date: String,
    val address: String,
    //val address_formal: String,
    val city: String,
    val street: String,
    val street2: String,
    val place: String,

    val main: Boolean,
    val title: String,
    val description: String
)
