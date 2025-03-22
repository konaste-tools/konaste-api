package dev.bauxe.konaste.service.memory.versions

import dev.bauxe.konaste.models.Address
import dev.bauxe.konaste.models.Path
import dev.bauxe.konaste.models.Pointer
import dev.bauxe.konaste.models.memory.PointerSize

class Ver2025031400 : Version {
  override fun getVersion(): String = "QCV:J:B:A:2025031400"

  @PathNotValidated
  override fun getSongCount(): Int {
    return 3072
  }

  override fun getCurrentUIPath(): Address {
    return Address(true, listOf(Path("sv6c.dll", listOf(), 0x72F5B4)))
  }

  override fun getVersionPath(): Address {
    return Address(true, listOf(Path("libeacnet.dll", listOf(), 0x9f88a)))
  }

  @PathNotValidated
  override fun getHighscoreTablePath(): Address {
    return Address(
        false,
        listOf(
            Path("sv6c.exe", listOf(Pointer(0x7B36C0, PointerSize.BYTE_4)), 0xc),
        ))
  }

  override fun getUserScoreTablePath(): Address {
    return Address(
        true,
        listOf(
            Path("sv6c.exe", listOf(Pointer(0x7780F8, PointerSize.BYTE_4)), 0x8),
        ))
  }

  @PathNotValidated
  override fun getSongSelectSectionPath(): Address {
    TODO("Not yet implemented")
  }

  override fun getNowPlayingPath(): Address {
    return Address(true, listOf(Path("sv6c.exe", listOf(), 0xE3E8F0)))
  }

  override fun getResultScreenPath(): Address {

    return Address(
        true,
        listOf(
            Path(
                "avs2-core.dll",
                listOf(
                    Pointer(0xf73c0, PointerSize.BYTE_4),
                    Pointer(0x10, PointerSize.BYTE_4),
                    Pointer(0x18, PointerSize.BYTE_4),
                    Pointer(0x10, PointerSize.BYTE_4),
                    Pointer(0x230, PointerSize.BYTE_4),
                ),
                0x0)))
  }

  override fun getGameDirectoryPath(): Address {
    return Address(true, listOf(Path("sv6c.exe", listOf(), 0xE12670)))
  }

  @PathNotValidated
  override fun getUserInfoPath(): Address {

    return Address(
        true, listOf(Path("sv6c.exe", listOf(Pointer(0x791740, PointerSize.BYTE_4)), 0x0)))
  }

  override fun getCurrentPlayDataPath(): Address {

    return Address(
        true,
        listOf(
            Path(
                "avs2-core.dll",
                listOf(
                    Pointer(0xFA548, PointerSize.BYTE_4),
                    Pointer(0xA0, PointerSize.BYTE_4),
                    Pointer(0x30, PointerSize.BYTE_4),
                    Pointer(0x48, PointerSize.BYTE_4),
                ),
                0x0),
            Path(
                "avs2-core.dll",
                listOf(
                    Pointer(0xFA548, PointerSize.BYTE_4),
                    Pointer(0xB8, PointerSize.BYTE_4),
                    Pointer(0x30, PointerSize.BYTE_4),
                    Pointer(0x48, PointerSize.BYTE_4),
                ),
                0x0)))
  }

  override fun getItemsPath(): Address {
    return Address(
        true,
        listOf(
            Path("sv6c.exe", listOf(Pointer(0x7781A0, PointerSize.BYTE_4)), 0x1f8),
        ))
  }
}
