package dev.bauxe.konaste.models.info

import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.utils.ByteArrayReader

/**
 * @param songId last selected song id
 * @param sessionPlaycount number of plays in this session a play is defined by selecting a song on
 *   the song select window - a retry does not count
 * @param difficulty last selected difficulty
 */
data class NowPlaying(val songId: Int, val sessionPlaycount: Int, val difficulty: Int) {
  companion object : ConvertableFromByteReader<NowPlaying> {
    override fun size(): Int = 12

    override fun fromByteReader(data: ByteArrayReader): NowPlaying =
        NowPlaying(
            data.nextInt(),
            data.nextInt(),
            data.nextInt(),
        )
  }
}
