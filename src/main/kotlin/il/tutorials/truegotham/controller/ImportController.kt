package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.ImportService
import il.tutorials.truegotham.utils.DateUtils
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ImportController(
    val importService: ImportService
) {

    @GetMapping("/api/import/{cityID}")
    fun importNewStatements(
        @Parameter( example = "4971" )
        @PathVariable cityID: String) = importService.import(cityID)


    @PostMapping("/api/import/process/next")
    fun processNextItem() = importService.processNext()

    @PostMapping("/api/import/process/next/single")
    fun processNextItemSingleStep() = importService.processNextSingle()

    @PostMapping("/api/import/process/next/incident")
    fun processIncidentItem() = importService.processIncidentItem()

    @PostMapping("/api/import/process/from/{date}")
    fun processFromDate(
        @Parameter( example = "10.11.2025 – 00:00" )
        @PathVariable date: String)
        = importService.processNextFrom(DateUtils.toUnixts(date)!!)
}