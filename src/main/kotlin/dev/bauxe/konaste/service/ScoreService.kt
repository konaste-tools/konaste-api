package dev.bauxe.konaste.service

import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.models.music.Difficulty
import dev.bauxe.konaste.models.scores.GlobalHighscore
import dev.bauxe.konaste.models.scores.UserHighscore
import dev.bauxe.konaste.repository.ItemRepository
import dev.bauxe.konaste.repository.score.MemoryBackedScoreRepository
import dev.bauxe.konaste.service.DifficultyMode.entries
import dev.bauxe.konaste.service.GradingMode.entries
import dev.bauxe.konaste.service.polling.GameWindowPoller
import dev.bauxe.konaste.utils.AggregationDirection
import dev.bauxe.konaste.utils.Table
import dev.bauxe.konaste.utils.table
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.max

class ScoreService(
    private val songService: SongService,
    private val scoreRepository: MemoryBackedScoreRepository,
    private val itemRepository: ItemRepository,
    gameWindowPoller: GameWindowPoller,
) {
  private val logger = KotlinLogging.logger {}

  init {
    gameWindowPoller.addOnEnd(GameWindow.UI_RESULT, this::onSongSelect)
  }

  suspend fun getUserHighscoreTable(): Map<Int, List<UserHighscore>> {
    return scoreRepository.loadUserHighscoreData(false).groupBy { it.songId }
  }

  suspend fun getGlobalHighscoreTable(): Map<Int, List<GlobalHighscore>> {
    return scoreRepository.loadHighscoreData().groupBy { it.songId }
  }

  suspend fun getUserHighscore(songId: Int, difficultyId: Int): UserHighscore? {
    return scoreRepository.loadUserHighscoreData().firstOrNull {
      it.songId == songId && it.difficulty == difficultyId
    }
  }

  suspend fun getGlobalHighscore(songId: Int, difficultyId: Int): GlobalHighscore? {
    return scoreRepository.loadHighscoreData().firstOrNull {
      it.songId == songId && it.difficulty == difficultyId
    }
  }

  suspend fun filterMissingItems(data: Pair<Difficulty, UserHighscore>): Boolean {
    return itemRepository.hasItem(data.first.itemId)
  }

  suspend fun getUserTable(
      difficulty: DifficultyMode,
      grade: GradingMode,
      aggregationDirection: AggregationDirection = AggregationDirection.NONE,
      columnFilterRange: IntRange,
      rowFilterRange: IntRange,
      dataFilters: List<suspend (Pair<Difficulty, UserHighscore>) -> Boolean> = listOf(),
  ): Table<Int, Int> {
    return table(getSongAndHighscoreGrouping()) {
          when (grade) {
            GradingMode.GRADE -> {
              columns(*(0..10).toList().toTypedArray())
              columnAccessor = {
                max(it.second.grade, max(it.second.oldArcadeGrade, it.second.arcadeGrade))
              }
            }
            GradingMode.CLEAR_MARK -> {
              columns(*(0..6).toList().toTypedArray())
              columnAccessor = {
                if (it.first.maxExScore == max(it.second.exScore, it.second.arcadeEx)) {
                  6
                } else {
                  max(
                      it.second.clearType,
                      max(it.second.oldArcadeClearMark, it.second.arcadeClearMark))
                }
              }
            }
          }

          when (difficulty) {
            DifficultyMode.LEVEL -> {
              rows(*(1..20).toList().toTypedArray())
              rowAccessor = { it.first.difficultyLevel.toInt() }
            }
            DifficultyMode.DIFFICULTY -> {
              rows(*(0..8).toList().toTypedArray())
              rowAccessor = { it.first.difficultyLevel.toInt() }
            }
          }

          aggregation = aggregationDirection

          columnFilter = { columnFilterRange.contains(it) }
          rowFilter = { rowFilterRange.contains(it) }

          filters = dataFilters.toMutableList()
        }
        .also {
          logger.debug {
            "Created score table with ${it.columnTitles.size} columns and ${it.rowTitles.size} rows."
          }
        }
  }

  private suspend fun getSongAndHighscoreGrouping(): List<Pair<Difficulty, UserHighscore>> {
    val songData = songService.getSongs(3000, 0)
    val highscoreData = getUserHighscoreTable()

    return songData.values
        .flatMap { it.difficulties }
        .zip(highscoreData.values.flatten())
        .filterNot { it.first.difficultyLevel == 0u.toUByte() }
  }

  private suspend fun onSongSelect() = scoreRepository.loadUserHighscoreData(true)
}

enum class DifficultyMode(val value: String) {
  LEVEL("level"),
  DIFFICULTY("difficulty");

  companion object {
    fun from(s: String): DifficultyMode? {
      return entries.firstOrNull { it.value == s }
    }
  }
}

enum class GradingMode(val value: String) {
  GRADE("grade"),
  CLEAR_MARK("clear_mark");

  companion object {
    fun from(s: String): GradingMode? {
      return entries.firstOrNull { it.value == s }
    }
  }
}
