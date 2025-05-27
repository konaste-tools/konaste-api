package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.repository.ItemRepository
import dev.bauxe.konaste.service.SongService
import dev.bauxe.konaste.service.memory.DecryptionService
import dev.bauxe.konaste.service.memory.GameInfoService
import dev.bauxe.konaste.service.memory.ResultService
import dev.bauxe.konaste.service.polling.GameWindowPoller
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.gameRouting() {
  val gameInfoService by inject<GameInfoService>()
  val gameWindowPoller by inject<GameWindowPoller>()
  val resultService by inject<ResultService>()
  val songService by inject<SongService>()
  val decryptionService by inject<DecryptionService>()
  val itemRepository by inject<ItemRepository>()

  routing {
    route("game") {
      route("history") { gameHistory(resultService) }
      route("nowplaying") {
        gameNowPlayingSong(gameInfoService, songService)
        route("score") {}
      }

      route("scoreinfo") { gameScoreinfo(gameInfoService) }
      route("ui") { ui(gameInfoService) }
      route("songselect") { route("selected") {} }
      route("files") { getFile(decryptionService) }
      route("items") { get { call.respond(itemRepository.getItems()) } }
    }
    route("sse") {
      route("game") {
        route("ui") { sseUi(gameWindowPoller) }
        route("nowplaying") { route("stats") { nowPlayingInfoSse(gameInfoService) } }
      }
    }
    route("ws") {
      route("game") {
        route("ui") { websocketUi(gameWindowPoller) }
        route("score") {}
        route("nowplaying") { route("stats") { nowPlayingInfo(gameInfoService) } }
      }
    }
  }
}
