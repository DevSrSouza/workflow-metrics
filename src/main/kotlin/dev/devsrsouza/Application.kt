package dev.devsrsouza

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import dev.devsrsouza.collector.initCollector
import dev.devsrsouza.functions.callStatus
import dev.devsrsouza.functions.callStop
import dev.devsrsouza.functions.callStart
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dev.devsrsouza.plugins.*
import io.ktor.server.resources.*

fun startBackgroundClient() {
    initCollector()
    initHttpServer()
}

class MainCommand : CliktCommand() {
    override fun run() = Unit
}

class StartCommand : CliktCommand(name = "start") {
    // TODO: should have a option to specify how much top will be selected?

    override fun run() {
        callStart()
    }

}

class StopCommand : CliktCommand(name = "stop") {
    override fun run() {
        callStop()
    }
}

class CsvCommand : CliktCommand(name = "csv") {
    override fun run() {
        TODO("Should get the file and parse to Csv")
    }
}

class StatusCommand : CliktCommand(name = "status") {
    override fun run() {
        callStatus()
    }
}

class HttpClientCommand : CliktCommand(
    name = "initClient",
    help = "Run blocking the client and collector, this should not be used directly"
) {
    override fun run() {
        println("Initializing background client")
        startBackgroundClient()
    }
}

fun main(args: Array<String>) = MainCommand().subcommands(
    StartCommand(),
    StopCommand(),
    CsvCommand(),
    StatusCommand(),
    HttpClientCommand(),
).main(args)
