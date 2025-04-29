package dev.bauxe.konaste.service.composition

class EventManager(val listeners: List<EventListener>) {
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
