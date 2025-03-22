package dev.bauxe.konaste.helpers

import com.sun.jna.Pointer

class FakeMemoryBlock(private val startAddress: Long, private val data: ByteArray) {
  fun blockStart(): Long = startAddress

  fun blockEnd(): Long = startAddress + data.size

  fun read(pointer: Pointer, size: Int): ByteArray {
    val start = Pointer.nativeValue(pointer) - startAddress
    return data.copyOfRange(start.toInt(), start.toInt() + size)
  }
}
