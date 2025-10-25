package il.tutorials.truegotham.utils

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import javax.imageio.ImageIO

object FileUtils {
    fun save(bytes: ByteArray, path: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }

    fun exists(path: String) = File(path).exists()
    fun read(path: String) =  File(path).readBytes()

    fun readAllFilesInDir(path: String): List<ByteArray> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.readBytes() }
            ?: emptyList()
    }

    fun readAllTextFilesInDir(path: String) =
        readAllFilesInDir(path).map { bytes ->
            String(bytes, Charsets.UTF_8)
        }


    fun imagesAreEqual(bytes1: ByteArray, bytes2: ByteArray): Boolean {
        val img1: BufferedImage = ImageIO.read(ByteArrayInputStream(bytes1))
            ?: return false
        val img2: BufferedImage = ImageIO.read(ByteArrayInputStream(bytes2))
            ?: return false

        // Prüfen, ob Breite/Höhe gleich sind
        if (img1.width != img2.width || img1.height != img2.height) return false

        // Pixel für Pixel vergleichen
        for (y in 0 until img1.height) {
            for (x in 0 until img1.width) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false
            }
        }

        return true
    }

    fun saveTextFile(text: String, path: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(text, StandardCharsets.UTF_8)
    }

    fun getLastModifiedFile(path: String): File? {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return null

        return dir.listFiles()
            ?.filter { it.isFile }
            ?.maxByOrNull { it.lastModified() }
    }

    fun getLastModifiedFileText(path: String): String?{
        val file = FileUtils.getLastModifiedFile(path)
        return file?.readText(StandardCharsets.UTF_8);
    }

    fun getTmpPath(vararg path:String): String {
        return Paths.get(System.getProperty("java.io.tmpdir"), *path)
                     .toString();
    }
}