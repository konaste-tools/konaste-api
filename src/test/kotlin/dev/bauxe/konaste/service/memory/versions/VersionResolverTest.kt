package dev.bauxe.konaste.service.memory.versions

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.client.PointerResult
import dev.bauxe.konaste.models.GameVersion
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.ReducedGameVersion
import dev.bauxe.konaste.repository.version.VersionRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class VersionResolverTest :
    FunSpec({
      val memoryClient = mockk<MemoryClient>()
      val versionRepository = mockk<VersionRepository>()
      val clock = mockk<Clock>(relaxed = true)

      beforeAny { clearAllMocks() }

      test("When no active version can be found, getActiveVersion returns error") {
        every { memoryClient.followPath(any()) } returns PointerResult.NotFound
        every { clock.now() } returns Instant.DISTANT_PAST
        every { memoryClient.lastUpdatedAt() } returns Instant.DISTANT_FUTURE
        coEvery { versionRepository.getSupportedKonasteVersions("0.1.1a") } returns emptyList()

        val versionResolver =
            VersionResolver(memoryClient, clock, versionRepository, false, null)
        val result = versionResolver.getActiveVersion()

        result shouldBe VersionResult.NotFound
      }

      test("When active version found in first slot, getActiveVersion returns success") {
        val supportedVersions =
            listOf(ReducedGameVersion("KFC:TEST:1", Path("module-1", listOf(), 10)))
        val resolvedVersion = GameVersion("0.1.1", "0.1.1", "KFC:TEST:1", mapOf())

        every { clock.now() } returns Instant.DISTANT_PAST
        every { memoryClient.lastUpdatedAt() } returns Instant.DISTANT_FUTURE

        every { memoryClient.followPath(any()) } returns PointerResult.Ok(Pointer(100L))
        every { memoryClient.read(eq(Pointer(100L)), eq(10)) } returns
            MemoryResult.Ok("KFC:TEST:1".encodeToByteArray())

        coEvery { versionRepository.getSupportedKonasteVersions(eq("0.1.1a")) } returns
            supportedVersions
        coEvery { versionRepository.getKonasteVersionDefintion(eq("KFC:TEST:1")) } returns
            resolvedVersion

        val versionResolver =
            VersionResolver(memoryClient, clock, versionRepository, false, null)
        val result = versionResolver.getActiveVersion()

        result.shouldBeInstanceOf<VersionResult.Ok>().version.getVersion().shouldBe("KFC:TEST:1")
      }

      test("When last updated is after memory client last updated, do not evaluate version") {
        every { memoryClient.lastUpdatedAt() } returns
            Instant.fromEpochMilliseconds(0).minus(1.seconds)

        val versionResolver =
            VersionResolver(memoryClient, clock, versionRepository, false, null)
        val result = versionResolver.getActiveVersion()

        result.shouldBeInstanceOf<VersionResult.NotFound>()
      }

      test("When active version previously found, memory client not updated, use cached version") {
        val supportedVersions =
            listOf(ReducedGameVersion("KFC:TEST:1", Path("module-1", listOf(), 10)))
        val resolvedVersion = GameVersion("0.1.1", "0.1.1", "KFC:TEST:1", mapOf())

        val baseTimestamp = Instant.fromEpochMilliseconds(0).plus(5.seconds)
        every { clock.now() } returns baseTimestamp.plus(5.seconds)
        every { memoryClient.lastUpdatedAt() } returns baseTimestamp

        every { memoryClient.followPath(any()) } returns
            PointerResult.Ok(Pointer(100L)) andThenThrows
            RuntimeException()
        every { memoryClient.read(eq(Pointer(100L)), eq(10)) } returns
            MemoryResult.Ok("KFC:TEST:1".encodeToByteArray()) andThenThrows
            RuntimeException()

        coEvery { versionRepository.getSupportedKonasteVersions(eq("0.1.1a")) } returns
            supportedVersions andThenThrows
            RuntimeException()
        coEvery { versionRepository.getKonasteVersionDefintion(eq("KFC:TEST:1")) } returns
            resolvedVersion andThenThrows
            RuntimeException()

        val versionResolver =
            VersionResolver(memoryClient, clock, versionRepository, false, null)
        val initialResult = versionResolver.getActiveVersion()
        val nextResult = versionResolver.getActiveVersion()

        initialResult
            .shouldBeInstanceOf<VersionResult.Ok>()
            .version
            .getVersion()
            .shouldBe("KFC:TEST:1")
        nextResult
            .shouldBeInstanceOf<VersionResult.Ok>()
            .version
            .getVersion()
            .shouldBe("KFC:TEST:1")
      }
    }) {}
