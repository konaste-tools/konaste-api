package dev.bauxe.konaste.utils

import dev.bauxe.konaste.models.UnknownField
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import kotlin.math.max
import kotlin.math.min

class ByteArrayReader(
    private val byteArray: ByteArray,
) {
  private val logger = KotlinLogging.logger {}

  private var position = 0

  fun next(num: Int): ByteArray {
    var take = num
    if (take + position > byteArray.size) {
      take = byteArray.size - position
    }
    val sub =
        byteArray.copyOfRange(
            position,
            position + take,
        )
    position += take
    return sub
  }

  fun nextInt(): Int {
    return nextInt(ByteOrder.LITTLE_ENDIAN)
  }

  fun nextInt(byteOrder: ByteOrder): Int {
    if (position + Int.SIZE_BYTES > byteArray.size) {
      logger.error {
        "Tried to take ${Int.SIZE_BYTES} bytes but only ${byteArray.size - position} were available"
      }
      return -1
    }
    val sub =
        ByteBuffer.wrap(
            byteArray.copyOfRange(
                position,
                position + Int.SIZE_BYTES,
            ),
        )
    position += Int.SIZE_BYTES
    return sub.order(
            byteOrder,
        )
        .getInt()
  }

  fun nextFloat(): Float {
    if (position + Float.SIZE_BYTES > byteArray.size) {
      logger.error {
        "Tried to take ${Float.SIZE_BYTES} bytes but only ${byteArray.size - position} were available"
      }
      return 0.0f
    }
    val sub =
        ByteBuffer.wrap(
            byteArray.copyOfRange(
                position,
                position + Float.SIZE_BYTES,
            ),
        )
    position += Float.SIZE_BYTES
    return sub.order(
            ByteOrder.LITTLE_ENDIAN,
        )
        .getFloat()
  }

  fun nextLong(): Long {
    if (position + Long.SIZE_BYTES > byteArray.size) {
      logger.error {
        "Tried to take ${Long.SIZE_BYTES} bytes but only ${byteArray.size - position} were available"
      }
      return -1
    }
    val sub =
        ByteBuffer.wrap(
            byteArray.copyOfRange(
                position,
                position + Long.SIZE_BYTES,
            ),
        )
    position += Long.SIZE_BYTES
    return sub.order(
            ByteOrder.LITTLE_ENDIAN,
        )
        .getLong()
  }

  fun nextString(length: Int): String {
    if (position + length > byteArray.size) {
      logger.error {
        "Tried to take $length bytes but only ${byteArray.size - position} were available"
      }
      return ""
    }
    val bytes = next(length)
    return bytes.decodeToString(
        endIndex =
            min(bytes.indexOfFirst { it.compareTo(0) == 0 }.takeIf { it >= 0 } ?: length, length))
  }

  fun nextString(length: Int, format: String): String {
    if (position + length > byteArray.size) {
      logger.error {
        "Tried to take $length bytes but only ${byteArray.size - position} were available"
      }
      return ""
    }
    val next = next(length)
    return String(
        next.copyOfRange(0, max(next.indexOfFirst { it.compareTo(0) == 0 }, 0)),
        Charset.forName(format))
  }

  fun nextUnknownInt(): UnknownField {
    return UnknownField.Companion.fromInt(nextInt())
  }

  fun nextUnknownLong(): UnknownField {
    return UnknownField.Companion.fromLong(nextLong())
  }

  fun skip(num: Int) {
    position += num
  }
}
