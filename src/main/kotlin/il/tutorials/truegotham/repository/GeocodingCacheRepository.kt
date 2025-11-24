package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.GeocodingCacheItem
import org.springframework.data.jpa.repository.JpaRepository


interface GeocodingCacheRepository : JpaRepository<GeocodingCacheItem, String> {

}