package com.sales.features.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.installJwtSecurity() {
    val cfg = environment.config

    // 1) Lê de application.conf, depois ENV, depois default DEV
    val realm = cfg.propertyOrNull("jwt.realm")?.getString()
        ?: System.getenv("JWT_REALM")
        ?: "app-servicos"

    val issuer = cfg.propertyOrNull("jwt.issuer")?.getString()
        ?: System.getenv("JWT_ISSUER")
        ?: "com.sales"

    val audience = cfg.propertyOrNull("jwt.audience")?.getString()
        ?: System.getenv("JWT_AUDIENCE")
        ?: "com.sales.api"

    // aceita tanto JWT_ACCESS_SECRET quanto JWT_SECRET
    val secret = cfg.propertyOrNull("jwt.secret")?.getString()
        ?: System.getenv("JWT_ACCESS_SECRET")
        ?: System.getenv("JWT_SECRET")
        ?: "dev-secret-change-me"

    val algorithm = Algorithm.HMAC256(secret)

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm

            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )

            validate { cred ->
                // exige subject (usuário) no token
                if (cred.payload.subject.isNullOrBlank()) null else JWTPrincipal(cred.payload)
            }

            // Resposta 401 padrão quando o token falha/ausente
            challenge { _, _ ->
                call.respond(UnauthorizedResponse())
            }
        }
    }
}
