package dev.srsouza

import com.github.ajalt.clikt.core.subcommands
import dev.srsouza.collector.initCollector

fun startBackgroundClient() {
    initCollector()
    initHttpServer()
}

fun main(args: Array<String>) = MainCommand().subcommands(
    StartCommand(),
    StopCommand(),
    CsvCommand(),
    StatusCommand(),
    HttpClientCommand(),
).main(args)
