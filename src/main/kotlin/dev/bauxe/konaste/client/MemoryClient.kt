package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.Path
import kotlinx.datetime.Instant

interface MemoryClient {
  fun read(
      address: Pointer,
      size: Int,
  ): MemoryResult

  fun followPath(path: Path): PointerResult

  fun followPath(path: Path, basePointer: Pointer): PointerResult

  fun lastUpdatedAt(): Instant
}

sealed class MemoryResult {
  class Ok(
      val data: ByteArray,
  ) : MemoryResult()

  class KernelError(val code: Int) : MemoryResult()

  data object ReadViolation : MemoryResult()

  data object ProcessNotFound : MemoryResult()
}

sealed class PointerResult {
  class Ok(val pointer: Pointer) : PointerResult()

  data object NotFound : PointerResult()

  data object ProcessNotFound : PointerResult()
}
