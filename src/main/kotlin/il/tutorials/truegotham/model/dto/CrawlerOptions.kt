package il.tutorials.truegotham.model.dto

data class CrawlerOptions(
    val cityID:String = "4971", //"https://www.presseportal.de/blaulicht/nr/4971",
    val startDate: String = "2015-01-01",
    val endDate: String = "2025-10-03"
)
