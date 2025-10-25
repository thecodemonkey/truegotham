package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.Insights
import il.tutorials.truegotham.model.LabelDataPair
import il.tutorials.truegotham.model.MultiLineData
import il.tutorials.truegotham.model.StatData
import il.tutorials.truegotham.model.entity.StatsData
import il.tutorials.truegotham.repository.StatementRepository
import il.tutorials.truegotham.repository.StatsRepository
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

@Service
class InsightsService(
    val statements: StatementRepository,
    val statementsService: StatementService,
    val stats: StatsRepository
) {

    fun load() = Insights(
        getCrimeTypeStats(),
        getTotalCrimesStats(),
        getDistrictsStats(10)
    )

    fun getCrimeTypeStats() =
        stats.findByName("crime types")?.let {
            JsonUtils.fromJSON<StatData>(it.data)
        } ?: StatData(listOf(), listOf())

    fun getTotalCrimesStats() =
        stats.findByName("crimes timeline hy")?.let {
            JsonUtils.fromJSON<MultiLineData>(it.data).withoutCurrent()
        } ?: MultiLineData(listOf(), listOf())


    fun getDistrictsStats(limit: Int? = null) : List<LabelDataPair> {
        val grouped = statements.findAll()
            .filter { it.crime == true }
            .mapNotNull { it.district }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

        val limited = limit?.let { grouped.take(it) } ?: grouped
        return limited.map { (district, count) -> LabelDataPair(district, count) }
    }


    //@PostConstruct
    fun creatStats() {
        val s = getCrimeTypesAsStatData()
        val sd = StatsData(UUID.randomUUID(), "crime types", JsonUtils.toJSON(s))
        println("Crime types: $sd")


        val sm = getCategoryStatsByHalfYear(listOf())
        val sd2 = StatsData(UUID.randomUUID(), "crimes timeline hy", JsonUtils.toJSON(sm))
        println("Crime counts per hy: $sd2")
        //stats.save(sd2)
    }

    fun getCrimeTypesAsStatData() : StatData {
        val s = getCrimeTypes()

        return StatData(
            data = s.map { it.second },
            labels = s.map { it.first }
        )
    }

    fun getCrimeTypes() = statements.findAll()
        .flatMap { it.categories ?: emptyList() }
        .filter { it.equals("sonstiges", true).not() &&
                it.equals("Unfall", true).not()}
        .groupingBy { it }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }

    fun getCategoryStatsByMonth(): MultiLineData {
        val stmts = statements.findAll()

        // Alle Statements mit Kategorien in (Kategorie, Jahr, Monat, Count) umwandeln
        val categoryMonthStats = stmts
            .filter { it.categories != null && it.categories!!.isNotEmpty() }
            .flatMap { statement ->
                val instant = Instant.ofEpochSecond(statement.unixts)
                val localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
                val yearMonth = YearMonth.from(localDate)

                statement.categories!!.map { category ->
                    Triple(category, yearMonth, 1)
                }
            }
            .groupBy { it.first to it.second } // Gruppieren nach (Kategorie, YearMonth)
            .map { (key, group) ->
                Triple(key.first, key.second, group.size)
            }

        // Alle einzigartigen Monate sammeln und sortieren
        val allMonths = categoryMonthStats
            .map { it.second }
            .distinct()
            .sorted()

        // Labels im Format "YYYY-MM" erstellen
        val labels = allMonths.map { "${it.year}-${it.monthValue.toString().padStart(2, '0')}" }

        // Für jede Kategorie eine StatData-Zeile erstellen
        val categoriesData = categoryMonthStats
            .groupBy { it.first } // Nach Kategorie gruppieren
            .map { (category, stats) ->
                val statsMap = stats.associate { it.second to it.third }

                // Für jeden Monat den Count holen (oder 0 wenn nicht vorhanden)
                val data = allMonths.map { month ->
                    statsMap[month] ?: 0
                }

                StatData(
                    data = data,
                    label = category
                )
            }
            .sortedBy { it.label }

        return MultiLineData(
            labels = labels,
            data = categoriesData
        )
    }

    fun getCategoryStatsByQuarter(): MultiLineData {
        val stmnts = statements.findAll()

        // Alle Statements mit Kategorien in (Kategorie, Jahr, Quartal, Count) umwandeln
        val categoryQuarterStats = stmnts
            .filter { it.categories != null && it.categories!!.isNotEmpty() }
            .flatMap { statement ->
                val instant = Instant.ofEpochSecond(statement.unixts)
                val localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
                val year = localDate.year
                val quarter = (localDate.monthValue - 1) / 3 + 1 // 1-4

                statement.categories!!.map { category ->
                    Triple(category, Pair(year, quarter), 1)
                }
            }
            .groupBy { it.first to it.second } // Gruppieren nach (Kategorie, (Jahr, Quartal))
            .map { (key, group) ->
                Triple(key.first, key.second, group.size)
            }

        // Alle einzigartigen Quartale sammeln und sortieren
        val allQuarters = categoryQuarterStats
            .map { it.second }
            .distinct()
            .sortedWith(compareBy({ it.first }, { it.second }))

        // Labels im Format "YYYY-Q1", "YYYY-Q2", etc.
        val labels = allQuarters.map { (year, quarter) -> "$year-Q$quarter" }

        // Für jede Kategorie eine StatData-Zeile erstellen
        val categoriesData = categoryQuarterStats
            .groupBy { it.first } // Nach Kategorie gruppieren
            .map { (category, stats) ->
                val statsMap = stats.associate { it.second to it.third }

                // Für jedes Quartal den Count holen (oder 0 wenn nicht vorhanden)
                val data = allQuarters.map { quarter ->
                    statsMap[quarter] ?: 0
                }

                StatData(
                    data = data,
                    label = category
                )
            }
            .sortedBy { it.label }

        return MultiLineData(
            labels = labels,
            data = categoriesData
        )
    }

    fun getCategoryStatsByHalfYear(districts: List<String>?): MultiLineData {
        val stmnts = statementsService.getFiltered(null, null, districts, Pageable.unpaged()).content;

        //findAll()

        // Alle Statements mit Kategorien in (Kategorie, Jahr, Halbjahr, Count) umwandeln
        val categoryHalfYearStats = stmnts
            .filter { it.categories != null && it.categories!!.isNotEmpty() }
            .flatMap { statement ->
                val instant = Instant.ofEpochSecond(statement.unixts)
                val localDate = LocalDate.ofInstant(instant, ZoneId.systemDefault())
                val year = localDate.year
                val halfYear = if (localDate.monthValue <= 6) 1 else 2

                statement.categories!!.map { category ->
                    Triple(category, Pair(year, halfYear), 1)
                }
            }
            .groupBy { it.first to it.second } // Gruppieren nach (Kategorie, (Jahr, Halbjahr))
            .map { (key, group) ->
                Triple(key.first, key.second, group.size)
            }

        // Alle einzigartigen Halbjahre sammeln und sortieren
        val allHalfYears = categoryHalfYearStats
            .map { it.second }
            .distinct()
            .sortedWith(compareBy({ it.first }, { it.second }))

        // Labels im Format "YYYY-H1", "YYYY-H2"
        val labels = allHalfYears.map { (year, halfYear) -> "$year-H$halfYear" }

        // Für jede Kategorie eine StatData-Zeile erstellen
        val categoriesData = categoryHalfYearStats
            .groupBy { it.first } // Nach Kategorie gruppieren
            .map { (category, stats) ->
                val statsMap = stats.associate { it.second to it.third }

                // Für jedes Halbjahr den Count holen (oder 0 wenn nicht vorhanden)
                val data = allHalfYears.map { halfYear ->
                    statsMap[halfYear] ?: 0
                }

                StatData(
                    data = data,
                    label = category
                )
            }
            .sortedBy { it.label }

        return MultiLineData(
            labels = labels,
            data = categoriesData
        )
    }
}