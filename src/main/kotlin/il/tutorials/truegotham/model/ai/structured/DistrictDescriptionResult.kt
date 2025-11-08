package il.tutorials.truegotham.model.ai.structured

import il.tutorials.truegotham.model.ai.PromptInfo

@PromptInfo(id = "pmpt_68ff6cf352848190930958ed6a3da92209fef16161fe913d")
data class DistrictDescriptionResult(
    var location: String,
    var atmosphere: String,
    var population: String,
    var migration_background: String,
    var social_structure: String,
    var sense_of_security: String,
    var special_features_of_the_district: String
) {
    //override to string
    override fun toString(): String {
        return """
            Location: $location
            Atmosphere: $atmosphere
            Population: $population
            Migration Background: $migration_background
            Social Structure: $social_structure
            Sense of Security: $sense_of_security
            Special Features of the District: $special_features_of_the_district"""
            .trimMargin()
    }
}
