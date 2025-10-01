package com.sales.features.auth

import java.util.UUID

data class AuthContext(
    val usuarioId: UUID,
    val systemAdmin: Boolean
)