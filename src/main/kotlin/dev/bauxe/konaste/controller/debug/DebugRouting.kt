package dev.bauxe.konaste.controller.debug

import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.service.SongService
import dev.bauxe.konaste.service.memory.ResultService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.debugRouting() {
  val resultService by inject<ResultService>()
  val songService by inject<SongService>()
  val scoreService by inject<ScoreService>()

  routing {
    route("debug") {
      route("history") { get { call.respond(resultService.getResultHistoryData()) } }
      route("songs") { get { call.respond(songService.getSongs(3000, 0)) } }
      route("scores") { get { call.respond(scoreService.getUserHighscoreTable()) } }
    }
  }
}
