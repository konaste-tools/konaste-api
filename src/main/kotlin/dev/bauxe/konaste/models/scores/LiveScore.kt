package dev.bauxe.konaste.models.scores

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.models.UnknownField
import dev.bauxe.konaste.utils.ByteArrayReader
import kotlinx.serialization.Serializable

@Serializable
data class NowPlayingPointInTime(
    val chartMaxEx: Int,
    val chartMaxCombo: Int,
    val currentNoteCount: Long,
    val maxCombo: Int,
    val score: Int,
    val maxPossibleScore: Int,
    val ex: Int,
    val missedEx: Int,
    val combo: Int,
    val timestamp: Float,
)

@Serializable
data class NowPlayingData(
    val unk1: Int,
    val maxEx: Int,
    val maxCombo: Int,
    val unk2: Int,
    val scoreData: List<LiveScore>,
)

data class NowPlayingMemoryData(
    val unk1: Int,
    val maxEx: Int,
    val maxCombo: Int,
    val unk2: UnknownField,
    val baseScoreData: Pointer,
    val unk3: UnknownField,
    val nextScoreData: Pointer,
    val unk4: UnknownField,
) {
  companion object : ConvertableFromByteReader<NowPlayingMemoryData> {
    // full size might be 416
    const val SIZE = 32

    override fun size(): Int = UserHighscore.SIZE

    override fun fromByteReader(data: ByteArrayReader): NowPlayingMemoryData {
      return NowPlayingMemoryData(
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextUnknownInt(),
          Pointer(data.nextInt().toLong()),
          data.nextUnknownInt(),
          Pointer(data.nextInt().toLong()),
          data.nextUnknownInt())
    }
  }
}

@Serializable
data class LiveScore(
    val maybeTimestamp: Float,
    val combo: Int,
    val comboSequence: Int, // increments every miss after a combo
    val maxCombo: Int,
    val unk2: UnknownField,
    val unk3: UnknownField,
    val score: Int,
    val maxPossibleScore: Int,
    val exScore: Int,
    val missedExScore: Int,
    val unk5: UnknownField,
    val unk6: UnknownField,
    val unk7: UnknownField,
) {
  companion object : ConvertableFromByteReader<LiveScore> {
    const val SIZE = 52

    override fun size(): Int = UserHighscore.SIZE

    override fun fromByteReader(data: ByteArrayReader): LiveScore {
      return LiveScore(
          data.nextFloat(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextUnknownInt(),
          data.nextUnknownInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextUnknownInt(),
          data.nextUnknownInt(),
          data.nextUnknownInt())
    }
  }
}
