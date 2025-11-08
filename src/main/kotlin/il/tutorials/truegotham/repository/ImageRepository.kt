package il.tutorials.truegotham.repository

import il.tutorials.truegotham.model.entity.District
import il.tutorials.truegotham.model.entity.Image
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ImageRepository : JpaRepository<Image, UUID> {
}