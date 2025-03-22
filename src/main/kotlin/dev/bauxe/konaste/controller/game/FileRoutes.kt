package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.service.memory.DecryptionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getFile(decryptionService: DecryptionService) {
  get {
    val file = call.request.queryParameters["filename"].toString()

    val data =
        decryptionService.decryptFile(file) ?: return@get call.respond(HttpStatusCode.NotFound)

    when (file.substringAfterLast('.').lowercase()) {
      "png" -> call.respondBytes(ContentType.Image.PNG) { data }
      else -> call.respondBytes(ContentType.Text.Any) { data }
    }
  }
}
