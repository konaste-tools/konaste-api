package dev.bauxe.konaste.service.memory.readers

import com.sun.jna.Pointer
import dev.bauxe.konaste.helpers.FakeMemoryBlock
import dev.bauxe.konaste.helpers.FakeMemoryClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeInstanceOf

class PngReaderTest :
    FunSpec({
      test("Can parse png in memory") {
        val samplePng = PngReaderTest::class.java.getResource("/images/logo.png").readBytes()
        val memoryBlock = FakeMemoryBlock(0x10000000, samplePng)
        val memoryClient = FakeMemoryClient(listOf(memoryBlock), mapOf())
        val pngReader = PngReader(memoryClient)

        val result = pngReader.fromPointer(Pointer(0x10000000))

        result.shouldBeInstanceOf<ByteArray>()
        result.size.shouldBeEqual(samplePng.size)
        result.toList().shouldBeEqual(samplePng.toList())
      }

      test("When png magic not found, null result") {
        val samplePng = PngReaderTest::class.java.getResource("/images/logo.png").readBytes()
        val memoryBlock = FakeMemoryBlock(0x10000000, samplePng.drop(8).toByteArray())
        val memoryClient = FakeMemoryClient(listOf(memoryBlock), mapOf())
        val pngReader = PngReader(memoryClient)

        val result = pngReader.fromPointer(Pointer(0x10000000))

        result.shouldBeNull()
      }
    }) {}
