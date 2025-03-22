package dev.bauxe.konaste.utils

import java.nio.ByteBuffer
import java.nio.ByteOrder

class ByteHelpers {
  companion object {
    fun littleEndianByteConversion(bytes: ByteArray): Long {

      val bb: ByteBuffer =
          ByteBuffer.wrap(
              bytes,
          )
      return when (bytes.size) {
        4 ->
            bb.order(
                    ByteOrder.LITTLE_ENDIAN,
                )
                .getInt()
                .toLong() and 0xFFFFFFFFL
        2 ->
            bb.order(
                    ByteOrder.LITTLE_ENDIAN,
                )
                .getShort()
                .toLong() and 0xFFFFL
        1 -> bb[0].toLong() and 0xFFL
        else ->
            bb.order(
                    ByteOrder.LITTLE_ENDIAN,
                )
                .getLong()
      }
    }
  }
}
