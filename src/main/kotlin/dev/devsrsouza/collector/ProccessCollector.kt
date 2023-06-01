package dev.devsrsouza.collector

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun startProcessCollector() {
    GlobalScope.launch {
        // TODO: start collecting top process and adding to a file, maybe SQL, maybe influx, maybe generic way?
        // TODO: use Kotlinx.datetime to serialize dateTime
    }
}