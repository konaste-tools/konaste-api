package dev.bauxe.konaste.service.memory

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.models.info.NowPlaying
import dev.bauxe.konaste.models.scores.LiveScore
import dev.bauxe.konaste.models.scores.NowPlayingData
import dev.bauxe.konaste.models.scores.NowPlayingMemoryData
import dev.bauxe.konaste.models.scores.NowPlayingPointInTime
import dev.bauxe.konaste.repository.nowplaying.NowPlayingRepository
import dev.bauxe.konaste.service.memory.versions.DataReadResult
import dev.bauxe.konaste.service.memory.versions.PointerReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.polling.GameWindowPoller
import dev.bauxe.konaste.utils.ByteArrayReader
import io.github.oshai.kotlinlogging.KotlinLogging

class GameInfoService(
    private val nowPlayingRepository: NowPlayingRepository,
    private val memoryClient: MemoryClient,
    private val versionResolver: VersionResolver,
    gameWindowPoller: GameWindowPoller,
) {
  private val logger = KotlinLogging.logger {}
  private var gameWindow: GameWindow = GameWindow.UI_UNKNOWN
  private var lastPointer: PointerReadResult? = null

  init {
    gameWindowPoller.addOnChange {
      gameWindow = it
      lastPointer = null
    }
  }

  fun getGameWindow(): GameWindow {
    return gameWindow
  }

  suspend fun getNowPlaying(): NowPlaying {
    return nowPlayingRepository.getNowPlaying()
  }

  suspend fun getPlayData(): NowPlayingData? {
    val nowPlayingMemoryData = getNowPlayingMemoryData() ?: return null

    val liveScoreData =
        generateSequence(nowPlayingMemoryData.baseScoreData) {
              it.share(LiveScore.Companion.SIZE.toLong())
            }
            .takeWhile { it != nowPlayingMemoryData.nextScoreData }
            .map {
              when (val result = memoryClient.read(it, LiveScore.Companion.SIZE)) {
                is MemoryResult.KernelError -> return@map null
                is MemoryResult.Ok -> {
                  val reader = ByteArrayReader(result.data)
                  LiveScore.Companion.fromByteReader(reader)
                }
                MemoryResult.ReadViolation -> return@map null
                MemoryResult.ProcessNotFound -> return@map null
              }
            }
            .filterNotNull()

    return NowPlayingData(
        nowPlayingMemoryData.unk1,
        nowPlayingMemoryData.maxEx,
        nowPlayingMemoryData.maxCombo,
        nowPlayingMemoryData.unk2.asInt1,
        liveScoreData.toList())
  }

  suspend fun getNowPlayingPointIntime(): NowPlayingPointInTime? {
    val nowPlayingMemoryData = getNowPlayingMemoryData() ?: return null

    val liveScoreData =
        when (val result =
            memoryClient.read(
                nowPlayingMemoryData.nextScoreData.share(-LiveScore.Companion.SIZE.toLong()),
                LiveScore.Companion.SIZE)) {
          is MemoryResult.KernelError -> return null
          is MemoryResult.Ok -> {
            val reader = ByteArrayReader(result.data)
            LiveScore.Companion.fromByteReader(reader)
          }
          MemoryResult.ReadViolation -> return null
          MemoryResult.ProcessNotFound -> return null
        }

    return NowPlayingPointInTime(
        nowPlayingMemoryData.maxEx,
        nowPlayingMemoryData.maxCombo,
        ((Pointer.nativeValue(nowPlayingMemoryData.nextScoreData) -
            Pointer.nativeValue(nowPlayingMemoryData.baseScoreData)) / LiveScore.Companion.SIZE) -
            1,
        liveScoreData.maxCombo,
        liveScoreData.score,
        liveScoreData.maxPossibleScore,
        liveScoreData.exScore,
        liveScoreData.missedExScore,
        liveScoreData.combo,
        liveScoreData.maybeTimestamp)
  }

  private suspend fun getNowPlayingMemoryData(): NowPlayingMemoryData? {
    if (getGameWindow() != GameWindow.UI_SONG_PLAY) return null

    val pointer = lastPointer ?: versionResolver.accept { it.getCurrentPlayDataPath() }
    return when (val result = pointer.readMemory(memoryClient, NowPlayingMemoryData.Companion)) {
      is DataReadResult.Error -> {
        logger.error { "Failed to read play data: ${result.reason}" }
        lastPointer = null
        return null
      }
      is DataReadResult.Ok -> {
        lastPointer = pointer
        result.data
      }
    }
  }
}
