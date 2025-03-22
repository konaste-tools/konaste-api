package dev.bauxe.konaste.service.memory.readers

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.utils.ByteArrayReader
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.ByteOrder
import kotlin.math.min

class PngReader(private val memoryClient: MemoryClient) : FileReader<ByteArray> {
  companion object {
    val LOGGER = KotlinLogging.logger {}

    private val PNG_HEADER = byteArrayOf((0x89).toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)
  }

  override fun fromPointer(pointer: Pointer): ByteArray? {
    when (val result = memoryClient.read(pointer, 8)) {
      is MemoryResult.KernelError -> return null
      is MemoryResult.Ok -> {
        if (!result.data.contentEquals(PNG_HEADER)) {
          return null
        }
      }
      MemoryResult.ReadViolation -> return null
      MemoryResult.ProcessNotFound -> return null
    }
    var sectionSize = 0L
    var section = ""

    val endPointer =
        generateSequence(pointer.share(0x8L)) { it.share(sectionSize + 12) }
            .map {
              if (section == "IEND") {
                section = "OK"
                return@map it
              }
              when (val result = memoryClient.read(it, 8)) {
                is MemoryResult.Ok -> {
                  val byteArrayReader = ByteArrayReader(result.data)
                  sectionSize = byteArrayReader.nextInt(ByteOrder.BIG_ENDIAN).toLong()
                  section = byteArrayReader.nextString(4)
                }
                is MemoryResult.KernelError -> {
                  section = "TERM"
                }
                MemoryResult.ReadViolation -> {
                  section = "TERM"
                }
                MemoryResult.ProcessNotFound -> {
                  section = "TERM"
                }
              }
              LOGGER.debug {
                "Found section $section at ${Pointer.nativeValue(it) - Pointer.nativeValue(pointer)} with size $sectionSize"
              }
              it
            }
            .takeWhile { section != "TERM" && section != "OK" }
            .last()
            .share(12)
    if (section == "TERM") {
      LOGGER.error { "endPointer result ended in TERM" }
      return null
    }
    LOGGER.debug {
      "Detected length of image is ${Pointer.nativeValue(endPointer) - Pointer.nativeValue(pointer)}"
    }
    var image = byteArrayOf()
    val lastPointer =
        generateSequence(pointer) { it.share(0xFFFF) }
            .map {
              val readSize = min(0xFFFF, Pointer.nativeValue(endPointer) - Pointer.nativeValue(it))
              LOGGER.debug { "Reading $readSize bytes" }
              when (val response = memoryClient.read(it, readSize.toInt())) {
                is MemoryResult.KernelError -> return@map Pointer(Long.MAX_VALUE)
                is MemoryResult.Ok -> {
                  image += response.data
                }
                MemoryResult.ReadViolation -> return@map Pointer(Long.MAX_VALUE)
                MemoryResult.ProcessNotFound -> return@map Pointer(Long.MAX_VALUE)
              }
              it
            }
            .takeWhile { Pointer.nativeValue(endPointer) >= Pointer.nativeValue(it) + 0xFFFF }
            .last()
    if (Pointer.nativeValue(lastPointer) == Long.MAX_VALUE) {
      return null
    }

    return image
  }
}
