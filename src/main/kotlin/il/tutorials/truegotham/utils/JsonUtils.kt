package il.tutorials.truegotham.utils

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.geojson.GeoJsonObject
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.nio.charset.StandardCharsets


@JsonFilter("dynamicFilter")
interface DynamicFilterMixIn

object JsonUtils {
    val mapper = jacksonObjectMapper()

    inline fun <reified T> fromJSON(json: String): T {
        return mapper.readValue(json)
    }

    fun toJSON(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }

    fun toJSON(obj: Any, vararg excludeProps: String): String {
        val m = jacksonObjectMapper().apply {
            setFilterProvider(
                SimpleFilterProvider().addFilter(
                    "dynamicFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept(*excludeProps)
                )
            )
            addMixIn(Any::class.java, DynamicFilterMixIn::class.java)
        }


        return m.writeValueAsString(obj)
    }

    inline fun <reified T> fromJSONResource(resourcePath: String): T {
        val inputStream = this::class.java.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource $resourcePath not found")
        val json = inputStream.readAllBytes().toString(StandardCharsets.UTF_8)
        return fromJSON(json)
    }

    fun loadJSONLResource(resourcePath: String): String {
        val resource = ClassPathResource(resourcePath)
        return resource.inputStream.bufferedReader().use { it.readText() };
    }

    inline fun <reified T> fromJSONFile(filePath: String): T {
        val file = File(filePath)
        if (!file.exists()) throw IllegalArgumentException("File $filePath not found")

        val json = file.readBytes().toString(StandardCharsets.UTF_8)
        return fromJSON(json)
    }

    fun toJSONFile(obj: Any, filePath: String) = FileUtils.saveTextFile(toJSON(obj), filePath)

    fun loadGEOJSONResource(resourcePath: String): GeoJsonObject? {
        val resource = ClassPathResource(resourcePath)
        return ObjectMapper().readValue(resource.inputStream, GeoJsonObject::class.java)

    }
}