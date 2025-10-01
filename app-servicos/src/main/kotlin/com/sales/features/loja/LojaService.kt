package com.sales.features.loja

import java.util.UUID

data class AuthContext(
    val usuarioId: UUID,
    val systemAdmin: Boolean = false
)

interface LojaMembershipChecker {
    fun roleNaLoja(usuarioId: UUID, lojaId: UUID): LojaRole? // null se não vinculado
    fun podeCriarLoja(userId: UUID): Boolean
}

class LojaService(
    private val repo: LojaRepository,
    private val membership: LojaMembershipChecker
) {
    fun criar(ctx: AuthContext, req: LojaCreateRequest): LojaResponse {
        if (! membership.podeCriarLoja(ctx.usuarioId)) error("Sem permissão para criar lojas.")
        return repo.create(req)
    }

    fun obter(ctx: AuthContext, id: UUID): LojaResponse {
        val loja = repo.findById(id) ?: error("Loja não encontrada.")
        if (!ctx.systemAdmin) {
            membership.roleNaLoja(ctx.usuarioId, id) ?: error("Sem acesso a esta loja.")
        }
        return loja
    }

    fun listar(ctx: AuthContext, offset: Long, limit: Int, q: String?): List<LojaResponse> {
        return if (ctx.systemAdmin) repo.list(offset, limit, q)
        else repo.listarLojasDoUsuario(ctx.usuarioId)
    }

    fun atualizar(ctx: AuthContext, id: UUID, req: LojaUpdateRequest): LojaResponse {
        checarAdmin(ctx, id)
        return repo.update(id, req) ?: error("Loja não encontrada.")
    }

    fun deletar(ctx: AuthContext, id: UUID): Boolean {
        checarAdmin(ctx, id)
        return repo.softDelete(id)
    }

    fun atribuirUsuario(ctx: AuthContext, lojaId: UUID, usuarioId: UUID, role: LojaRole) {
        checarAdmin(ctx, lojaId)
        repo.atribuirUsuario(lojaId, usuarioId, role)
    }

    fun removerUsuario(ctx: AuthContext, lojaId: UUID, usuarioId: UUID): Boolean {
        checarAdmin(ctx, lojaId)
        return repo.removerUsuario(lojaId, usuarioId)
    }

    fun listarUsuarios(ctx: AuthContext, lojaId: UUID): List<LojaUsuarioResponse> {
        checarMembro(ctx, lojaId) // ADMIN ou VENDEDOR
        return repo.listarUsuarios(lojaId)
    }

    fun minhasLojas(ctx: AuthContext): List<LojaResponse> =
        repo.listarLojasDoUsuario(ctx.usuarioId)

    private fun checarAdmin(ctx: AuthContext, lojaId: UUID) {
        if (ctx.systemAdmin) return
        val role = membership.roleNaLoja(ctx.usuarioId, lojaId) ?: error("Sem acesso a esta loja.")
        if (role != LojaRole.ADMIN) error("Ação permitida apenas para ADMIN da loja.")
    }

    private fun checarMembro(ctx: AuthContext, lojaId: UUID) {
        if (ctx.systemAdmin) return
        membership.roleNaLoja(ctx.usuarioId, lojaId) ?: error("Sem acesso a esta loja.")
    }
}
