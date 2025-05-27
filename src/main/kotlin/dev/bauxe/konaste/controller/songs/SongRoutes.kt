package dev.bauxe.konaste.controller.songs

import dev.bauxe.konaste.controller.game.models.SongResponse
import dev.bauxe.konaste.service.SongService
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.songs(songService: SongService) {
  get({
    operationId = "songs"
    summary = "Get page of songs"
    tags("songs")
    request {
      queryParameter<Int>("size") { description = "Number of songs that will be returned" }
      queryParameter<Int>("page") { description = "Page of songs, start determined by size * page" }
      queryParameter<Boolean>("raw") {
        description = "Enables raw data retrieval, will include unknown metadata in response"
      }
    }
    response {
      code(HttpStatusCode.OK) {
        description = "Returned page successfully"
        body<SongResponse> {}
      }
    }
    response {
      code(HttpStatusCode.NotFound) {
        description = "Failed to resolve game instance (is Konaste running?)"
      }
    }
  }) {
    val size = call.request.queryParameters["size"]?.toInt() ?: 100
    val page = call.request.queryParameters["page"]?.toInt() ?: 0
    val raw = call.request.queryParameters["raw"].toBoolean()

    val songs = songService.getSongs(size, page * size)
    when (raw) {
      true -> call.respond(HttpStatusCode.OK, songs)
      false ->
          call.respond(
              HttpStatusCode.OK, songs.map { SongResponse.Companion.fromMusic(it.key, it.value) })
    }
  }
}

fun Route.songId(songService: SongService) {
  get({
    operationId = "songs-id"
    summary = "Get song by ID"
    tags("songs")
    request {
      pathParameter<String>("songId") { description = "ID of song to lookup" }
      queryParameter<Boolean>("raw") {
        description =
            "Enables raw data retrieval, will include unknown metadata and additional fields in response"
      }
    }
  }) {
    val raw = call.request.queryParameters["raw"].toBoolean()

    when (val songInfo = songService.getSong(call.parameters["songId"]!!.toInt())) {
      null -> {
        call.respond(HttpStatusCode.NotFound)
      }
      else -> {
        when (raw) {
          true -> call.respond(HttpStatusCode.OK, songInfo)
          false -> call.respond(HttpStatusCode.OK, songInfo)
        }
      }
    }
  }
}
