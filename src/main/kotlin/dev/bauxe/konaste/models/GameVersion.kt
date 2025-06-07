package dev.bauxe.konaste.models

import dev.bauxe.konaste.models.memory.PointerSize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameVersion(
    val from: String,
    val to: String?,
    val version: String,
    val addresses: Map<AddressType, Address>
)

@Serializable data class ReducedGameVersion(val version: String, val path: Path)

@Serializable data class Address(val validated: Boolean = true, val paths: List<Path>)

@Serializable
data class Path(
    val module: String,
    val pointers: List<Pointer>,
    @SerialName("final_offset") val finalOffset: Long,
)

@Serializable
data class Pointer(
    val offset: Long,
    val size: PointerSize,
    val pattern: String? = null,
)

@Serializable
enum class AddressType() {
  @SerialName("version") VERSION,
  @SerialName("gamedirectory") GAME_DIRECTORY,
  @SerialName("activeui") ACTIVE_UI,
  @SerialName("highscores") HIGHSCORES,
  @SerialName("userscores") USER_SCORES,
  @SerialName("nowplaying") NOW_PLAYING,
  @SerialName("result") RESULT,
  @SerialName("liveplaydata") LIVE_PLAY_DATA,
  @SerialName("items") ITEMS
}
