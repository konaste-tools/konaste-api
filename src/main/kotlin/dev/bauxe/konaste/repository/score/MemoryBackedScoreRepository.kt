package dev.bauxe.konaste.repository.score

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.scores.GlobalHighscore
import dev.bauxe.konaste.models.scores.UserHighscore
import dev.bauxe.konaste.service.composition.EventListener
import dev.bauxe.konaste.service.memory.readers.ObjectReader
import dev.bauxe.konaste.service.memory.readers.ObjectReaderResult
import dev.bauxe.konaste.service.memory.versions.PointerReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.memory.versions.VersionResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryBackedScoreRepository(
    private val objectReader: ObjectReader,
    private val versionResolver: VersionResolver,
) : EventListener() {
  private val logger = KotlinLogging.logger {}
  private var userHighscore: List<UserHighscore> = listOf()
  private val userHighscoreLock: Mutex = Mutex()
  private var highscores: List<GlobalHighscore> = listOf()
  private val highscoreLock: Mutex = Mutex()

  suspend fun loadUserHighscoreData(forceRefresh: Boolean = false): List<UserHighscore> {
    userHighscoreLock.withLock {
      if (!forceRefresh && userHighscore.isNotEmpty()) {
        logger.info { "Used cached user highscore table" }
        return userHighscore
      }
      logger.info { "Loading user highscore table" }
      val songCount =
          when (val result = versionResolver.getActiveVersion()) {
            VersionResult.NotFound -> return listOf()
            is VersionResult.Ok -> result.version.getSongCount()
          }
      logger.debug { "Got song count of $songCount" }
      if (songCount == -1) {
        return listOf()
      }
      val tablePointer =
          when (val pointerResult = versionResolver.accept { it.getUserScoreTablePath() }) {
            is PointerReadResult.Error -> {
              logger.warn {
                "Failed to resolve pointer to user highscore table: ${pointerResult.reason}"
              }
              return listOf()
            }
            is PointerReadResult.Ok -> pointerResult.pointer
          }

      logger.debug { "Table start pointer is $tablePointer, trying to load ${songCount*5} results" }
      userHighscore =
          (0 ..< songCount * 5 * UserHighscore.Companion.SIZE step UserHighscore.Companion.SIZE)
              .mapNotNull {
                getUserHighscoreDataForSong(
                    tablePointer.share(it.toLong()),
                )
              }
              .filter { it.songId != -1 }
      logger.info { "Loaded ${userHighscore.size} user scores" }
      return userHighscore
    }
  }

  suspend fun loadHighscoreData(): List<GlobalHighscore> {
    highscoreLock.withLock {
      if (highscores.isNotEmpty()) {
        return highscores
      }
      logger.debug { "Loading highscore table" }

      val songCount =
          when (val result = versionResolver.getActiveVersion()) {
            VersionResult.NotFound -> return listOf()
            is VersionResult.Ok -> result.version.getSongCount()
          }
      logger.debug { "Got song count of $songCount" }
      if (songCount == -1) {
        return listOf()
      }

      val tablePointer =
          when (val pointerResult = versionResolver.accept { it.getHighscoreTablePath() }) {
            is PointerReadResult.Error -> {
              logger.warn {
                "Failed to resolve pointer to global highscore table: ${pointerResult.reason}"
              }
              return listOf()
            }
            is PointerReadResult.Ok -> pointerResult.pointer
          }

      logger.debug { "Table start pointer is $tablePointer, trying to load ${songCount*5} results" }
      highscores =
          (0 ..< songCount * 5)
              .mapNotNull {
                getHighscoreDataForSong(
                    tablePointer.share(
                        it * GlobalHighscore.Companion.SIZE.toLong(),
                    ),
                )
              }
              .filter { it.songId != -1 }
      return highscores
    }
  }

  private fun getHighscoreDataForSong(start: Pointer): GlobalHighscore? {
    return when (val result =
        objectReader.fromPointer(
            start, GlobalHighscore.Companion.SIZE, GlobalHighscore.Companion::fromByteReader)) {
      ObjectReaderResult.Fail -> null
      is ObjectReaderResult.Ok -> result.result
    }
  }

  private fun getUserHighscoreDataForSong(start: Pointer): UserHighscore? {
    return when (val result =
        objectReader.fromPointer(
            start, UserHighscore.Companion.SIZE, UserHighscore.Companion::fromByteReader)) {
      ObjectReaderResult.Fail -> null
      is ObjectReaderResult.Ok -> result.result
    }
  }

  override fun onLogin() {
    userHighscore = listOf()
    highscores = listOf()
  }
}
