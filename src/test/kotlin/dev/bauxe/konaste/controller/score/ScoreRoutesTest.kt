package dev.bauxe.konaste.controller.score

import dev.bauxe.konaste.controller.scores.table
import dev.bauxe.konaste.helpers.partial
import dev.bauxe.konaste.service.DifficultyMode
import dev.bauxe.konaste.service.GradingMode
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.utils.AggregationDirection
import dev.bauxe.konaste.utils.Row
import dev.bauxe.konaste.utils.Table
import dev.bauxe.konaste.utils.Title
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import org.koin.dsl.module

class ScoreRoutesTest :
    FunSpec({
      coroutineTestScope = true
      val scoreService = mockk<ScoreService>()

      val table =
          Table(
              listOf(Title(1, 5), Title(2, 5)),
              listOf(Title(5, 5), Title(4, 5)),
              listOf(Row(listOf(3, 4)), Row(listOf(2, 3))))

      beforeAny { clearMocks(scoreService) }

      test("When lookup songs successful, returns success") {
        coEvery {
          scoreService.getUserTable(
              eq(DifficultyMode.LEVEL),
              eq(GradingMode.GRADE),
              eq(AggregationDirection.NONE),
              eq(0..10),
              eq(1..20))
        } returns table

        testApplication {
          partial(module {})
          application { routing { route("/test") { table(scoreService) } } }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/test")
          response.status shouldBe HttpStatusCode.OK

          val body = response.body<Table<String, Int>>()
          body.columnTitles shouldHaveSize 2
          body.rowTitles shouldHaveSize 2
          body.rows shouldHaveSize 2
        }
      }

      test("When gradingMode and difficultyMode are set, convert columns and rows") {
        coEvery {
          scoreService.getUserTable(
              eq(DifficultyMode.DIFFICULTY),
              eq(GradingMode.CLEAR_MARK),
              eq(AggregationDirection.NONE),
              eq(0..5),
              eq(1..5))
        } returns table

        testApplication {
          partial(module {})
          application {
            routing { route("/{gradingMode}/{difficultyMode}") { table(scoreService) } }
          }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/clear_mark/difficulty")
          response.status shouldBe HttpStatusCode.OK

          val body = response.body<Table<String, String>>()
          body.columnTitles shouldHaveSize 2
          body.columnTitles.map { it.title } shouldContainInOrder listOf("played", "comp")
          body.rowTitles shouldHaveSize 2
          body.rowTitles.map { it.title } shouldContainInOrder listOf("GRAVITY", "INFINITE")
          body.rows shouldHaveSize 2
        }
      }

      test("When invalid gradingMode is passed, returns not found") {
        testApplication {
          partial(module {})
          application {
            routing { route("/{gradingMode}/{difficultyMode}") { table(scoreService) } }
          }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/not_valid/difficulty")
          response.status shouldBe HttpStatusCode.NotFound
        }
      }

      test("When invalid difficultyMode is passed, returns not found") {
        testApplication {
          partial(module {})
          application {
            routing { route("/{gradingMode}/{difficultyMode}") { table(scoreService) } }
          }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/clear_mark/not_valid")
          response.status shouldBe HttpStatusCode.NotFound
        }
      }
    }) {}
