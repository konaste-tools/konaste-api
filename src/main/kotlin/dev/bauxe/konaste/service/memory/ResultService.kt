package dev.bauxe.konaste.service.memory

import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.models.scores.ResultScreen
import dev.bauxe.konaste.models.scores.ResultScreenData
import dev.bauxe.konaste.service.ImageSize
import dev.bauxe.konaste.service.SongService
import dev.bauxe.konaste.service.memory.versions.PointerReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.polling.GameWindowPoller
import dev.bauxe.konaste.utils.ByteArrayReader
import dev.bauxe.konaste.utils.ClearMarkConverter
import dev.bauxe.konaste.utils.DifficultyConverter
import dev.bauxe.konaste.utils.GradeConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System

class ResultService(
    context: CoroutineContext,
    private val versionResolver: VersionResolver,
    private val memoryClient: MemoryClient,
    private val gameWindowPoller: GameWindowPoller,
    private val songService: SongService,
    private val enableDumping: Boolean,
) {
  private val logger = KotlinLogging.logger {}
  private val resultHistory: MutableList<ResultScreen> = ArrayDeque()
  private val resultHistoryData: MutableList<ResultScreenData> = ArrayDeque()
  private val scope = CoroutineScope(context)

  init {
    gameWindowPoller.addOnStart(GameWindow.UI_RESULT, this::onResultScreen)
  }

  fun getResultHistory(): List<ResultScreen> {
    return resultHistory
  }

  fun getResultHistoryData(): List<ResultScreenData> {
    return resultHistoryData
  }

  fun getResultHistory(num: Int): List<ResultScreen> {
    return resultHistory.slice(0..num).toList()
  }

  private suspend fun buildResult(screen: ResultScreenData): ResultScreen? {
    resultHistoryData.addFirst(screen)
    val song = songService.getSong(screen.songId) ?: return null
    return ResultScreen(
        song.titleName,
        song.artistName,
        DifficultyConverter.Companion.convertDifficulty(screen.difficulty, song.infVer.toInt()),
        screen.level,
        screen.score,
        screen.ex,
        screen.combo,
        screen.bestScore,
        screen.bestEx,
        song.difficulties[screen.difficulty].maxExScore,
        ClearMarkConverter.Companion.convertClearMark(screen.clearMark),
        ClearMarkConverter.Companion.convertClearMark(screen.bestClearMark),
        GradeConverter.Companion.convertGrade(screen.grade),
        GradeConverter.Companion.convertGrade(screen.bestGrade),
        songService.getSongImagePath(screen.songId, song.ascii, screen.difficulty, ImageSize.S))
  }

  fun onResultScreen() {
    scope.launch {
      delay(2500.milliseconds)
      val pointerResult = versionResolver.accept { version -> version.getResultScreenPath() }
      if (pointerResult !is PointerReadResult.Ok) return@launch
      val memoryResult = memoryClient.read(pointerResult.pointer, ResultScreenData.Companion.SIZE)
      if (memoryResult !is MemoryResult.Ok) return@launch
      if (enableDumping) {
        var file =
            File(
                "${java.lang.System.getProperty("user.dir")}/dumps/result/${System.now().toString().replace(':', '_')}.bin")
        logger.debug { "Dumping result to ${file.canonicalPath}" }
        file.parentFile.mkdirs()
        FileOutputStream(file).use { it.write(memoryResult.data) }
      }
      this@ResultService.buildResult(
              ResultScreenData.Companion.fromByteReader(ByteArrayReader(memoryResult.data)))
          ?.let { resultHistory.addFirst(it) }
    }
  }
}
