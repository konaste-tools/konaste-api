package dev.bauxe.konaste.repository.state

import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.service.memory.versions.DataReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import io.github.oshai.kotlinlogging.KotlinLogging

class MemoryBackedGameStateRepository(
    val versionResolver: VersionResolver,
    val memoryClient: MemoryClient
) : GameStateRepository {
  private val logger = KotlinLogging.logger {}
  private var lastFailureReason: String = ""

  override suspend fun getCurrentUi(): GameWindow {
    return when (val result =
        versionResolver.accept { it.getCurrentUIPath() }.readMemory(memoryClient, 4)) {
      is DataReadResult.Error -> {
        if (result.reason.toString() != lastFailureReason) {
          logger.error { "Failed to retrieve active game window: ${result.reason}" }
        }
        lastFailureReason = result.reason.toString()
        return GameWindow.UI_UNKNOWN
      }
      is DataReadResult.Ok -> {
        lastFailureReason = ""
        GameWindow.Companion.fromInt(result.data.nextInt())
      }
    }
  }
}
