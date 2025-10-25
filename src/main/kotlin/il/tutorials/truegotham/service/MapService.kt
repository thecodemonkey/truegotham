package il.tutorials.truegotham.service

import il.tutorials.truegotham.utils.FileUtils
import il.tutorials.truegotham.utils.HttpUtils
import org.springframework.stereotype.Service

@Service
class MapService {
    var allImages: List<ByteArray>? = null;

    //@PostConstruct
    fun init() {
        //var tmp = System.getProperty("java.io.tmpdir")
        //allImages = FileUtils.readAllFilesInDir("${tmp}tiles")
    }

    fun loadTile(z: Int, x: Int, y: Int): ByteArray? {
        val tmp = System.getProperty("java.io.tmpdir")
        val tileName = "$z/$x/$y.png";
        val tilePath = "${tmp}tiles/${tileName.replace("/", "_")}"

        if (FileUtils.exists(tilePath))  return FileUtils.read(tilePath)

        val img = HttpUtils.fetchTile("https://tiles.stadiamaps.com/tiles/stamen_toner/$tileName")

        println("tile loaded, cache it: $tileName")
        img?.let {

            allImages?.forEach {
                val res = FileUtils.imagesAreEqual(img, it)
                if (res) {
                    println("same image: $tileName")
                }
            };


            FileUtils.save(img, tilePath)
        }

        return img
    }



}