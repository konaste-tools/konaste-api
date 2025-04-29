package dev.bauxe.konaste.service.memory.versions

import com.sun.jna.Pointer
import com.typesafe.config.ConfigFactory
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.client.PointerResult
import dev.bauxe.konaste.models.Address
import dev.bauxe.konaste.models.AddressType
import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.models.ReducedGameVersion
import dev.bauxe.konaste.models.memory.LookupPath
import dev.bauxe.konaste.repository.version.VersionRepository
import dev.bauxe.konaste.service.composition.EventListener
import dev.bauxe.konaste.utils.ByteArrayReader
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

open class VersionResolver(
    private val memoryClient: MemoryClient,
    private val clock: Clock,
    private val versionRepository: VersionRepository,
    private val offline: Boolean = false,
    private val forceVersion: String?,
) : EventListener() {
  companion object {
    private val offlineVersion = Ver2025031400()
  }

  val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  private var versionAddress: Pointer? = null
  private var version: Version? = null

  private var lastUpdatedAt: Instant
  private var ready = false

  init {
    this.lastUpdatedAt = Instant.fromEpochMilliseconds(0)
  }

  private val logger = KotlinLogging.logger {}

  suspend fun getSupportedVersions(): List<String> {
    if (offline) return listOf(offlineVersion.getVersion())

    val konasteApiVersion = ConfigFactory.load().getString("version")
    return versionRepository.getSupportedKonasteVersions(konasteApiVersion).map { it.version }
  }

  suspend fun getActiveVersion(): VersionResult {
    if (!ready) return VersionResult.NotFound
    if (forceVersion != null) {
      logger.info { "Forcing version to $forceVersion" }
      if (offline) {
        return when (offlineVersion.getVersion() == forceVersion) {
          false -> VersionResult.NotFound
          true -> VersionResult.Ok(offlineVersion)
        }
      }
      return when (val v = versionRepository.getKonasteVersionDefintion(forceVersion)) {
        null -> VersionResult.NotFound
        else -> VersionResult.Ok(DynamicVersion(v))
      }
    }
    logger.trace { "Fetching active version" }
    val currentVersion = version
    if (lastUpdatedAt > memoryClient.lastUpdatedAt()) {
      return when (currentVersion) {
        null -> VersionResult.NotFound
        else -> VersionResult.Ok(currentVersion)
      }
    }
    val currentVersionAddress = versionAddress
    if (currentVersionAddress != null &&
        currentVersion != null &&
        validateVersion(currentVersion.getVersion(), currentVersionAddress)) {
      logger.trace { "Using cached version" }
      lastUpdatedAt = clock.now()
      return VersionResult.Ok(
          currentVersion,
      )
    }

    val konasteApiVersion = ConfigFactory.load().getString("version")
    val gameVersions =
        versionRepository.getSupportedKonasteVersions(ConfigFactory.load().getString("version"))
    logger.info { "Found ${gameVersions.size} versions for $konasteApiVersion" }
    for (gameVersion in gameVersions) {
      val pointer =
          when (val pointerResult =
              getVersionPointer(
                  gameVersion,
              )) {
            PointerResult.ProcessNotFound -> continue
            is PointerResult.NotFound -> {
              logger.trace { "No pointer found for ${gameVersion.version}" }
              continue
            }
            is PointerResult.Ok -> pointerResult.pointer
          }
      val versionResult =
          validateVersion(
              gameVersion.version,
              pointer,
          )
      when (versionResult) {
        true -> {
          logger.info { "Version ${gameVersion.version} matched" }
          lastUpdatedAt = clock.now()
          val gameVersion =
              versionRepository.getKonasteVersionDefintion(gameVersion.version) ?: continue
          val newVersion = DynamicVersion(gameVersion)
          version = newVersion
          versionAddress = pointer
          return VersionResult.Ok(
              newVersion,
          )
        }
        false -> {
          logger.debug { "Version ${gameVersion.version} was not matched" }
          continue
        }
      }
    }
    logger.warn {
      "Could not identify game version supported by konaste-api version $konasteApiVersion"
    }
    lastUpdatedAt = clock.now()
    return VersionResult.NotFound
  }

  /**
   * Resolves a PointerReadResult off an arbitrary lookup path
   *
   * This method is intended to reduce a regular pattern of resolve version -> follow path ->
   * resolve pointer Instead, this will perform all lookups automatically, with the limitation of
   * reduced opportunity to handle errors. If you wish to manually handle a version resolution
   * failure, or a pointer result failure, this should not be used. However, such error reasons are
   * present in [PointerReadResult.Error]
   *
   * @param call the predicate to apply to a [Version] - must return a [LookupPath]
   * @return [PointerReadResult.Ok] containing the pointer, if successful. Otherwise, returns a
   *   [PointerReadResult.Error] with the error reason contained.
   */
  suspend fun accept(call: (Version) -> Address): PointerReadResult {
    val version =
        when (val versionResult = getActiveVersion()) {
          VersionResult.NotFound -> return PointerReadResult.Error(ErrorReason.VERSION_NOT_RESOLVED)
          is VersionResult.Ok -> versionResult.version
        }
    call(version).paths.map { path ->
      when (val pathResult = memoryClient.followPath(path)) {
        PointerResult.NotFound -> {}
        is PointerResult.Ok -> return PointerReadResult.Ok(pathResult.pointer)
        PointerResult.ProcessNotFound -> {}
      }
    }
    return PointerReadResult.Error(ErrorReason.MEMORY_PATH_FAILURE)
  }

  suspend fun testNewVersion(newVersion: String, comparison: String): Map<String, String> {
    return mapOf()
    logger.info { "Testing paths for: $newVersion" }
    val result = mutableMapOf<String, String>()
    val comparisonGameVersion =
        versionRepository.getKonasteVersionDefintion(comparison) ?: return result
    val reducedGameVersion =
        ReducedGameVersion(
            comparisonGameVersion.version,
            comparisonGameVersion.addresses[AddressType.VERSION]!!.paths[0])
    when (val versionPointer = getVersionPointer(reducedGameVersion)) {
      PointerResult.NotFound -> {
        logger.warn { "Could not follow version pointer" }
        result["version_pointer"] = "not_found"
      }
      is PointerResult.Ok -> {
        logger.info {
          "Validation attempt for version string, result was: ${validateVersion(newVersion,
     versionPointer.pointer)}"
        }
        result["version_pointer"] = "ok"
      }
      PointerResult.ProcessNotFound -> {
        logger.warn { "No sv6c.exe process" }
        result["version_pointer"] = "process_not_found"
        return result
      }
    }

    val comparisonVersion = DynamicVersion(comparisonGameVersion)
    result += testNewVersionPath(comparisonVersion, { v -> v.getCurrentUIPath() }, "current_ui")
    result += testNewVersionPath(comparisonVersion, { v -> v.getNowPlayingPath() }, "now_playing")
    result +=
        testNewVersionPath(
            comparisonVersion, { v -> v.getUserScoreTablePath() }, "user_score_table")
    result +=
        testNewVersionPath(
            comparisonVersion, { v -> v.getCurrentPlayDataPath() }, "current_play_data")
    result +=
        testNewVersionPath(comparisonVersion, { v -> v.getResultScreenPath() }, "result_screen")
    result +=
        testNewVersionPath(comparisonVersion, { v -> v.getHighscoreTablePath() }, "highscore_table")
    result += testNewVersionPath(comparisonVersion, { v -> v.getUserInfoPath() }, "user_info")
    return result
  }

  private fun testNewVersionPath(
      version: Version,
      call: (Version) -> Address,
      testName: String
  ): Pair<String, String> {
    val address =
        try {
          call(version)
        } catch (e: NotImplementedError) {
          return testName to "not_implemented"
        }
    when (memoryClient.followPath(address.paths[0])) {
      PointerResult.NotFound -> {
        logger.warn { "No pointer found for $testName" }
        return testName to "not_found"
      }
      is PointerResult.Ok -> {
        logger.info {
          "Pointer resolved for $testName - note this does not guarantee a correct pointer!"
        }
        return testName to "ok"
      }
      PointerResult.ProcessNotFound -> {
        logger.warn { "No sv6c.exe process" }
        return testName to "process_not_found"
      }
    }
  }

  private fun getVersionPointer(version: ReducedGameVersion): PointerResult {
    return memoryClient.followPath(
        version.path,
    )
  }

  private fun validateVersion(
      versionName: String,
      versionPointer: Pointer,
  ): Boolean {
    val data =
        memoryClient.read(
            versionPointer,
            versionName.length,
        )

    return when (data) {
      is MemoryResult.Ok -> {
        testVersionDataResult(
            data.data,
            versionName,
        )
      }
      is MemoryResult.KernelError -> {
        logger.warn { "Kernel error when testing current version: ${data.code}" }
        false
      }
      MemoryResult.ReadViolation -> {
        logger.warn { "Read violation when testing current version" }
        false
      }
      MemoryResult.ProcessNotFound -> {
        logger.warn { "Process not found when testing current version" }
        false
      }
    }
  }

  private fun testVersionDataResult(
      data: ByteArray,
      versionName: String,
  ): Boolean {
    return (data.decodeToString() == versionName).also { result ->
      if (!result) {
        logger.trace { "Tried to match $versionName but got ${data.decodeToString()} instead" }
      }
    }
  }

  override fun onGameBoot() {
    scope.launch {
      delay(30.seconds)
      ready = true
    }
  }

  override fun onGameClose() {
    ready = false
    versionAddress = null
    version = null
    lastUpdatedAt = clock.now()
  }
}

