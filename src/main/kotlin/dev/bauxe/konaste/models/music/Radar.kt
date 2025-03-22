package dev.bauxe.konaste.models.music

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.serialization.Serializable

@Serializable
data class Radar(
    val notes: UByte,
    val peak: UByte,
    val tsumami: UByte,
    val tricky: UByte,
    val handTrip: UByte,
    val oneHand: UByte
) {
  companion object {
    fun fromObjectMapper(mapper: JsonNode): Radar {
      return Radar(
          mapper["notes"][""].asInt().toUByte(),
          mapper["peak"][""].asInt().toUByte(),
          mapper["tsumami"][""].asInt().toUByte(),
          mapper["tricky"][""].asInt().toUByte(),
          mapper["hand-trip"][""].asInt().toUByte(),
          mapper["one-hand"][""].asInt().toUByte(),
      )
    }
  }
}
