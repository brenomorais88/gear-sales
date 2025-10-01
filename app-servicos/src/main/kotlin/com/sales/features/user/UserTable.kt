package com.sales.features.user

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = uuid("id").uniqueIndex()
    val nome = text("nome")
    val sobrenome = text("sobrenome")
    val documento = text("documento").uniqueIndex()
    val dataNascimento = date("data_nascimento")
    val passwordHash = text("password_hash")
    val role = text("role").default("USER")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    override val primaryKey = PrimaryKey(id)
}