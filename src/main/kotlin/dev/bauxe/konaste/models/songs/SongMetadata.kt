package dev.bauxe.konaste.models.songs

import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.models.UnknownField
import dev.bauxe.konaste.utils.ByteArrayReader
import kotlinx.serialization.Serializable

@Serializable
data class SongMetadata(
    val songId: Int,
    val unk1: UnknownField, // bgnum u16
    val unk2: UnknownField,
    val unk3: UnknownField,
    val unk4: UnknownField,
    val dateAdded: Int,
    val minBpm: Int,
    val maxBpm: Int,
    val unk8: UnknownField,
    val unk9: UnknownField,
    val unk10: UnknownField,
    val unk11: UnknownField,
    val unk12: UnknownField,
    val unk13: UnknownField,
    val noviceDifficulty: Int,
    val advancedDifficulty: Int,
    val exhaustDifficulty: Int,
    val maximumDifficulty: Int,
    val extraDifficulty: Int, // 0xFFFFFFFFFFFFFFFF
    val unk18: UnknownField, // 0xFFFFFFFFFFFFFFFF
    val unk19: UnknownField, // 0xFFFFFFFFFFFFFFFF
    val unk20: UnknownField, // 0xFFFFFFFFFFFFFFFF
    val maxEx0: Int,
    val maxEx1: Int,
    val maxEx2: Int,
    val maxEx3: Int,
    val maxEx4: Int,
    val unk26: UnknownField, // 0xFFFFFFFFFFFFFFFF
    val unk27: UnknownField, // 0xFFFFFFFFFFFFFFFF
    val unk28: UnknownField,
    val unk29: UnknownField,
    val unk30: UnknownField,
    val unk31: UnknownField,
    val unk32: UnknownField,
    val unk33: UnknownField,
    val unk34: UnknownField,
    val unk35: UnknownField,
    val unk36: UnknownField,
    val unk37: UnknownField,
    val unk38: UnknownField,
    val unk39: UnknownField,
    val unk40: UnknownField,
    val unk41: UnknownField,
    val unk42: UnknownField,
    val unk43: UnknownField,
    val unk44: UnknownField,
    val unk45: UnknownField,
    val unk46: UnknownField,
    val unk47: UnknownField,
    val unk48: UnknownField,
    val unk49: UnknownField,
    val unk50: UnknownField,
    val unk51: UnknownField,
    val songCode: String, // 8 bytes
    val songFile: String, // 64 bytes
    val songName: String, // 256 bytes
    val songNameRomanized: String, // 256 bytes
    val artist: String, // 256 bytes
    val songNameJapanese: String, // 256 bytes
    val artistJapanese: String, // 256 bytes 0x00
    val guidFilename: String, // 256 bytes
    val illustrator0: String, // 256 bytes
    val illustrator1: String, // 256 bytes
    val illustrator2: String, // 256 bytes
    val illustrator3: String, // 256 bytes
    val illustrator4: String, // 256 bytes
    val effector0: String, // 256 bytes
    val effector1: String, // 256 bytes
    val effector2: String, // 256 bytes
    val effector3: String, // 256 bytes
    val effector4: String, // 256 bytes
    val unkChartMeta0: String, // 32 bytes
    val unkChartMeta1: String, // 32 bytes
    val unkChartMeta2: String, // 32 bytes
    val unkChartMeta3: String, // 32 bytes
    val unkChartMeta4: String, // 32 bytes
    val maybePadding: String, // 40 bytes
) {
  companion object : ConvertableFromByteReader<SongMetadata> {
    const val SIZE = 4608

    override fun size(): Int = SIZE

    override fun fromByteReader(data: ByteArrayReader): SongMetadata {
      return SongMetadata(
          songId = data.nextInt(),
          unk1 = data.nextUnknownInt(),
          unk2 = data.nextUnknownInt(),
          unk3 = data.nextUnknownInt(),
          unk4 = data.nextUnknownLong(),
          dateAdded = data.nextInt(),
          minBpm = data.nextInt(),
          maxBpm = data.nextInt(),
          unk8 = data.nextUnknownInt(),
          unk9 = data.nextUnknownInt(),
          unk10 = data.nextUnknownInt(),
          unk11 = data.nextUnknownInt(),
          unk12 = data.nextUnknownInt(),
          unk13 = data.nextUnknownInt(),
          noviceDifficulty = data.nextInt(),
          advancedDifficulty = data.nextInt(),
          exhaustDifficulty = data.nextInt(),
          maximumDifficulty = data.nextInt(),
          extraDifficulty = data.nextInt(),
          unk18 = data.nextUnknownInt(),
          unk19 = data.nextUnknownLong(),
          unk20 = data.nextUnknownLong(),
          maxEx0 = data.nextInt(),
          maxEx1 = data.nextInt(),
          maxEx2 = data.nextInt(),
          maxEx3 = data.nextInt(),
          maxEx4 = data.nextInt(),
          unk26 = data.nextUnknownLong(),
          unk27 = data.nextUnknownLong(),
          unk28 = data.nextUnknownInt(),
          unk29 = data.nextUnknownInt(),
          unk30 = data.nextUnknownLong(),
          unk31 = data.nextUnknownLong(),
          unk32 = data.nextUnknownInt(),
          unk33 = data.nextUnknownInt(),
          unk34 = data.nextUnknownInt(),
          unk35 = data.nextUnknownInt(),
          unk36 = data.nextUnknownInt(),
          unk37 = data.nextUnknownInt(),
          unk38 = data.nextUnknownInt(),
          unk39 = data.nextUnknownInt(),
          unk40 = data.nextUnknownInt(),
          unk41 = data.nextUnknownInt(),
          unk42 = data.nextUnknownInt(),
          unk43 = data.nextUnknownInt(),
          unk44 = data.nextUnknownInt(),
          unk45 = data.nextUnknownInt(),
          unk46 = data.nextUnknownInt(),
          unk47 = data.nextUnknownInt(),
          unk48 = data.nextUnknownInt(),
          unk49 = data.nextUnknownInt(),
          unk50 = data.nextUnknownInt(),
          unk51 = data.nextUnknownInt(),
          songCode = data.nextString(8),
          songFile = data.nextString(64),
          songName = data.nextString(256, "SHIFT-JIS"),
          songNameRomanized = data.nextString(256, "SHIFT-JIS"),
          artist = data.nextString(256, "SHIFT-JIS"),
          songNameJapanese = data.nextString(256, "SHIFT-JIS"),
          artistJapanese = data.nextString(256, "SHIFT-JIS"),
          guidFilename = data.nextString(256, "SHIFT-JIS"),
          illustrator0 = data.nextString(256, "SHIFT-JIS"),
          illustrator1 = data.nextString(256, "SHIFT-JIS"),
          illustrator2 = data.nextString(256, "SHIFT-JIS"),
          illustrator3 = data.nextString(256, "SHIFT-JIS"),
          illustrator4 = data.nextString(256, "SHIFT-JIS"),
          effector0 = data.nextString(256, "SHIFT-JIS"),
          effector1 = data.nextString(256, "SHIFT-JIS"),
          effector2 = data.nextString(256, "SHIFT-JIS"),
          effector3 = data.nextString(256, "SHIFT-JIS"),
          effector4 = data.nextString(256, "SHIFT-JIS"),
          unkChartMeta0 = data.nextString(32),
          unkChartMeta1 = data.nextString(32),
          unkChartMeta2 = data.nextString(32),
          unkChartMeta3 = data.nextString(32),
          unkChartMeta4 = data.nextString(32),
          maybePadding = data.nextString(40),
      )
    }
  }
}
