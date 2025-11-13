package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.*
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.entity.Statement
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.repository.StatementSpecs
import il.tutorials.truegotham.utils.FileUtils
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class StatementService(val db: StatementRepository) {

    val NOT_CRIME_CATEGORIES = listOf("sonstiges", "Unfall", "Informationsmitteilung")

    fun latest(pageSize: Int) =
        db.findAllByOrderByUnixtsDesc(PageRequest.of(0, pageSize))

    fun loadOriginal(id: UUID) =
        db.findByIdOrNull(id)

    fun getFiltered(
        search: String?,
        categories: List<String>?,
        districts: List<String>?,
        pageable: Pageable
    ): Page<Statement> =
        db.findAll(StatementSpecs.filter(
            search,
            CrimeTypes.mapCrimes(categories),
            districts,
            true),  // active only
            pageable)



    fun load() =
        JsonUtils.fromJSONResource<List<Statement>>("data/current.statements.json")

    fun filterNotCrimes() =
        JsonUtils.fromJSONResource<List<Statement>>("data/raw/classified.result.json")
            .filter {
                (it.categories?.size == 1 && NOT_CRIME_CATEGORIES.contains(it.categories?.first())) || !it.crime
            }

    fun filterCrimesOnly(): List<Statement> {
        val all = JsonUtils.fromJSONResource<List<Statement>>("data/raw/classified.result.json")
        val notCrimes = filterNotCrimes()

        val filterd = all.filter { !notCrimes.any { nc -> nc.id == it.id } }

        filterd.forEach { it.categories = it.categories?.filterNot { f -> NOT_CRIME_CATEGORIES.contains(f) } }

        //db.saveAll(filterd)

        return filterd
    }


    fun loadAllPressReleases(): List<RawStatement> {
        val dir = FileUtils.getTmpPath("press");
        val jsons = FileUtils.readAllTextFilesInDir(dir)

        return jsons.flatMap {
            JsonUtils.fromJSON<List<RawStatement>>(it)
        }
    }

    fun normilizeCategories() {
        db.findAll().forEach { s ->
            //println("Normalizing ${s.id} - ${s.categories}")


            if (s.categories?.any { c -> !Crimes.isTopCategory(c) }!!) {
                val normalized = s.categories?.mapNotNull { c -> Crimes.normalize(c) }?.distinct()

                println(" Need normalization! ${s.categories} -> $normalized")

                s.categories = normalized;
                db.save(s)
            }
        }
    }

    //@PostConstruct
    fun createCSV() {
        //normilizeCategories()
        //convertPressreleasesToStatements();

        //val press = loadAllPressReleases()
        //val fileName = FileUtils.getTmpPath("press", "all.csv");
        //CsvUtils.writePressReleasesToCsv(press, fileName);

/*        val topics = press.flatMap { it.topics }
                          .distinctBy { it.lowercase() }*/


        //val cms = JsonUtils.fromJSONFile<List<CrimeKind>>( FileUtils.getTmpPath("press", "topics.json"))

        //val topics1 = JsonUtils.fromJSONResource<List<CrimeKind>>("data/raw/categories.json")


/*        cms.forEach {
            topics1.find { t -> t.tag == it.tag }?.apply {
                crime = it.crime
            }
        }*/


        //val crimeTrue = topics1.filter { it.crime }



        //press.filter { it.top }


        //val topics2 = JsonUtils.fromJSONResource<List<CrimeKind>>("data/raw/categories2.json")
        //val diff = (topics1.toSet() union topics2.toSet()) - (topics1.toSet() intersect topics2.toSet())


        //FileUtils.saveTextFile(JsonUtils.toJSON(crimeTrue), FileUtils.getTmpPath("press", "topics.json"))
    }

    private fun batchIsCrime(){
        val statements = JsonUtils.fromJSONResource<List<Statement>>("data/raw/statements.json")

        //val files = FileUtils.readAllTextFilesInDir(FileUtils.getTmpPath("press"))
/*        val statements = files.flatMap { JsonUtils.fromJSON<List<PressRelease>>(it) }
            .map { Statement(
                id = UUID.randomUUID(),
                unixts = DateUtils.toUnixts(it.timestamp)!!,
                url = it.url,
                content = it.content,
                title = it.title
            ) }*/

        JsonUtils.toJSONFile(statements, FileUtils.getTmpPath("press", "statements.json"))
    }

/*    private fun convertPressreleasesToStatements(){
        val files = FileUtils.readAllTextFilesInDir(FileUtils.getTmpPath("press"))
        val statements = files.flatMap { JsonUtils.fromJSON<List<PressRelease>>(it) }
            .map { Statement(
                id = randomUUID(),
                unixts = DateUtils.toUnixts(it.timestamp)!!,
                url = it.url,
                content = it.content,
                //title = it.title
            ) }

        JsonUtils.toJSONFile(statements, FileUtils.getTmpPath("press", "statements.json"))
    }*/




}