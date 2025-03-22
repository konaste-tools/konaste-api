package dev.bauxe.konaste.repository.version

import dev.bauxe.konaste.models.GameVersion
import dev.bauxe.konaste.models.ReducedGameVersion
import dev.bauxe.konaste.repository.version.models.VersionConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VersionRepository(val versionConfiguration: VersionConfiguration) {
  private val logger = KotlinLogging.logger {}
  val client = HttpClient(Apache5) { install(ContentNegotiation) { json() } }
  val proto =
      when (versionConfiguration.ssl) {
        true -> "https://"
        false -> "http://"
      }
  val urlBase = "$proto${versionConfiguration.host}:${versionConfiguration.port}"

  suspend fun getSupportedKonasteVersions(version: String): List<ReducedGameVersion> {
    val response = client.get("$urlBase/versions/supported/$version") {}
    return withContext(Dispatchers.IO) { response.body<List<ReducedGameVersion>>() }
  }

  suspend fun getKonasteVersionDefintion(version: String): GameVersion? {
    val response = client.get("$urlBase/versions/$version")
    if (response.status == HttpStatusCode.NotFound) return null
    return withContext(Dispatchers.IO) { response.body<GameVersion>() }
  }
}
