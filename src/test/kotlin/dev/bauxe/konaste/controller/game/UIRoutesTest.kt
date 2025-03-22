package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.helpers.partial
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.repository.state.GameStateRepository
import dev.bauxe.konaste.service.memory.GameInfoService
import dev.bauxe.konaste.service.polling.GameWindowPoller
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.koin.dsl.module

class UIRoutesTest :
    FunSpec({
      coroutineTestScope = true

      val gameInfoService = mockk<GameInfoService>()
      val gameStateRepository = mockk<GameStateRepository>()

      test("Can fetch current UI") {
        every { gameInfoService.getGameWindow() } returns GameWindow.UI_AUTOPLAY

        testApplication {
          partial(module {})
          application { routing { route("test") { ui(gameInfoService) } } }
          val client = createClient {}

          val response = client.get("/test")

          response.status shouldBe HttpStatusCode.OK
          response.bodyAsText() shouldBe "\"UI_AUTOPLAY\""
        }
      }

      test("UI changes are emitted over webhook") {
        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(
                GameWindow.UI_UNKNOWN,
                GameWindow.UI_BOOT,
                GameWindow.UI_BOOT,
                GameWindow.UI_START_CREDIT)

        val scheduler = TestCoroutineScheduler()
        val context = StandardTestDispatcher(scheduler)
        val gameWindowPoller = GameWindowPoller(context, 100.milliseconds, gameStateRepository)

        testApplication {
          partial(module {})
          application { routing { route("test") { websocketUi(gameWindowPoller) } } }
          val client = createClient { install(WebSockets) }

          client.webSocket("/test") {
            scheduler.advanceTimeBy(500.milliseconds)
            val response1 = (incoming.receive() as Frame.Text).readText()
            val response2 = (incoming.receive() as Frame.Text).readText()
            response1 shouldBe "UI_BOOT"
            response2 shouldBe "UI_START_CREDIT"
            gameWindowPoller.cancel()
            this.close()
          }
        }
      }
    })
