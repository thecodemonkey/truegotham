package il.tutorials.truegotham.service

import com.openai.models.batches.Batch
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.ai.BatchResult
import il.tutorials.truegotham.model.ai.PromptRequest
import il.tutorials.truegotham.model.ai.structured.CrimeClassificationResult
import il.tutorials.truegotham.model.ai.structured.CrimeIsCrimeResult
import il.tutorials.truegotham.model.ai.structured.LocationPrimaryResult
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.utils.FileUtils
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Service

@Service
class AIBatchService(
    val oai: AIService,
    val statementsRepo: StatementRepository
)  {
    fun loadBatchLocationPrimaryResult(batchID: String): List<BatchResult<LocationPrimaryResult>?> {
        val result = oai.batchResult(batchID).map { it.output<LocationPrimaryResult>() }

        val statements = statementsRepo.findAll()
        statements.forEach {
            val i = result.find { r -> r!!.id == it.id.toString() }
            if (i != null) {

                i.result?.let { r ->
                    it.address = r.street.take(250)
                    it.addressVerbose = r.location.take(250)
                    it.district = r.district
                    it.city = r.city
                }

                println("item result: ${it.address} => ${it.addressVerbose} => ${it.district} => ${it.city} ")

                try{
                    statementsRepo.save(it)
                } catch (e:Exception){
                    println("ERROR: ${e.message}")
                }


            } else {
                println("ERROR: no item ${it.id} in result found!")
            }
        }

        return result;
    }

    fun loadBatchClassificationResult(batchID: String): List<BatchResult<CrimeClassificationResult>?> {
        val result = oai.batchResult(batchID).map { it.output<CrimeClassificationResult>() }

        val statements = JsonUtils.fromJSONResource<List<Statement>>("data/raw/iscrime.json")
        statements.forEach {
            val i = result.find { r -> r!!.id == it.id.toString() }
            if (i != null) {
                it.categories = i.result?.categories;
                println("item ${it.id} has categories: ${it.categories} ")
            } else {
                println("ERROR: no item ${it.id} in result found!")
            }
        }

        JsonUtils.toJSONFile(statements, FileUtils.getTmpPath("press", "classified.result.json"))

        return result;
    }

    fun loadBatchIsCrimeResult(batchID: String): List<BatchResult<CrimeIsCrimeResult>?> {
        val result = oai.batchResult(batchID).map { it.output<CrimeIsCrimeResult>() }

        val statements = JsonUtils.fromJSONResource<List<Statement>>("data/raw/statements.json")
        statements.forEach {
            val i = result.find { r -> r!!.id == it.id.toString() }
            if (i != null) {
                it.crime = i.result?.crime!!;
                println("item ${it.id} has categories: ${it.crime} ")
            } else {
                println("ERROR: no item ${it.id} in result found!")
            }
        }

        //JsonUtils.toJSONFile(statements, FileUtils.getTmpPath("press", "iscrime.json"))

        return result;
    }

    fun <T: Any> startBatch(cls: Class<T>): Batch {
        //val statements = JsonUtils.fromJSONResource<List<Statement>>("data/raw/statements.json")
        val statements = statementsRepo.findAll()

        val requests = statements.map {
            PromptRequest(
                custom_id = it.id.toString(),
                responseClass = cls,
                content = it.content!!
            )
        }

        return requests?.let {
            oai.createAndUploadBatch(it)
        }!!
    }
}