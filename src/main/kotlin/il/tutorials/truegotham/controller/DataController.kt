package il.tutorials.truegotham.controller


import il.tutorials.truegotham.service.StatementService
import org.springframework.web.bind.annotation.*


@RestController
class DataController(val statements:StatementService) {

    @GetMapping("/api/statements")
    fun loadStatements() = statements.load()

    @GetMapping("/api/statements/notcrime")
    fun filterNotCrimes() = statements.filterNotCrimes()

    @GetMapping("/api/statements/crimeonly")
    fun filterCrimesOnly() = statements.filterCrimesOnly()
}