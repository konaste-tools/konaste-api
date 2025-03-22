package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.Path
import kotlinx.datetime.Instant

class MacMemoryClient : MemoryClient {
  override fun read(address: Pointer, size: Int): MemoryResult {
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
