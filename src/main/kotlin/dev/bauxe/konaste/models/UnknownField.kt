package dev.bauxe.konaste.models

import kotlinx.serialization.Serializable

@Serializable
data class UnknownField(
    val asInt1: Int,
    val asInt2: Int,
    val asLong: Long,
    val asFloat1: Float,
    val asFloat2: Float,
    val asDouble: Double,
    val asString: String
) {
  companion object {
    fun fromInt(i: Int): UnknownField =
        UnknownField(
            i,
            0,
            i.toLong(),
            i.toFloat(),
            0.0f,
            i.toDouble(),
            ByteArray(Int.SIZE_BYTES) { n -> (i shr n * 8).toByte() }.decodeToString())

    fun fromLong(l: Long): UnknownField =
        UnknownField(
            l.toInt(),
            l.shr(8 * Int.SIZE_BYTES).toInt(),
            l,
            l.toInt().toFloat(),
            l.shr(8 * Int.SIZE_BYTES).toInt().toFloat(),
            l.toDouble(),
            ByteArray(Long.SIZE_BYTES) { n -> (l shr n * 8).toByte() }.decodeToString())
  }
}
