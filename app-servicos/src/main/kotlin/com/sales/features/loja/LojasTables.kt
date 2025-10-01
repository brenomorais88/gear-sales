package com.sales.features.loja

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object Lojas : Table("lojas") {
    val id: Column<UUID> = uuid("id")
    val nome = varchar("nome", 160)
    val documento = varchar("documento", 20)          // só dígitos (CPF/CNPJ)
    val endereco = text("endereco")
    val telefone = varchar("telefone", 20)
    val email = varchar("email", 160).nullable()

    // Se você já usa suporte JSONB do Exposed:
    // val horarioFuncionamento = jsonb("horario_funcionamento", Map::class).nullable()
    // Caso contrário, mantenha TEXT e salve JSON em string:
    val horarioFuncionamentoText = text("horario_funcionamento").nullable()

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

enum class LojaRole { ADMIN, VENDEDOR }

object LojaUsuarios : Table("loja_usuarios") {
    val id: Column<UUID> = uuid("id")
    val lojaId = uuid("loja_id")              // FK real definida na migration SQL
    val usuarioId = uuid("usuario_id")        // FK real definida na migration SQL
    val role = enumerationByName("role", 10, LojaRole::class)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(lojaId, usuarioId)        // um vínculo por usuário/loja
    }
}
