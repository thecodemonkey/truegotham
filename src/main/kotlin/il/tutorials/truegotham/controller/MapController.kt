package il.tutorials.truegotham.controller

import il.tutorials.truegotham.service.MapService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class MapController(val map: MapService) {

    @GetMapping("/api/map/tiles/{z}/{x}/{y}.png")
    fun loadTiles(
        @PathVariable z:Int,
        @PathVariable x:Int,
        @PathVariable y:Int
    ): ResponseEntity<ByteArray> {
        val imageBytes = map.loadTile(z, x, y)

        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
            .contentType(MediaType.IMAGE_PNG)
            .body(imageBytes)
    }
}
