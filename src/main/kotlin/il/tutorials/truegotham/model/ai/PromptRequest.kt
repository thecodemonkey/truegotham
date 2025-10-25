package il.tutorials.truegotham.model.ai

data class PromptRequest<T>(
    val content: String,
    val responseClass: Class<T>,

    val custom_id: String? = null,
    val id: String? = null,
    val version: String? = null
) {
    private fun getAnnotation(): PromptInfo? = responseClass.getAnnotation(PromptInfo::class.java)
    fun getPromptID() = getAnnotation()?.id!!
    fun getPromptVersion() = getAnnotation()?.version
}

inline fun <reified T> promptRequest(content: String, custom_id: String? = null): PromptRequest<T> =
    PromptRequest(content, T::class.java, custom_id)