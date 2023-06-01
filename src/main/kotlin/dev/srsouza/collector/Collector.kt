package dev.srsouza.collector

import dev.srsouza.SQL

fun initCollector() {
    SQL.initTables()
    startProcessCollector()
}