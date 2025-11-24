package il.tutorials.truegotham.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.web.client.RestTemplate
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
data class OverpassResponseElms(
    val elements: List<OverpassElement>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OverpassElement(
    val id: Long,
    val type: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val tags: Map<String, String>? = null,
    val nodes: List<Long>? = null
)

@Service
class GeocodingIntersectionService(
    val addressNormalizer: AddressNormalizer
) {


    private val restTemplate = RestTemplate()
    private val mapper = jacksonObjectMapper()

    private val nodesFile = File("nodes.json")
    private val waysFile = File("ways.json")

    private val nodes = mutableMapOf<Long, Node>()
    private val ways = mutableListOf<Way>()
    private val intersections = mutableMapOf<Long, Set<String>>()

    @PostConstruct
    fun init() {
        if (!nodesFile.exists() || !waysFile.exists()) {
            loadFromOverpass()
        }

        val loadedNodes: List<Node> = mapper.readValue(nodesFile)
        loadedNodes.forEach { nodes[it.id] = it }

        ways.addAll(mapper.readValue(waysFile))

        intersections.putAll(computeIntersections(nodes, ways))
        println("Init abgeschlossen: ${nodes.size} nodes, ${ways.size} ways, ${intersections.size} intersections")
    }

    private fun loadFromOverpass() {
        val query = """
            [out:json][timeout:180];
            area["name"="Dortmund"]->.a;
            way["highway"]["name"](area.a);
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val url = "https://overpass-api.de/api/interpreter?data=${query}"

        val responseJson: String =
            restTemplate.getForObject(url, String::class.java)
                ?: throw RuntimeException("Empty Overpass response")

        // Direct Jackson mapping into strongly typed model
        val response: OverpassResponseElms = mapper.readValue(responseJson)

        val tmpNodes = mutableMapOf<Long, Node>()
        val tmpWays = mutableListOf<Way>()

        response.elements.forEach { e ->
            when (e.type) {
                "node" -> if (e.lat != null && e.lon != null) {
                    tmpNodes[e.id] = Node(e.id, e.lat, e.lon)
                }

                "way" -> {
                    val name = e.tags?.get("name") ?: return@forEach
                    val nIds = e.nodes ?: return@forEach
                    tmpWays.add(Way(
                        id = e.id,
                        name = addressNormalizer.normalize(name),
                        nodeIds = nIds,
                        listOf()))
                }
            }
        }

        mapper.writeValue(nodesFile, tmpNodes.values.toList())
        mapper.writeValue(waysFile, tmpWays)
    }

    private fun computeIntersections(nodes: Map<Long, Node>, ways: List<Way>): Map<Long, Set<String>> {
        val nodeStreets = mutableMapOf<Long, MutableSet<String>>()
        for (way in ways) {
            for (nodeId in way.nodeIds) {
                nodeStreets.computeIfAbsent(nodeId) { mutableSetOf() }.add(
                    addressNormalizer.normalize(way.name)
                )
            }
        }
        return nodeStreets.filter { it.value.size >= 2 }
    }

    fun geocode(crossing: String): Pair<Double, Double>? {
        var (s1, s2) = crossing.split("/").map { it.trim() }

        s1 = addressNormalizer.normalize(s1)
        s2 = addressNormalizer.normalize(s2)

        intersections.forEach { (nodeId, streets) ->
            //println("streets at node $nodeId: $streets")

            if (s1 in streets && s2 in streets) {
                val node = nodes[nodeId]!!
                return Pair(node.lat, node.lon)
            }
        }
        return null
    }
}
