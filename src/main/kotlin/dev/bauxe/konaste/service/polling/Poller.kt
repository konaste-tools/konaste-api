package dev.bauxe.konaste.service.polling

import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class Poller(context: CoroutineContext, private val pollFrequency: Duration) {
  private val scope = CoroutineScope(context)

  init {
    poll()
  }

  private fun poll() {
    scope.launch {
      while (scope.isActive) {
        pollingFn()
        delay(pollFrequency)
      }
    }
  }

  internal abstract suspend fun pollingFn()
}
