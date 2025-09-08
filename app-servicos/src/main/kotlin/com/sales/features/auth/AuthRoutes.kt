package com.sales.features.auth

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.time.LocalDate
import java.util.UUID

fun Route.authRoutes() {
    val auth: AuthService by inject()

    route("/auth") {
        post("/register") {
            @Serializable
            data class Req(
                val nome: String,
                val sobrenome: String,
                val documento: String,
                val dataNascimento: String, // ISO: "1999-12-31"
                val senha: String
            )
            val r = call.receive<Req>()
            val res = auth.register(
                r.nome.trim(),
                r.sobrenome.trim(),
                r.documento.filter { it.isDigit() || it.isLetter() || it == '.' || it == '-' || it == '/' }, // simples
                LocalDate.parse(r.dataNascimento),
                r.senha
            )
            call.respond(HttpStatusCode.Created, res)
        }

        post("/login") {
            @Serializable data class Req(val documento: String, val senha: String)
            val r = call.receive<Req>()
            val res = auth.login(r.documento, r.senha)
            call.respond(res)
        }
    }

    authenticate("auth-jwt") {
        get("/me") {
            val p = call.principal<JWTPrincipal>()!!
            val userId = UUID.fromString(p.subject!!)
            val nome = p.payload.getClaim("nome").asString()
            val documento = p.payload.getClaim("doc").asString()
            call.respond(mapOf("id" to userId.toString(), "nome" to nome, "documento" to documento))
        }
    }
}
