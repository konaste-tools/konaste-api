package dev.bauxe.konaste.controller.scores

import dev.bauxe.konaste.controller.songs.models.UserScoreResponse
import dev.bauxe.konaste.service.DifficultyMode
import dev.bauxe.konaste.service.GradingMode
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.utils.AggregationDirection
import dev.bauxe.konaste.utils.ClearMarkConverter
import dev.bauxe.konaste.utils.DifficultyConverter
import dev.bauxe.konaste.utils.GradeConverter
import dev.bauxe.konaste.utils.Table
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userScores(scoreService: ScoreService) {
  get({
    operationId = "user-scores"
    summary = "Lists all user best scores"
    tags("scores")
    request {
      queryParameter<String>("raw") {
        description =
            "Enables raw data retrieval, will include unknown metadata and additional fields in response"
      }
    }
    response { code(HttpStatusCode.OK) { body<Map<String, UserScoreResponse>> {} } }
  }) {
    val rawScores = scoreService.getUserHighscoreTable()
    when (call.request.queryParameters["raw"]) {
      "true" -> call.respond(rawScores)
      else ->
          call.respond(
              rawScores.map {
                Pair(
                    it.key,
                    it.value.map { score -> UserScoreResponse.Companion.fromUserScore(score) })
              })
    }
  }
}

fun Route.userScore(scoreService: ScoreService) {
  get({
    operationId = "user-score-difficulty"
    summary = "Lists user best score for song difficulty combo"
    tags("scores")
    request {
      pathParameter<String>("songId") { description = "Song to lookup score for" }
      pathParameter<String>("difficultyId") { description = "Difficulty to lookup" }
    }
    response { code(HttpStatusCode.OK) { body<UserScoreResponse> {} } }
  }) {
    val songId = call.parameters["songId"]!!.toInt()
    val difficultyId = call.parameters["difficultyId"]!!.toInt()
    when (val result = scoreService.getUserHighscore(songId, difficultyId)) {
      null -> call.respond(HttpStatusCode.NotFound)
      else -> call.respond(UserScoreResponse.Companion.fromUserScore(result))
    }
  }
}

fun Route.table(scoreService: ScoreService) {
  get({
    operationId = "user-score-table"
    summary = "Lists a tabulated form of a user's scores"
    tags("scores")
    request {
      pathParameter<String>("gradingMode") {
        description = "Grading mode for table"
        example("grade") {
          value = "grade"
          description = "Will use grade for column display (no-play through to S)"
        }
        example("clear_mark") {
          value = "clear_mark"
          description = "Will use clear mark for column display (no-play to PUC)"
        }
      }
      pathParameter<String>("difficultyMode") { description = "Difficulty mode for table" }
      queryParameter<String>("aggregation") {
        description = "Form of table aggregation"
        example("No aggregation") {
          value = "none"
          description = "Will leave table values exactly as is"
        }
        example("Left aggregation") {
          value = "left"
          description = "Will sum table values fro right to left"
        }
      }
      queryParameter<String>("columnRange") {
        description = "Filters the columns (gradingMode) to the specified range"
        example("Filter clear mark from EX to PUC") { value = "3..5" }
        example("Filter grade from AA+ to S") { value = "7..10" }
      }
      queryParameter<String>("rowRange") {
        description = "Filters the rows (difficultyMode) to the specified range"
        example("Filter difficulty from EXHAUST to MAXIMUM") { value = "3..5" }
        example("Filter levels 16 to 20") { value = "16..20" }
      }
      queryParameter<String>("ignoreMissingItems") {
        description = "Do not include data in the table for missing items"
        example("Exclude difficulties from missing items") { value = "true" }
      }
    }
    response { code(HttpStatusCode.OK) { body<Table<Int, Int>> {} } }
  }) {
    val gradingMode =
        GradingMode.Companion.from(call.parameters["gradingMode"] ?: "grade")
            ?: run {
              call.respond(HttpStatusCode.NotFound)
              return@get
            }
    val difficultyMode =
        DifficultyMode.Companion.from(call.parameters["difficultyMode"] ?: "level")
            ?: run {
              call.respond(HttpStatusCode.NotFound)
              return@get
            }
    val aggregation =
        AggregationDirection.Companion.from(call.request.queryParameters["aggregation"] ?: "none")

    val columnRange =
        (call.request.queryParameters["columnRange"]
                ?: when (gradingMode) {
                  GradingMode.GRADE -> "0..10"
                  GradingMode.CLEAR_MARK -> "0..5"
                })
            .split("..")
            .map { it.toInt() }
            .let { it[0]..it[1] }

    val rowRange =
        (call.request.queryParameters["rowRange"]
                ?: when (difficultyMode) {
                  DifficultyMode.DIFFICULTY -> "1..5"
                  DifficultyMode.LEVEL -> "1..20"
                })
            .split("..")
            .map { it.toInt() }
            .let { it[0]..it[1] }

    val ignoreMissingItems = call.request.queryParameters["ignoreMissingItems"] == "true"

    val columnConverter: (Int) -> String =
        when (gradingMode) {
          GradingMode.GRADE -> GradeConverter.Companion::convertGrade
          GradingMode.CLEAR_MARK -> ClearMarkConverter.Companion::convertClearMark
        }

    val rowConverter: (Int) -> String =
        when (difficultyMode) {
          DifficultyMode.DIFFICULTY -> { n ->
                DifficultyConverter.Companion.convertDifficulty(n).name
              }
          DifficultyMode.LEVEL -> { level -> level.toString() }
        }

    val filters =
        when (ignoreMissingItems) {
          true -> listOf(scoreService::filterMissingItems)
          false -> listOf()
        }

    call.respond(
        scoreService
            .getUserTable(difficultyMode, gradingMode, aggregation, columnRange, rowRange, filters)
            .transform(columnConverter, rowConverter))
  }
}
