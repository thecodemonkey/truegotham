package il.tutorials.truegotham.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import il.tutorials.truegotham.model.GeoJson
import il.tutorials.truegotham.model.Geocoordinates
import il.tutorials.truegotham.model.NominatimResponse
import il.tutorials.truegotham.model.ai.structured.IncidentLocationResult
import il.tutorials.truegotham.model.entity.GeocodingCacheItem
import il.tutorials.truegotham.repository.GeocodingCacheRepository
import il.tutorials.truegotham.repository.IncidentRepository
import jakarta.annotation.PostConstruct
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
data class Node(val id: Long, val lat: Double, val lon: Double)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Way(val id: Long, val name: String, val nodeIds: List<Long>, val geometry: List<Node>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OverpassResponse(val elements: List<Way>)
data class Zone( val name: String, val geometry: Geometry)
data class GeocodedAddress(val coordinates: Geocoordinates?, val address: String)

@Service
class GeocodingService(
    val cache: GeocodingCacheRepository,
    val incidentRepo: IncidentRepository,
    val addressNormalizer: AddressNormalizer,
    val intersectionService: GeocodingIntersectionService
) {

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

    fun geocodeLocationResult(location: IncidentLocationResult): GeocodedAddress {
        var coordinates: Geocoordinates? = null
        var address = ""

        val isIntersection =
            !location.street.isNullOrBlank() &&
            !location.street2.isNullOrBlank() &&
            location.street != location.street2

        if (isIntersection) {
            address = "${location.street} / ${location.street2}, ${location.city}"
            coordinates = geocodeIntersections(location.street, location.street2)
        }

        if (coordinates == null) {
            address = addressNormalizer.combineAddress(
                street = location.street,
                district = null, //todo: append district
                city = location.city,
                houseNumber = null, //todo: append houseno
                place = location.place,
                normalize = false
            )

            val addressCacheKey = addressNormalizer.combineAddress(
                street = location.street,
                district = null, //todo: append district
                city = location.city,
                houseNumber = null, //todo: append houseno
                place = location.place,
                normalize = true
            )

            coordinates = geocode(address, addressCacheKey)
        }

        //fallback to district if exists
        if (coordinates == null && location.district.isNotBlank()) {
            val distaddr = "${location.district}, ${location.city}"

            coordinates = geocode(distaddr, distaddr.lowercase().trim())
        }

        return GeocodedAddress(
            coordinates,
            address
        )
    }

    fun geocode(address: String, cacheKey: String? =  null): Geocoordinates? {
        cacheKey?.let {
            val existing = loadFromCache(cacheKey)
            if (existing != null) {
                println("Cache Treffer für Adresse: $cacheKey -> $existing")
                return existing
            }
        }


        println("try geocoding address: $address")
        val url = "https://nominatim.openstreetmap.org/search?format=json&q=$address"

        return try {
            val response = restTemplate.getForObject(url, String::class.java)
            val results: List<NominatimResponse> = objectMapper.readValue(response ?: "[]")

            println("Response: $response")

            results.firstOrNull()?.let {
                val coords = Geocoordinates(it.lat.toDouble(), it.lon.toDouble())
                putToCache(cacheKey ?: addressNormalizer.normalize(address), coords)
                coords
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

    fun loadFromCache(address: String): Geocoordinates? {
        val item:Geocoordinates? = null

        cache.findById(address.lowercase().trim()).ifPresent {
            println("address: '$address' found in cache...")
            Geocoordinates(it.latitude, it.longitude)
        }

        return item
    }

    fun putToCache(address: String, coordinates: Geocoordinates) {
        val item = GeocodingCacheItem(address.lowercase().trim(), coordinates.lat, coordinates.lon)
        if (cache.findById(address.lowercase().trim()).isPresent) return
        cache.save(item)
    }

    fun updateGeocodingCache() {
        incidentRepo.findAll().forEachIndexed{ i, incident ->
            incident.locations.filter {
                it.coordinates != null &&
                it.city?.lowercase() == "dortmund" &&
                it.street != null
            }.
            forEach{ location ->
                val address = addressNormalizer.combineAddress(
                    street = location.street,
                    district = null,
                    city = location.city,
                    houseNumber = null,
                    place = location.place,
                    normalize = true
                )

                if(address.isNotBlank()) {
                    println("${i.toString().padEnd(3)} Geocoding address: ${address.padEnd(40)}  - ${location.coordinates}" )
                    putToCache(address, location.coordinates!!)
                }
            }
        }
    }

    // Kreuzung berechnen
    fun geocodeIntersections(street1: String, street2: String): Geocoordinates? {
        println("try geocoding intersection: $street1 / $street2")

        val intersectionAddress = "${addressNormalizer.normalize(street1)} / ${addressNormalizer.normalize(street2)}, dortmund"
        val intersectionAddress2 = "${addressNormalizer.normalize(street2)} / ${addressNormalizer.normalize(street1)}, dortmund"

        var result = loadFromCache(intersectionAddress);
        if (result != null) return result

        result = loadFromCache(intersectionAddress2);
        if (result != null) return result

        result = intersectionService.geocode("$street1 / $street2")?.let {
            val coords = Geocoordinates(it.first, it.second)
            putToCache(intersectionAddress, coords)
            coords
        }

        // fallback with reversed intersection
        if (result == null) {
            result = intersectionService.geocode("$street2 / $street1")?.let {
                val coords = Geocoordinates(it.first, it.second)
                putToCache(intersectionAddress2, coords)
                coords
            }
        }

        return result
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