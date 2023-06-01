package dev.srsouza.functions

import dev.srsouza.httpClient
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import java.io.File

fun callStart() = runBlocking {
    val result = withTimeoutOrNull(2000L) {
        println("Checking if the service is ready running, wait 2sec")
        runCatching { httpClient.get(StatusResource()).bodyAsText() }.getOrNull()
    }

    if(result != null) {
        println("Service is already running, skipping start")
    } else {
        println("Starting client in background")
        // TODO: start application in background by running shell, maybe running it self? don't know
        // java -jar JAR-PATH.jar initClient
        initInBackground()
    }

    httpClient.close()
}

private fun initInBackground() {
    val currentJar: File = File(StatusResource::class.java.protectionDomain.codeSource.location.toURI())

    // TODO: make smaller Java max footprint?
    val command = listOf(
        "java",
        "-Xmx50M",
        "-jar",
        currentJar.path,
        "initClient",
    )

    println("Start in background: ${command.joinToString(" ")}")

    val builder = ProcessBuilder(command)
    builder.start()
    System.exit(0)
}