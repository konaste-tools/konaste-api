package dev.bauxe.konaste.controller.game.models

import dev.bauxe.konaste.models.music.Difficulty
import dev.bauxe.konaste.models.music.Music
import kotlinx.serialization.Serializable

@Serializable
data class NowPlayingResponse(
    val songId: Int,
    val difficultyId: Int,
    val title: String,
    val artist: String,
    val difficulty: String,
    val infiniteVersion: UByte,
    val level: UByte,
    val notes: UByte,
    val peak: UByte,
    val tsumami: UByte,
    val tricky: UByte,
    val handTrip: UByte,
    val oneHand: UByte,
    val jacketSmall: String,
    val jacketNormal: String,
    val jacketBig: String,
) {
  companion object {
    fun fromMusic(
        id: Int,
        difficultyId: Int,
        music: Music,
        difficulty: Difficulty,
        jacketSmall: String,
        jacketNormal: String,
        jacketBig: String
    ): NowPlayingResponse {
      return NowPlayingResponse(
          id,
          difficultyId,
          music.titleName,
          music.artistName,
          difficulty.difficultyName,
          music.infVer,
          difficulty.difficultyLevel,
          difficulty.radar.notes,
          difficulty.radar.peak,
          difficulty.radar.tsumami,
          difficulty.radar.tricky,
          difficulty.radar.handTrip,
          difficulty.radar.oneHand,
          jacketSmall,
          jacketNormal,
          jacketBig)
    }
  }
}
