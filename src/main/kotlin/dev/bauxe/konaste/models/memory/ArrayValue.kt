package dev.bauxe.konaste.models.memory

import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.utils.ByteArrayReader

/**
 * ArrayValue follows the assumption that arrays in konaste are typically structured as offset+0
 * points to the start of the array, and offset+8 points to the byte after the array. This will not
 * deal with object size, that is up to whatever reads the array data.
 */
data class ArrayValue(
    val startingOffset: Long,
    val length: Long,
) {
  companion object : ConvertableFromByteReader<ArrayValue> {
    override fun size(): Int = 16

    override fun fromByteReader(data: ByteArrayReader): ArrayValue {
      val firstPointer = data.nextLong()
      return ArrayValue(firstPointer, data.nextLong() - firstPointer)
    }
  }
}
