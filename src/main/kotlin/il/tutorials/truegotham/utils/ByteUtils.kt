package il.tutorials.truegotham.utils

import java.util.*

object ByteUtils {

    fun toDataUrl(byteArray: ByteArray?, mimeType: String): String? {
        if (byteArray == null) return null

        val base64 = Base64.getEncoder().encodeToString(byteArray)
        return "data:$mimeType;base64,$base64"
    }
}