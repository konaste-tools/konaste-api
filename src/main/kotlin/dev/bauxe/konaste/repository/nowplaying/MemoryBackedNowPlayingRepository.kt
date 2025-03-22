package dev.bauxe.konaste.repository.nowplaying

import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.models.info.NowPlaying
import dev.bauxe.konaste.service.memory.versions.PointerReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.utils.ByteArrayReader

class MemoryBackedNowPlayingRepository(
    val versionResolver: VersionResolver,
    val memoryClient: MemoryClient,
) : NowPlayingRepository {
  override suspend fun getNowPlaying(): NowPlaying {
    val pointer =
        when (val pointerResult = versionResolver.accept { it.getNowPlayingPath() }) {
          is PointerReadResult.Error -> return NowPlaying(-1, -1, -1)
          is PointerReadResult.Ok -> pointerResult.pointer
        }
    return when (val result = memoryClient.read(pointer, 12)) {
      is MemoryResult.KernelError -> return NowPlaying(-1, -1, -1)
      is MemoryResult.Ok -> {
        val reader = ByteArrayReader(result.data)
        NowPlaying(reader.nextInt(), reader.nextInt(), reader.nextInt())
      }
      MemoryResult.ReadViolation -> return NowPlaying(-1, -1, -1)
      MemoryResult.ProcessNotFound -> return NowPlaying(-1, -1, -1)
    }
  }
}
