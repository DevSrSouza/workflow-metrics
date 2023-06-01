package dev.srsouza

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import dev.srsouza.functions.callStart
import dev.srsouza.functions.callStatus
import dev.srsouza.functions.callStop

class MainCommand : CliktCommand() {
    override fun run() = Unit
}

class StartCommand : CliktCommand(name = "start") {
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