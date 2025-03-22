package dev.bauxe.konaste.service.memory.versions

import dev.bauxe.konaste.models.Address

interface Version {
  fun getVersion(): String

  fun getSongCount(): Int

  fun getVersionPath(): Address

  fun getHighscoreTablePath(): Address

  fun getUserScoreTablePath(): Address

  fun getSongSelectSectionPath(): Address

  fun getNowPlayingPath(): Address

  fun getCurrentPlayDataPath(): Address

  fun getCurrentUIPath(): Address

  fun getUserInfoPath(): Address

  fun getResultScreenPath(): Address

  fun getGameDirectoryPath(): Address

  fun getItemsPath(): Address
}
