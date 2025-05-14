package dev.bauxe.konaste.service.composition

import io.github.oshai.kotlinlogging.KotlinLogging

class EventManager() {
  private val listeners: MutableList<EventListener> = mutableListOf()
  private val logger = KotlinLogging.logger {}

  fun register(listener: EventListener) {
    logger.info { "Registering event listener ${listener::class}" }
    listeners.add(listener)
  }

  fun fireOnGameBoot() {
    listeners.forEach { it.onGameBoot() }
  }

  fun fireOnGameClose() {
    listeners.forEach { it.onGameClose() }
  }

  fun fireOnGameStart() {
    listeners.forEach { it.onGameStart() }
  }

  fun fireOnLogin() {
    listeners.forEach { it.onLogin() }
  }

  fun fireOnLogout() {
    listeners.forEach { it.onLogout() }
  }
}
