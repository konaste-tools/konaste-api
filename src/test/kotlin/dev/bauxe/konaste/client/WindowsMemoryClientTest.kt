package dev.bauxe.konaste.client

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.windowswrapper.Kernel32Wrapper
import dev.bauxe.konaste.client.windowswrapper.PsapiWrapper
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.memory.PointerSize
import dev.bauxe.konaste.service.composition.EventManager
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class WindowsMemoryClientTest :
    FunSpec({
      coroutineTestScope = true

      val kernel32Wrapper = mockk<Kernel32Wrapper>()
      val psapiWrapper = mockk<PsapiWrapper>()
      val clock = mockk<Clock>(relaxed = true)
      val eventManager = mockk<EventManager>()

      beforeTest { clearAllMocks() }

      fun setStandardProcessEnumeration() {

        every { clock.now() } returns Instant.fromEpochMilliseconds(0L)

        val processModule = Pointer(0x1000)
        every { psapiWrapper.enumerateProcesses() } returns listOf(1, 2, 3, 4, 5)

        every { kernel32Wrapper.openProcess(any()) } returns null
        every { kernel32Wrapper.openProcess(4) } returns processModule

        every { psapiWrapper.getModuleFilename(processModule, null) } returns "sv6c.exe"
        every { kernel32Wrapper.tryGetProcessHandle(4) } returns processModule

        every { psapiWrapper.getProcModules(processModule) } returns
            listOf(
                processModule,
                Pointer(0x4000),
                Pointer(0x5000),
            )
        every { psapiWrapper.getModuleFilename(processModule, processModule) } returns "sv6c.exe"
        every { psapiWrapper.getModuleFilename(processModule, Pointer(0x4000)) } returns
            "libeacnet.dll"
        every { psapiWrapper.getModuleFilename(processModule, Pointer(0x5000)) } returns
            "avs2-core.dll"

        every { psapiWrapper.getModuleBaseAddress(Pointer(0x1000), Pointer(0x4000)) } returns
            Pointer(0x40000)
        every { psapiWrapper.getModuleBaseAddress(Pointer(0x1000), Pointer(0x5000)) } returns
            Pointer(0x50000)

        every { eventManager.fireOnGameBoot() } just Runs
      }

      test("Will resolve process handle from initial poll") {
        val scheduler = TestCoroutineScheduler()
        setStandardProcessEnumeration()
        every { clock.now() } returnsMany
            listOf(
                Instant.fromEpochMilliseconds(0L),
                Instant.fromEpochMilliseconds(5000L),
            )

        val client =
            WindowsMemoryClient(
                kernel32Wrapper,
                psapiWrapper,
                clock,
                eventManager,
                StandardTestDispatcher(scheduler),
                5.seconds)
        client.lastUpdatedAt() shouldBe Instant.fromEpochMilliseconds(0L)

        scheduler.runCurrent()

        client.lastUpdatedAt() shouldBe Instant.fromEpochMilliseconds(5000L)
        verify(exactly = 1) { eventManager.fireOnGameBoot() }
      }

      context("WindowsMemoryClient read") {
        test("Will read from supplied memory address") {
          val scheduler = TestCoroutineScheduler()
          setStandardProcessEnumeration()

          every {
            kernel32Wrapper.readProcessMemory(Pointer(0x1000), Pointer(0x8000), 0x10)
          } returns MemoryResult.Ok(ByteArray(10, { 0 }))

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.read(Pointer(0x8000), 0x10)
          result.shouldBeInstanceOf<MemoryResult.Ok>()
          result.data.size shouldBe 10
        }

        test("Will return error if no process has been opened") {
          val scheduler = TestCoroutineScheduler()

          every { psapiWrapper.enumerateProcesses() } returns listOf()

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.read(Pointer(0x8000), 0x10)
          result.shouldBeInstanceOf<MemoryResult.ProcessNotFound>()
        }
      }

      context("WindowsMemoryClient followPath") {
        test("Will return error if no process has been opened") {
          val scheduler = TestCoroutineScheduler()
          val path = Path("", listOf(), 0x0)

          every { psapiWrapper.enumerateProcesses() } returns listOf()

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.followPath(path)
          result.shouldBeInstanceOf<PointerResult.ProcessNotFound>()
        }

        test("Will return error when path module cannot be found") {
          val scheduler = TestCoroutineScheduler()
          setStandardProcessEnumeration()

          val path = Path("unknown-module", listOf(), 0x0)

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.followPath(path)
          result.shouldBeInstanceOf<PointerResult.NotFound>()
        }

        test("Will add final offset to module address when path has no pointers") {
          val scheduler = TestCoroutineScheduler()
          setStandardProcessEnumeration()

          val path = Path("avs2-core.dll", listOf(), 0x4321)

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.followPath(path)
          result.shouldBeInstanceOf<PointerResult.Ok>()
          result.pointer shouldBe Pointer(0x54321)
        }

        test("Will follow entire path to end address") {
          val scheduler = TestCoroutineScheduler()
          setStandardProcessEnumeration()

          val path =
              Path(
                  "avs2-core.dll",
                  listOf(
                      dev.bauxe.konaste.models.Pointer(0x50, PointerSize.BYTE_4),
                      dev.bauxe.konaste.models.Pointer(0x250, PointerSize.BYTE_4),
                      dev.bauxe.konaste.models.Pointer(0x3000, PointerSize.BYTE_4)),
                  0x4321)

          every { kernel32Wrapper.readProcessMemory(Pointer(0x1000), Pointer(0x50050), 4) } returns
              MemoryResult.Ok(byteArrayOf(0x00, 0x40, 0x88.toByte(), 0x10))
          every {
            kernel32Wrapper.readProcessMemory(Pointer(0x1000), Pointer(0x10884250), 4)
          } returns
              MemoryResult.Ok(
                  byteArrayOf(0xFF.toByte(), 0xCE.toByte(), 0x88.toByte(), 0xAB.toByte()))
          every {
            kernel32Wrapper.readProcessMemory(Pointer(0x1000), Pointer(0xAB88FEFF), 4)
          } returns MemoryResult.Ok(byteArrayOf(0x01, 0x00, 0x00, 0x00))

          val client =
              WindowsMemoryClient(
                  kernel32Wrapper,
                  psapiWrapper,
                  clock,
                  eventManager,
                  StandardTestDispatcher(scheduler),
                  5.seconds)
          scheduler.runCurrent()

          val result = client.followPath(path)
          result.shouldBeInstanceOf<PointerResult.Ok>()
          result.pointer shouldBe Pointer(0x4322)
        }
      }
    }) {}
