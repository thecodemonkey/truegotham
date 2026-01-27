package il.tutorials.truegotham.service

import il.tutorials.truegotham.import.ImportProcessor
import il.tutorials.truegotham.model.dto.CrawlerOptions
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.utils.DateUtils
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import org.springframework.stereotype.Service

@Service
class ImportService(
        val importRepo: ImportRepository,
        val crawler: Crawler,
        val ai: AIPromptService,
        val incidentRepo: IncidentRepository,
        val importProcessor: ImportProcessor
) {

    // crawl and import new statements, starting from last existing date...
    fun import(cityID: String): List<RawStatement> {
        val last = importRepo.findTopByOrderByUnixtsDesc()
        val startDate = DateUtils.formatUnixTimestamp(last?.unixts!!)

        val options = CrawlerOptions(cityID, startDate = startDate, endDate = "2099-01-01")
        val pressReleases = crawler.crawl(cityID, options.startDate, options.endDate)

        println("found ${pressReleases.size} new press releases since: $startDate")
        saveUnique(pressReleases)

        return pressReleases
    }

    fun importMonth(cityID: String, year: Int, month: Int): List<RawStatement> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.with(TemporalAdjusters.lastDayOfMonth())

        val pressReleases = crawler.crawl(cityID, startDate.toString(), endDate.toString())
        println("found ${pressReleases.size} new press releases for $year-$month")
        saveUnique(pressReleases)

        return pressReleases
    }

    fun saveUnique(rawList: List<RawStatement>) {
        val existingHashes = importRepo.findAll().mapNotNull { it.hash }.toSet()

        val toSave = rawList.filter { it.hash !in existingHashes }

        println("${toSave.size} new raw statements to db imported.")
        importRepo.saveAll(toSave)
    }

    fun processNext() {
        importProcessor.run()
    }

    fun processNextSingle() {
        importProcessor.runSingleStep(importRepo.findTopByOrderByUnixtsAsc())
    }

    fun processNextFrom(unixts: Long) {
        importProcessor.runNextFrom(unixts)
    }
}
