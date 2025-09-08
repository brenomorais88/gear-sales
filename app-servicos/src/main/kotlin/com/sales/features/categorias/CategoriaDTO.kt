package com.sales.features.categorias
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaEstabelecimentoDTO(
    val id: Int? = null,
    val nome: String
)