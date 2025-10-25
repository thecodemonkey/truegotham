package il.tutorials.truegotham.model

data class Insights(
    val typesOfCrime: StatData,
    val totalCrimes: MultiLineData,
    val districts: List<LabelDataPair> = listOf()
)

data class StatData(
    val data: List<Number>,
    val labels: List<String>? = null,
    val label: String? = null
)

data class MultiLineData(
    val labels: List<String>,
    val data: List<StatData>
) {
    fun withoutCurrent() =
        MultiLineData(
            labels.dropLast(1).drop(0),
            data.map {
                StatData(it.data.dropLast(1).drop(0), it.labels, it.label)
            }.filter {
                it.label.equals("Unfall", true).not() &&
                it.label.equals("Sonstiges", true).not()
            }
        )
}

data class LabelDataPair(
    val label: String,
    val data: Number
)
