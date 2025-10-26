package il.tutorials.truegotham.utils

import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValueContent(val value: String)


@Component
class ValueContentProcessor(
    private val resourceLoader: ResourceLoader
) : BeanPostProcessor {

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
        bean::class.java.declaredFields.forEach { field ->
            field.getAnnotation(ValueContent::class.java)?.let { annotation ->
                val resource = resourceLoader.getResource(annotation.value)
                val content = resource.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                field.isAccessible = true
                field.set(bean, content)
            }
        }
        return bean
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String) = bean
}