package dev.bauxe.konaste.repository.music

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import dev.bauxe.konaste.models.music.Music
import dev.bauxe.konaste.service.memory.DecryptionService
import java.nio.charset.Charset
import java.util.*

class FileSourceMusicRepository(
    private val decryptionService: DecryptionService,
) : MusicRepository {
  private val objectMapper = XmlMapper()
  var music: SortedMap<Int, Music> = sortedMapOf()

  override suspend fun getSong(songId: Int): Music? {
    return getMusic()[songId]
  }

  override suspend fun getSongs(size: Int, offset: Int): Map<Int, Music> {
    return getMusic().asIterable().drop(offset).take(size).associate { it.toPair() }
  }

  private suspend fun getMusic(): Map<Int, Music> {
    if (music.any()) {
      return music
    }
    val data =
        decryptionService
            .decryptFile("/others/music_db.xml")
            ?.toString(Charset.forName("SHIFT-JIS")) ?: return mapOf()
    val tree = objectMapper.readTree(data)

    music =
        tree
            .get("music")
            .associate { jsonNode ->
              jsonNode["id"].asInt() to Music.Companion.fromObjectMapper(jsonNode)
            }
            .toSortedMap()
    return music
  }
}
