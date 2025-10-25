package il.tutorials.truegotham.service

import il.tutorials.truegotham.model.CrimeCase
import il.tutorials.truegotham.utils.JsonUtils
import org.springframework.stereotype.Service
import java.util.*

@Service
class CaseService{
    fun loadCrimeCase(id: UUID) =
        JsonUtils.fromJSONResource<CrimeCase>("data/$id/crime-case.json")
}
