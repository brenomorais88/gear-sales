package com.sales.features.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val nome: String,
    val sobrenome: String,
    val documento: String,
    val dataNascimento: String
)