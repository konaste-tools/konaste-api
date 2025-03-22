package dev.bauxe.konaste.service.memory.readers

import com.sun.jna.Pointer

interface FileReader<T> {
  fun fromPointer(pointer: Pointer): T?
}
