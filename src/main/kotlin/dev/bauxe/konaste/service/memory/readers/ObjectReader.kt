package dev.bauxe.konaste.service.memory.readers

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.utils.ByteArrayReader

class ObjectReader(private val memoryClient: MemoryClient) {
  fun <T> fromPointer(
      pointer: Pointer,
      size: Int,
      converter: (byteArrayReader: ByteArrayReader) -> T
  ): ObjectReaderResult<T> {
    return when (val result = memoryClient.read(pointer, size)) {
      is MemoryResult.KernelError -> ObjectReaderResult.Fail
      is MemoryResult.Ok -> ObjectReaderResult.Ok<T>(converter.invoke(ByteArrayReader(result.data)))
      MemoryResult.ReadViolation -> ObjectReaderResult.Fail
      MemoryResult.ProcessNotFound -> ObjectReaderResult.Fail
    }
  }
}

sealed class ObjectReaderResult<out T> {
  data class Ok<T>(val result: T) : ObjectReaderResult<T>()

  data object Fail : ObjectReaderResult<Nothing>()
}
