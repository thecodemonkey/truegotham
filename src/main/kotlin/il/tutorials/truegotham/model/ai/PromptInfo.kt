package il.tutorials.truegotham.model.ai

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PromptInfo(val id: String, val version: String = "")


