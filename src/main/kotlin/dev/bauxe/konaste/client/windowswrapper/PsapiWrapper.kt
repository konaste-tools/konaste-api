package dev.bauxe.konaste.client.windowswrapper

import com.sun.jna.Pointer

interface PsapiWrapper {
  fun getProcModules(proc: Pointer): List<Pointer>

  fun getModuleFilename(proc: Pointer, module: Pointer?): String

  fun getModuleBaseAddress(
      procPointer: Pointer,
      modulePointer: Pointer?,
  ): Pointer?

  fun enumerateProcesses(): List<Int>
}
