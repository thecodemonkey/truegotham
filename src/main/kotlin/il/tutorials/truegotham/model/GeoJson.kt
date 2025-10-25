package il.tutorials.truegotham.model

import il.tutorials.truegotham.utils.JsonUtils
import org.geojson.FeatureCollection

object GeoJson {
    val features = JsonUtils.fromJSONResource<FeatureCollection>("static/data/dortmund.geojson")

    val districtNames: List<String> =
        features.mapNotNull { it.getProperty("statistischer_bezirk") as? String }
}