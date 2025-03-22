package dev.bauxe.konaste.client.windowswrapper

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.WinDef.HMODULE
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference
import io.github.oshai.kotlinlogging.KotlinLogging

class WinPsapiWrapper(val psapi: Psapi, val kernel32: Kernel32) : PsapiWrapper {
  private val logger = KotlinLogging.logger {}

  override fun getProcModules(proc: Pointer): List<Pointer> {
    val procHandle = HANDLE()
    procHandle.pointer = proc
    var moduleList: Array<HMODULE> = Array(1024) { HMODULE() }
    val size = 4096
    var sizeNeeded =
        IntByReference(
            0,
        )
    psapi.EnumProcessModules(
        procHandle,
        moduleList,
        size,
        sizeNeeded,
    )
    // this filter is required
    return moduleList.filterNotNull().mapNotNull { it.pointer }
  }

  override fun getModuleFilename(proc: Pointer, module: Pointer?): String {
    val procHandle = HANDLE()
    procHandle.pointer = proc
    val moduleHandle =
        module?.let {
          val m = HMODULE()
          m.pointer = module
          m
        }
    val nameBytes =
        ByteArray(
            1024,
        )
    val length =
        psapi.GetModuleFileNameExA(
            procHandle,
            moduleHandle,
            nameBytes,
            1024,
        )

    return String(
        nameBytes.copyOfRange(
            0,
            length,
        ))
  }

  override fun getModuleBaseAddress(
      procPointer: Pointer,
      modulePointer: Pointer?,
  ): Pointer? {
    val procHandle = HANDLE()
    procHandle.pointer = procPointer
    val moduleHandle = HMODULE()
    moduleHandle.pointer = modulePointer
    var moduleInfo = Psapi.MODULEINFO()
    if (!psapi.GetModuleInformation(
        procHandle,
        moduleHandle,
        moduleInfo,
        moduleInfo.size(),
    )) {
      logger.warn { "Failed to get module info: ${kernel32.GetLastError()}" }
      return null
    }
    return moduleInfo.lpBaseOfDll
  }

  override fun enumerateProcesses(): List<Int> {
    val processList = IntArray(4096)
    val byteCount = IntByReference(4096)
    if (!psapi.EnumProcesses(processList, 4096, byteCount)) {
      logger.warn { "Could not enumerate processes: ${kernel32.GetLastError()}" }
      return listOf()
    }
    return processList.toList()
  }
}
