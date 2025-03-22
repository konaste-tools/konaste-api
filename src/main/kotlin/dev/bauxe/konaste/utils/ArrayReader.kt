package dev.bauxe.konaste.utils

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.models.memory.ArrayValue
import dev.bauxe.konaste.models.songs.SongMetadata
import dev.bauxe.konaste.service.memory.readers.ObjectReader
import dev.bauxe.konaste.service.memory.readers.ObjectReaderResult
import io.github.oshai.kotlinlogging.KotlinLogging

class ArrayReader(val arrayValue: ArrayValue, val objectReader: ObjectReader) {
  val logger = KotlinLogging.logger {}

  fun <T> read(converter: ConvertableFromByteReader<T>): List<T> {
    val arrayLength = arrayValue.length / converter.size()
    val remainder = arrayValue.length % converter.size()
    if (remainder > 0) {
      logger.warn { "Array size was $arrayLength, but $remainder bytes remained" }
    }

    val start = Pointer(arrayValue.startingOffset)
    return (0 ..< arrayLength)
        .map {
          objectReader.fromPointer(
              start.share(it * SongMetadata.Companion.SIZE),
              converter.size(),
              converter::fromByteReader)
        }
        .mapNotNull {
          when (it) {
            ObjectReaderResult.Fail -> null
            is ObjectReaderResult.Ok -> it.result
          }
        }
        .apply {
          if (this.size.toLong() != arrayLength) {
            logger.warn { "Array size was $arrayLength, but only read ${this.size} elements" }
          }
        }
  }
}
