package com.sales.features.loja

import com.sales.features.util.DocumentoBR
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.lowerCase

import java.util.UUID

class LojaRepository {

    fun create(req: LojaCreateRequest): LojaResponse {
        val docNorm = DocumentoBR.normalize(req.documento)
        require(DocumentoBR.isValidCPFOrCNPJ(docNorm)) { "Documento inválido (CPF/CNPJ)." }

        val id = UUID.randomUUID()
        transaction {
            val exists = Lojas
                .selectAll()
                .where { (Lojas.documento eq docNorm) and Lojas.deletedAt.isNull() }
                .any()
            require(!exists) { "Já existe loja ativa com este documento." }

            Lojas.insert {
                it[Lojas.id] = id
                it[nome] = req.nome.trim()
                it[documento] = docNorm
                it[endereco] = req.endereco.trim()
                it[telefone] = req.telefone.trim()
                it[email] = req.email?.trim()
                it[horarioFuncionamentoText] = req.horarioFuncionamento?.let { map -> Json.encodeToString(map) }
            }
        }
        return findById(id) ?: error("Falha ao criar loja.")
    }

    fun findById(id: UUID): LojaResponse? = transaction {
        Lojas
            .selectAll()
            .where { (Lojas.id eq id) and Lojas.deletedAt.isNull() }
            .limit(1)
            .firstOrNull()
            ?.toLojaResponse()
    }

    fun list(offset: Long, limit: Int, q: String?): List<LojaResponse> = transaction {
        val base = Lojas
            .selectAll()
            .where { Lojas.deletedAt.isNull() }

        val filtered = if (!q.isNullOrBlank()) {
            val qNorm = DocumentoBR.normalize(q)      // para documento só dígitos
            val qLower = q.lowercase()                // para nome com case-insensitive

            base.andWhere {
                // nome ILIKE '%q%'  ⇢  lower(nome) LIKE '%qLower%'
                (Lojas.nome.lowerCase() like "%$qLower%") or
                        (Lojas.documento like "%$qNorm%")
            }
        } else base

        filtered
            .orderBy(Lojas.createdAt to SortOrder.DESC)
            .limit(limit, offset)
            .map { it.toLojaResponse() }
    }

    fun update(id: UUID, req: LojaUpdateRequest): LojaResponse? = transaction {
        val exists = Lojas
            .selectAll()
            .where { (Lojas.id eq id) and Lojas.deletedAt.isNull() }
            .any()
        if (!exists) return@transaction null

        Lojas.update({ Lojas.id eq id }) {
            req.nome?.let { v -> it[nome] = v.trim() }
            req.endereco?.let { v -> it[endereco] = v.trim() }
            req.telefone?.let { v -> it[telefone] = v.trim() }
            req.email?.let { v -> it[email] = v.trim() }
            req.horarioFuncionamento?.let { map -> it[horarioFuncionamentoText] = Json.encodeToString(map) }
        }

        Lojas
            .selectAll()
            .where { Lojas.id eq id }
            .first()
            .toLojaResponse()
    }

    fun softDelete(id: UUID): Boolean = transaction {
        Lojas.update({ (Lojas.id eq id) and Lojas.deletedAt.isNull() }) {
            it[deletedAt] = java.time.Instant.now()
        } > 0
    }

    fun atribuirUsuario(lojaId: UUID, usuarioId: UUID, role: LojaRole) {
        transaction {
            val lojaExiste = Lojas
                .selectAll()
                .where { (Lojas.id eq lojaId) and Lojas.deletedAt.isNull() }
                .any()
            require(lojaExiste) { "Loja não encontrada." }

            val vinculoExiste = LojaUsuarios
                .selectAll()
                .where { (LojaUsuarios.lojaId eq lojaId) and (LojaUsuarios.usuarioId eq usuarioId) }
                .any()

            if (vinculoExiste) {
                LojaUsuarios.update({ (LojaUsuarios.lojaId eq lojaId) and (LojaUsuarios.usuarioId eq usuarioId) }) {
                    it[LojaUsuarios.role] = role
                }
            } else {
                LojaUsuarios.insert {
                    it[LojaUsuarios.id] = UUID.randomUUID()
                    it[LojaUsuarios.lojaId] = lojaId
                    it[LojaUsuarios.usuarioId] = usuarioId
                    it[LojaUsuarios.role] = role
                }
            }
        }
    }

    fun removerUsuario(lojaId: UUID, usuarioId: UUID): Boolean = transaction {
        LojaUsuarios.deleteWhere { (LojaUsuarios.lojaId eq lojaId) and (LojaUsuarios.usuarioId eq usuarioId) } > 0
    }

    fun listarUsuarios(lojaId: UUID): List<LojaUsuarioResponse> = transaction {
        LojaUsuarios
            .selectAll()
            .where { LojaUsuarios.lojaId eq lojaId }
            .map { LojaUsuarioResponse(it[LojaUsuarios.usuarioId], it[LojaUsuarios.role]) }
    }

    fun listarLojasDoUsuario(usuarioId: UUID): List<LojaResponse> = transaction {
        (Lojas innerJoin LojaUsuarios)
            .selectAll()
            .where { (LojaUsuarios.usuarioId eq usuarioId) and Lojas.deletedAt.isNull() }
            .map { it.toLojaResponse() }
    }

    private fun ResultRow.toLojaResponse(): LojaResponse {
        val horarioStr = this[Lojas.horarioFuncionamentoText]
        val horarioMap = try {
            horarioStr?.let { Json.decodeFromString<Map<String, String>>(it) }
        } catch (_: Exception) {
            null
        }

        return LojaResponse(
            id = this[Lojas.id],
            nome = this[Lojas.nome],
            documento = this[Lojas.documento],
            endereco = this[Lojas.endereco],
            telefone = this[Lojas.telefone],
            email = this[Lojas.email],
            horarioFuncionamento = horarioMap
        )
    }
}
