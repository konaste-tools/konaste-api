package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE
import dev.bauxe.konaste.client.windowswrapper.Kernel32Wrapper
import dev.bauxe.konaste.client.windowswrapper.PsapiWrapper
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.memory.PointerSize
import dev.bauxe.konaste.service.composition.EventManager
import dev.bauxe.konaste.service.polling.Poller
import dev.bauxe.konaste.utils.ByteHelpers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

/**
 * [dev.bauxe.konaste.client.WindowsMemoryClient] is an interface between konaste-api Sound Voltex
 * Konaste through memory.
 *
 * @param kernel32Wrapper Wraps kernel32.dll via JNA
 * @param psapiWrapper Wraps Psapi.dll via JNA
 * @param clock System clock
 * @param context Coroutine context
 * @param pollingFrequency Frequency to run [Poller]
 */
class WindowsMemoryClient(
    private val kernel32Wrapper: Kernel32Wrapper,
    private val psapiWrapper: PsapiWrapper,
    private val clock: Clock,
    private val eventManager: EventManager,
    context: CoroutineContext,
    pollingFrequency: Duration,
) : MemoryClient, Poller(context, pollingFrequency), KoinComponent {
  private val logger = KotlinLogging.logger {}
  private var procHandle: Pointer? = null
  private var lastUpdatedAt = clock.now()

  companion object {
    /**
     * This is a set of modules consumed by konaste-api. They should be updated if other modules are
     * used in lookups.
     */
    private val IMPORTANT_MODULES = setOf("sv6c.exe", "libeacnet.dll", "avs2-core.dll")
  }

  override fun read(
      address: Pointer,
      size: Int,
  ): MemoryResult {
    val procHandle = getStoredProcessHandle() ?: return MemoryResult.ProcessNotFound
    return kernel32Wrapper.readProcessMemory(procHandle, address, size)
  }

  override fun followPath(path: Path): PointerResult {
    val procHandle = getStoredProcessHandle() ?: return PointerResult.ProcessNotFound
    val module = getProcessModuleHandle(procHandle, path.module) ?: return PointerResult.NotFound
    val moduleInfo =
        psapiWrapper.getModuleBaseAddress(procHandle, module) ?: return PointerResult.NotFound
    return followPath(path, moduleInfo)
  }

  override fun followPath(path: Path, basePointer: Pointer): PointerResult {
    var pointerPosition = basePointer
    path.pointers.forEachIndexed { index, step ->
      val readsize =
          when (step.size) {
            PointerSize.BYTE_4 -> 4
            PointerSize.BYTE_8 -> 8
          }

      val address =
          when (step.pattern) {
            null -> pointerPosition.share(step.offset)
            else -> {
              val patternAddress = scanForPattern(step.pattern.split(' '), pointerPosition)
              if (patternAddress == -1L) {
                logger.warn { "Could not match pattern ${step.pattern}" }
                return PointerResult.NotFound
              }
              Pointer(patternAddress).share(step.offset)
            }
          }
      val data =
          read(
              address,
              readsize,
          )
      val bytes =
          when (data) {
            is MemoryResult.Ok -> data.data
            is MemoryResult.KernelError -> {
              logger.warn {
                "Failed to resolve path, failed at step $index of ${path.pointers.size} (${step.offset})"
              }
              logger.trace {
                "Kernel error was ${kernel32Wrapper.getLastError()}. pointer was ${pointerPosition.share(step.offset)}. data code was ${data.code}"
              }
              return PointerResult.NotFound
            }
            is MemoryResult.ProcessNotFound -> {
              procHandle = null
              return PointerResult.NotFound
            }
            else -> return PointerResult.NotFound
          }
      pointerPosition = Pointer(ByteHelpers.Companion.littleEndianByteConversion(bytes))
    }
    return PointerResult.Ok(
        pointerPosition.share(
            path.finalOffset,
        ),
    )
  }

  private fun scanForPattern(
      pattern: List<String>,
      startingOffset: Pointer,
      useLimit: Boolean = false
  ): Long {
    val procHandle = getStoredProcessHandle() ?: return -1
    var limit = 20
    var offset = startingOffset
    var bytes = ByteArray(0)
    while (!useLimit || limit-- > 0) {
      val memoryInformation = kernel32Wrapper.virtualQueryEx(procHandle, offset) ?: return -1
      if (bytes.isEmpty()) {
        bytes = ByteArray(memoryInformation.regionSize.toInt() + pattern.size) { 0x0 }
      }

      when (val result =
          kernel32Wrapper.readProcessMemory(
              procHandle, memoryInformation.baseAddress, memoryInformation.regionSize.toInt())) {
        is MemoryResult.Ok -> result.data.copyInto(bytes, pattern.size, 0, result.data.size)
        is MemoryResult.KernelError -> return -1
        MemoryResult.ProcessNotFound -> return -1
        MemoryResult.ReadViolation -> return -1
      }

      var patternIndex = 0
      var patternStartAddress = -1L

      bytes.asIterable().forEachIndexed { index, byte ->
        if (pattern[index] == "??" || byte == pattern[index].toByte(16)) patternIndex++
        else patternIndex = 0

        if (patternIndex == pattern.size) {
          patternStartAddress =
              Pointer.nativeValue(memoryInformation.baseAddress) + index - pattern.size
          return@forEachIndexed
        }
      }
      if (patternStartAddress != -1L) {
        return patternStartAddress
      }

      offset = offset.share(memoryInformation.regionSize.toLong())
      bytes.copyOfRange(bytes.size - pattern.size, bytes.size).copyInto(bytes, 0, 0, pattern.size)
    }
    return -1
  }

  override fun lastUpdatedAt(): Instant = lastUpdatedAt

  private fun getStoredProcessHandle(): Pointer? {
    return procHandle
  }

  private fun isProcessHandleValid(procHandle: Pointer): Boolean {
    val processId = kernel32Wrapper.getProcessId(procHandle)
    if (processId == 0) return false
    val name = psapiWrapper.getModuleFilename(procHandle, null)
    return name.endsWith(
        "sv6c.exe",
    )
  }

  private fun tryGetProcessHandle(): Pointer? {
    val procId = getProcessId()
    if (procId == -1 || procId == 0) return null
    logger.info { "Successfully opened process handle" }
    return kernel32Wrapper.tryGetProcessHandle(procId)
  }

  private fun getProcessModuleHandle(
      procHandle: Pointer,
      targetModule: String,
  ): Pointer? {
    val modules = psapiWrapper.getProcModules(procHandle)
    for (module in modules) {
      if (psapiWrapper
          .getModuleFilename(procHandle, module)
          .endsWith(
              targetModule,
          )) {
        return module
      }
    }
    return null
  }

  private fun getProcessId(): Int {
    val processList = psapiWrapper.enumerateProcesses()
    for (pid in processList) {
      if (pid == 0) {
        continue
      }
      val processHandle = kernel32Wrapper.openProcess(pid) ?: continue
      val name = psapiWrapper.getModuleFilename(processHandle, null)
      if (name.endsWith("sv6c.exe")) {
        return pid
      }
    }
    return -1
  }

  /**
   * This method will poll with the frequency defined by
   * [dev.bauxe.konaste.client.WindowsMemoryClient.pollFrequency] This method will aim to keep the
   * process handle for Sound Voltex Konaste current
   */
  override suspend fun pollingFn() {
    var currentProcHandle = procHandle
    if (currentProcHandle != null && !isProcessHandleValid(currentProcHandle)) {
      eventManager.fireOnGameClose()
      logger.info { "Process handle has become invalid - trying to refresh" }
      procHandle = null
      currentProcHandle = null
      lastUpdatedAt = clock.now()
    }
    if (currentProcHandle != null) {
      return
    }
    val newProcHandle = tryGetProcessHandle()
    if (newProcHandle == null) {
      logger.debug { "Failed to resolve process handle " }
      return
    }

    if (IMPORTANT_MODULES.all { module -> getProcessModuleHandle(newProcHandle, module) != null }) {
      eventManager.fireOnGameBoot()
      procHandle = newProcHandle
      lastUpdatedAt = clock.now()
    } else {
      logger.info { "Could not load all important modules" }
    }
  }
}

sealed class ProcHandleResult {
  data class Ok(val handle: HANDLE) : ProcHandleResult()

  data object NotFound : ProcHandleResult()
}
