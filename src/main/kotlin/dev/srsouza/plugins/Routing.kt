package dev.srsouza.plugins

import dev.srsouza.functions.status
import io.ktor.server.routing.*
import io.ktor.server.resources.Resources
import io.ktor.server.application.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        status()
    }
}
