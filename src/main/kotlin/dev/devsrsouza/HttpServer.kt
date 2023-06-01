package dev.devsrsouza

import dev.devsrsouza.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.resources.*

val port = 7854
val host = "127.0.0.1"
val serviceUrl = "http://$host:$port"

var serverReference: ApplicationEngine? = null

fun initHttpServer() {
    serverReference = embeddedServer(
        Netty,
        port = port,
        host = host,
        module = Application::module
    )

    serverReference!!.start(wait = true)
}

fun Application.module() {
    configureRouting()
}