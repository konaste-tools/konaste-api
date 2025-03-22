package dev.bauxe.konaste.models.music

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.serialization.Serializable

@Serializable
data class Difficulty(
    val difficultyName: String,
    val difficultyLevel: UByte,
    val illustrator: String,
    val effectedBy: String,
    val price: Int,
    val itemId: String,
    val limited: UByte,
    val jacketPrint: Int,
    val jacketMask: Int,
    val maxExScore: Int,
    val radar: Radar
) {
  companion object {
    fun fromObjectMapper(difficultyName: String, mapper: JsonNode): Difficulty {
      return Difficulty(
          difficultyName,
          mapper["difnum"][""].asInt().toUByte(),
          mapper["illustrator"].asText(),
          mapper["effected_by"].asText(),
          mapper["price"][""].asInt(),
          mapper["item_id"].asText(),
          mapper["limited"][""].asInt().toUByte(),
          mapper["jacket_print"][""].asInt(),
          mapper["jacket_mask"][""].asInt(),
          mapper["max_exscore"][""].asInt(),
          Radar.fromObjectMapper(mapper["radar"]),
      )
    }
  }
}
