package com.sales.features.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val documento: String,
    val senha: String
)