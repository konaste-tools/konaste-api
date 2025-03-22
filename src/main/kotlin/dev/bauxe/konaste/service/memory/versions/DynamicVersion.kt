package dev.bauxe.konaste.service.memory.versions

import dev.bauxe.konaste.models.Address
import dev.bauxe.konaste.models.AddressType
import dev.bauxe.konaste.models.GameVersion

class DynamicVersion(val version: GameVersion) : Version {
  override fun getVersion(): String {
    return version.version
  }

  override fun getSongCount(): Int {
    return 3072
  }

  override fun getVersionPath(): Address {
    return version.addresses[AddressType.VERSION]!!
  }

  override fun getHighscoreTablePath(): Address {
    return version.addresses[AddressType.HIGHSCORES]!!
  }

  override fun getUserScoreTablePath(): Address {
    return version.addresses[AddressType.USER_SCORES]!!
  }

  override fun getSongSelectSectionPath(): Address {
    TODO("Not yet implemented")
  }

  override fun getNowPlayingPath(): Address {
    return version.addresses[AddressType.NOW_PLAYING]!!
  }

  override fun getCurrentPlayDataPath(): Address {
    return version.addresses[AddressType.LIVE_PLAY_DATA]!!
  }

  override fun getCurrentUIPath(): Address {
    return version.addresses[AddressType.ACTIVE_UI]!!
  }

  override fun getUserInfoPath(): Address {
    TODO("Not yet implemented")
  }

  override fun getResultScreenPath(): Address {
    return version.addresses[AddressType.RESULT]!!
  }

  override fun getGameDirectoryPath(): Address {
    return version.addresses[AddressType.GAME_DIRECTORY]!!
  }

  override fun getItemsPath(): Address {
    return version.addresses[AddressType.ITEMS]!!
  }
}
