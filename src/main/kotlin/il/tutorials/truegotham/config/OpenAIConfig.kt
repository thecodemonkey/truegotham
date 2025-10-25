package il.tutorials.truegotham.config

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig {

    @Value("\${ai.api-key}")
    private lateinit var apiKey: String

    @Bean
    fun openAIClient(): OpenAIClient =
        OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build()

}