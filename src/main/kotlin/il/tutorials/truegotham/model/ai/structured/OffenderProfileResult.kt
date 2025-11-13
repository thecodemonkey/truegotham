package il.tutorials.truegotham.model.ai.structured

import il.tutorials.truegotham.model.ai.PromptInfo

@PromptInfo(id = "pmpt_691214fb354c8190b89880be67d6527e00a0734888e3c959")
data class OffenderProfileResults(
    val profiles: List<OffenderProfileResult>
)



data class OffenderProfileResult(
    val age: Int?,
    val housing_situation: String?,
    val gender: String?,
    val hair: String?,
    val look: String?,
    val drugTest: Boolean?,
    val alkoholTest: Boolean?,
    val psychological_assessment: String?
)
