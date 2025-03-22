package dev.bauxe.konaste.service.memory

import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.repository.decryption.DecryptionRepository
import dev.bauxe.konaste.service.memory.versions.DataReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.memory.versions.VersionResult
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileInputStream

class DecryptionService(
    val versionResolver: VersionResolver,
    val decryptionRepository: DecryptionRepository,
    val memoryClient: MemoryClient
) {
  private val logger = KotlinLogging.logger {}

  suspend fun fileExists(filename: String): Boolean {
    val dataPath =
        when (val result =
            versionResolver.accept { it.getGameDirectoryPath() }.readMemory(memoryClient, 260)) {
          is DataReadResult.Error -> return false
          is DataReadResult.Ok -> {
            result.data.nextString(260).plus("game/data")
          }
        }
    val version =
        when (val result = versionResolver.getActiveVersion()) {
          VersionResult.NotFound -> return false
          is VersionResult.Ok -> result.version.getVersion()
        }
    val shortVersion = version.substringAfterLast(":", "")
    val hashedFilePath = decryptionRepository.getHashedFilename(filename, shortVersion)
    val file = File(dataPath.plus(hashedFilePath))
    return file.exists()
  }

  suspend fun decryptFile(filename: String): ByteArray? {
    val dataPath =
        when (val result =
            versionResolver.accept { it.getGameDirectoryPath() }.readMemory(memoryClient, 260)) {
          is DataReadResult.Error -> return null
          is DataReadResult.Ok -> {
            result.data.nextString(260).plus("game/data")
          }
        }
    return decryptFile(dataPath, filename)
  }

  suspend fun decryptFile(dataPath: String, filename: String): ByteArray? {
    val version =
        when (val result = versionResolver.getActiveVersion()) {
          VersionResult.NotFound -> return null
          is VersionResult.Ok -> result.version.getVersion()
        }
    val shortVersion = version.substringAfterLast(":", "")
    val hashedFilePath = decryptionRepository.getHashedFilename(filename, shortVersion)
    if (hashedFilePath.isEmpty()) {
      return null
    }
    val file = File(dataPath.plus(hashedFilePath))
    logger.info { "Decrypting ${file.path}" }
    FileInputStream(file).use {
      return decryptionRepository.decryptFile(filename, shortVersion, it)
    }
  }
}
