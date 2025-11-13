package il.tutorials.truegotham.import.steps

import il.tutorials.truegotham.import.ImportContext
import il.tutorials.truegotham.model.ImportStatus
import il.tutorials.truegotham.model.ai.structured.IncidentLocationResult
import il.tutorials.truegotham.model.entity.incident.IncidentLocation
import il.tutorials.truegotham.repository.IncidentRepository
import il.tutorials.truegotham.service.AIPromptService
import il.tutorials.truegotham.utils.DateUtils
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocationExtractionImportStep(
    val ai: AIPromptService,
    val incidentRepo: IncidentRepository
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

                context.incident.locations.add(
                    IncidentLocation(
                        id = UUID.randomUUID(),
                        unixts = current!!,
                        address = l.address,
                        addressFormal = combineFormalAddress(l),

                        city = l.city,
                        street = l.street,
                        street2 = l.street2,
                        place = l.place,

                        main = l.main,
                        title = l.title,
                        description = l.description,
                        incident = context.incident
                    )
                )
            }
        }

        context.incident.locations.firstOrNull()?.let {
            context.statement.city = it.city
        }

        context.statement.district = "City" //append default district


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