package dev.bauxe.konaste.client.windowswrapper

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryResult

interface Kernel32Wrapper {
  fun readProcessMemory(handlePointer: Pointer, address: Pointer, size: Int): MemoryResult

  fun getProcessId(procHandle: Pointer): Int

  fun tryGetProcessHandle(processId: Int): Pointer?

  fun openProcess(pid: Int): Pointer?

  fun getLastError(): Int
}
