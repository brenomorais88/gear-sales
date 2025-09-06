package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(
        url: String = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/appservicos",
        user: String = System.getenv("DB_USER") ?: "app",
        pass: String = System.getenv("DB_PASS") ?: "app"
    ) {
        val cfg = HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 10
        }
        val ds = HikariDataSource(cfg)

        Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        Database.connect(ds)
    }
}
