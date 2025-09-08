package com.sales.features.auth

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT

fun Application.installJwtSecurity() {
    val issuer = System.getenv("JWT_ISSUER")
    val audience = System.getenv("JWT_AUDIENCE")
    val realm = System.getenv("JWT_REALM")
    val secret = System.getenv("JWT_ACCESS_SECRET")

    install(Authentication) {
        jwt("auth-jwt") {
            this.realm = realm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build()
            )
            validate { cred ->
                if (cred.payload.subject.isNullOrBlank()) null else JWTPrincipal(cred.payload)
            }
        }
    }
}
