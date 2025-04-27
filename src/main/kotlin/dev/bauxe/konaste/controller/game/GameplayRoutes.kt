package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.controller.game.models.NowPlayingResponse
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.models.info.GameWindow.Companion.logger
import dev.bauxe.konaste.models.scores.NowPlayingData
import dev.bauxe.konaste.models.scores.ResultScreenData
import dev.bauxe.konaste.service.ImageSize
import dev.bauxe.konaste.service.SongService
import dev.bauxe.konaste.service.memory.GameInfoService
import dev.bauxe.konaste.service.memory.ResultService
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

fun Route.websocketGameplayScore() {
  webSocket {
    GameWindow.Companion.logger.info { "New connection opened for /ws/gameplay/score" }
    val frequency = call.request.queryParameters["rate"]?.toLong() ?: 50
    try {
      while (this.isActive) {
        send(
            Frame.Text(
                "test",
            ),
        )
        delay(
            frequency,
        )
      }
    } catch (e: Exception,) {
      GameWindow.Companion.logger.warn { "Websocket closed due to an error: ${e.message}" }
    } finally {
      GameWindow.Companion.logger.info { "Connection closed for /ws/gameplay/score" }
    }
  }
}

fun Route.gameNowPlayingSong(gameInfoService: GameInfoService, songService: SongService) {
  get({
    operationId = "nowplaying"
    summary = "Current now playing info"
    description = "Returns basic data about the current song. Requires UI_SONG_PLAY to function."
    tags("nowplaying")
    response {
      code(HttpStatusCode.OK) {
        description = "Returns now playing details"
        body<NowPlayingResponse> {}
      }
      code(HttpStatusCode.NotFound) {
        description = "Could not find data about the current song."
        body<Unit> {}
      }
    }
  }) {
    val raw = call.request.queryParameters["raw"].toBoolean()
    val nowPlaying = gameInfoService.getNowPlaying()
    if (nowPlaying.songId == -1) {
      logger.debug { "Failed to get now playing song because now playing was not found" }
      call.respond(HttpStatusCode.NotFound)
      return@get
    }
    val songInfo = songService.getSong(nowPlaying.songId)
    if (songInfo == null) {
      logger.debug {
        "Failed to get now playing song ${nowPlaying.songId} because song was not found"
      }
      call.respond(HttpStatusCode.NotFound)
      return@get
    }

    when (raw) {
      true -> call.respond(songInfo)
      false ->
          call.respond(
              NowPlayingResponse.Companion.fromMusic(
                  nowPlaying.songId,
                  nowPlaying.difficulty,
                  songInfo,
                  songInfo.difficulties[nowPlaying.difficulty],
                  songService.getSongImagePath(
                      nowPlaying.songId, songInfo.ascii, nowPlaying.difficulty, ImageSize.S),
                  songService.getSongImagePath(
                      nowPlaying.songId, songInfo.ascii, nowPlaying.difficulty, ImageSize.N),
                  songService.getSongImagePath(
                      nowPlaying.songId, songInfo.ascii, nowPlaying.difficulty, ImageSize.B)))
    }
  }
}

fun Route.gameScoreinfo(gameInfoService: GameInfoService) {
  get({
    operationId = "scoreinfo"
    summary = "Get current score info"
    description = "Returns live score data of current play. Requires UI_SONG_PLAY to function."
    tags("scoreinfo", "websocket-supported")
    response {
      code(HttpStatusCode.OK) {
        description = "Returns live score info"
        body<NowPlayingData> {}
      }
      code(HttpStatusCode.NotFound) {
        description = "Could not find live score info."
        body<Unit> {}
      }
    }
  }) {
    val scoreInfo = gameInfoService.getPlayData()
    if (scoreInfo == null) {
      call.respond(HttpStatusCode.NotFound)
    } else {
      call.respond(scoreInfo)
    }
  }
}

fun Route.nowPlayingInfo(
    gameInfoService: GameInfoService,
) {
  webSocket {
    val frequency = call.request.queryParameters["rate"]?.toLong() ?: 1000

    try {
      while (this.isActive) {
        val scoreInfo = gameInfoService.getNowPlayingPointIntime()
        send(
            Frame.Text(
                Json.encodeToString(scoreInfo),
            ),
        )
        delay(
            frequency,
        )
      }
    } catch (e: Exception,) {
      logger.warn { "Websocket closed due to an error: ${e.message}" }
    } finally {
      logger.info { "Connection closed for /ws/game/nowplaying/stats" }
    }
  }
}

fun Route.gameHistory(resultService: ResultService) {
  get({
    operationId = "history"
    summary = "Get current session result history"
    description = "Returns score data from current play session"
    response {
      code(HttpStatusCode.OK) {
        description = "Session result history"
        body<List<ResultScreenData>> {}
      }
    }
  }) {
    call.respond(resultService.getResultHistory())
  }
}

fun Route.user() {
  get(
      "/user/settings",
  ) {}
}
