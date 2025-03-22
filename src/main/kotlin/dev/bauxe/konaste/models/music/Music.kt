package dev.bauxe.konaste.models.music

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.serialization.Serializable

@Serializable
data class Music(
    val label: String,
    val titleName: String,
    val titleYomigana: String,
    val artistName: String,
    val artistYomigana: String,
    val ascii: String,
    val bpmMax: UInt,
    val bpmMin: UInt,
    val distributionDate: UInt,
    val volume: UShort,
    val bgNo: UShort,
    val genre: UInt,
    val isFixed: UByte,
    val version: UByte,
    val demoPri: Byte,
    val filenameObfuscatedPath: String,
    val infVer: UByte,
    val difficulties: List<Difficulty>
) {
  companion object {
    fun fromObjectMapper(mapper: JsonNode): Music {
      val infoFragment = mapper["info"]
      return Music(
          infoFragment["label"].asText(),
          infoFragment["title_name"].asText(),
          infoFragment["title_yomigana"].asText(),
          infoFragment["artist_name"].asText(),
          infoFragment["artist_yomigana"].asText(),
          infoFragment["ascii"].asText(),
          infoFragment["bpm_max"][""].asInt().toUInt(),
          infoFragment["bpm_min"][""].asInt().toUInt(),
          infoFragment["bpm_max"][""].asInt().toUInt(),
          infoFragment["volume"][""].asInt().toUShort(),
          infoFragment["bg_no"][""].asInt().toUShort(),
          infoFragment["genre"][""].asInt().toUInt(),
          infoFragment["is_fixed"][""].asInt().toUByte(),
          infoFragment["version"][""].asInt().toUByte(),
          infoFragment["demo_pri"][""].asInt().toByte(),
          infoFragment["filename_obfuscated_path"].asText(),
          infoFragment["inf_ver"][""].asInt().toUByte(),
          listOf("novice", "advanced", "exhaust", "infinite", "maximum").map {
            Difficulty.fromObjectMapper(it, mapper["difficulty"][it])
          })
    }
  }
}
