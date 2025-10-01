package com.sales.features.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nome: String,
    val sobrenome: String,
    val documento: String,
    val email: String,
    val dataNascimento: String,
    val senha: String
)


