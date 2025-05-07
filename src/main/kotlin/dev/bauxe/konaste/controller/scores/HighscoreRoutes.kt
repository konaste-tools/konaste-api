package dev.bauxe.konaste.controller.scores

import dev.bauxe.konaste.controller.scores.models.Scores
import dev.bauxe.konaste.controller.songs.models.ScoreResponse
import dev.bauxe.konaste.controller.songs.models.SongScoreResponse
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.highscores(scoreService: ScoreService, versionResolver: VersionResolver) {
  highscoreForDifficulty(versionResolver, scoreService)

  get<Scores.HighScores> {
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
  get<Scores.HighScores.Difficulty> {
    val data =
        scoreService.getGlobalHighscore(
            it.songId,
            it.difficultyId,
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
