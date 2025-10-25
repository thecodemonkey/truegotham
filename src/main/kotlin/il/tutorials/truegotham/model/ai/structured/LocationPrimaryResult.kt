package il.tutorials.truegotham.model.ai.structured

import il.tutorials.truegotham.model.ai.PromptInfo

@PromptInfo(id = "pmpt_68ed348c3694819680edbf87fa1be4da0de4233a0ac3aee2")
data class LocationPrimaryResult(
    val location: String,
    val street: String,
    val district: String,
    val city: String
)
