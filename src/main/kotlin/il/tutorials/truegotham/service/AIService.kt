package il.tutorials.truegotham.service


import com.openai.client.OpenAIClient
import com.openai.core.JsonSchemaLocalValidation
import com.openai.core.JsonValue
import com.openai.models.ChatModel
import com.openai.models.Reasoning
import com.openai.models.ReasoningEffort
import com.openai.models.batches.Batch
import com.openai.models.batches.BatchCreateParams
import com.openai.models.batches.BatchCreateParams.CompletionWindow
import com.openai.models.batches.BatchCreateParams.Endpoint
import com.openai.models.files.FileCreateParams
import com.openai.models.files.FileObject
import com.openai.models.files.FilePurpose
import com.openai.models.images.*
import com.openai.models.responses.ResponseCreateParams
import com.openai.models.responses.ResponsePrompt
import com.openai.models.responses.StructuredResponse
import com.openai.models.responses.StructuredResponseCreateParams

import il.tutorials.truegotham.model.ai.*
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Base64
import java.util.UUID


@Service
class AIService(
    private val openAIClient: OpenAIClient
) {

    fun prompt(message: String): String{
        println { "Creating response for plain text prompt" }

        try {
            val params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5_NANO)
                .reasoning(
                    Reasoning.builder()
                        .summary(null)
                        .effort(ReasoningEffort.MEDIUM)
                        .build()
                )
                .input(message.trimIndent())
                .build()

            val response = openAIClient.responses().create(params)

            val result = response.output().stream()
                .flatMap { it.message().stream() }
                .flatMap { it.content().stream() }
                .flatMap { it.outputText().stream() }
                .findFirst()
                .orElseThrow { RuntimeException("No response content found") }

            return result.text()
        } catch (e: Exception) {
            println { "Error creating OpenAI response" }
            throw RuntimeException("Failed to create OpenAI response: ${e.message}", e)
        }
    }

    //generate image via gpt-image-1-mini
    fun generateImage(prompt: String): ByteArray {

        try {

            val response: ImagesResponse = openAIClient.images()
                .generate(
                    ImageGenerateParams.builder()
                        .model("gpt-image-1-mini")
                        .prompt(prompt)
                        .n(1)
                        .outputFormat(ImageGenerateParams.OutputFormat.JPEG)
                        .size(ImageGenerateParams.Size._1024X1024)
                        .quality(ImageGenerateParams.Quality.MEDIUM)
                        .build()
                )

            val img = response.data().get()[0];

            return Base64.getDecoder().decode(img.b64Json().get())
        } catch (e: Exception) {
            println { "Error generating image" }
            throw RuntimeException("Failed to generate image: ${e.message}", e)
        }
    }

    fun <T : Any> prompt(promptRequest: PromptRequest<T>): T {
        println { "Creating response for prompt: ${promptRequest.getPromptID()}, version: ${promptRequest.getPromptVersion()}" }

        try {
            val params = structuredResponseCreateParams(promptRequest)
            val response = structuredResponse(params)
            val result = readResponse(response)

            return result
        } catch (e: Exception) {
            println { "Error creating OpenAI response" }
            throw RuntimeException("Failed to create OpenAI response: ${e.message}", e)
        }
    }

    private fun <T : Any> readResponse(response: StructuredResponse<T>): T =
        response.output().stream()
            .flatMap { it.message().stream() }
            .flatMap { it.content().stream() }
            .flatMap { it.outputText().stream() }
            .findFirst()
            .orElseThrow { RuntimeException("No response content found") }

    private fun <T : Any> structuredResponse(params: StructuredResponseCreateParams<T>): StructuredResponse<T> =
        openAIClient.responses().create(params)

    private fun <T : Any> structuredResponseCreateParams(promptRequest: PromptRequest<T>): StructuredResponseCreateParams<T> =
        ResponseCreateParams.builder()
            .model(promptRequest.model ?: ChatModel.GPT_5_NANO)
            .prompt(
                ResponsePrompt.builder()
                    .id(promptRequest.getPromptID())
                    .version(promptRequest.getPromptVersion())
                    .variables(
                        promptRequest.variables?.mapValues { JsonValue.from(it.value) }?.let {
                            ResponsePrompt.Variables.builder()
                                .putAllAdditionalProperties(it)
                                .build()
                        }
                    )
                    .build()
            )
            .store(false)
            .reasoning(
                if (promptRequest.model != ChatModel.GPT_4_1)
                    Reasoning.builder()
                        .summary(null)
                        .effort(ReasoningEffort.LOW)
                        .build()
                else null
            )
            .text(promptRequest.responseClass,
                // no local validation for string responses
                if (promptRequest.responseClass == String::class.java)
                    JsonSchemaLocalValidation.NO
                else
                    JsonSchemaLocalValidation.YES
            )
            .input(promptRequest.content.trimIndent())
            .build()

    fun <T : Any> createAndUploadBatch(prompts: List<PromptRequest<T>>) : Batch {
        val requests = prompts.map { createBatchRequest(it) }
        val jsonl = requests.map { JsonUtils.toJSON(it, "isValid") }
                            .joinToString("\n")

        return uploadBatch(jsonl)
    }

    fun <T : Any> createBatchRequest(promptRequest: PromptRequest<T>): BatchRequest {
        val params = ResponseCreateParams.builder()
            .model(ChatModel.GPT_5_NANO)
            .prompt(
                ResponsePrompt.builder()
                    .id(promptRequest.getPromptID())
                    .version(promptRequest.getPromptVersion())
                    .build()
            )
            .store(false)
            .reasoning(
                Reasoning.builder()
                    .summary(null)
                    .effort(ReasoningEffort.LOW)
                    .build()
            )
            .text(promptRequest.responseClass, JsonSchemaLocalValidation.YES)
            .input(promptRequest.content.trimIndent())
            .build()

        return BatchRequest(
            custom_id = promptRequest.custom_id.toString(),
            method = "POST",
            url = "/v1/responses",
            body = params.rawParams._body()
        )
    }

    fun uploadBatch(): Batch {
        val jsonl = JsonUtils.loadJSONLResource("prompts/batch.classification3.jsonl");

        //BatchCreateParams.builder().body().ad


        /*
                val classloader = Thread.currentThread().contextClassLoader
                val requestsPath = Paths.get(classloader.getResource("prompts/batch.classification3.jsonl").toURI())

                val jsonl = Files.readString(requestsPath)
        */

        return uploadBatch(jsonl)
    }

    fun uploadBatch(jsonl: String): Batch {
        val tempFile: Path = Files.createTempFile("tmp_batch_requests_" + UUID.randomUUID(), ".jsonl")

        Files.writeString(tempFile, jsonl, StandardOpenOption.WRITE)

        try {
            val fileParams: FileCreateParams = FileCreateParams.builder()
                .purpose(FilePurpose.BATCH)
                .file(tempFile)
                .build()

            val file: FileObject = openAIClient.files().create(fileParams)

            val batchParams = BatchCreateParams.builder()
                .inputFileId(file.id())
                .endpoint(Endpoint.V1_RESPONSES)
                .completionWindow(CompletionWindow._24H)
                .build()

            return openAIClient.batches().create(batchParams)
        } catch (e: Exception) {
            println("ERROR while creating batch")
            throw e
        } finally {
            Files.deleteIfExists(tempFile)
        }
    }

    fun batchStatus(batchID: String): Batch {
        return openAIClient.batches().retrieve(batchID)
    }

    fun batchResult(batchID: String): List<BatchResponse> {
        val batch = openAIClient.batches().retrieve(batchID);

        when (batch.status()) {
            Batch.Status.COMPLETED -> {

                val results = if (batch.outputFileId().isPresent) readOutput(batch.outputFileId().get()) else mutableListOf()

                if (batch.errorFileId().isPresent)
                    results.addAll(readOutput(batch.errorFileId().get()))


                return results
            }

            Batch.Status.FAILED -> {
                println("ERROrS: " + batch.errors())
            }

            Batch.Status.EXPIRED,
            Batch.Status.CANCELLED -> println("batch canceled or expired!")

            else -> println("batch status: ${batch.status()}")
        }

        return listOf()
    }

    fun readOutput(fieldID: String): MutableList<BatchResponse> {
        val result = openAIClient.files().content(fieldID).use { response ->
            response.body().readAllBytes().toString(Charsets.UTF_8)
        }

        println("batch succeeded! $result")

        return JsonUtils.fromJSON<MutableList<BatchResponse>>(
            "[${result.split("\n").filter { it.isNotEmpty() }.joinToString()}]"
        );
    }
}
