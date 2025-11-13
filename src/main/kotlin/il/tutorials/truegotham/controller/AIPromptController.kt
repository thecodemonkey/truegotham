package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.AIPromptService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AIPromptController(
    val prompt: AIPromptService
) {
    @Operation(
        summary = "classify statement",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [
                io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "text/plain",
                    examples = [
                        io.swagger.v3.oas.annotations.media.ExampleObject(
                            value = "Gegen 16.20 Uhr beobachteten zwei Zivilkräfte der Polizei an der Westerbleichstraße / Ecke Baumstraße ein offensichtliches Drogengeschäft. Anschließend wollten sie einen 18-jährigen Tatverdächtigen festnehmen. Sie gaben sich laut und deutlich als Polizeibeamte zu erkennen. Weil der Dealer einer Festnahme entgehen wollte, wurde er überwältigt und sollte am Boden liegend gefesselt werden.\n\nEin von der Festnahme nicht betroffener 26-jähriger Dortmunder, der in unmittelbarer Nähe des vorausgegangenen Drogengeschäfts gestanden hatte, hatte den Einsatz der Zivilkräfte beobachtet und wollte die Festnahme vermutlich verhindern. Er soll auf den Polizisten zugelaufen und ihm mit voller Wucht ins Gesicht getreten haben. Am Fuß trug er einen Schuh mit einer Vielzahl spitz zulaufender Nieten. Der schwer verletzte Polizeibeamte verlor kurzzeitig das Bewusstsein.\n\nTrotz heftiger Gegenwehr brachte ein weiterer Polizeibeamter den Angreifer zu Boden. Wiederholt gab sich der Beamte lautstark als Polizist zu erkennen. Laut Zeugen wurde der Mann von einer weiteren Person mehrfach darauf hingewiesen, dass er es mit der Polizei zu tun habe. Schließlich konnte der Beamte dem 26-Jährigen Handschellen angelegen.\n\nDer Tatverdächtige wurde vorläufig festgenommen und ins Polizeigewahrsam eingeliefert. Da keine Haftgründe vorlagen, konnte er das Gewahrsam nach Abschluss der polizeilichen Maßnahmen wieder verlassen. Die Dortmunder Polizei ermittelt wegen eines tätlichen Angriffs auf Vollstreckungsbeamte, Widerstands gegen Vollstreckungsbeamte und gefährlicher Körperverletzung.\n\nDer Polizeibeamte wurde bei dem Angriff mit dem Schuh so schwer verletzt, dass er zur stationären Behandlung in ein Krankenhaus eingeliefert worden ist. Er ist bis auf weiteres dienstunfähig."
                        )
                    ]
                )
            ]
        )
    )
    @PostMapping("/api/prompt/classify")
    fun classify(
        @RequestBody statement: String) =
        prompt.classifyStatement(statement)

    @PostMapping("/api/prompt/location/primary")
    fun extractPrimaryLocation(
        @RequestBody message: String) =
            prompt.genericPrompt("pmpt_68ed348c3694819680edbf87fa1be4da0de4233a0ac3aee2", message)

    @PostMapping("/api/prompt/location/extract")
    fun extractIncidentLocation(
        @RequestBody message: String) =
        prompt.extractIncidentLocations(message)

    @PostMapping("/api/prompt/profiles/extract")
    fun extractOffenderProfiles(
        @RequestBody message: String) =
        prompt.extractOffenderProfiles(message)




    @PostMapping("/api/prompt/{promptID}")
    fun prompt(
        @PathVariable promptID: String,
        @RequestBody message: String) =
        prompt.genericPrompt(promptID, message)

    @PostMapping("/api/prompt")
    fun promptPlain(@RequestBody message: String) =
        prompt.prompt(message)

    @PostMapping("/api/prompt/image")
    fun promptImage(@RequestBody message: String) =
        prompt.imageAsDataURL(message)
}