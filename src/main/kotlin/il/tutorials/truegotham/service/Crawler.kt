package il.tutorials.truegotham.service

import com.microsoft.playwright.*
import com.microsoft.playwright.options.WaitUntilState
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.entity.RawStatement
import il.tutorials.truegotham.model.dto.CrawlerOptions
import il.tutorials.truegotham.utils.CommonUtils
import il.tutorials.truegotham.utils.DateUtils
import il.tutorials.truegotham.utils.FileUtils
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.util.*


@Service
class Crawler {
    private val url = "https://www.presseportal.de/blaulicht/nr/"

    fun crawl(cityID: String, startDate: String, endDate: String): List<RawStatement> {
        val allReleases = mutableListOf<RawStatement>()
        val baseUrl = "${url}${cityID}"
        var pageUrl = "$baseUrl?startDate=$startDate&endDate=$endDate"

        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch(
                BrowserType.LaunchOptions().setHeadless(true)
            )

            browser.newContext().use { context ->
                val page = context.newPage()

                var currentPage = 1
                var hasMorePages = true

                while (hasMorePages) {
                    println("Crawling Seite $currentPage...")

                    /*                    val pageUrl = if (currentPage == 1) {
                                            "$baseUrl?startDate=$startDate&endDate=$endDate"
                                        } else {
                                            "$baseUrl?startDate=$startDate&endDate=$endDate&page=$currentPage"
                                        }*/

                    page.navigate(
                        pageUrl, Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    )

                    // Warte, bis die Artikel geladen sind
                    page.waitForSelector(
                        "article", Page.WaitForSelectorOptions()
                            .setTimeout(10000.0)
                    )

                    // Extrahiere alle Artikel-Links auf der Seite
                    val articleLinks = page.locator("article h3 a").all()
                        .mapNotNull { it.getAttribute("href") }
                        .filter { it.contains("/blaulicht/pm/") }
                        .map { it }
                        .distinct()

                    println("Gefunden: ${articleLinks.size} Artikel auf Seite $currentPage")


                    val pageReleases = mutableListOf<RawStatement>()

                    // Crawle jeden Artikel
                    articleLinks.forEach { articleUrl ->
                        try {
                            val release = crawlArticle(page, articleUrl)
                            allReleases.add(release)
                            pageReleases.add(release)
                            println("✓ Erfolgreich: ${release.lfd_nr} - ${release.title?.take(50)}...")
                        } catch (e: Exception) {
                            println("✗ Fehler bei $articleUrl: ${e.message}")
                        }

                        // Kurze Pause zwischen Requests
                        Thread.sleep(500)
                    }


                    //navigate back to overview:
                    page.navigate(pageUrl, Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
                    page.waitForSelector("article", Page.WaitForSelectorOptions().setTimeout(10000.0))


                    // Prüfe, ob es eine nächste Seite gibt
                    hasMorePages = try {
                        val nextButton = page.locator(".pagination .btn.pagination-next").first()

                        if (nextButton.isVisible) {
                            val u = nextButton.getAttribute("data-url")
                            pageUrl = "$url${u.replace("@/blaulicht/nr/", "")}"
                        }

                        nextButton.isVisible
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }


//                    save to file
//                    val json = JsonUtils.toJSON(pageReleases);
//                    val fileName = "page_${pageReleases.last().timestamp}.json".replace(":", "_").replace("-", "_").replace(" ", "")
//                    val tmpFileName = Paths.get(System.getProperty("java.io.tmpdir"), "press", fileName).toString()
//                    FileUtils.saveTextFile(json, tmpFileName);


                    if (hasMorePages) {
                        currentPage++
                        Thread.sleep(1000) // Pause zwischen Seiten
                    }
                }

                println("\nCrawling abgeschlossen! ${allReleases.size} Pressemitteilungen gesammelt.")
            }

            browser.close()
        }


        return allReleases
    }

    private fun crawlArticle(page: Page, url: String): RawStatement {
        page.navigate(
            url, Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
        )

        // Warte auf den Artikel-Inhalt
        page.waitForSelector(
            "article", Page.WaitForSelectorOptions()
                .setTimeout(10000.0)
        )

        // Extrahiere Timestamp
        val timestamp = page.locator("article time").first().textContent().trim()

        // Extrahiere Titel
        val title = page.locator("article h1").first().textContent().trim()

        // Extrahiere Lfd. Nr.
        val lfdNr = page.locator("article p:has-text('Lfd. Nr.:')").first()
            .textContent().trim()
            .replace("Lfd. Nr.:", "").trim()

        // Extrahiere den gesamten Content (alle Paragraphen im Artikel)
        val contentParagraphs = page.locator("article.story p").all()
            .asSequence()
            .filter {
                val cls = it.getAttribute("class") ?: ""
                !cls.split(" ").any {
                    c ->
                    (c.startsWith("contact-") ||
                     c.startsWith("originator") ||
                     c.startsWith("date") ||
                     c.startsWith("customer"))
                }
            }
            .map { it.textContent().trim() }
            .filter { it.isNotEmpty() && !it.startsWith("Lfd. Nr.:") }
            .filter { !it.contains("Journalisten wenden sich") }
            .filter { !it.contains("Original-Content von:") }
            .joinToString("\n\n").trim().replace("Dortmund (ots)", "").trim()

        // Extrahiere Topics
        val topics = try {
            page.locator("text='Themen in dieser Meldung'")
                .first()
                .locator("xpath=following-sibling::*[1]")
                .locator("a")
                .all()
                .map { it.textContent().trim() }
        } catch (e: Exception) {
            emptyList()
        }

        return RawStatement(
            id = UUID.randomUUID(),
            hash = CommonUtils.hashString(timestamp + lfdNr),
            unixts = DateUtils.fromRawToUnix(timestamp),
            //timestamp = timestamp,
            title = title,
            content = contentParagraphs,
            lfd_nr = lfdNr.toInt(),
            url = url,
            topics = topics,
            status = ImportStatus.IMPORTED
        )
    }

    fun continueCrawling(cityID: String): List<RawStatement> {
        val tmpPath = Paths.get(System.getProperty("java.io.tmpdir"), "press").toString()
        val json = FileUtils.getLastModifiedFileText(tmpPath)

        if (json != null) {
            val statements: List<RawStatement> = JsonUtils.fromJSON(json)
            val dt = ""//DateUtils.convertDate(statements.last().timestamp);

            if (dt != null) {
                val options = CrawlerOptions(
                    cityID,
                    startDate = "2015-01-01",
                    endDate = dt
                )
                return crawl(cityID, options.startDate, options.endDate)
            }
        }

        throw Exception("cant read new file..");

    }
}
