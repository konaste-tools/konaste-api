package dev.bauxe.konaste.repository.nowplaying

import dev.bauxe.konaste.models.info.NowPlaying

interface NowPlayingRepository {
  suspend fun getNowPlaying(): NowPlaying
}
