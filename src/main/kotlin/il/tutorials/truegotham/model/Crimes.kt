package il.tutorials.truegotham.model

import il.tutorials.truegotham.utils.JsonUtils


object Crimes {
    private val categories: Map<String, List<String>> by lazy {
        JsonUtils.fromJSONResource("data/crimes.json")
    }

    private val lookup by lazy {
        categories.flatMap { (top, sub) ->
            sub.map { it.lowercase() to top }
        }.toMap()
    }

    fun normalize(sub: String) =
        if (isTopCategory(sub)) sub else lookup[sub.lowercase()]

    fun isTopCategory(name: String) =
        categories.keys.any { it.equals(name, true) }
}
