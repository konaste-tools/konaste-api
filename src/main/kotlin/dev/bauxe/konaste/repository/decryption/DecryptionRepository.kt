package dev.bauxe.konaste.repository.decryption

import dev.bauxe.konaste.repository.decryption.models.DecryptionConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DecryptionRepository(decryptionConfiguration: DecryptionConfiguration) {
  private val logger = KotlinLogging.logger {}
  val client = HttpClient(Apache5) { install(ContentNegotiation) { json() } }
  val proto =
      when (decryptionConfiguration.ssl) {
        true -> "https://"
        false -> "http://"
      }
  val urlBase = "$proto${decryptionConfiguration.host}:${decryptionConfiguration.port}"

  @OptIn(ExperimentalEncodingApi::class)
  suspend fun getHashedFilename(filename: String, version: String): String {
    val filenameEncoded = Base64.UrlSafe.encode(filename.toByteArray())
    val response = client.get("$urlBase/hash/$filenameEncoded?version=$version") {}
    return response.bodyAsText(Charsets.UTF_8)
  }

  @OptIn(ExperimentalEncodingApi::class)
  suspend fun decryptFile(filename: String, version: String, data: InputStream): ByteArray? {
    val filenameEncoded = Base64.UrlSafe.encode(filename.toByteArray())

    val response =
        client.post("$urlBase/decrypt/$filenameEncoded?version=$version") {
          setBody(data.toByteReadChannel())
        }
    if (response.status == HttpStatusCode.BadRequest) {
      val errorReason = response.readBytes().toString(Charsets.UTF_8)
      logger.warn { "Failed to decrypt $filename: $errorReason" }
      return null
    }
    return withContext(Dispatchers.IO) { response.bodyAsChannel().toInputStream().readBytes() }
  }
}
