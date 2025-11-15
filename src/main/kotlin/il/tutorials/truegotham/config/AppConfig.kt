package il.tutorials.truegotham.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
class AppConfig {
    lateinit var map: MapConfig

    class MapConfig {
        var caching: Boolean = false
    }
}