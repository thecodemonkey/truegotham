package il.tutorials.truegotham.model

object CrimeTypes {
    private val crimeTypes = hashMapOf(
        "all" to "all",
        "violence" to "Gewalt-Delikte",
        "traffic" to "Straßenverkehrsdelikte",
        "theft" to "Diebstahl",
        "robbery" to "Raub",
        "arson_damage" to "Brand-/Sachbeschädigung",
        "drugs" to "Drogen-Delikte",
        "fraud" to "Betrug",
        "homicide" to "Tötungsdelikt",
        "threats" to "Bedrohung / Einschüchterung",
        "public_trespassing" to "Ordnungs-/Hausfriedensdelikte",
        "political" to "Politisch motivierte Taten",
        "sexual" to "Sexualdelikte",
        "organised" to "Organisiertes Verbrechen",
        "cyber" to "Cyberkriminalität"
    )

    fun getCrime(key: String) = crimeTypes[key] ?: key

    fun mapCrimes(keys: List<String>?) =
        if (keys.isNullOrEmpty()) listOf()
        else keys.map { getCrime(it) }

}