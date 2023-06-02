package dev.srsouza

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.srsouza.collector.ProcessEntriesTable
import dev.srsouza.collector.ProcessInfoTable
import dev.srsouza.collector.TelemetryInstantsTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

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

        transaction(database) {
            SchemaUtils.create(
                ProcessEntriesTable,
                TelemetryInstantsTable,
                ProcessInfoTable,
            )
        }
    }
}

