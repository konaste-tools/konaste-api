package dev.bauxe.konaste.controller.game.models

import kotlinx.serialization.Serializable

@Serializable
data class SongResult(
    val songId: Int,
    val difficulty: Int,
    val songName: String,
    val artist: String,
    val score: Int,
    val ex: Int,
    val combo: Int,
) {}
