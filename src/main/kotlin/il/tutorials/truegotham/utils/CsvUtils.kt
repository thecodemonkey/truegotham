package il.tutorials.truegotham.utils

import il.tutorials.truegotham.model.PressRelease
import java.io.File

object CsvUtils {
    fun writePressReleasesToCsv(pressReleases: List<PressRelease>, path: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        val separator = ";"

        file.bufferedWriter(Charsets.UTF_8).use { out ->
            out.appendLine("timestamp${separator}title${separator}content${separator}lfd_nr${separator}url${separator}topics")

            for (pr in pressReleases) {
                val topicsJoined = pr.topics.joinToString(";")
                val escapedContent = pr.content.replace("\"", "\"\"")
                val escapedTitle = pr.title.replace("\"", "\"\"")

                out.appendLine(
                    "\"${pr.timestamp}\"$separator\"$escapedTitle\"$separator\"$escapedContent\"$separator\"${pr.lfd_nr}\"$separator\"${pr.url}\"$separator\"$topicsJoined\""
                )
            }
        }
    }
}
