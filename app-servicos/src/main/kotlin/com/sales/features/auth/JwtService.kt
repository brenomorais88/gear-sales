package com.sales.features.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.*

class JwtService(
    private val issuer: String,
    private val audience: String,
    private val accessSecret: String,
    private val accessTtlSeconds: Long
) {
    fun newAccessToken(userId: UUID, documento: String, nome: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("doc", documento)
            .withClaim("nome", nome)
            .withExpiresAt(Date.from(Instant.now().plusSeconds(accessTtlSeconds)))
            .sign(Algorithm.HMAC256(accessSecret))
}
