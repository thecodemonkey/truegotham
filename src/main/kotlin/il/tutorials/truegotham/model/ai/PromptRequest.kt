package il.tutorials.truegotham.model.ai

import com.openai.models.ChatModel

data class PromptRequest<T>(
    val content: String,
    val responseClass: Class<T>,

    val custom_id: String? = null,
    val id: String? = null,
    val version: String? = null,
    val variables: Map<String, String>? = null,
    val model: ChatModel? = null
) {
    private fun getAnnotation(): PromptInfo? = responseClass.getAnnotation(PromptInfo::class.java)
    fun getPromptID() = getAnnotation()?.id ?: id!!
    fun getPromptVersion() = getAnnotation()?.version
}

inline fun <reified T> promptRequest(
    content: String,
    id: String? = null,
    custom_id: String? = null,
    variables: Map<String, String>? = null,
    model: ChatModel? = null): PromptRequest<T> =
    PromptRequest(content = content,
        id = id,
        responseClass = T::class.java,
        custom_id = custom_id,
        variables = variables,
        model = model)