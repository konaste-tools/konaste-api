package dev.bauxe.konaste.service

import dev.bauxe.konaste.models.music.Music
import dev.bauxe.konaste.repository.music.MusicRepository
import dev.bauxe.konaste.service.memory.DecryptionService
import io.github.oshai.kotlinlogging.KotlinLogging

class SongService(
    private val musicRepo: MusicRepository,
    private val decryptionService: DecryptionService
) {
  private val logger = KotlinLogging.logger {}

  suspend fun getSongs(size: Int, offset: Int): Map<Int, Music> {
    return musicRepo.getSongs(size, offset)
  }

  suspend fun getSong(songId: Int): Music? {
    return musicRepo.getSong(songId)
  }

  suspend fun getSongImagePath(
      songId: Int,
      songAscii: String,
      difficultyNumber: Int,
      size: ImageSize
  ): String {
    val paddedId = songId.toString().padStart(4, '0')
    val baseImage = "/music/${paddedId}_${songAscii}/jk_${paddedId}_"
    val suffix =
        when (size) {
          ImageSize.S -> "_s"
          ImageSize.N -> ""
          ImageSize.B -> "_b"
        }.plus(".png")

    return when (decryptionService.fileExists("$baseImage${difficultyNumber}${suffix}")) {
      true -> "$baseImage${difficultyNumber}${suffix}"
      false -> "${baseImage}1${suffix}"
    }
  }

  fun findSong(searchTerm: String) {}
}

enum class ImageSize {
  S,
  N,
  B
}
