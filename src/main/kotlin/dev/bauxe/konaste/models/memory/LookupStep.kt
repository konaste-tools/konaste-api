package dev.bauxe.konaste.models.memory

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

data class LookupStep(
    val offset: Long,
    val pointerSize: PointerSize,
)

@Serializable(with = PointerSizeSerializer::class)
enum class PointerSize {
  BYTE_4,
  BYTE_8,
}

class PointerSizeSerializer : KSerializer<PointerSize> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("PointerSize", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: PointerSize) {
    when (value) {
      PointerSize.BYTE_4 -> 4
      PointerSize.BYTE_8 -> 8
    }.let { i -> encoder.encodeInt(i) }
  }

  override fun deserialize(decoder: Decoder): PointerSize {
    return when (decoder.decodeInt()) {
      4 -> PointerSize.BYTE_4
      8 -> PointerSize.BYTE_8
      else -> throw IllegalArgumentException("invalid pointer size")
    }
  }
}
