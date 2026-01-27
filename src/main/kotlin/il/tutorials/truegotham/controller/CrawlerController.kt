package il.tutorials.truegotham.controller

import il.tutorials.truegotham.model.dto.CrawlerOptions
import il.tutorials.truegotham.service.Crawler
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CrawlerController(val crawler: Crawler) {

    @PostMapping("/api/crawl")
    fun crawl(@RequestBody options: CrawlerOptions) =
            crawler.crawl(options.cityID, options.startDate, options.endDate)

    @PostMapping("/api/crawl/continue/{cityID}")
    fun continueCrawling(@Parameter(example = "4971") @PathVariable cityID: String) =
            crawler.continueCrawling(cityID)

    @PostMapping("/api/crawl/{cityID}/{year}/{month}")
    fun crawlMonth(
            @Parameter(example = "4971") @PathVariable cityID: String,
            @PathVariable year: Int,
            @PathVariable month: Int
    ) {
        val startDate = java.time.LocalDate.of(year, month, 1)
        val endDate = startDate.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
        crawler.crawl(cityID, startDate.toString(), endDate.toString())
    }
}
