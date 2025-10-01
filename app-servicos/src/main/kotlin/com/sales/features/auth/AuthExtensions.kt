package com.sales.features.auth

import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.auth.*
import java.util.UUID

fun ApplicationCall.authCtx(): AuthContext {
    val principal = this.principal<JWTPrincipal>() ?: error("Unauthorized")

    val uid = principal.payload.getClaim("uid").asString()
        ?: principal.payload.subject
        ?: error("Token sem uid.")

    val userId = runCatching { UUID.fromString(uid) }.getOrElse {
        error("uid inválido no token.")
    }

    val isSysAdmin = principal.payload.getClaim("sysadmin").asBoolean() ?: false
    return AuthContext(usuarioId = userId, systemAdmin = isSysAdmin)
}
