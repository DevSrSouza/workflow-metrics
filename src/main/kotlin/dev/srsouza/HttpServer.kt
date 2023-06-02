package dev.srsouza

import dev.srsouza.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

val port = 7854
val host = "127.0.0.1"
val serviceUrl = "http://$host:$port"

var serverReference: ApplicationEngine? = null

fun initHttpServer() {
    println("Initializing http server")
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
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/stop"
        exitCodeSupplier = {
            mainJob.cancel()
            0
        }
    }
}