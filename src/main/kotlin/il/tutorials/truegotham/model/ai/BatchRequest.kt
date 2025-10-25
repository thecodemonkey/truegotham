package il.tutorials.truegotham.model.ai

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.openai.models.responses.ResponseCreateParams

@JsonIgnoreProperties(ignoreUnknown = true)
data class BatchRequest(
    val custom_id: String,
    val method: String,
    val url: String,
    val body: ResponseCreateParams.Body
)

