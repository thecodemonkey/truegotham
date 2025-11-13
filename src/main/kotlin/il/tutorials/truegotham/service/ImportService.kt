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
    val db: StatementRepository,
    val importRepo: ImportRepository,
    val crawler: Crawler,
    val ai: AIPromptService,
    val imageRepository: ImageRepository,
    val incidentRepo: IncidentRepository,
    val importProcessor: ImportProcessor
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
        importProcessor.run();
    }

    fun processNextSingle() {
        importProcessor.runSingleStep();
    }

    fun _processNext() {
        val next = importRepo.findTopByStatusOrderByUnixtsAsc(ImportStatus.IMPORTED);
        next?.let { it ->

            it.status = ImportStatus.IMPORTED;
            importRepo.save(it)

//1. create basic statement
            val statement = Statement(
                id = next.id,           // same id as raw item
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

            statement.district = "City";

//5. persist statement

            statement.active = true

            println("insert new statement...")
            db.save(statement)

            println("remove raw statement")
            // importRepo.delete(it)


            println("statement: ${JsonUtils.toJSON(statement)}")
        }
    }

    fun processIncidentItem() {
        val next = importRepo.findTopByStatusOrderByUnixtsAsc(ImportStatus.IMPORTED);
        next?.let { it ->
            val incident = Incident()

//6. extract locations
            println("start extracting locations...")
            val locationsResult = ai.extractIncidentLocations(it.content!!)
            println("result of location extraction: $locationsResult");

            var prevDT:Long? = null;

            locationsResult.locations.forEach {
                l ->
                run {
                    var current = DateUtils.toUnixtsTime(l.date)
                    if (current == null) current = if (prevDT == null) 0 else prevDT ;
                    if (current != null) prevDT = current;

                    incident.locations.add(
                        IncidentLocation(
                            id = UUID.randomUUID(),
                            unixts = current!!,
                            address = l.address,
                            //addressFormal = l.address_formal,

                            main = l.main,
                            title = l.title,
                            description = l.description,
                            incident = incident
                        )
                    )
                }
            }

//7. extract offender profile
            println("start extracting offender profiles...")
            val profilesResult = ai.extractOffenderProfiles(it.content)
            println("result of offender profile extraction: $profilesResult");

            profilesResult.profiles.forEach {
                    l -> incident.offenderProfiles.add(IncidentOffenderProfile(
                id = UUID.randomUUID(),
                age = l.age ?: -1,
                location = l.housing_situation ?: "",
                gender = Gender.MALE, //if (l.gender.isNullOrBlank()) Gender.MALE else Gender.FEMALE,
                hair = l.hair ?: "",
                behaviour = "",
                drugTest = l.drugTest ?: false,
                alcoholTest = l.alkoholTest ?: false,
                summary = l.psychological_assessment ?: "",
                motive = "",
                look = "",
                incident = incident
            ))
            }

//7.1 extract motive

//8. generate profile image description
//8.1 generate profile image

            println("start generating profil image...")
            val profilesJSON = JsonUtils.toJSON(
                if (profilesResult.profiles.size > 1) profilesResult.profiles else profilesResult.profiles.first()
            )
            val primgbtarray = ai.generateOffenderImage(profilesJSON, profilesResult.profiles.size > 1)
            //println("result of location extraction: $profilesResult");

            val imgProfileEntity = imageRepository.save(
                Image(UUID.randomUUID(), "image/jpeg", primgbtarray))

            incident.offenderProfiles.firstOrNull()?.let {
                it.imageId = imgProfileEntity.id;
            }

//9. extract tools and evidence

//10. offenses


//11 save new incident....
            println("persist incident...")
            incidentRepo.save(incident);

//11.1 update state of the import task...

//            TODO: update status or remove the raw item...
//            it.status = ImportStatus.FINISHED;
//            importRepo.save(it);

        }
    }

}