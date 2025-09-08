package com.sales.features.user

import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class UserRecord(
        val id: UUID,
        val nome: String,
        val sobrenome: String,
        val documento: String,
        val dataNascimento: LocalDate,
        val passwordHash: String
)

class UserDao {
    fun create(
            nome: String,
            sobrenome: String,
            documento: String,
            dataNascimento: LocalDate,
            senha: String
    ): UserRecord = transaction {
        val now = OffsetDateTime.now().toInstant()
        val id = UUID.randomUUID()
        val hash = BCrypt.withDefaults().hashToString(12, senha.toCharArray())

        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.nome] = nome
            it[UsersTable.sobrenome] = sobrenome
            it[UsersTable.documento] = documento
            it[UsersTable.dataNascimento] = dataNascimento
            it[UsersTable.passwordHash] = hash
            it[UsersTable.createdAt] = Instant.from(now)
            it[UsersTable.updatedAt] = Instant.from(now)
        }

        findById(id)!!
    }

    fun findById(id: UUID): UserRecord? = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toRecord()
    }

    fun findByDocumento(doc: String): UserRecord? = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.documento eq doc }
            .limit(1)
            .firstOrNull()
            ?.toRecord()
    }

    private fun ResultRow.toRecord() = UserRecord(
            id = this[UsersTable.id],
            nome = this[UsersTable.nome],
            sobrenome = this[UsersTable.sobrenome],
            documento = this[UsersTable.documento],
            dataNascimento = this[UsersTable.dataNascimento],
            passwordHash = this[UsersTable.passwordHash]
    )
}
