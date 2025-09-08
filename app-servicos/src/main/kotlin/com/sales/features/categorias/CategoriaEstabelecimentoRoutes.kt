package com.sales.features.categorias

import com.sales.features.categorias.CategoriaEstabelecimento
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.categoriaRoutes() {
    route("/v1/categorias") {

        // LISTAR
        get {
            val list = transaction {
                CategoriaEstabelecimento
                    .selectAll()
                    .orderBy(CategoriaEstabelecimento.id to SortOrder.ASC)
                    .map {
                        CategoriaEstabelecimentoDTO(
                            id = it[CategoriaEstabelecimento.id].value,
                            nome = it[CategoriaEstabelecimento.nome]
                        )
                    }
            }
            call.respond(list)
        }

        // DETALHE
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "id inválido")

            val entityId = EntityID(id, CategoriaEstabelecimento)

            val item = transaction {
                CategoriaEstabelecimento
                    .selectAll()
                    .where { CategoriaEstabelecimento.id eq entityId }   // DSL nova
                    .singleOrNull()
                    ?.let {
                        CategoriaEstabelecimentoDTO(
                            id = it[CategoriaEstabelecimento.id].value,
                            nome = it[CategoriaEstabelecimento.nome]
                        )
                    }
            } ?: return@get call.respond(HttpStatusCode.NotFound, "não encontrado")

            call.respond(item)
        }

        // CRIAR
        post {
            val body = call.receive<CategoriaEstabelecimentoDTO>()
            if (body.nome.isBlank())
                return@post call.respond(HttpStatusCode.UnprocessableEntity, "nome é obrigatório")

            val newId = transaction {
                CategoriaEstabelecimento.insertAndGetId {
                    it[nome] = body.nome
                }.value
            }
            call.respond(HttpStatusCode.Created, mapOf("id" to newId))
        }

        // ATUALIZAR
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "id inválido")

            val body = call.receive<CategoriaEstabelecimentoDTO>()
            val entityId = EntityID(id, CategoriaEstabelecimento)

            val updated = transaction {
                CategoriaEstabelecimento.update({ CategoriaEstabelecimento.id eq entityId }) {
                    it[nome] = body.nome
                }
            }
            if (updated == 0) return@put call.respond(HttpStatusCode.NotFound, "não encontrado")
            call.respond(mapOf("updated" to updated))
        }

        // DELETAR
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "id inválido")

            val entityId = EntityID(id, CategoriaEstabelecimento)
            val deleted = transaction {
                CategoriaEstabelecimento.deleteWhere { CategoriaEstabelecimento.id eq entityId }
            }
            if (deleted == 0) return@delete call.respond(HttpStatusCode.NotFound, "não encontrado")
            call.respond(mapOf("deleted" to deleted))
        }
    }
}
