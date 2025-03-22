package dev.bauxe.konaste.service.memory

import dev.bauxe.konaste.repository.music.MusicRepository
import dev.bauxe.konaste.service.SongService
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk

class SongResolverTest :
    FunSpec({
      val musicRepository = mockk<MusicRepository>()
      val decryptionService = mockk<DecryptionService>()

      val songService = SongService(musicRepository, decryptionService)

      test("Songs are fetched from memory client successfully") {}
    }) {}
