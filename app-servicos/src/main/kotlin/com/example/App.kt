package com.example

import com.example.config.DatabaseFactory
import com.example.routes.categoriaRoutes
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

fun main() {
    embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(CallLogging)
    install(ContentNegotiation) { json() }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "unknown")))
            throw cause
        }
    }

    routing {
        get("/") { call.respondText("API ok") }
        categoriaRoutes()
    }
}
