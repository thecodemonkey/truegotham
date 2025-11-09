package il.tutorials.truegotham.service

import com.openai.models.ChatModel
import com.openai.models.ReasoningEffort
import il.tutorials.truegotham.model.ai.PromptInfo
import il.tutorials.truegotham.model.ai.PromptRequest
import il.tutorials.truegotham.model.ai.promptRequest
import il.tutorials.truegotham.model.ai.structured.*
import il.tutorials.truegotham.utils.ByteUtils
import il.tutorials.truegotham.utils.ValueContent
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AIPromptService(val oai: AIService) {
    @ValueContent("classpath:prompts/statement.cover.prompt.txt")
    lateinit var STATEMENT_COVER_PROMPT: String

    val promptClasses: List<KClass<out Any>> = listOf(
        CrimeClassificationResult::class,
        CrimeIsCrimeResult::class,
        LocationPrimaryResult::class
    )

// statement stuff

    fun classifyStatement(statement: String) = oai.prompt(
        promptRequest<CrimeClassificationResult>(statement)
    )

    fun generateStatementSummary(description: String) = oai.prompt(
        promptRequest<SimpleResult>(
            content = "Erstelle eine Beschreibung",
            id = "pmpt_6900d2eaae648194a27ac001e23b02870470f90e83bd4f9b",
            variables = mapOf(
                "description" to description
            ),
            model = ChatModel.GPT_5_MINI
        )
    )

    fun generateStatementImageDescription(description: String) = oai.prompt(
        promptRequest<SimpleResult>(
            content = "Erstelle eine Beschreibung",
            id = "pmpt_690f246534108194905b68acbc79746d00417d059ad780d9",
            variables = mapOf(
                "description" to description
            ),
            model = ChatModel.GPT_5
        )
    )

    fun generateStatementCoverImage(description: String) =
        oai.generateImage(STATEMENT_COVER_PROMPT.replace("[IMG_DESC]", description))


    fun extractIncidentLocations(description: String) =
        oai.prompt(
            promptRequest<IncidentEventsResult>(
                content = "Extrahiere Standortinformationen: \n\n$description",
                model = ChatModel.GPT_5,
                reasoning = ReasoningEffort.MEDIUM)
    )


// district stuff
    fun generateDistrictRawDescription(city: String, district: String) = oai.prompt(
        promptRequest<DistrictDescriptionResult>(
            content = "erstelle eine Beschreibung in JSON Format.",
            variables = mapOf(
                "city" to city,
                "district" to district
            ),
            model = ChatModel.GPT_4_1)
    )

    fun generateDistrictDescription(city: String, description: String) = oai.prompt(
        promptRequest<SimpleResult>(
            content = "Erstelle eine Beschreibung",
            id = "pmpt_68ff9816bc348196bc09c6e1207603d500124efef8990a41",
            variables = mapOf(
                "city" to city,
                "description" to description
            ),
            model = ChatModel.GPT_4_1
        )
    )

    fun prompt(message: String) = oai.prompt(message)

    fun genericPrompt(promptID: String, message: String): Any? {
        val cls = getResultClass(promptID)

        if (cls != null) {
            return oai.prompt(
                PromptRequest(
                    content = message,
                    responseClass = cls.java
                )
            )
        }

        return null

    }

    fun imageAsDataURL(message: String) =
        ByteUtils.toDataUrl(oai.generateImage(message), "image/jpeg")

    private fun getResultClass(promptID: String): KClass<out Any>? {
        val cls = promptClasses.firstOrNull { clazz ->
            clazz.annotations.filterIsInstance<PromptInfo>().firstOrNull()?.id == promptID
        }
        return cls
    }


}