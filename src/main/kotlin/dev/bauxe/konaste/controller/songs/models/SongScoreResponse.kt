package dev.bauxe.konaste.controller.songs.models

import kotlinx.serialization.Serializable

@Serializable data class SongScoreResponse(val songId: Int, val scores: List<ScoreResponse>)
