package com.sales.features.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.sales.features.user.UserDao
import com.sales.features.auth.model.LoginResult
import java.time.LocalDate

class AuthService(
    private val users: UserDao,
    private val jwt: JwtService
) {
    fun register(
        nome: String,
        sobrenome: String,
        documento: String,
        dataNascimento: LocalDate,
        senha: String
    ): LoginResult {
        val u = users.create(nome, sobrenome, documento, dataNascimento, senha)
        val token = jwt.newAccessToken(u.id, u.documento, u.nome)
        return LoginResult(token)
    }

    fun login(documento: String, senha: String): LoginResult {
        val u = users.findByDocumento(documento) ?: error("Credenciais inválidas")
        val ok = BCrypt.verifyer().verify(senha.toCharArray(), u.passwordHash).verified
        if (!ok) error("Credenciais inválidas")
        val token = jwt.newAccessToken(u.id, u.documento, u.nome)
        return LoginResult(token)
    }
}
