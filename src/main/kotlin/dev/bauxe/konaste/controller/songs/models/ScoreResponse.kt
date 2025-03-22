package dev.bauxe.konaste.controller.songs.models

import kotlinx.serialization.Serializable

@Serializable
data class ScoreResponse(
    val songId: Int,
    val difficulty: Int,
    val score: Int,
    val userId: String,
    val userName: String,
)
