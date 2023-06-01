@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.srsouza.collector

import dev.srsouza.SQL.database
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jutils.jprocesses.JProcesses
import oshi.SystemInfo
import kotlin.time.Duration.Companion.seconds

// OSHI limitations: https://github.com/oshi/oshi/issues/893

val collectionDelay = 3.seconds

fun startProcessCollector() {
    GlobalScope.launch {
        while(true) {
            delay(collectionDelay)
            val currentSystemInfo = collectCurrentSystemInfo()
            insertProcessInfo(currentSystemInfo)
        }
    }
}

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
    val processes = JProcesses.getProcessList()

    // physicalMemory in KB
    val sorted = processes.sortedByDescending {
        it.physicalMemory.toLong()
    }
    val top10 = sorted.take(10)

    println(top10.map { it.cpuUsage })

    return top10.map {
        RunningProcess(
            pid = it.pid.toInt(),
            memoryInKb = it.physicalMemory.toLong(),
            command = it.command,
            cpuUsageInPercent = it.cpuUsage.toDouble(),
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
    GlobalScope.launch {
        newSuspendedTransaction(db = database) {
            val telemetryInstant = TelemetryInstantsTable.insert {
                it[instant] = systemInfo.timeStamp
            }.get(TelemetryInstantsTable.id)

            for(process in systemInfo.topRunningProcesses) {
                val processEntryId = ProcessEntriesTable.select { ProcessEntriesTable.pid eq process.pid }
                    .firstOrNull()
                    ?.get(ProcessEntriesTable.id)
                    ?: ProcessEntriesTable.insert {
                        it[pid] = process.pid
                        it[command] = process.command
                    }.get(ProcessEntriesTable.id)

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