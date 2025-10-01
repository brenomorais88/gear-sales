package com.sales.core.di

import com.sales.features.auth.AuthService
import com.sales.features.auth.JwtService
import com.sales.features.user.UserDao
import com.typesafe.config.ConfigFactory
import org.koin.dsl.module

/**
 * Lê do application.conf (HOCON) -> ENV -> default.
 */
private fun getConfEnvOrDefault(
    confPath: String,
    envKey: String,
    defaultValue: String
): String {
    val conf = ConfigFactory.load()
    val fromConf = if (conf.hasPath(confPath)) conf.getString(confPath) else null
    val fromEnv = System.getenv(envKey)
    return fromConf ?: fromEnv ?: defaultValue
}

/**
 * Resolve o SECRET aceitando:
 *  - application.conf: jwt.secret
 *  - ENV: JWT_ACCESS_SECRET (prioridade) ou JWT_SECRET
 *  - default DEV
 */
private fun resolveAccessSecret(): String {
    val conf = ConfigFactory.load()
    val fromConf = if (conf.hasPath("jwt.secret")) conf.getString("jwt.secret") else null
    val fromEnvAccess = System.getenv("JWT_ACCESS_SECRET")
    val fromEnv = System.getenv("JWT_SECRET")
    return fromConf ?: fromEnvAccess ?: fromEnv ?: "dev-secret-change-me"
}

/**
 * Lê TTL em segundos de:
 *  - application.conf: jwt.accessTtlSeconds
 *  - ENV: JWT_ACCESS_TTL_SECONDS
 *  - default: 900s (15 min)
 */
private fun resolveAccessTtlSeconds(): Long {
    val conf = ConfigFactory.load()
    val fromConf = if (conf.hasPath("jwt.accessTtlSeconds")) conf.getLong("jwt.accessTtlSeconds") else null
    val fromEnv = System.getenv("JWT_ACCESS_TTL_SECONDS")?.toLongOrNull()
    return fromConf ?: fromEnv ?: 900L
}

val appModule = module {
    // DAO de usuários
    single { UserDao() }

    // JWT Service resiliente
    single {
        val issuer = getConfEnvOrDefault(
            confPath = "jwt.issuer",
            envKey = "JWT_ISSUER",
            defaultValue = "com.sales"
        )
        val audience = getConfEnvOrDefault(
            confPath = "jwt.audience",
            envKey = "JWT_AUDIENCE",
            defaultValue = "com.sales.api"
        )
        val accessSecret = resolveAccessSecret()
        val accessTtlSeconds = resolveAccessTtlSeconds()

        JwtService(
            issuer = issuer,
            audience = audience,
            accessSecret = accessSecret,
            accessTtlSeconds = accessTtlSeconds
        )
    }

    // AuthService depende de JwtService e UserDao
    single { AuthService(get(), get()) }
}
