package il.tutorials.truegotham.utils

import il.tutorials.truegotham.model.entity.RawStatement
import java.io.File

object CsvUtils {
    fun writeRawStatementsToCsv(rawStatements: List<RawStatement>, path: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        val separator = ";"

        file.bufferedWriter(Charsets.UTF_8).use { out ->
            out.appendLine("timestamp${separator}title${separator}content${separator}lfd_nr${separator}url${separator}topics")

            for (pr in rawStatements) {
                val topicsJoined = pr.topics?.joinToString(";")
                val escapedContent = pr.content?.replace("\"", "\"\"")
                val escapedTitle = pr.title?.replace("\"", "\"\"")

                out.appendLine(
                    "\"${pr.unixts}\"$separator\"$escapedTitle\"$separator\"$escapedContent\"$separator\"${pr.lfd_nr}\"$separator\"${pr.url}\"$separator\"$topicsJoined\""
                )
            }
        }
    }
}
