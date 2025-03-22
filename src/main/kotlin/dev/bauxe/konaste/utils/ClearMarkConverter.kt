package dev.bauxe.konaste.utils

class ClearMarkConverter {
  companion object {
    val CLEAR_MARKS = listOf("no", "played", "comp", "ex", "uc", "puc")

    fun convertClearMark(clearMark: Int): String {
      return when (clearMark) {
        0 -> "no"
        1 -> "played"
        2 -> "comp"
        3 -> "ex"
        4 -> "uc"
        5 -> "puc"
        else -> "no"
      }
    }
  }
}
