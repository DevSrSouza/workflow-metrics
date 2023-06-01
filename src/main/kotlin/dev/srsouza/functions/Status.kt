package dev.srsouza.functions

import dev.srsouza.httpClient
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

fun Routing.status() {
    get<StatusResource> {
        call.respondText("Hello World!") // TODO json response
    }
}

@Resource("/status")
class StatusResource

fun callStatus() = runBlocking {
    val response = httpClient.get(StatusResource()).bodyAsText()

    println(response)

    httpClient.close()
}