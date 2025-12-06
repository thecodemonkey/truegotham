package il.tutorials.truegotham.service

import il.tutorials.truegotham.import.ImportProcessor
import il.tutorials.truegotham.model.Gender
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.dto.CrawlerOptions
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.model.entity.incident.Incident
import il.tutorials.truegotham.model.entity.incident.IncidentLocation
import il.tutorials.truegotham.model.entity.incident.IncidentOffenderProfile
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.utils.ByteUtils
import il.tutorials.truegotham.utils.DateUtils
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Service
import java.util.*

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
        val last = importRepo.findTopByOrderByUnixtsDesc();
        val startDate = DateUtils.formatUnixTimestamp(last?.unixts!!);

        val options = CrawlerOptions(cityID, startDate = startDate, endDate = "2099-01-01")
        val pressReleases = crawler.crawl(cityID, options.startDate, options.endDate);

        println("found ${pressReleases.size} new press releases since: $startDate")
        saveUnique(pressReleases)

        return pressReleases;
    }

    fun saveUnique(rawList: List<RawStatement>) {
        val existingHashes = importRepo.findAll()
            .mapNotNull { it.hash }
            .toSet()

        val toSave = rawList.filter { it.hash !in existingHashes }

        println("${toSave.size} new raw statements to db imported.")
        importRepo.saveAll(toSave)
    }

    fun processNext() {
        importProcessor.run();
    }

    fun processNextSingle() {
        importProcessor.runSingleStep(
            importRepo.findTopByOrderByUnixtsAsc()
        );
    }

    fun processNextFrom(unixts: Long) {
        importProcessor.runNextFrom(unixts);
    }

}