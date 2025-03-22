package dev.bauxe.konaste.controller.songs

import dev.bauxe.konaste.controller.songs.models.ScoreResponse
import dev.bauxe.konaste.controller.songs.models.SongScoreResponse
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.memory.versions.VersionResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.highscores(scoreService: ScoreService, versionResolver: VersionResolver) {
  highscoreForDifficulty(versionResolver, scoreService)

  get {
    val version =
        when (val versionResult = versionResolver.getActiveVersion()) {
          is VersionResult.NotFound -> {
            call.respond(HttpStatusCode.NotFound)
            return@get
          }
          is VersionResult.Ok -> versionResult.version
        }
    val data = scoreService.getGlobalHighscoreTable()
    val response =
        data.map {
          SongScoreResponse(
              it.key,
              it.value.map { highscore ->
                ScoreResponse(
                    highscore.songId,
                    highscore.difficulty,
                    highscore.score,
                    highscore.userId,
                    highscore.userName)
              })
        }
    call.respond(response)
  }
}

fun Route.highscoreForDifficulty(versionResolver: VersionResolver, scoreService: ScoreService) {
  get("/song/{id}/difficulty/{difficultyId") {
    val songId = call.parameters["songId"]?.toInt()
    val difficultyId = call.parameters["difficultyId"]?.toInt()
    if (songId == null || difficultyId == null) {
      call.respond(HttpStatusCode.BadRequest)
      return@get
    }
    val version =
        when (val versionResult = versionResolver.getActiveVersion()) {
          is VersionResult.NotFound -> {
            call.respond(HttpStatusCode.NotFound)
            return@get
          }
          is VersionResult.Ok -> versionResult.version
        }
    val data =
        scoreService.getGlobalHighscore(
            songId,
            difficultyId,
        )
    when (data) {
      null ->
          call.respond(
              HttpStatusCode.NotFound,
          )
      else ->
          call.respond(
              ScoreResponse(data.songId, data.difficulty, data.score, data.userId, data.userName),
          )
    }
  }
}
