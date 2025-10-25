package il.tutorials.truegotham.model.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import il.tutorials.truegotham.utils.JsonUtils

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchResponse(
    val id: String,
    @JsonProperty("custom_id")
    val customId: String,
    val response: Response?,
    val error: BatchError? = null
) {
    inline fun <reified T> output(): BatchResult<T>? {
        val body = response?.body
        val error = body?.error ?: this.error

        val outputText = body?.output
            ?.firstOrNull { it.type == "message" }
            ?.content?.firstOrNull { it.type == "output_text" }
            ?.text

        return when {
            outputText != null -> {
                BatchResult(
                    id = customId,
                    result = JsonUtils.fromJSON(outputText),
                    error = null
                )
            }
            error != null -> {
                BatchResult(
                    id = customId,
                    result = null,
                    error = error
                )
            }
            else -> {
                BatchResult(
                    id = customId,
                    result = null,
                    error = BatchError(
                        message = "No output or error found in response.",
                        type = "empty_response"
                    )
                )
            }
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Response(
    @JsonProperty("status_code")
    val statusCode: Int,
    @JsonProperty("request_id")
    val requestId: String,
    val body: BatchBody?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchBody(
    val id: String? = null,
    @JsonProperty("object")
    val obj: String? = null,
    @JsonProperty("created_at")
    val createdAt: Long? = null,
    val status: String? = null,

    // alle optionalen Felder, die in deinem Erfolgsfall-JSON vorkommen:
    val background: Boolean? = null,
    val billing: Map<String, Any>? = null,
    val incomplete_details: Any? = null,
    val instructions: List<BatchOutput>? = null,
    val max_output_tokens: Any? = null,
    val max_tool_calls: Any? = null,
    val model: String? = null,
    val parallel_tool_calls: Boolean? = null,
    val previous_response_id: String? = null,
    val prompt: Any? = null,
    val prompt_cache_key: Any? = null,
    val reasoning: Any? = null,
    val safety_identifier: Any? = null,
    val service_tier: String? = null,
    val store: Boolean? = null,
    val temperature: Double? = null,
    val text: Any? = null,
    val tool_choice: String? = null,
    val tools: List<Any>? = null,
    val top_logprobs: Int? = null,
    val top_p: Double? = null,
    val truncation: String? = null,
    val usage: Any? = null,
    val user: Any? = null,
    val metadata: Any? = null,

    // eigentliche AI-Ausgabe
    val output: List<BatchOutput>? = null,

    // Fehlerobjekt im Fehlerfall
    val error: BatchError? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchOutput(
    val id: String? = null,
    val type: String,
    val status: String? = null,
    val content: List<BatchContent>? = null,
    val role: String? = null,
    val summary: List<Any>? = null // nötig für "reasoning"-Output-Typ
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchContent(
    val type: String,
    val text: String? = null,
    val annotations: List<Any>? = null,
    val logprobs: List<Any>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchError(
    val message: String? = null,
    val type: String? = null,
    val param: String? = null,
    val code: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchResult<T>(
    val id: String,
    val result: T?,
    val error: BatchError? = null
)
