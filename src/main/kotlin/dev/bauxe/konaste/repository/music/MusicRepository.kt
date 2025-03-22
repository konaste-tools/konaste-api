package dev.bauxe.konaste.repository.music

import dev.bauxe.konaste.models.music.Music

interface MusicRepository {
  suspend fun getSong(songId: Int): Music?

  suspend fun getSongs(size: Int, offset: Int): Map<Int, Music>
}
