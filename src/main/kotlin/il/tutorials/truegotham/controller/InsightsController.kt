package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.InsightsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class InsightsController(val insights: InsightsService) {

    @GetMapping("/api/insights")
    fun getAll() = insights.load()

    @GetMapping("/api/insights/crimetypes")
    fun getCrimeTypes() = insights.getCrimeTypes()

    @GetMapping("/api/insights/total")
    fun getTotal(@RequestParam districts: List<String>?) =
        insights.getCategoryStatsByHalfYear(districts)

    @GetMapping("/api/insights/districts")
    fun getDistricts(@RequestParam limit: Int?) = insights.getDistrictsStats(limit)

}
