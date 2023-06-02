package dev.srsouza

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.srsouza.collector.ProcessEntriesTable
import dev.srsouza.collector.ProcessInfoTable
import dev.srsouza.collector.TelemetryInstantsTable
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.util.logging.Logger

val mainJob = SupervisorJob()
val mainScope = CoroutineScope(
    mainJob +
            Dispatchers.IO +
            DefaultUncaughtExceptionHandler { LoggerFactory.getLogger("Exception") })

val dataFolderPath = File(System.getProperty("user.home"), ".workflow-metrics")
val cacheFolder = File(dataFolderPath, "cache")

object SQL {
    fun clearCache() {
        if(dataFolderPath.exists().not()) {
            dataFolderPath.mkdirs()
        }

        if(cacheFolder.exists()) cacheFolder.deleteRecursively()
        cacheFolder.mkdirs()
    }

    val dataSource by lazy {
        val h2File = File(cacheFolder, "sql.db")

        HikariDataSource(
            HikariConfig().apply {
                driverClassName = "org.h2.Driver"
                jdbcUrl = "jdbc:h2:file:${h2File.absolutePath}"
            }
        )
    }

    val database by lazy {
        Database.connect(dataSource)
    }

    fun initTables() {
        clearCache()

        runBlocking {
            newSuspendedTransaction(mainJob, db = database) {
                SchemaUtils.create(
                    ProcessEntriesTable,
                    TelemetryInstantsTable,
                    ProcessInfoTable,
                )
            }
        }
    }
}

