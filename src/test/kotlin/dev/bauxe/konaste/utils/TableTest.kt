package dev.bauxe.konaste.utils

import dev.bauxe.konaste.models.music.Music
import dev.bauxe.konaste.models.scores.UserHighscore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.math.max
import kotlinx.serialization.json.Json

class TableTest :
    FunSpec({
      val scoreData =
          Json.decodeFromString<List<UserHighscore>>(
              this::class.java.getResource("/testdata/tabulation/scores.json").readText())
      val songData =
          Json.decodeFromString<Map<Int, Music>>(
              this::class.java.getResource("/testdata/tabulation/songs.json").readText())
      val zippedData =
          scoreData.zip(songData.toList().flatMap { it.second.difficulties }).map {
            it.second to it.first
          }

      test("Can create a default table") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
            }

        table.columnTitles shouldHaveSize 11
        table.rowTitles shouldHaveSize 20
        table.rows shouldHaveSize 20
        table.rows.forEach { it.data shouldHaveSize 11 }
      }

      test("Can transform a table's columns and rows") {
        val table =
            table(zippedData) {
                  columns(*(0..10).toList().toTypedArray())
                  rows(*(1..20).toList().toTypedArray())
                  columnAccessor = {
                    max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
                  }
                  rowAccessor = { it.first.difficultyLevel.toInt() }
                }
                .transform(GradeConverter::convertGrade) { it * 10 }

        table.columnTitles shouldHaveSize 11
        table.rowTitles shouldHaveSize 20
        table.rows shouldHaveSize 20
        table.rows.forEach { it.data shouldHaveSize 11 }

        table.columnTitles.map { it.title } shouldContainInOrder GradeConverter.grades
        table.rowTitles.map { it.title } shouldContainInOrder (10..200 step 10).toList()
      }

      test("Aggregating left will aggregate values left, but not vertically") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.LEFT
            }

        table.rows.first().data shouldContainInOrder
            listOf(17, 14, 12, 12, 12, 12, 12, 12, 12, 12, 12)
        table.rows.map { it.data.first() } shouldContainInOrder
            listOf(
                17,
                63,
                191,
                317,
                515,
                555,
                168,
                130,
                155,
                286,
                355,
                467,
                536,
                389,
                437,
                575,
                632,
                559,
                131,
                26)
      }

      test("Aggregating right will aggregate values right, but not vertically") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.RIGHT
            }

        table.rows.first().data shouldContainInOrder listOf(3, 5, 5, 5, 5, 5, 5, 5, 5, 17)
        table.rows.map { it.data.first() } shouldContainInOrder
            listOf(
                3,
                58,
                189,
                310,
                511,
                545,
                168,
                125,
                154,
                283,
                347,
                438,
                478,
                359,
                341,
                322,
                85,
                198,
                27,
                5)
      }

      test("Aggregating up will aggregate values up, but not horizontally") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.UP
            }

        table.rows.first().data shouldContainInOrder
            listOf(4946, 50, 4, 13, 14, 66, 114, 216, 191, 446, 444)
        table.rows.map { it.data.first() } shouldContainInOrder
            listOf(
                4946,
                4943,
                4885,
                4696,
                4386,
                3875,
                3330,
                3162,
                3037,
                2883,
                2600,
                2253,
                1815,
                1337,
                978,
                637,
                315,
                230,
                32,
                5)
      }

      test("Aggregating down will aggregate values down, but not horizontally") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.DOWN
            }

        table.rows.first().data shouldContainInOrder listOf(3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 12)
        table.rows.map { it.data.first() } shouldContainInOrder
            listOf(
                3,
                61,
                250,
                560,
                1071,
                1616,
                1784,
                1909,
                2063,
                2346,
                2693,
                3131,
                3609,
                3968,
                4309,
                4631,
                4716,
                4914,
                4941,
                4946)
      }

      test("Will leave data unsorted") {
        val table =
            table(zippedData) {
              columns(*(10 downTo 0).toList().toTypedArray())
              rows(*(20 downTo 1).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.DOWN
            }

        table.columnTitles.map { it.title } shouldContainInOrder (10 downTo 0).toList()
        table.rowTitles.map { it.title } shouldContainInOrder (20 downTo 1).toList()
      }

      test("Can re-sort columns") {
        val table =
            table(zippedData) {
              columns(*(10 downTo 0).toList().toTypedArray())
              rows(*(20 downTo 1).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              aggregation = AggregationDirection.DOWN
              columnSorter = Comparator.naturalOrder()
              rowSorter = Comparator.naturalOrder()
            }

        table.columnTitles.map { it.title } shouldContainInOrder (0..10).toList()
        table.rowTitles.map { it.title } shouldContainInOrder (1..20).toList()
      }

      test("Can filter columns") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }

              columnFilter = { it: Int -> it > 6 }
            }

        table.columnTitles.map { it.title } shouldContainInOrder (7..10).toList()
        table.rowTitles.map { it.title } shouldContainInOrder (1..20).toList()

        table.rows.first().data shouldContainInOrder listOf(0, 0, 0, 12)
        table.rowTitles.first().count shouldBe 17
      }

      test("Can still aggregate across filtered columns") {
        val table =
            table(zippedData) {
              columns(*(0..10).toList().toTypedArray())
              rows(*(1..20).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
              rowAccessor = { it.first.difficultyLevel.toInt() }
              columnFilter = { it: Int -> it > 6 }

              aggregation = AggregationDirection.LEFT
            }

        table.columnTitles.map { it.title } shouldContainInOrder (7..10).toList()
        table.rowTitles.map { it.title } shouldContainInOrder (1..20).toList()

        table.rows[16].data shouldContainInOrder listOf(547, 542, 469, 154)
        table.rowTitles.first().count shouldBe 17
      }
    })
