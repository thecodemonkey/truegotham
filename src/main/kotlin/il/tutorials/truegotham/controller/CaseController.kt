package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.CaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class CaseController(val cases: CaseService) {
    @GetMapping("/api/cases/{id}")
    fun loadCrimeCase(@PathVariable id: UUID) = cases.loadCrimeCase(id)

}