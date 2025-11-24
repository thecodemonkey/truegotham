package il.tutorials.truegotham.import.services

import il.tutorials.truegotham.service.AddressNormalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class AddressNormalizerTest {

    private val normalizer = AddressNormalizer()

    @ParameterizedTest(name = "{index} => Eingabe: ''{0}'' sollte normalisiert werden zu ''{1}''")
    @CsvSource(
        "Kampstrasse, kampstrasse",
        "Kampstraße, kampstrasse",
        "Kamp Str., kampstrasse",
        "Kamp Str, kampstrasse",
        "Kamp Straße, kampstrasse",
        "Kamp Strasse, kampstrasse",
        "Kamp-Strasse, kampstrasse",
        "Kamp-Straße, kampstrasse",
        "Kamp-Str, kampstrasse",
        "Kamp-Str., kampstrasse",
        "KAMPSTRASSE, kampstrasse",
        "kampstrasse, kampstrasse"
    )
    @DisplayName("Normalisierung von Kampstraße Varianten")
    fun `sollte verschiedene Schreibweisen von Kampstrasse normalisieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @CsvSource(
        "Königsallee, koenigsallee",
        "Königs-Allee, koenigsallee",
        "Königs Allee, koenigsallee",
        "Königs All., koenigsallee",
        "Königs All, koenigsallee"
    )
    @DisplayName("Normalisierung von Allee mit Umlauten")
    fun `sollte Allee-Varianten mit Umlauten normalisieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @CsvSource(
        "Müller Weg, muellerweg",
        "Müller-Weg, muellerweg",
        "MüllerWeg, muellerweg",
        "Goethe-Platz, goetheplatz",
        "Goethe Platz, goetheplatz",
        "Heinrich-Sondermann-Platz, heinrichsondermannplatz",
        "Heinrich Sondermann Platz, heinrichsondermannplatz",
        "Goethe Pl., goetheplatz",
        "Goethe Pl, goetheplatz"
    )
    @DisplayName("Normalisierung verschiedener Straßentypen")
    fun `sollte verschiedene Strassentypen normalisieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @CsvSource(
        "'   Hauptstraße   ', hauptstrasse",
        "'Haupt  Straße', hauptstrasse",
        "' Haupt - Straße ', hauptstrasse"
    )
    @DisplayName("Normalisierung mit Leerzeichen")
    fun `sollte führende nachfolgende und mehrfache Leerzeichen entfernen`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @MethodSource("provideUmlautTestCases")
    @DisplayName("Normalisierung von Umlauten")
    fun `sollte alle Umlaute korrekt konvertieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @CsvSource(
        "Große-Straße, grossestrasse",
        "Große Straße, grossestrasse",
        "Grosse Strasse, grossestrasse",
        "Große Str., grossestrasse"
    )
    @DisplayName("Normalisierung von ß")
    fun `sollte ß in ss konvertieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @ParameterizedTest(name = "{index} => ''{0}'' -> ''{1}''")
    @CsvSource(
        "Berliner Ring, berlinerring",
        "Rhein-Damm, rheindamm",
        "Rhein Damm, rheindamm",
        "See-Ufer, seeufer",
        "Haupt-Gasse, hauptgasse",
        "Haupt Gasse, hauptgasse"
    )
    @DisplayName("Normalisierung weiterer Straßentypen")
    fun `sollte weitere Strassentypen normalisieren`(input: String, expected: String) {
        assertEquals(expected, normalizer.normalize(input))
    }

    @Test
    @DisplayName("Batch-Normalisierung")
    fun `sollte mehrere Strassennamen auf einmal normalisieren`() {
        val input = listOf("Kampstraße", "Müller Weg", "Goethe-Platz")
        val expected = mapOf(
            "Kampstraße" to "kampstrasse",
            "Müller Weg" to "muellerweg",
            "Goethe-Platz" to "goetheplatz"
        )

        val result = normalizer.normalizeAll(input)

        assertEquals(expected, result)
    }

    @Test
    @DisplayName("Leere Eingabe")
    fun `sollte leere Strings korrekt behandeln`() {
        assertEquals("", normalizer.normalize(""))
        assertEquals("", normalizer.normalize("   "))
    }



    companion object {
        @JvmStatic
        fun provideUmlautTestCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("Äpfel-Straße", "aepfelstrasse"),
                Arguments.of("Öl-Weg", "oelweg"),
                Arguments.of("Über-Allee", "ueberallee"),
                Arguments.of("Müller Straße", "muellerstrasse"),
                Arguments.of("Bäcker Weg", "baeckerweg"),
                Arguments.of("Löwen Platz", "loewenplatz")
            )
        }
    }
}