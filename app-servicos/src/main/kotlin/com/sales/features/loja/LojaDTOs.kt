package com.sales.features.loja

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlinx.serialization.Contextual

@Serializable
data class LojaCreateRequest(
    val nome: String,
    val documento: String,
    val endereco: String,
    val telefone: String,
    val email: String? = null,
    val horarioFuncionamento: Map<String,String>? = null
)

@Serializable
data class LojaUpdateRequest(
    val nome: String? = null,
    val endereco: String? = null,
    val telefone: String? = null,
    val email: String? = null,
    val horarioFuncionamento: Map<String,String>? = null
)

@Serializable
data class LojaResponse(
    @Contextual val id: UUID,
    val nome: String,
    val documento: String,
    val endereco: String,
    val telefone: String,
    val email: String? = null,
    val horarioFuncionamento: Map<String,String>? = null
)

@Serializable
data class AtribuirUsuarioRequest(
    @Contextual val usuarioId: UUID,
    val role: LojaRole
)

@Serializable
data class LojaUsuarioResponse(
    @Contextual val usuarioId: UUID,
    val role: LojaRole
)
