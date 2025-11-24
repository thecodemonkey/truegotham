package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.ai.structured.IncidentLocationResult
import il.tutorials.truegotham.model.entity.incident.IncidentLocation
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import il.tutorials.truegotham.service.AddressNormalizer
import il.tutorials.truegotham.service.GeocodingService
import il.tutorials.truegotham.utils.DateUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocationExtractionImportStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository,
    val geocodingService: GeocodingService,
    val addressNormalizer: AddressNormalizer
) : ImportStepBase() {
    override fun status() = ImportStatus.LOCATION_EXTRACTION

    override fun run(context: ImportContext) {
        val locationsResult = ai.extractIncidentLocations(context.rawStatement.content!!)

        var prevDT:Long? = null;

        locationsResult.locations.forEach {
                l ->
            run {
                var current = DateUtils.toUnixtsTime(l.date)
                if (current == null) current = if (prevDT == null) 0 else prevDT ;
                if (current != null) prevDT = current;

                val addressNormalized = addressNormalizer.combineAddress(
                    street = l.street, //todo: append street2/intersections
                    district = null, //todo: append district
                    city = l.city,
                    houseNumber = null, //todo: append houseno
                    place = l.place,
                    normalize = true
                )



                // val address = combineFormalAddress(l)
                val geocoded = geocodingService.geocodeLocationResult(l)

                context.incident.locations.add(
                    IncidentLocation(
                        id = UUID.randomUUID(),
                        unixts = current!!,
                        address = l.address,
                        addressFormal = geocoded.address,

                        coordinates = geocoded.coordinates,

                        city = l.city,
                        street = l.street,
                        street2 = l.street2,
                        district = l.district,
                        place = l.place,

                        main = l.main,
                        title = l.title,
                        description = l.description,
                        incident = context.incident
                    )
                )
            }
        }

        context.statement.district = "City" //append default district

        context.incident.locations.firstOrNull()?.let { location ->
            context.statement.city = location.city

            location.coordinates?.let { coords ->
                geocodingService.findDistrictByCoords(coords)?.let {
                    context.statement.district = it
                }
            }
        }


        if (context.statement.city?.uppercase()?.trimIndent() != "DORTMUND") {
            println("statement does not belong to dortmund: ${context.statement.city}");
            context.rawStatement.status = ImportStatus.REMOVE_IRRELEVANT;
        }

        incidentRepo.save(context.incident)
    }

    private fun combineFormalAddress(result: IncidentLocationResult): String {
        var address = "";

        if (result.street.isNotBlank())  address += result.street
        if (result.street2.isNotBlank())  address += "/" + result.street2

        if (result.street.isBlank() && result.street2.isBlank() && result.place.isNotBlank())
            address += result.place

        if (result.city.isNotBlank())  address += ", " + result.city

        return address
    }
}