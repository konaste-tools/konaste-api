package dev.bauxe.konaste.models.scores

import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.models.UnknownField
import dev.bauxe.konaste.utils.ByteArrayReader
import kotlinx.serialization.Serializable

@Serializable
data class UserHighscore(
    val songId: Int,
    val difficulty: Int,
    val score: Int,
    val exScore: Int,
    val clearType: Int,
    val grade: Int,
    val maxChain: Int,
    val playcount: Int,
    val unk4_1: UnknownField, // critical ? + ?
    val unk4_2: UnknownField, // critical ? + ?
    val unk5: Int, // error ?
    val timestampInSeconds: Int,
    val unk6: Int, // 0x00000004
    val unk7: Int, // 0x00000b36 2870 -> 0x0000343a
    val unk8: Int, // 0x000015e0 5600 -> 0x00006644
    val unk9: Int, // 0x00001fb8 8120 -> 0x000093a8
    val unk10: Int, // 0x0
    val unk11: Int, // 0x00000532 8120 -> 0x000018e2
    val unk12: Int, // 0x00000046 70 -> 0x00000230
    val oldArcadeScore: Int,
    val oldArcadeClearMark: Int,
    val oldArcadeGrade: Int,
    val maybeOldArcadeCombo: Int,
    val maybeOldArcadePlaycount: Int,
    val arcadeScore: Int,
    val arcadeEx: Int,
    val arcadeClearMark: Int,
    val arcadeGrade: Int,
    val maybeArcadePlaycount: Int,
    val unk19: Int,
    val l6: UnknownField,
    val l7: UnknownField,
) {
  companion object : ConvertableFromByteReader<UserHighscore> {
    const val SIZE = 136

    override fun size(): Int = SIZE

    override fun fromByteReader(data: ByteArrayReader): UserHighscore {
      return UserHighscore(
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextUnknownInt(),
          data.nextUnknownInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextInt(),
          data.nextUnknownLong(),
          data.nextUnknownLong(),
      )
    }
  }
}

@Serializable
data class GlobalHighscore(
    val songId: Int,
    val difficulty: Int,
    val score: Int,
    val userId: String,
    val userName: String,
    val empty1: Long,
    val empty2: Long,
    val empty3: Long,
    val empty4: Long,
    val empty5: Long,
    val empty6: Long,
    val empty7: Long,
    val empty8: Long,
    val empty9: Long,
    val empty10: Long,
    val empty11: Long,
    val empty12: Long,
    val unknown0: Int,
    val unknown1: Int,
    val unknown2: Int,
) {
  companion object : ConvertableFromByteReader<GlobalHighscore> {
    const val SIZE = 144

    override fun size(): Int = UserHighscore.SIZE

    override fun fromByteReader(data: ByteArrayReader): GlobalHighscore {
      return GlobalHighscore(
          songId = data.nextInt(),
          difficulty = data.nextInt(),
          score = data.nextInt(),
          userId = data.nextString(16),
          userName = data.nextString(8),
          empty1 = data.nextLong(),
          empty2 = data.nextLong(),
          empty3 = data.nextLong(),
          empty4 = data.nextLong(),
          empty5 = data.nextLong(),
          empty6 = data.nextLong(),
          empty7 = data.nextLong(),
          empty8 = data.nextLong(),
          empty9 = data.nextLong(),
          empty10 = data.nextLong(),
          empty11 = data.nextLong(),
          empty12 = data.nextLong(),
          unknown0 = data.nextInt(),
          unknown1 = data.nextInt(),
          unknown2 = data.nextInt(),
      )
    }
  }
}
