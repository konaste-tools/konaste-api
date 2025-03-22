package dev.bauxe.konaste.controller.songs.models

import dev.bauxe.konaste.models.scores.UserHighscore
import kotlin.math.max
import kotlinx.serialization.Serializable

@Serializable
data class UserScoreResponse(
    val difficulty: Int,
    val konasteScore: Int,
    val konasteEx: Int,
    val konasteClearType: Int,
    val konasteGrade: Int,
    val maxChain: Int,
    val timestamp: Int,
    val arcadeScore: Int,
    val arcadeEx: Int,
    val arcadeClearType: Int,
    val arcadeGrade: Int,
) {
  companion object {
    fun fromUserScore(score: UserHighscore): UserScoreResponse =
        UserScoreResponse(
            score.difficulty,
            score.score,
            score.exScore,
            score.clearType,
            score.grade,
            score.maxChain,
            score.timestampInSeconds,
            max(score.oldArcadeScore, score.arcadeScore),
            score.arcadeEx,
            max(score.oldArcadeClearMark, score.arcadeClearMark),
            max(score.oldArcadeGrade, score.arcadeGrade))
  }
}
