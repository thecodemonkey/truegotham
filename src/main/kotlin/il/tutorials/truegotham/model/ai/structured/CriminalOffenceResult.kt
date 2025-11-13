package il.tutorials.truegotham.model.ai.structured

import il.tutorials.truegotham.model.ai.PromptInfo

@PromptInfo(id = "pmpt_6915b604563481908b049b04e3f81b05071d1d37df986d2d")
class CriminalOffenceResult (
    val crimes: List<SingleCrimeResult>
)

data class SingleCrimeResult(
    val crime: String,
    val paragraph_of_penal_code: String?
)