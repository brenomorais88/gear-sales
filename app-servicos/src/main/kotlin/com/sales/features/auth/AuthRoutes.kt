package com.sales.features.auth

import com.sales.features.auth.model.LoginRequest
import com.sales.features.auth.model.RegisterRequest
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import org.koin.core.context.GlobalContext
import java.time.LocalDate
import java.util.UUID

fun Route.authRoutes() {
    val auth: AuthService = GlobalContext.get().get()   // resolve do Koin

    route("/auth") {
        post("/register") {
            val r = call.receive<RegisterRequest>()
            val res = auth.register(
                r.nome.trim(),
                r.sobrenome.trim(),
                r.documento,
                LocalDate.parse(r.dataNascimento),
                r.senha
            )
            call.respond(HttpStatusCode.Created, res)
        }

        post("/login") {
            val r = call.receive<LoginRequest>()
            call.respond(auth.login(r.documento, r.senha))
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
