package repository.persistence.history

import dev.bauxe.konaste.models.UnknownField
import dev.bauxe.konaste.models.scores.ResultScreenData
import dev.bauxe.konaste.repository.persistence.history.ScoreHistoryRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.Json

@OptIn(ExperimentalUuidApi::class)
class ScoreHistoryRepositoryTest :
    FunSpec({
      val tableName = Uuid.random().toString() + ".db"

      beforeTest {}

      afterTest { File(tableName).delete() }

      // This test will change with every migration
      test("Creates default table with version of 1") {
        val repository = ScoreHistoryRepository(tableName)
        repository.getVersion() shouldBe 1
      }

      test("Inserts and retrieves same records") {
        val record1 = generateRecord(1)
        val record2 = generateRecord(2)

        val repository = ScoreHistoryRepository(tableName)
        repository.addRecord(record1, Json.encodeToString(record1).toByteArray())
        repository.addRecord(record2, Json.encodeToString(record2).toByteArray())

        repository.getRecords() shouldBe listOf(record2, record1)
      }

      test("Can retrieve raw data") {
        val record1 = generateRecord(1)

        val repository = ScoreHistoryRepository(tableName)
        repository.addRecord(record1, Json.encodeToString(record1).toByteArray())

        repository.getRecordBytes(1) shouldBe Json.encodeToString(record1).toByteArray()
      }
    }) {}

@OptIn(ExperimentalUuidApi::class)
private fun generateRecord(n: Int): ResultScreenData =
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
        songId = n,
        unknown1 = 0,
        score = 1,
        bestScore = 1,
        combo = 1,
        criticals = 1,
        nears = 1,
        misses = 1,
        health = 1,
        level = 1,
        difficulty = 1,
        grade = 1,
        bestGrade = 1,
        int0 = UnknownField.fromInt(0),
        clearMark = 1,
        bestClearMark = 1,
        int1 = UnknownField.fromInt(0),
        long19 = UnknownField.fromLong(0),
        int2 = UnknownField.fromInt(0),
        ex = 1,
        bestEx = 1,
        errorEarly = 1,
        nearEarly = 1,
        criticalEarly = 1,
        sCritical = 1,
        criticalLate = 1,
        nearLate = 1,
        errorLate = 1,
        sCriticalChip = 1,
        criticalChip = 1,
        nearChip = 1,
        errorChip = 1,
        sCriticalLong = 1,
        missLong = 1,
        sCriticalTsumami = 1,
        missTsumami = 1,
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
        long68 = UnknownField.fromLong(0))
