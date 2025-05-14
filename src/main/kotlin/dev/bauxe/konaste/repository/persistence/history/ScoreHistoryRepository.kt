package dev.bauxe.konaste.repository.persistence.history

import dev.bauxe.konaste.models.UnknownField
import dev.bauxe.konaste.models.scores.ResultScreenData
import dev.bauxe.konaste.repository.persistence.history.migrations.Migration1
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager
import kotlin.math.min

class ScoreHistoryRepository(database: String) {
  companion object {
    private const val CURRENT_VERSION = 1
    private val MIGRATIONS = listOf(Migration1())
    private val LOGGER = KotlinLogging.logger {}
  }

  private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:$database")

  init {
    connection.autoCommit = false
    runMigrations()
  }

  fun addRecord(data: ResultScreenData, rawData: ByteArray) {
    val sql =
        """
            INSERT INTO score_history (
                song_id, score, best_score, combo, criticals, nears, misses, health,
                level, difficulty, grade, best_grade, clear_mark, best_clear_mark,
                ex, best_ex, error_early, near_early, critical_early, s_critical,
                critical_late, near_late, error_late, s_critical_chip, critical_chip,
                near_chip, error_chip, s_critical_long, miss_long, s_critical_tsumami,
                miss_tsumami, raw_data, entity_version
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
            .trimIndent()
    connection.prepareStatement(sql).use { statement ->
      statement.setInt(1, data.songId)
      statement.setInt(2, data.score)
      statement.setInt(3, data.bestScore)
      statement.setInt(4, data.combo)
      statement.setInt(5, data.criticals)
      statement.setInt(6, data.nears)
      statement.setInt(7, data.misses)
      statement.setInt(8, data.health)
      statement.setInt(9, data.level)
      statement.setInt(10, data.difficulty)
      statement.setInt(11, data.grade)
      statement.setInt(12, data.bestGrade)
      statement.setInt(13, data.clearMark)
      statement.setInt(14, data.bestClearMark)
      statement.setInt(15, data.ex)
      statement.setInt(16, data.bestEx)
      statement.setInt(17, data.errorEarly)
      statement.setInt(18, data.nearEarly)
      statement.setInt(19, data.criticalEarly)
      statement.setInt(20, data.sCritical)
      statement.setInt(21, data.criticalLate)
      statement.setInt(22, data.nearLate)
      statement.setInt(23, data.errorLate)
      statement.setInt(24, data.sCriticalChip)
      statement.setInt(25, data.criticalChip)
      statement.setInt(26, data.nearChip)
      statement.setInt(27, data.errorChip)
      statement.setInt(28, data.sCriticalLong)
      statement.setInt(29, data.missLong)
      statement.setInt(30, data.sCriticalTsumami)
      statement.setInt(31, data.missTsumami)
      statement.setBytes(32, rawData)
      statement.setInt(33, CURRENT_VERSION)
      statement.executeUpdate()
    }
    connection.commit()
    LOGGER.info { "Stored new result in database" }
  }

  fun getRecords(limit: Int = 50): List<ResultScreenData> {
    val sql = "SELECT * FROM score_history ORDER BY id DESC LIMIT ?"
    val results = mutableListOf<ResultScreenData>()
    connection.prepareStatement(sql).use { statement ->
      statement.setInt(1, limit)
      val rs = statement.executeQuery()
      while (rs.next()) {
        results.add(
            ResultScreenData(
                long0 = UnknownField.fromLong(0),
                long1 = UnknownField.fromLong(0),
                long2 = UnknownField.fromLong(0),
                long3 = UnknownField.fromLong(0),
                long4 = UnknownField.fromLong(0),
                long5 = UnknownField.fromLong(0),
                long6 = UnknownField.fromLong(0),
                long7 = UnknownField.fromLong(0),
                long8 = UnknownField.fromLong(0),
                long9 = UnknownField.fromLong(0),
                long10 = UnknownField.fromLong(0),
                long11 = UnknownField.fromLong(0),
                long12 = UnknownField.fromLong(0),
                long13 = UnknownField.fromLong(0),
                long14 = UnknownField.fromLong(0),
                long15 = UnknownField.fromLong(0),
                songId = rs.getInt("song_id"),
                unknown1 = 0,
                score = rs.getInt("score"),
                bestScore = rs.getInt("best_score"),
                combo = rs.getInt("combo"),
                criticals = rs.getInt("criticals"),
                nears = rs.getInt("nears"),
                misses = rs.getInt("misses"),
                health = rs.getInt("health"),
                level = rs.getInt("level"),
                difficulty = rs.getInt("difficulty"),
                grade = rs.getInt("grade"),
                bestGrade = rs.getInt("best_grade"),
                int0 = UnknownField.fromInt(0),
                clearMark = rs.getInt("clear_mark"),
                bestClearMark = rs.getInt("best_clear_mark"),
                int1 = UnknownField.fromInt(0),
                long19 = UnknownField.fromLong(0),
                int2 = UnknownField.fromInt(0),
                ex = rs.getInt("ex"),
                bestEx = rs.getInt("best_ex"),
                errorEarly = rs.getInt("error_early"),
                nearEarly = rs.getInt("near_early"),
                criticalEarly = rs.getInt("critical_early"),
                sCritical = rs.getInt("s_critical"),
                criticalLate = rs.getInt("critical_late"),
                nearLate = rs.getInt("near_late"),
                errorLate = rs.getInt("error_late"),
                sCriticalChip = rs.getInt("s_critical_chip"),
                criticalChip = rs.getInt("critical_chip"),
                nearChip = rs.getInt("near_chip"),
                errorChip = rs.getInt("error_chip"),
                sCriticalLong = rs.getInt("s_critical_long"),
                missLong = rs.getInt("miss_long"),
                sCriticalTsumami = rs.getInt("s_critical_tsumami"),
                missTsumami = rs.getInt("miss_tsumami"),
                int3 = UnknownField.fromInt(0),
                long20 = UnknownField.fromLong(0),
                long21 = UnknownField.fromLong(0),
                long22 = UnknownField.fromLong(0),
                long23 = UnknownField.fromLong(0),
                long24 = UnknownField.fromLong(0),
                long25 = UnknownField.fromLong(0),
                long26 = UnknownField.fromLong(0),
                long27 = UnknownField.fromLong(0),
                long28 = UnknownField.fromLong(0),
                long29 = UnknownField.fromLong(0),
                long30 = UnknownField.fromLong(0),
                long31 = UnknownField.fromLong(0),
                long32 = UnknownField.fromLong(0),
                long33 = UnknownField.fromLong(0),
                long34 = UnknownField.fromLong(0),
                long35 = UnknownField.fromLong(0),
                long36 = UnknownField.fromLong(0),
                long37 = UnknownField.fromLong(0),
                long38 = UnknownField.fromLong(0),
                long39 = UnknownField.fromLong(0),
                long40 = UnknownField.fromLong(0),
                long41 = UnknownField.fromLong(0),
                long42 = UnknownField.fromLong(0),
                long43 = UnknownField.fromLong(0),
                long44 = UnknownField.fromLong(0),
                long45 = UnknownField.fromLong(0),
                long46 = UnknownField.fromLong(0),
                long47 = UnknownField.fromLong(0),
                long48 = UnknownField.fromLong(0),
                long49 = UnknownField.fromLong(0),
                long50 = UnknownField.fromLong(0),
                long51 = UnknownField.fromLong(0),
                long52 = UnknownField.fromLong(0),
                long53 = UnknownField.fromLong(0),
                long54 = UnknownField.fromLong(0),
                long55 = UnknownField.fromLong(0),
                long56 = UnknownField.fromLong(0),
                long57 = UnknownField.fromLong(0),
                long58 = UnknownField.fromLong(0),
                long59 = UnknownField.fromLong(0),
                long60 = UnknownField.fromLong(0),
                long61 = UnknownField.fromLong(0),
                long62 = UnknownField.fromLong(0),
                long63 = UnknownField.fromLong(0),
                long64 = UnknownField.fromLong(0),
                long65 = UnknownField.fromLong(0),
                long66 = UnknownField.fromLong(0),
                long67 = UnknownField.fromLong(0),
                long68 = UnknownField.fromLong(0)))
      }
    }
    LOGGER.info { "Loaded ${results.size} songs from database" }
    return results
  }

  fun getRecordBytes(recordId: Int): ByteArray? {
    val sql = "SELECT raw_data FROM score_history WHERE id = ?"
    connection.prepareStatement(sql).use { statement ->
      statement.setInt(1, recordId)
      val rs = statement.executeQuery()
      return if (rs.next()) rs.getBytes(1) else null
    }
  }

  fun getVersion(): Int =
      connection.createStatement().use {
        val rs = it.executeQuery("PRAGMA user_version")
        return if (rs.next()) {
          rs.getInt(1) // Column index starts at 1 in JDBC
        } else {
          -1 // Default fallback
        }
      }

  private fun runMigrations() {
    val tableVersion = getVersion()
    if (tableVersion >= MIGRATIONS.size) return
    (tableVersion until min(CURRENT_VERSION, MIGRATIONS.size)).forEach {
      MIGRATIONS[it].applyMigration(connection)
    }
  }
}
