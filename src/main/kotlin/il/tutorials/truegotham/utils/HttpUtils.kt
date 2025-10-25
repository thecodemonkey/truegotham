package il.tutorials.truegotham.utils

import okhttp3.OkHttpClient
import okhttp3.Request

object HttpUtils {

    fun get(url: String): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Unexpected code $response")
            return response.body?.string() ?: ""
        }
    }

    fun getBinary(url: String): ByteArray? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Fehler beim Laden: ${response.code}")
                return null
            }
            return response.body?.bytes()
        }
    }


    fun fetchTile(url: String): ByteArray? {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .addHeader("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Pragma", "no-cache")
            .addHeader("Referer", "http://localhost:8080/")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Fehler beim Laden: ${response.code}")
                return null
            }
            return response.body?.bytes()
        }
    }
}