package dev.bauxe.konaste.controller.scores.models

import dev.bauxe.konaste.service.DifficultyMode
import dev.bauxe.konaste.service.GradingMode
import dev.bauxe.konaste.utils.AggregationDirection
import io.ktor.resources.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Resource("/scores")
class Scores(val raw: Boolean = false) {

  @Resource("/table/{gradingMode}/{difficultyMode}")
  class Table(
      val parent: Scores = Scores(),
      val gradingMode: GradingMode,
      val difficultyMode: DifficultyMode,
      val aggregation: AggregationDirection = AggregationDirection.NONE,
      @Serializable(with = RangeSerializer::class) val columnRange: IntRange = 0..0,
      @Serializable(with = RangeSerializer::class) val rowRange: IntRange = 0..0,
      val ignoreMissingItems: Boolean = true,
      val scoreThresholds: String = "",
  )

  @Resource("/{songId}")
  class SongId(val parent: Scores = Scores(), val songId: Int) {
    @Resource("/difficulties/{difficultyId}")
    class Difficulty(val parent: SongId, val difficultyId: Int)
  }

  @Resource("/highscores")
  class HighScores(val parent: Scores = Scores()) {
    @Resource("/{songId}/difficulties/{difficultyId}")
    class Difficulty(val parent: HighScores = HighScores(), val songId: Int, val difficultyId: Int)
  }
}

object RangeSerializer : KSerializer<IntRange> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: IntRange) {
    encoder.encodeString("${value.first}..${value.endInclusive}")
  }

  override fun deserialize(decoder: Decoder): IntRange {
    return decoder.decodeString().split("..").map { it.toInt() }.let { it[0]..it[1] }
  }
}
