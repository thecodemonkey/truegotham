package il.tutorials.truegotham.service

import org.springframework.stereotype.Service

@Service
class AddressNormalizer {

    companion object {
        private val streetTypeMappings = mapOf(
            // Straße/Strasse
            "straße" to "strasse",
            "strasse" to "strasse",
            "str." to "strasse",
            "str" to "strasse",

            // Weg
            "weg" to "weg",

            // Platz
            "platz" to "platz",
            "pl." to "platz",
            "pl" to "platz",

            // Allee
            "allee" to "allee",
            "all." to "allee",
            "all" to "allee",

            // Gasse
            "gasse" to "gasse",
            "g." to "gasse",

            // Ring
            "ring" to "ring",

            // Damm
            "damm" to "damm",

            // Ufer
            "ufer" to "ufer",

            // Promenade
            "promenade" to "promenade",
            "prom." to "promenade",

            // Chaussee
            "chaussee" to "chaussee",
            "ch." to "chaussee",

            // Avenue/Avenida (für internationale Adressen)
            "avenue" to "avenue",
            "ave." to "avenue",
            "ave" to "avenue",

            // Boulevard
            "boulevard" to "boulevard",
            "blvd." to "boulevard",
            "blvd" to "boulevard"
        )
    }

    /**
     * Normalisiert einen Straßennamen für Geocoding-Cache-Keys
     *
     * @param streetName Der zu normalisierende Straßenname
     * @return Normalisierter Straßenname in Kleinbuchstaben ohne Sonderzeichen
     */
    fun normalize(streetName: String): String {
        var normalized = streetName.trim()

        // 1. Zu Kleinbuchstaben konvertieren
        normalized = normalized.lowercase()

        // 2. ß durch ss ersetzen
        normalized = normalized.replace("ß", "ss")

        // 3. Umlaute ersetzen
        normalized = normalized
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")

        // 4. Mehrfache Leerzeichen normalisieren
        normalized = normalized.replace(Regex("\\s+"), " ")

        // 5. Bindestriche durch Leerzeichen ersetzen (für einheitliche Verarbeitung)
        normalized = normalized.replace("-", " ")

        // 6. Punkte entfernen (werden oft in Abkürzungen verwendet)
        normalized = normalized.replace(".", "")

        // 7. Straßentyp-Abkürzungen und Varianten normalisieren
        normalized = normalizeStreetType(normalized)

        // 8. Alle verbleibenden Leerzeichen entfernen
        normalized = normalized.replace(" ", "")

        // 9. Alle nicht-alphanumerischen Zeichen entfernen
        //normalized = normalized.replace(Regex("[^a-z0-9]"), "")

        return normalized
    }

    fun combineAddress(street: String?, district: String?, city: String?, houseNumber: String?, place: String?, normalize: Boolean = false): String {
        val components = mutableListOf<String>()

        if(!street.isNullOrBlank()) {
            var str = if (normalize) normalize(street) else street
            houseNumber?.let { str += " $houseNumber" }
            components.add(str)
        } else {
            if (district.isNullOrBlank()) {
                place?.let {
                   components.add(if (normalize) normalize(place) else place)
                }
            } else {
                components.add(if (normalize) normalize(district) else district)
            }
        }

        city?.let {
            components.add(if (normalize) normalize(city) else city)
        }

        return components.joinToString(", ")
            .trim(',')
            .trim()
    }


    /**
     * Normalisiert Straßentypen (Straße, Weg, Platz, etc.)
     */
    private fun normalizeStreetType(streetName: String): String {
        var result = streetName

        // Versuche, das letzte Wort als Straßentyp zu identifizieren
        val parts = result.split(" ")
        if (parts.size >= 2) {
            val lastPart = parts.last()
            val mappedType = streetTypeMappings[lastPart]

            if (mappedType != null) {
                // Ersetze das letzte Wort durch die normalisierte Form
                result = parts.dropLast(1).joinToString(" ") + " " + mappedType
            }
        }

        return result
    }

    /**
     * Batch-Normalisierung für mehrere Straßennamen
     */
    fun normalizeAll(streetNames: List<String>): Map<String, String> {
        return streetNames.associateWith { normalize(it) }
    }


}