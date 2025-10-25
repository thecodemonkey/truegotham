package il.tutorials.truegotham.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    fun formatUnixTs(unixTimestampMillis: Long): String? {
        return try {
            val instant = Instant.ofEpochMilli(unixTimestampMillis)
            val date = LocalDate.ofInstant(instant, ZoneId.systemDefault())
            val formatterOut = DateTimeFormatter.ISO_LOCAL_DATE
            date.format(formatterOut)
        } catch (e: Exception) {
            null
        }
    }

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

    fun toUnixts(input: String): Long? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy – HH:mm", Locale.GERMAN)
            val localDateTime = LocalDateTime.parse(input, formatter)
            localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
        } catch (e: Exception) {
            null
        }
    }
}