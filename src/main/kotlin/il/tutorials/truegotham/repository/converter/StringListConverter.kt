package il.tutorials.truegotham.repository.converter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>?, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<String>?): String =
        mapper.writeValueAsString(attribute ?: emptyList<String>())

    override fun convertToEntityAttribute(dbData: String?): List<String> =
        dbData?.takeIf { it.isNotBlank() }
            ?.let { mapper.readValue(it, object : TypeReference<List<String>>() {}) }
            ?: emptyList()
}