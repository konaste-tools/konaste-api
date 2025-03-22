package dev.bauxe.konaste.models.info

import dev.bauxe.konaste.models.info.GameWindow.entries
import io.github.oshai.kotlinlogging.KotlinLogging

enum class GameWindow(val id: Int) {
  UI_UNKNOWN(-1), //
  UI_BOOT(0), //
  UI_LOAD_START(2), //
  UI_END_OF_CREDIT(9),
  UI_HOME(11),
  UI_SPLASH(12),
  UI_RESULT(14),
  UI_SONG_SELECT(16),
  UI_AUTOPLAY(17),
  UI_DEMO(18),
  UI_START_CREDIT(20),
  UI_START_LOGIN(21), //
  UI_LOGGED_IN(23),
  UI_MODE_SELECT(26),
  UI_SKILL_ANALYZER(34), //
  UI_MY_ROOM(39),
  UI_SONG_PLAY(40);

  companion object {
    val logger = KotlinLogging.logger {}

    fun fromInt(windowId: Int): GameWindow {
      val result = entries.firstOrNull { it.id == windowId }
      if (result == null) {
        logger.warn { "Could not find matching window id $windowId" }
        return UI_UNKNOWN
      }
      return result
    }
  }
}
