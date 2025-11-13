package il.tutorials.truegotham.utils

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    fun convertDate(input: String): String? {
        return try {
            val formatterIn = DateTimeFormatter.ofPattern("dd.MM.yyyy – HH:mm", Locale.GERMAN)
            val formatterOut = DateTimeFormatter.ISO_LOCAL_DATE // ergibt yyyy-MM-dd
            val date = LocalDate.parse(input, formatterIn)
            date.format(formatterOut)
        } catch (e: Exception) {
            null
        }
    }

    fun toUnixtsTime(input: String): Long? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMAN)
            val localDateTime = LocalDateTime.parse(input, formatter)
            localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        } catch (e: Exception) {
            null
        }
    }

    fun toUnixts(input: String): Long? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy – HH:mm", Locale.GERMAN)
            val localDateTime = LocalDateTime.parse(input, formatter)
            localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        } catch (e: Exception) {
            null
        }
    }

    fun fromRawToUnix(input: String): Long {
        val inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy – HH:mm")

        val zone = ZoneId.of("Europe/Berlin")
        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return dateTime.atZone(zone).toEpochSecond()
    }

    fun formatUnixTimestamp(unixTimestamp: Long): String {
        val instant = Instant.ofEpochSecond(unixTimestamp)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.of("Europe/Berlin"))
        return formatter.format(instant)
    }

    fun duration(start: Long) = System.currentTimeMillis() - start

    fun elapsedFormatted(start: Long) = formatDuration(duration(start))

    fun formatDuration(ms: Long) =  when {
            ms < 1_000 -> "$ms ms"
            ms < 60_000 -> String.format("%.2f s", ms / 1000.0)
            else -> String.format("%.2f min", ms / 60_000.0)
        }

}