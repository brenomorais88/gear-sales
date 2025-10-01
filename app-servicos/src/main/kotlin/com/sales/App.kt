package com.sales

import com.sales.config.DatabaseFactory
import com.sales.core.di.appModule
import com.sales.features.auth.authRoutes
import com.sales.features.auth.installJwtSecurity
import com.sales.features.categorias.categoriaRoutes
import com.sales.features.loja.lojaModule
import com.sales.features.loja.lojaRoutes

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.UUID
import com.sales.serializer.UUIDSerializer   // se o seu serializer estiver em outro pacote, ajuste aqui

import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.getKoin
import io.ktor.server.application.ApplicationStarted

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    // 1) DB/Migrations com LOG explícito (pra mostrar a causa real de queda)
    try {
        log.info("DB init: iniciando DatabaseFactory.init()…")
        DatabaseFactory.init()
        log.info("DB init: OK")
    } catch (t: Throwable) {
        log.error("DB init: FALHOU dentro de DatabaseFactory.init()", t)
        throw t
    }

    // 2) Plugins básicos
    install(CallLogging)

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                serializersModule = SerializersModule {
                    contextual(UUID::class, UUIDSerializer)
                }
            }
        )
    }

    // 3) Koin + módulos
    install(Koin) {
        modules(
            appModule,
            lojaModule
        )
    }

    // 4) Segurança (JWT) antes das rotas que usam authenticate{}
    installJwtSecurity()

    // 5) Opcional: verificação de DI em DEV (não derruba em prod).
    if ((System.getenv("DEV_CHECK_DI") ?: "true").toBoolean()) {
        environment.monitor.subscribe(ApplicationStarted) {
            try {
                val lojaService = getKoin().get<com.sales.features.loja.LojaService>()
                log.info("Check DI: LojaService resolvido com sucesso: $lojaService")
            } catch (t: Throwable) {
                log.error("Check DI: falha ao resolver LojaService na inicialização", t)
                // Não relançamos aqui para não mascarar problemas do DB;
                // se quiser derrubar a app em dev, descomente:
                // throw t
            }
        }
    }

    // 6) Tratamento de erros
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled", cause)
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: cause::class.qualifiedName ?: "unknown"))
            )
        }
    }

    // 7) Rotas
    routing {
        get("/") { call.respondText("API ok") }
        authRoutes()
        categoriaRoutes()
        lojaRoutes()
    }
}
