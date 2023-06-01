package dev.srsouza.plugins

import dev.srsouza.functions.status
import dev.srsouza.functions.stop
import io.ktor.server.routing.*
import io.ktor.server.resources.Resources
import io.ktor.server.application.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        stop()
        status()
    }
}
