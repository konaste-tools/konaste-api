package dev.bauxe.konaste.controller.songs

import dev.bauxe.konaste.service.SongService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.songRouting() {
  val songService: SongService by inject<SongService>()

  routing {
    route("songs") {
      songs(songService)
      route("{songId}") {
        songId(songService)
        route("difficulties") { route("{difficultyId}") {} }
      }
    }
  }
}
