package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.service.composition.EventManager
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MacMemoryClient() : MemoryClient, KoinComponent {
  val eventManager: EventManager by inject()
  var started = false

  override fun read(address: Pointer, size: Int): MemoryResult {
    if (!started) {
      eventManager.fireOnGameBoot()
    }
    return MemoryResult.ProcessNotFound
  }

  override fun followPath(path: Path): PointerResult {
    return PointerResult.ProcessNotFound
  }

  override fun followPath(path: Path, basePointer: Pointer): PointerResult {
    return PointerResult.ProcessNotFound
  }

  override fun lastUpdatedAt(): Instant {
    return Instant.fromEpochMilliseconds(0)
  }
}
