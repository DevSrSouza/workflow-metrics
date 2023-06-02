package dev.srsouza.functions

import dev.srsouza.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.resources.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

@Resource("/stop")
class StopResource

fun callStop() = runBlocking {
    val response = httpClient.use { it.post(StopResource()).status }
    println("Response code: $response")
}
