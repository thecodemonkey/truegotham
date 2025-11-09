package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.dto.CrawlerOptions
import il.tutorials.truegotham.model.entity.Image
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.repository.ImageRepository
import il.tutorials.truegotham.repository.ImportRepository
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.utils.ByteUtils
import il.tutorials.truegotham.utils.DateUtils
import il.tutorials.truegotham.utils.JsonUtils
import il.tutorials.truegotham.utils.ValueContent
import org.springframework.stereotype.Service
import java.util.*

@Service
class ImportService(
    val db: StatementRepository,
    val importRepo: ImportRepository,
    val crawler: Crawler,
    val ai: AIPromptService,
    val imageRepository: ImageRepository
) {

    val NOT_CRIME_CATEGORIES = listOf("sonstiges", "Unfall", "Informationsmitteilung")

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
        val next = importRepo.findTopByStatusOrderByUnixtsAsc(ImportStatus.IMPORTED);
        next?.let { it ->

            it.status = ImportStatus.INPROGRESS;
            importRepo.save(it)

//1. create basic statement
            val statement = Statement(
                id = UUID.randomUUID(),
                unixts = it.unixts!!,
                content = it.content,
                url = it.url,
                title = it.title!!);

//2. classify statement, categories + iscrime or not
            println("start classification of raw statement...")
            val result = ai.classifyStatement(it.content!!);
            println("result of classification: $result");

            statement.categories = result.categories;
            statement.crime = result.categories.all { c -> NOT_CRIME_CATEGORIES.contains(c) }.not();

            if (!statement.crime) {
//2.1 remove raw statement if it's not a crime
                println("statement is not a crime: ${statement.categories}");
                importRepo.delete(it)
                return;
            }

//3. create summary in batman style...
            println("start summary generation of raw statement...")
            val summaryResult = ai.generateStatementSummary(statement.content!!)
            println("result of summary generation: ${summaryResult.result}");

            statement.summary = summaryResult.result;

//4.0 generate image description
            println("start generation of image description...")
            val imgDescResult = ai.generateStatementImageDescription(statement.content)
            println("result of image description: ${imgDescResult.result}");

//4.1 generate image
            println("start generation of statement cover image...")
            val img = ai.generateStatementCoverImage(imgDescResult.result)

            val imgEntity = imageRepository.save(Image(UUID.randomUUID(), "image/jpeg", img))
            statement.imageId = imgEntity.id;

            val imgDataURL = ByteUtils.toDataUrl(img, "image/jpeg")
            println("result of statement cover image: $imgDataURL");

//5.1 address
            println("start extracting locations...")
            val locationsResult = ai.extractIncidentLocations(statement.content)
            println("result of location extraction: $locationsResult");

            statement.district = "City";

//6. persist statement

            statement.active = true

            println("insert new statement...")
            db.save(statement)

            println("remove raw statement")
            // importRepo.delete(it)


            println("statement: ${JsonUtils.toJSON(statement)}")
        }
    }

}