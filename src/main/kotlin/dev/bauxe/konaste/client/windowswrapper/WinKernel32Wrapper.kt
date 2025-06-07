package dev.bauxe.konaste.client.windowswrapper

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.BaseTSD
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.platform.win32.WinNT.MEMORY_BASIC_INFORMATION
import com.sun.jna.platform.win32.WinNT.PROCESS_QUERY_INFORMATION
import com.sun.jna.platform.win32.WinNT.PROCESS_VM_READ
import com.sun.jna.ptr.IntByReference
import dev.bauxe.konaste.client.MemoryResult

class WinKernel32Wrapper(val kernel32: Kernel32) : Kernel32Wrapper {
  override fun readProcessMemory(
      handlePointer: Pointer,
      address: Pointer,
      size: Int
  ): MemoryResult {
    val handle = HANDLE()
    handle.pointer = handlePointer
    val memory = Memory(size.toLong())
    var bytesRead =
        IntByReference(
            0,
        )
    if (!kernel32.ReadProcessMemory(
        handle,
        address,
        memory,
        size,
        bytesRead,
    )) {
      return MemoryResult.KernelError(
          kernel32.GetLastError(),
      )
    }
    return MemoryResult.Ok(
        memory.getByteArray(
            0,
            size,
        ),
    )
  }

  override fun getProcessId(procHandle: Pointer): Int {
    val handle = HANDLE()
    handle.pointer = procHandle
    return kernel32.GetProcessId(handle)
  }

  override fun tryGetProcessHandle(processId: Int): Pointer? {
    return kernel32
        .OpenProcess(PROCESS_VM_READ.or(PROCESS_QUERY_INFORMATION), true, processId)
        ?.pointer
  }

  override fun openProcess(pid: Int): Pointer? {
    val processHandle =
        kernel32.OpenProcess(
            PROCESS_VM_READ.or(
                PROCESS_QUERY_INFORMATION,
            ),
            false,
            pid,
        )
    if (kernel32.GetLastError() != 0) return null
    return processHandle.pointer
  }

  override fun getLastError(): Int {
    return kernel32.GetLastError()
  }

  override fun virtualQueryEx(procHandle: Pointer, addr: Pointer): MEMORY_BASIC_INFORMATION? {
    val mbi = MEMORY_BASIC_INFORMATION()
    val hdl = HANDLE()
    hdl.pointer = procHandle

    if (kernel32.VirtualQueryEx(hdl, addr, mbi, BaseTSD.SIZE_T(mbi.size().toLong())).toInt() == 0) {
      return null
    }
    return mbi
  }
}
