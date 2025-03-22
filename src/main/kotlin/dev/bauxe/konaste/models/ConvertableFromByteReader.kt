package dev.bauxe.konaste.models

import dev.bauxe.konaste.utils.ByteArrayReader

interface ConvertableFromByteReader<T> {
  fun size(): Int

  fun fromByteReader(data: ByteArrayReader): T
}
