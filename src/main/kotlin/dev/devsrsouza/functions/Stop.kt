package dev.devsrsouza.functions

import dev.devsrsouza.httpClient
import dev.devsrsouza.serverReference
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.post
import io.ktor.client.statement.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun Routing.stop() {
    post<StopResource> {
        stopServer()
        call.respondText("OK")
    }
}

@Resource("/stop")
class StopResource

fun callStop() = runBlocking {
    val response = httpClient.post(StopResource()).bodyAsText()
    println("Response: $response")

    httpClient.close()
}

private fun stopServer() {
    GlobalScope.launch {
        serverReference!!.stop()
    }
}