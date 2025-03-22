package dev.bauxe.konaste.repository.state

import dev.bauxe.konaste.models.info.GameWindow

interface GameStateRepository {
  suspend fun getCurrentUi(): GameWindow
}
