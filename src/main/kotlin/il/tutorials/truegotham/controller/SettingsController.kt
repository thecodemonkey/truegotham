package il.tutorials.truegotham.controller

import il.tutorials.truegotham.config.AppConfig
import il.tutorials.truegotham.model.dto.Settings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController



@RestController
class SettingsController(
    val settings: AppConfig) {

    @GetMapping("/api/settings")
    fun settings() = Settings(
            mapTilesCaching = settings.map.caching
        )

}