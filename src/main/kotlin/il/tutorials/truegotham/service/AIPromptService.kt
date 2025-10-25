package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.ai.PromptInfo
import il.tutorials.truegotham.model.ai.PromptRequest
import il.tutorials.truegotham.model.ai.structured.CrimeClassificationResult
import il.tutorials.truegotham.model.ai.promptRequest
import il.tutorials.truegotham.model.ai.structured.CrimeIsCrimeResult
import il.tutorials.truegotham.model.ai.structured.LocationPrimaryResult
import il.tutorials.truegotham.utils.ByteUtils
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AIPromptService(val oai: AIService) {
    val promptClasses: List<KClass<out Any>> = listOf(
        CrimeClassificationResult::class,
        CrimeIsCrimeResult::class,
        LocationPrimaryResult::class
    )

    fun classifyStatement(statement: String) = oai.prompt(
        promptRequest<CrimeClassificationResult>(statement)
    )

//    fun startBatchClassification(): Batch {
//        val path = FileUtils.getTmpPath("press")
//        /*
//                val press = FileUtils.getLastModifiedFile(path)?.let {
//                    JsonUtils.fromJSONFile<List<PressRelease>>(it.absolutePath)
//                }
//        */
//
//        val statements = JsonUtils.fromJSONResource<List<Statement>>("data/raw/statements.json")
//
//        val requests = statements?.map {
//            promptRequest<CrimeClassificationResult>(
//                custom_id = it.id.toString(),
//                content = it.content!!
//            )
//
//            /*            PromptRequest(
//                        id = "pmpt_68e3a7e7abb481979fd71ee04f2a28f60ab10fdb164b11bf",
//                        content = it.content!!,
//                        responseClass =  CrimeClassification::class.java,
//                        custom_id = it.id.toString()*/
//            //)
//        }
//
//        return requests?.let {
//            oai.createAndUploadBatch(it)
//        }!!
//
//    }

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