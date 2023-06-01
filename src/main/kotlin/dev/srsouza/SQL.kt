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

object SQL {
    val dataSource by lazy {
        if(dataFolderPath.exists().not()) {
            dataFolderPath.mkdirs()
        }
        val cacheFolder = File(dataFolderPath, "cache")
        if(cacheFolder.exists()) cacheFolder.deleteRecursively()
        cacheFolder.mkdirs()

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
        transaction(database) {
            SchemaUtils.create(
                ProcessEntriesTable,
                TelemetryInstantsTable,
                ProcessInfoTable,
            )
        }
    }
}

