package dev.srsouza

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*

val httpClient = HttpClient(OkHttp) {
    defaultRequest {
        url(urlString = serviceUrl)
    }

    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        logger = Logger.ANDROID
        level = LogLevel.NONE
    }
    install(Resources)
}