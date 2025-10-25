package il.tutorials.truegotham.model.ai

data class OpenAIPromptResponse(
    val id: String,
    val content: String,
    val model: String? = null,
    val createdAt: Double? = null
)