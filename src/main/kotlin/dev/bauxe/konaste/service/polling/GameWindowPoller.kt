package dev.bauxe.konaste.service.polling

import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.repository.state.GameStateRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameWindowPoller(
    context: CoroutineContext,
    private val pollingRate: Duration,
    private val gameStateRepository: GameStateRepository,
) {
  private val logger = KotlinLogging.logger {}

  private val onStart = HashMap<GameWindow, List<suspend () -> Unit>>()
  private val onEnd = HashMap<GameWindow, List<suspend () -> Unit>>()
  private var onChange: List<suspend (GameWindow) -> Unit> = listOf()

  private val scope = CoroutineScope(context)

  private var lastWindow = GameWindow.UI_UNKNOWN

  private var active = false

  init {
    logger.info { "Started game window polling with $pollingRate rate" }
    start()
  }

  fun addOnStart(window: GameWindow, fn: suspend () -> Unit) {
    onStart[window] = onStart[window]?.plus(fn) ?: listOf(fn)
    logger.info { "Added function ${fn.hashCode()} to onStart" }
  }

  fun removeOnStart(window: GameWindow, fn: suspend () -> Unit) {
    onStart[window] = onStart[window]?.minus(fn) ?: listOf()
    logger.info { "Removed function ${fn.hashCode()} from onStart" }
  }

  fun addOnEnd(window: GameWindow, fn: suspend () -> Unit) {
    onEnd[window] = onEnd[window]?.plus(fn) ?: listOf(fn)
    logger.info { "Added function ${fn.hashCode()} to onEnd" }
  }

  fun removeOnEnd(window: GameWindow, fn: suspend () -> Unit) {
    onEnd[window] = onEnd[window]?.minus(fn) ?: listOf()
    logger.info { "Removed function ${fn.hashCode()} from onEnd" }
  }

  fun addOnChange(fn: suspend (GameWindow) -> Unit) {
    onChange = onChange.plus(fn)
    logger.info { "Added function ${fn.hashCode()} to onChange" }
  }

  fun removeOnChange(fn: suspend (GameWindow) -> Unit) {
    onChange = onChange.minus(fn)
    logger.info { "Removed function ${fn.hashCode()} from onChange" }
  }

  private fun start() {
    if (active) return
    active = true
    scope.launch {
      while (active) {
        val nextWindow = gameStateRepository.getCurrentUi()
        if (lastWindow != nextWindow) {
          logger.debug { "Window changed from $lastWindow to $nextWindow" }
          onStart[nextWindow]?.forEach { it.invoke() }
          onEnd[lastWindow]?.forEach { it.invoke() }
          onChange.forEach {
            logger.trace { "Sending for ${it.hashCode()}" }
            it.invoke(nextWindow)
          }
        }
        lastWindow = nextWindow
        delay(pollingRate)
      }
    }
  }

  fun cancel() {
    active = false
    scope.cancel()
  }
}
