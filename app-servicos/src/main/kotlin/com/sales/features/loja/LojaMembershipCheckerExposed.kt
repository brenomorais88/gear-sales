package com.sales.features.loja

import com.sales.features.user.UserDao
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import java.util.UUID

class LojaMembershipCheckerExposed(
    private val userDao: UserDao
) : LojaMembershipChecker {
    override fun roleNaLoja(usuarioId: UUID, lojaId: UUID): LojaRole? = transaction {

        LojaUsuarios.selectAll()
            .where { (LojaUsuarios.usuarioId eq usuarioId) and (LojaUsuarios.lojaId eq lojaId) }
            .limit(1)
            .firstOrNull()
            ?.get(LojaUsuarios.role)
    }

    override fun podeCriarLoja(userId: UUID): Boolean {
        val role = userDao.getRole(userId)
        return role == "ADMIN"
    }
}
