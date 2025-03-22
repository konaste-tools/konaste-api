package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.controller.game.models.SongResponse
import dev.bauxe.konaste.controller.songs.songs
import dev.bauxe.konaste.helpers.partial
import dev.bauxe.konaste.models.music.Music
import dev.bauxe.konaste.service.SongService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldMatchInOrder
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.koin.dsl.module

class SongsTest :
    FunSpec({
      coroutineTestScope = true
      val songService = mockk<SongService>()

      test("When lookup songs successful, returns success") {
        coEvery { songService.getSongs(any(), any()) } returns mapOf()

        testApplication {
          partial(module {})
          application { routing { route("/test") { songs(songService) } } }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/test")
          response.status shouldBe HttpStatusCode.OK

          val body: List<SongResponse> = response.body<List<SongResponse>>()
          body.size shouldBe 0
        }
      }

      test("Songs are returned in response form") {
        coEvery { songService.getSongs(any(), any()) } returns mapOf(createMusic(1), createMusic(2))

        testApplication {
          partial(module {})
          application { routing { route("/test") { songs(songService) } } }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/test")
          response.status shouldBe HttpStatusCode.OK

          val body: List<SongResponse> = response.body<List<SongResponse>>()
          body.size shouldBe 2
          body shouldMatchInOrder listOf({ it.id shouldBe 1 }, { it.id shouldBe 2 })
        }
      }

      test("Songs are returned in raw form if query specified") {
        coEvery { songService.getSongs(any(), any()) } returns mapOf(createMusic(1), createMusic(2))

        testApplication {
          partial(module {})
          application { routing { route("/test") { songs(songService) } } }

          val client = createClient { install(ContentNegotiation) { json() } }

          val response = client.get("/test", { url { parameters.append("raw", "true") } })
          response.status shouldBe HttpStatusCode.OK

          val body: Map<Int, Music> = response.body<Map<Int, Music>>()
          body.size shouldBe 2
          body shouldContainKey 1
          body shouldContainKey 2
        }
      }
    })

fun createMusic(songId: Int): Pair<Int, Music> {
  return songId to
      Music(
          label = "ridiculus",
          titleName = "Cole Brewer",
          titleYomigana = "impetus",
          artistName = "Marietta Pacheco",
          artistYomigana = "definitiones",
          ascii = "deseruisse",
          bpmMax = 14800u,
          bpmMin = 1800u,
          distributionDate = 20241111u,
          volume = 50u,
          bgNo = 1u,
          genre = 1u,
          isFixed = 1u,
          version = 4u,
          demoPri = 2,
          filenameObfuscatedPath = "Herminia Lynch",
          infVer = 3u,
          difficulties = listOf())
}
