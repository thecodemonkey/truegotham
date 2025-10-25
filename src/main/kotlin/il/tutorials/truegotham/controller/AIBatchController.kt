package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.ai.structured.CrimeClassificationResult
import il.tutorials.truegotham.model.ai.structured.CrimeIsCrimeResult
import il.tutorials.truegotham.model.ai.structured.LocationPrimaryResult
import il.tutorials.truegotham.service.AIBatchService
import il.tutorials.truegotham.service.AIService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AIBatchController(
    val batches: AIBatchService,
    val ai: AIService
) {

    @GetMapping("/api/batches/{batchID}/status")
    fun batchStatus(
        @Parameter(example = "batch_68e500ad89ac8190b7c4ae43fd5644a7")
        @PathVariable batchID: String) = ai.batchStatus(batchID)

    @GetMapping("/api/batches/{batchID}")
    fun getBatch(
        @Parameter(example = "batch_68e500ad89ac8190b7c4ae43fd5644a7")
        @PathVariable batchID: String) = ai.batchResult(batchID)

    @GetMapping("/api/batches/{batchID}/classification")
    fun getClassificationResult(
        @Parameter(example = "batch_68e500ad89ac8190b7c4ae43fd5644a7")
        @PathVariable batchID: String) = batches.loadBatchClassificationResult(batchID)

    @GetMapping("/api/batches/{batchID}/iscrime")
    fun getIsCrimeResult(
        @Parameter(example = "batch_68e500ad89ac8190b7c4ae43fd5644a7")
        @PathVariable batchID: String) = batches.loadBatchIsCrimeResult(batchID)

    @GetMapping("/api/batches/{batchID}/primarylocation")
    fun getPrimaryLocationResult(
        @PathVariable batchID: String) = batches.loadBatchLocationPrimaryResult(batchID)




    @PostMapping("/api/batches/test")
    fun uploadBatch() = ai.uploadBatch()

    @PostMapping("/api/batches/classification")
    fun startBatchClassification() =
        batches.startBatch(CrimeClassificationResult::class.java)

    @PostMapping("/api/batches/iscrime")
    fun startBatchIsCrime() =
        batches.startBatch(CrimeIsCrimeResult::class.java)

    @PostMapping("/api/batches/primarylocation")
    fun startPrimaryLocation() =
        batches.startBatch(LocationPrimaryResult::class.java)
}