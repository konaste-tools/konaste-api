package dev.bauxe.konaste.models.songs

import kotlinx.serialization.Serializable

@Serializable
enum class ChartDifficulty {
  NOVICE,
  ADVANCED,
  EXHAUST,
  MAXIMUM,
  INFINITE,
  GRAVITY,
  HEAVENLY,
  VIVID,
  EXCEED,
  UNKNOWN,
}
