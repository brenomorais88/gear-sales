package com.sales.features.categorias

import org.jetbrains.exposed.dao.id.IntIdTable

object CategoriaEstabelecimento : IntIdTable("categoriasEstabelecimento") {
    val nome = text("nome").uniqueIndex()
}