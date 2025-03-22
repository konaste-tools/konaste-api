package dev.bauxe.konaste.helpers

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.client.PointerResult
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.memory.PointerSize
import dev.bauxe.konaste.utils.ByteHelpers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FakeMemoryClient(
    private val memoryBlocks: List<FakeMemoryBlock>,
    private val fileOffsets: Map<String, Long>
) : MemoryClient {
  private val logger = KotlinLogging.logger {}

  override fun read(address: Pointer, size: Int): MemoryResult {
    val block =
        memoryBlocks.firstOrNull {
          it.blockStart() <= Pointer.nativeValue(address) &&
              it.blockEnd() >= Pointer.nativeValue(address.share(size.toLong()))
        } ?: return MemoryResult.ReadViolation
    return MemoryResult.Ok(block.read(address, size))
  }

  override fun followPath(path: Path): PointerResult {
    return followPath(path, Pointer(fileOffsets[path.module] ?: 0))
  }

  override fun followPath(path: Path, basePointer: Pointer): PointerResult {
    return PointerResult.Ok(
        path.pointers
            .fold(basePointer) { a, b ->
              when (val result =
                  read(
                      a.share(b.offset),
                      when (b.size) {
                        PointerSize.BYTE_4 -> 4
                        PointerSize.BYTE_8 -> 8
                      })) {
                is MemoryResult.Ok -> {
                  val pointer = Pointer(ByteHelpers.littleEndianByteConversion(result.data))
                  logger.info { "Shifting pointer to $pointer" }
                  pointer
                }
                is MemoryResult.KernelError -> Pointer(0)
                MemoryResult.ProcessNotFound -> Pointer(0)
                MemoryResult.ReadViolation -> Pointer(0)
              }
            }
            .share(path.finalOffset))
  }

  override fun lastUpdatedAt(): Instant {
    return Clock.System.now()
  }
}
