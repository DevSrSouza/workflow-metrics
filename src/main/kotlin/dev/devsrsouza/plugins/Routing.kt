package dev.devsrsouza.plugins

import dev.devsrsouza.functions.status
import dev.devsrsouza.functions.stop
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.resources.*
import io.ktor.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.application.*

fun Application.configureRouting() {
    install(Resources)
    routing {
        stop()
        status()
    }
}
