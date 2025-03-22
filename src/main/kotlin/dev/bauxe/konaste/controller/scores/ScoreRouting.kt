package dev.bauxe.konaste.controller.scores

import dev.bauxe.konaste.controller.songs.highscores
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.scoreRouting() {
  val scoreService by inject<ScoreService>()
  val versionResolver by inject<VersionResolver>()

  routing {
    route("/scores") {
      userScores(scoreService)
      route("/table/{gradingMode}/{difficultyMode}") { table(scoreService) }
      route("{songId}") {
        route("difficulties") { route("{difficultyId}") { userScore(scoreService) } }
      }

      route("/highscores") {
        highscores(scoreService, versionResolver)

        route("{songId}") { route("difficulties") { route("{difficultyId}") {} } }
      }
    }
  }
}
