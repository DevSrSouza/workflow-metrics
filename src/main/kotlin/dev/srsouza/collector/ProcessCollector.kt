@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.srsouza.collector

import dev.srsouza.SQL
import dev.srsouza.SQL.database
import dev.srsouza.dataFolderPath
import dev.srsouza.mainScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jutils.jprocesses.JProcesses
import oshi.SystemInfo
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// OSHI limitations: https://github.com/oshi/oshi/issues/893

val collectionDelay = 3.seconds
val processTopCount = 7

fun main() {
    while (true) {
        println("-----------------")
        println(collectTopMemoryProcess().map { it.pid to ((it.memoryInKb / 1000).toString() + "Mb && ${it.cpuUsageInPercent}" ) }.joinToString("\n"))
        Thread.sleep(3000)
    }
}

fun startProcessCollector() {
    mainScope.launch {
        while(true) {
            delay(collectionDelay)
            val currentSystemInfo = collectCurrentSystemInfo()
            insertProcessInfo(currentSystemInfo)
        }
    }
}

fun generateUsageCsv() {
    println("Start usage csv generation.")

    Database.connect(SQL.dataSource)

    val memoryCsv = File(dataFolderPath, "memory.csv")
    if(memoryCsv.exists()) memoryCsv.delete()
    memoryCsv.createNewFile()

    val cpuCsv = File(dataFolderPath, "cpu.csv")
    if(cpuCsv.exists()) cpuCsv.delete()
    cpuCsv.createNewFile()

    val pidTable = File(dataFolderPath, "pid-table.md")
    if(pidTable.exists()) pidTable.delete()
    pidTable.createNewFile()

    transaction {
        val processes = ProcessEntriesTable.selectAll().associate {
            it[ProcessEntriesTable.id].value to (it[ProcessEntriesTable.pid] to it[ProcessEntriesTable.command])
        }.toSortedMap()

        println("Generating pid-table.md")
        pidTable.appendText("| Process ID | Command |\n| --- | --- |\n")
        pidTable.appendText(processes.values.joinToString("\n") { "| ${it.first} | ${it.second} |" })

        val min = TelemetryInstantsTable.selectAll().orderBy(TelemetryInstantsTable.instant to SortOrder.ASC).limit(1)
            .first()[TelemetryInstantsTable.instant].epochSeconds

        val max = TelemetryInstantsTable.selectAll().orderBy(TelemetryInstantsTable.instant to SortOrder.DESC).limit(1)
            .first()[TelemetryInstantsTable.instant].epochSeconds

        println("DURATION SECONDS - ${max - min} - MIN SECONDS: $min - MAX SECONDS: $max")

        memoryCsv.appendText("Instant," + processes.values.map { it.first }.joinToString(",") { "pid $it" } + "\n")
        cpuCsv.appendText("Instant," + processes.values.map { it.first }.joinToString(",") + "\n")
        TelemetryInstantsTable.selectAll().forEach { telemetry ->
            val instant = telemetry[TelemetryInstantsTable.instant]
            val instantInEchoSeconds = instant.epochSeconds - min
            println("Appending moment $instantInEchoSeconds")

            val processInfos = ProcessInfoTable.select { ProcessInfoTable.instant eq telemetry[TelemetryInstantsTable.id] }
            val usageMap = processInfos.associate {
                it[ProcessInfoTable.process].value to (it[ProcessInfoTable.memoryInKb] to it[ProcessInfoTable.cpuUsagePercent])
            }.toSortedMap()

            val processUsageOrNull = processes.keys.associateWith { usageMap[it] }
            val csvMemoryResult = processUsageOrNull.values.map { it?.first }.joinToString(",") { it?.let { kbToMb(it) }?.toString() ?: "" }
            val csvCpuResult = processUsageOrNull.values.map { it?.second }.joinToString(",") { it?.toString() ?: "" }

            memoryCsv.appendText("$instantInEchoSeconds,$csvMemoryResult\n")
            cpuCsv.appendText("$instantInEchoSeconds,$csvCpuResult\n")
        }
    }


    SQL.dataSource.close()
    println("Complete csv generation")
}

private fun kbToMb(kb: Long): Long = kb / 1000

private fun collectCurrentSystemInfo(): CurrentSystemInfo {
    val systemInfo = SystemInfo()
    val hardware = systemInfo.hardware
    val memory = hardware.memory

    return CurrentSystemInfo(
        maxMemoryInBytes = memory.total,
        usageMemoryInBytes = memory.available,
        topRunningProcesses = collectTopMemoryProcess(),
        timeStamp = Clock.System.now(),
    )
}
private fun collectTopMemoryProcess(): List<RunningProcess> {
    // TODO: maybe use TOP instead?

    val systemInfo = SystemInfo()
    val processes = systemInfo.operatingSystem.processes

    // physicalMemory in KB
    val sorted = processes.sortedByDescending {
        it.residentSetSize
    }
    val top10 = sorted.take(processTopCount)

    return top10.map {
        RunningProcess(
            pid = it.processID,
            memoryInKb = it.residentSetSize / 1000,
            command = it.commandLine,
            cpuUsageInPercent = 0.0, // TODO
        )
    }
}

data class RunningProcess(
    val pid: Int,
    val memoryInKb: Long,
    val command: String,
    val cpuUsageInPercent: Double,
)

data class CurrentSystemInfo(
    val maxMemoryInBytes: Long,
    val usageMemoryInBytes: Long,
    val topRunningProcesses: List<RunningProcess>,
    val timeStamp: Instant,
)

// SQL

object ProcessEntriesTable : IntIdTable(
    "process_entries",
) {
    val pid = integer("pid")
    val command = text("command")
}

object TelemetryInstantsTable : IntIdTable("telemetry_instants") {
    val instant = timestamp("instant")
}

object ProcessInfoTable : IntIdTable("process_info") {
    val instant = reference("instant_id", TelemetryInstantsTable)
    val process = reference("process_id", ProcessEntriesTable)

    val memoryInKb = long("memory_in_kb")
    val cpuUsagePercent = double("cpu_usage_percent")
}

private fun insertProcessInfo(systemInfo: CurrentSystemInfo) {
    mainScope.launch {
        newSuspendedTransaction(db = database) {
            val telemetryInstant = TelemetryInstantsTable.insertAndGetId {
                it[instant] = systemInfo.timeStamp
            }

            for(process in systemInfo.topRunningProcesses) {
                val processEntryId = ProcessEntriesTable.select { ProcessEntriesTable.pid eq process.pid }
                    .firstOrNull()
                    ?.get(ProcessEntriesTable.id)
                    ?: ProcessEntriesTable.insertAndGetId {
                        it[pid] = process.pid
                        it[command] = process.command
                    }

                ProcessInfoTable.insert {
                    it[instant] = telemetryInstant
                    it[ProcessInfoTable.process] = processEntryId
                    it[memoryInKb] = process.memoryInKb
                    it[cpuUsagePercent] = process.cpuUsageInPercent
                }
            }
        }
    }
}