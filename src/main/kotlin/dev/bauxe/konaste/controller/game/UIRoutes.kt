package dev.bauxe.konaste.controller.game

import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.service.memory.GameInfoService
import dev.bauxe.konaste.service.polling.GameWindowPoller
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

fun Route.ui(gameInfoService: GameInfoService) {
  get({
    summary = "Get current game UI"
    description = "Returns the current UI view in Konaste."
    tags("metadata", "websocket-supported")
    response {
      code(HttpStatusCode.OK) {
        description = "Found the current UI view"
        body<String> { example("UI Response") { value = "UI_SONG_SELECT" } }
      }
    }
  }) {
    call.respond(gameInfoService.getGameWindow())
  }
}

fun Route.websocketUi(gameWindowPoller: GameWindowPoller) {
  webSocket {
    GameWindow.Companion.logger.debug { "Opened connection to /ws/ui" }
    val sendData: suspend (GameWindow, GameWindow) -> Unit = { _, window ->
      this@webSocket.send(
          Frame.Text(
              window.name,
          ),
      )
    }
    try {
      gameWindowPoller.addOnChange(sendData)
      while (this.isActive) delay(10.milliseconds)
    } catch (e: Exception,) {
      GameWindow.Companion.logger.warn { "Websocket closed due to an error: ${e.message}" }
    } finally {
      GameWindow.Companion.logger.info { "Connection closed for /ws/ui" }
      gameWindowPoller.removeOnChange(sendData)
    }
  }
}
