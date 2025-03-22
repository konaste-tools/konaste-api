package dev.bauxe.konaste.helpers

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.client.PointerResult
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.memory.PointerSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class FakeMemoryClientTest :
    FunSpec({
      test("Can read from a fake memory block") {
        val blocks =
            listOf(
                FakeMemoryBlock(100L, (0 ..< 100).map { it.toByte() }.toByteArray()),
            )
        val memoryClient = FakeMemoryClient(blocks, mapOf())

        val data = memoryClient.read(Pointer(110L), 8)
        data.shouldBeInstanceOf<MemoryResult.Ok>()
        data.data shouldBe byteArrayOf(0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11)
      }

      test("Can infer base address from module") {
        val blocks =
            listOf(
                FakeMemoryBlock(100L, (0 ..< 100).map { it.toByte() }.toByteArray()),
            )
        val lookupPath = Path("sv6c.exe", listOf(), 0x10L)

        val memoryClient = FakeMemoryClient(blocks, mapOf(Pair("sv6c.exe", 0x80)))
        val data = memoryClient.followPath(lookupPath)

        data.shouldBeInstanceOf<PointerResult.Ok>()
        data.pointer shouldBe Pointer(0x90)
      }

      test("Reads will fail if address is too low") {
        val blocks =
            listOf(
                FakeMemoryBlock(100L, (0 ..< 100).map { it.toByte() }.toByteArray()),
            )
        val memoryClient = FakeMemoryClient(blocks, mapOf())

        val data = memoryClient.read(Pointer(99L), 8)
        data.shouldBeInstanceOf<MemoryResult.ReadViolation>()
      }

      test("Reads will fail if read exceeds available bytes") {
        val blocks =
            listOf(
                FakeMemoryBlock(100L, (0 ..< 100).map { it.toByte() }.toByteArray()),
            )
        val memoryClient = FakeMemoryClient(blocks, mapOf())

        val data = memoryClient.read(Pointer(193L), 8)
        data.shouldBeInstanceOf<MemoryResult.ReadViolation>()
      }

      test("Reads will succeed on boundaries") {
        val blocks =
            listOf(
                FakeMemoryBlock(100L, (0 ..< 100).map { it.toByte() }.toByteArray()),
            )
        val memoryClient = FakeMemoryClient(blocks, mapOf())

        val data = memoryClient.read(Pointer(100L), 100)
        data.shouldBeInstanceOf<MemoryResult.Ok>()
      }

      test("Can traverse through memory blocks") {
        val blocks =
            listOf(
                FakeMemoryBlock(0x1000, (0 ..< 100).map { it.toByte() }.toByteArray()),
                FakeMemoryBlock(0x13121110, (0 ..< 100).map { it.toByte() }.toByteArray()),
                FakeMemoryBlock(
                    0x1d1c1b1a19181716, (500 ..< 1000).map { it.toByte() }.toByteArray()),
            )

        val memoryClient = FakeMemoryClient(blocks, mapOf(Pair("sv6c.exe", 0x1000)))

        val result =
            memoryClient.followPath(
                Path(
                    "sv6c.exe",
                    listOf(
                        dev.bauxe.konaste.models.Pointer(0x10, PointerSize.BYTE_4),
                        dev.bauxe.konaste.models.Pointer(0x16, PointerSize.BYTE_8),
                    ),
                    0x200))

        result.shouldBeInstanceOf<PointerResult.Ok>()
        result.pointer shouldBe Pointer(0x1d1c1b1a19181916)
      }
    }) {}
