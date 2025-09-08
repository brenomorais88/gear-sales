package com.sales.core.di

import org.koin.dsl.module
import com.sales.features.user.UserDao
import com.sales.features.auth.JwtService
import com.sales.features.auth.AuthService

val appModule = module {
    single { UserDao() }

    single {
        val env = System.getenv()
        fun req(name: String) = env[name] ?: error("$name not set")
        val ttl = env["JWT_ACCESS_TTL_SECONDS"]?.toLong() ?: 900L

        JwtService(
            issuer = req("JWT_ISSUER"),
            audience = req("JWT_AUDIENCE"),
            accessSecret = req("JWT_ACCESS_SECRET"),
            accessTtlSeconds = ttl
        )
    }

    single { AuthService(get(), get()) }
}

