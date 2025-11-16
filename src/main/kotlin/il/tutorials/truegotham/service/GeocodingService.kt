package il.tutorials.truegotham.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import il.tutorials.truegotham.model.GeoJson
import il.tutorials.truegotham.model.Geocoordinates
import il.tutorials.truegotham.model.NominatimResponse
import il.tutorials.truegotham.utils.ValueContent
import jakarta.annotation.PostConstruct
import org.geojson.FeatureCollection
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LatLon(val lat: Double, val lon: Double)
typealias Line = List<LatLon>
data class Node(val lat: Double, val lon: Double)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Way(val geometry: List<Node>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OverpassResponse(val elements: List<Way>)

data class Zone( val name: String, val geometry: Geometry)

@Service
class GeocodingService {

    private lateinit var zoneGeometries: List<Zone>
    private val gf = GeometryFactory()
    private val reader = GeoJsonReader(gf)

    private val restTemplate = RestTemplate()
    private val objectMapper = jacksonObjectMapper()

    @PostConstruct
    fun init() {

        zoneGeometries = GeoJson.features.map { feature ->
            val geomJson = objectMapper.writeValueAsString(feature.geometry)
            val geom = reader.read(geomJson)
            val name = feature.getProperty("statistischer_bezirk") ?: "unbekannt"
            Zone(name, geom)
        }
    }

    fun geocode(address: String): Geocoordinates? {
        val url = "https://nominatim.openstreetmap.org/search?format=json&q=$address"

        return try {
            val response = restTemplate.getForObject(url, String::class.java)
            val results: List<NominatimResponse> = objectMapper.readValue(response ?: "[]")

            println("Response: $response")

            results.firstOrNull()?.let {
                Geocoordinates(it.lat.toDouble(), it.lon.toDouble())
            }
        } catch (e: Exception) {
            println("Fehler: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun findDistrictByCoords(coordinates: Geocoordinates): String? {
        val point = gf.createPoint(org.locationtech.jts.geom.Coordinate(coordinates.lon, coordinates.lat))
        return zoneGeometries.firstOrNull { it.geometry.contains(point) }?.name
    }

    // Kreuzung berechnen
    fun geocodeIntersections(street1: String, street2: String): List<LatLon> {
        val city = "Dortmund"

        val bbox = listOf(
            51.5139021889297, 7.454399557183694,
            51.51732058926283, 7.462038641327812
        )

        val street1Lines = fetchStreetLines(street1, city, bbox)
        val street2Lines = fetchStreetLines(street2, city, bbox)
        val intersections = mutableListOf<LatLon>()

        for(l1 in street1Lines) {
            for(l2 in street2Lines) {
                for(i in 0 until l1.size-1) {
                    for(j in 0 until l2.size-1) {
                        val p = lineIntersection(l1[i], l1[i+1], l2[j], l2[j+1])
                        if(p != null) intersections.add(p)
                    }
                }
            }
        }
        return intersections
    }

    // Schnittpunkt zweier Liniensegmente berechnen
    private fun lineIntersection(a1: LatLon, a2: LatLon, b1: LatLon, b2: LatLon): LatLon? {
        val x1 = a1.lon; val y1 = a1.lat
        val x2 = a2.lon; val y2 = a2.lat
        val x3 = b1.lon; val y3 = b1.lat
        val x4 = b2.lon; val y4 = b2.lat

        val denom = (x1 - x2)*(y3 - y4) - (y1 - y2)*(x3 - x4)
        if (denom == 0.0) return null

        val px = ((x1*y2 - y1*x2)*(x3-x4) - (x1-x2)*(x3*y4 - y3*x4)) / denom
        val py = ((x1*y2 - y1*x2)*(y3-y4) - (y1-y2)*(x3*y4 - y3*x4)) / denom

        if (px < minOf(x1,x2) || px > maxOf(x1,x2)) return null
        if (px < minOf(x3,x4) || px > maxOf(x3,x4)) return null
        if (py < minOf(y1,y2) || py > maxOf(y1,y2)) return null
        if (py < minOf(y3,y4) || py > maxOf(y3,y4)) return null

        return LatLon(py, px)
    }

    private fun fetchStreetLines(street: String, city: String, bbox: List<Double>? = null): List<Line> {
        val bboxQuery = if (bbox != null && bbox.size == 4) {
            // Benutzerdefinierte Bounding Box
            "(${bbox[0]}, ${bbox[1]}, ${bbox[2]}, ${bbox[3]})"
        } else {
            // Standard: Innenstadt Dortmund
            "(51.50982, 7.45074, 51.52019, 7.47716)"
        }

        val query = """
        [out:json][timeout:300];  // Overpass Timeout 5 Minuten
        way["highway"]["name"="$street"]$bboxQuery;
        out geom;
    """.trimIndent()


        val urlStr = "https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(query, "UTF-8")
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection

        // Timeout setzen (Millisekunden)
        conn.connectTimeout = 30_000   // Verbindung maximal 30 Sekunden
        conn.readTimeout = 300_000     // Lese-Timeout 5 Minuten

        val response = conn.inputStream.bufferedReader().use { it.readText() }

        println("result of fetch street lines: ${response.take(500)}...") // nur Anfang ausgeben

        // Jackson nutzen, unbekannte Felder ignorieren
        val overpassResponse: OverpassResponse = objectMapper.readValue(response)

        return overpassResponse.elements.map { way ->
            way.geometry.map { node -> LatLon(node.lat, node.lon) }
        }
    }
}