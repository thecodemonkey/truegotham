package il.tutorials.truegotham.import.services

import il.tutorials.truegotham.service.AddressNormalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CombineAddressTest {
    private val normalizer = AddressNormalizer()

    companion object {
        @JvmStatic
        fun provideCombineAddressTestCases(): Stream<Arguments> {
            return Stream.of(


                Arguments.of("Heinrich-Sondermann-Platz", null, "Dortmund", null, null, true, "heinrichsondermannplatz, dortmund"),

                // Standard-Fall: Alle Werte vorhanden, keine Normalisierung
                Arguments.of("Hauptstraße", "Mitte", "Berlin", "123", null, false, "Hauptstraße 123, Berlin"),

                // Mit Normalisierung
                Arguments.of("Haupt-Straße", "Mitte", "Berlin", "123", null, true, "hauptstrasse 123, berlin"),
                Arguments.of("Kamp Str.", "Altstadt", "Köln", "45", null, true, "kampstrasse 45, koeln"),

                // Nur Street und City (kein District, kein Place)
                Arguments.of("Hauptstraße", null, "Berlin", null, null, false, "Hauptstraße, Berlin"),

                // Street, City und HouseNumber (ohne District)
                Arguments.of("Hauptstraße", null, "Berlin", "123", null, false, "Hauptstraße 123, Berlin"),

                // Street ist null, District vorhanden
                Arguments.of(null, "Mitte", "Berlin", null, null, false, "Mitte, Berlin"),

                // Street ist null, kein District, aber Place vorhanden
                Arguments.of(null, null, "Berlin", null, "Alexanderplatz", false, "Alexanderplatz, Berlin"),

                // Street ist blank (leer), kein District, aber Place vorhanden
                Arguments.of("", null, "Berlin", null, "Alexanderplatz", false, "Alexanderplatz, Berlin"),
                Arguments.of("   ", null, "Berlin", null, "Alexanderplatz", false, "Alexanderplatz, Berlin"),

                // Street ist null, District hat Vorrang vor Place
                Arguments.of(null, "Mitte", "Berlin", null, "Alexanderplatz", false, "Mitte, Berlin"),

                // Street ist blank, District hat Vorrang vor Place
                Arguments.of("", "Kreuzberg", "Berlin", null, "Alexanderplatz", false, "Kreuzberg, Berlin"),

                // District ist blank/leer, Place wird verwendet
                Arguments.of(null, "", "Hamburg", null, "Altona", false, "Altona, Hamburg"),
                Arguments.of(null, "   ", "Hamburg", null, "Altona", false, "Altona, Hamburg"),

                // Nur City
                Arguments.of(null, null, "Berlin", null, null, false, "Berlin"),

                // Street vorhanden, District wird hinzugefügt
                Arguments.of("Hauptstraße", "Mitte", "Berlin", null, "Alexanderplatz", false, "Hauptstraße, Berlin"),

                // Street vorhanden mit HouseNumber, District wird hinzugefügt
                Arguments.of("Hauptstraße", "Mitte", "Berlin", "123", "Alexanderplatz", false, "Hauptstraße 123, Berlin"),

                // HouseNumber ohne Street (sollte ignoriert werden, da Street null)
                Arguments.of(null, "Mitte", "Berlin", "123", null, false, "Mitte, Berlin"),

                // Alle Werte null
                Arguments.of(null, null, null, null, null, false, ""),

                // Nur Place (kein Street, kein District)
                Arguments.of(null, null, null, null, "Alexanderplatz", false, "Alexanderplatz"),

                // Nur District (kein Street, kein Place)
                Arguments.of(null, "Mitte", null, null, null, false, "Mitte"),

                // Komplexe Hausnummern
                Arguments.of("Hauptstraße", null, "Berlin", "123a", null, false, "Hauptstraße 123a, Berlin"),
                Arguments.of("Hauptstraße", null, "Berlin", "12-14", null, false, "Hauptstraße 12-14, Berlin"),

                // Sonderzeichen in Werten (ohne Normalisierung)
                Arguments.of("Haupt-Straße", "Berlin-Mitte", "Berlin", "123/1", null, false, "Haupt-Straße 123/1, Berlin"),

                // Lange Adresse mit allen Komponenten
                Arguments.of("Unter den Linden", "Mitte", "Berlin", "1", "Brandenburger Tor", false, "Unter den Linden 1, Berlin"),

                // City null, aber andere Werte vorhanden
                Arguments.of("Hauptstraße", "Mitte", null, "123", null, false, "Hauptstraße 123"),

                // Street blank, District blank, Place vorhanden
                Arguments.of("", "", "München", null, "Marienplatz", false, "Marienplatz, München"),

                // Normalisierung mit Umlauten
                Arguments.of("Königsallee", "Altstadt", "Düsseldorf", "1", null, true, "koenigsallee 1, duesseldorf"),
                Arguments.of("Müller Weg", null, "Hamburg", "99", null, true, "muellerweg 99, hamburg"),

                // Normalisierung mit ß
                Arguments.of("Große Straße", null, "Berlin", "7", null, true, "grossestrasse 7, berlin"),

                // Alle blank/empty (außer City)
                Arguments.of("", "", "München", "", "", false, "München"),

                // Street mit HouseNumber, kein District, Place wird ignoriert
                Arguments.of("Bahnhofstraße", null, "Köln", "50", "Hauptbahnhof", false, "Bahnhofstraße 50, Köln"),

                // Normalisierung: default false
                Arguments.of("Kamp-Str.", null, "Bergkamen", "10", null, false, "Kamp-Str. 10, Bergkamen")
            )
        }
    }

    @ParameterizedTest(name = "{index} => street=''{0}'', district=''{1}'', city=''{2}'', houseNumber=''{3}'', place=''{4}'', normalize={5} -> ''{6}''")
    @MethodSource("provideCombineAddressTestCases")
    fun `sollte Adresskomponenten korrekt kombinieren`(
        street: String?,
        district: String?,
        city: String?,
        houseNumber: String?,
        place: String?,
        normalize: Boolean,
        expected: String
    ) {
        val result = normalizer.combineAddress(street, district, city, houseNumber, place, normalize)
        assertEquals(expected, result)
    }
}