package com.sales.features.user

import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
    val passwordHash: String,
    val role: String
)

class UserDao {

    fun create(
        nome: String,
        sobrenome: String,
        documento: String,
        dataNascimento: LocalDate,
        senha: String,
        role: String = "USER"
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
            it[UsersTable.role] = role
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

    fun updateRole(userId: UUID, newRole: String): Boolean = transaction {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[role] = newRole
            it[updatedAt] = Instant.now()
        } > 0
    }

    fun getRole(userId: UUID): String? = transaction {
        UsersTable
            .select(UsersTable.role)
            .where { UsersTable.id eq userId }
            .limit(1)
            .map { it[UsersTable.role] }
            .firstOrNull()
    }

    fun getRoleByDocumento(doc: String): String? = transaction {
        UsersTable
            .select(UsersTable.role)
            .where { UsersTable.documento eq doc }
            .limit(1)
            .map { it[UsersTable.role] }
            .firstOrNull()
    }

    fun isAdmin(userId: UUID): Boolean = getRole(userId) == "ADMIN"
    fun isAdminByDocumento(doc: String): Boolean = getRoleByDocumento(doc) == "ADMIN"

    private fun ResultRow.toRecord() = UserRecord(
        id = this[UsersTable.id],
        nome = this[UsersTable.nome],
        sobrenome = this[UsersTable.sobrenome],
        documento = this[UsersTable.documento],
        dataNascimento = this[UsersTable.dataNascimento],
        passwordHash = this[UsersTable.passwordHash],
        role = this[UsersTable.role]
    )
}
