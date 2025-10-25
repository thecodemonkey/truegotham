package il.tutorials.truegotham.controller

import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.service.StatementService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class StatementController(
    val statementService: StatementService,
    val statementRepository: StatementRepository) {

    @GetMapping("/api/statements/latest")
    fun loadLatest(@RequestParam  limit: Int) =
        statementService.latest(limit)

    @GetMapping("/api/statements/{id}")
    fun loadStatement(@PathVariable id: UUID) =
        statementService.loadOriginal(id)

    @GetMapping("/api/statements/search")
    fun getStatements(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) categories: List<String>?,
        @RequestParam(required = false) districts: List<String>?,
        pageable: Pageable
    ) = statementService.getFiltered(search, categories, districts, pageable)

}