package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT.HANDLE
import dev.bauxe.konaste.client.windowswrapper.Kernel32Wrapper
import dev.bauxe.konaste.client.windowswrapper.PsapiWrapper
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.memory.PointerSize
import dev.bauxe.konaste.service.polling.Poller
import dev.bauxe.konaste.utils.ByteHelpers
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

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
    context: CoroutineContext,
    pollingFrequency: Duration,
) : MemoryClient, Poller(context, pollingFrequency) {
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
    val procHandle = getStoredProcessHandle() ?: return PointerResult.NotFound
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
      val data =
          read(
              pointerPosition.share(
                  step.offset,
              ),
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
      logger.info { "Process handle found" }
      delay(10.seconds) // delay to ensure offsets are all loaded
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
