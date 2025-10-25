package il.tutorials.truegotham.model

data class PressRelease(
    val timestamp: String,
    val title: String,
    val content: String,
    val lfd_nr: String,
    val url: String,
    val topics: List<String>
)
