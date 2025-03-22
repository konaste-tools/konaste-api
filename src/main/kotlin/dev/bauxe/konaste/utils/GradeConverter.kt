package dev.bauxe.konaste.utils

class GradeConverter {
  companion object {
    val grades = listOf("no", "D", "C", "B", "A", "A+", "AA", "AA+", "AAA", "AAA+", "S")

    fun convertGrade(grade: Int): String =
        when (grade) {
          0 -> "no"
          1 -> "D"
          2 -> "C"
          3 -> "B"
          4 -> "A"
          5 -> "A+"
          6 -> "AA"
          7 -> "AA+"
          8 -> "AAA"
          9 -> "AAA+"
          10 -> "S"
          else -> "no"
        }
  }
}