sealed class VersionResult {
  data class Ok(
      val version: Version,
  ) : VersionResult()

  data object NotFound : VersionResult()
}

sealed class PointerReadResult {
  data class Ok(val pointer: Pointer) : PointerReadResult()

  data class Error(val reason: ErrorReason) : PointerReadResult()

  fun readMemory(memoryClient: MemoryClient, readSize: Int): DataReadResult<ByteArrayReader> {
    val pointer =
        when (this) {
          is Error -> return DataReadResult.Error(this.reason)
          is Ok -> this.pointer
        }

    return when (val memoryResult = memoryClient.read(pointer, readSize)) {
      is MemoryResult.KernelError -> DataReadResult.Error(ErrorReason.KERNEL_ERROR)
      is MemoryResult.Ok -> DataReadResult.Ok(ByteArrayReader(memoryResult.data))
      MemoryResult.ReadViolation -> DataReadResult.Error(ErrorReason.READ_VIOLATION)
      MemoryResult.ProcessNotFound -> DataReadResult.Error(ErrorReason.PROCESS_NOT_FOUND)
    }
  }

  /**
   * Resolves an object from a [PointerReadResult]'s [PointerReadResult.Ok.pointer]
   *
   * This method is intended to chain from a [PointerReadResult], in which a [PointerReadResult.Ok]
   * will read the [Pointer] and try to deserialize as [T], returning a [DataReadResult.Ok], while a
   * [PointerReadResult.Error] will return itself as a [DataReadResult.Error].
   *
   * @param memoryClient the memory client to use for reading from a [Pointer]
   * @param converter a [ConvertableFromByteReader] for the target underlying class [T]
   * @return [DataReadResult.Ok] if successfully converted into [T]
   * @return [DataReadResult.Error] if unable to convert into [T]
   */
  fun <T> readMemory(
      memoryClient: MemoryClient,
      converter: ConvertableFromByteReader<T>
  ): DataReadResult<T> {
    return when (val result = readMemory(memoryClient, converter.size())) {
      is DataReadResult.Error -> DataReadResult.Error(result.reason)
      is DataReadResult.Ok -> DataReadResult.Ok(converter.fromByteReader(result.data))
    }
  }
}

sealed class DataReadResult<T> {
  data class Ok<T>(val data: T) : DataReadResult<T>()

  data class Error<T>(val reason: ErrorReason) : DataReadResult<T>()
}

enum class ErrorReason {
  VERSION_NOT_RESOLVED,
  MEMORY_PATH_FAILURE,
  KERNEL_ERROR,
  READ_VIOLATION,
  PROCESS_NOT_FOUND
}
