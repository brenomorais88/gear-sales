package com.sales.features.loja

import com.sales.features.auth.authCtx
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.getKoin
import java.util.UUID

fun Route.lojaRoutes() {
    val service: LojaService by lazy { application.getKoin().get<LojaService>() }

    authenticate("auth-jwt") {
        route("/lojas") {

            post {
                val ctx = call.authCtx()
                val req = call.receive<LojaCreateRequest>()
                call.respond(service.criar(ctx, req))
            }

            get {
                val ctx = call.authCtx()
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                val q = call.request.queryParameters["q"]
                call.respond(service.listar(ctx, offset, limit, q))
            }

            get("{id}") {
                val ctx = call.authCtx()
                val id = call.parameters["id"]!!.let(UUID::fromString)
                call.respond(service.obter(ctx, id))
            }

            put("{id}") {
                val ctx = call.authCtx()
                val id = call.parameters["id"]!!.let(UUID::fromString)
                val req = call.receive<LojaUpdateRequest>()
                call.respond(service.atualizar(ctx, id, req))
            }

            delete("{id}") {
                val ctx = call.authCtx()
                val id = call.parameters["id"]!!.let(UUID::fromString)
                call.respond(mapOf("deleted" to service.deletar(ctx, id)))
            }

            // Vínculos usuário ↔ loja
            post("{id}/usuarios") {
                val ctx = call.authCtx()
                val lojaId = call.parameters["id"]!!.let(UUID::fromString)
                val body = call.receive<AtribuirUsuarioRequest>()
                service.atribuirUsuario(ctx, lojaId, body.usuarioId, body.role)
                call.respond(mapOf("ok" to true))
            }

            delete("{id}/usuarios/{usuarioId}") {
                val ctx = call.authCtx()
                val lojaId = call.parameters["id"]!!.let(UUID::fromString)
                val usuarioId = call.parameters["usuarioId"]!!.let(UUID::fromString)
                call.respond(mapOf("removed" to service.removerUsuario(ctx, lojaId, usuarioId)))
            }

            get("{id}/usuarios") {
                val ctx = call.authCtx()
                val lojaId = call.parameters["id"]!!.let(UUID::fromString)
                call.respond(service.listarUsuarios(ctx, lojaId))
            }
        }

        get("/me/lojas") {
            val ctx = call.authCtx()
            call.respond(service.minhasLojas(ctx))
        }
    }
}
