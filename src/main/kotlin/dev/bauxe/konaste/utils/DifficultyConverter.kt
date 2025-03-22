package dev.bauxe.konaste.utils

import dev.bauxe.konaste.models.songs.ChartDifficulty

class DifficultyConverter {
  companion object {
    fun convertDifficulty(chartDifficulty: Int): ChartDifficulty {
      return ChartDifficulty.entries[chartDifficulty]
    }

    fun convertDifficulty(difficultyId: Int, infType: Int): ChartDifficulty {
      return when (difficultyId) {
        0 -> ChartDifficulty.NOVICE
        1 -> ChartDifficulty.ADVANCED
        2 -> ChartDifficulty.EXHAUST
        4 -> ChartDifficulty.MAXIMUM
        5 ->
            when (infType) {
              2 -> ChartDifficulty.INFINITE
              3 -> ChartDifficulty.GRAVITY
              4 -> ChartDifficulty.HEAVENLY
              5 -> ChartDifficulty.VIVID
              6 -> ChartDifficulty.EXCEED
              else -> ChartDifficulty.UNKNOWN
            }
        else -> ChartDifficulty.UNKNOWN
      }
    }

    fun convertDifficulty(difficultyName: String): ChartDifficulty {
      return when (difficultyName.lowercase()) {
        "novice" -> ChartDifficulty.NOVICE
        "advanced" -> ChartDifficulty.ADVANCED
        "exhaust" -> ChartDifficulty.EXHAUST
        "maximium" -> ChartDifficulty.MAXIMUM
        "infinite" -> ChartDifficulty.INFINITE
        "gravity" -> ChartDifficulty.GRAVITY
        "heavenly" -> ChartDifficulty.HEAVENLY
        "vivid" -> ChartDifficulty.VIVID
        "exceed" -> ChartDifficulty.EXCEED
        else -> ChartDifficulty.UNKNOWN
      }
    }
  }
}
