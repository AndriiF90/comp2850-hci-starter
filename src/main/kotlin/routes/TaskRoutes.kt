package routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import model.Task
import model.ValidationResult
import storage.TaskStore
import utils.Logger
import java.util.UUID

fun Route.taskRoutes(store: TaskStore) {

    get("/tasks") {
        val tasks = store.getAll().map { it.toPebbleContext() }
        val html = call.renderTemplate("tasks/index.peb", mapOf("tasks" to tasks))
        call.respondText(html, ContentType.Text.Html)
    }

    post("/tasks") {
        val start = System.currentTimeMillis()
        val sessionId = call.request.cookies["sid"] ?: "P_anon"
        val requestId = UUID.randomUUID().toString().take(8)
        val jsMode = if (call.request.headers["HX-Request"] == "true") "on" else "off"

        val params = call.receiveParameters()
        val title = params["title"] ?: ""

        when (val result = Task.validate(title)) {
            is ValidationResult.Error -> {
                Logger.validationError(sessionId, requestId, "T1_add", "invalid_title", jsMode)
                call.respondText("Error: ${result.message}", ContentType.Text.Html, HttpStatusCode.BadRequest)
            }
            is ValidationResult.Success -> {
                store.add(Task(title = title))
                Logger.success(sessionId, requestId, "T1_add", System.currentTimeMillis() - start, jsMode)
                call.respondRedirect("/tasks")
            }
        }
    }

    post("/tasks/{id}/edit") {
        val start = System.currentTimeMillis()
        val sessionId = call.request.cookies["sid"] ?: "P_anon"
        val requestId = UUID.randomUUID().toString().take(8)
        val jsMode = if (call.request.headers["HX-Request"] == "true") "on" else "off"

        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.NotFound)
        val params = call.receiveParameters()
        val title = params["title"] ?: ""
        
        val existing = store.getById(id)
        if (existing != null) {
             store.update(existing.copy(title = title))
             Logger.success(sessionId, requestId, "T2_edit", System.currentTimeMillis() - start, jsMode)
             call.respondRedirect("/tasks")
        } else {
             call.respond(HttpStatusCode.NotFound)
        }
    }

    post("/tasks/{id}/delete") {
        val start = System.currentTimeMillis()
        val sessionId = call.request.cookies["sid"] ?: "P_anon"
        val requestId = UUID.randomUUID().toString().take(8)
        val jsMode = if (call.request.headers["HX-Request"] == "true") "on" else "off"

        val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.NotFound)
        
        if (store.delete(id)) {
            Logger.success(sessionId, requestId, "T3_delete", System.currentTimeMillis() - start, jsMode)
            call.respondRedirect("/tasks")
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
