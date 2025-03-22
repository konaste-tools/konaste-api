package dev.bauxe.konaste.controller.game.models

import dev.bauxe.konaste.models.music.Music
import kotlinx.serialization.Serializable

@Serializable
data class SongResponse(
    val id: Int,
    val minBpm: UInt,
    val maxBpm: UInt,
    val name: String,
    val artist: String,
) {
  companion object {
    fun fromMusic(id: Int, music: Music) =
        SongResponse(id, music.bpmMin, music.bpmMax, music.titleName, music.artistName)
  }
}
