package dev.bauxe.konaste

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.bauxe.konaste.client.MacMemoryClient
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.WindowsMemoryClient
import dev.bauxe.konaste.client.windowswrapper.Kernel32Wrapper
import dev.bauxe.konaste.client.windowswrapper.PsapiWrapper
import dev.bauxe.konaste.client.windowswrapper.WinKernel32Wrapper
import dev.bauxe.konaste.client.windowswrapper.WinPsapiWrapper
import dev.bauxe.konaste.controller.debug.debugRouting
import dev.bauxe.konaste.controller.game.gameRouting
import dev.bauxe.konaste.controller.meta.metaRouting
import dev.bauxe.konaste.controller.scores.scoreRouting
import dev.bauxe.konaste.controller.songs.songRouting
import dev.bauxe.konaste.repository.ItemRepository
import dev.bauxe.konaste.repository.MemoryBackedItemRepository
import dev.bauxe.konaste.repository.decryption.DecryptionRepository
import dev.bauxe.konaste.repository.decryption.models.DecryptionConfiguration
import dev.bauxe.konaste.repository.music.FileSourceMusicRepository
import dev.bauxe.konaste.repository.music.MusicRepository
import dev.bauxe.konaste.repository.nowplaying.MemoryBackedNowPlayingRepository
import dev.bauxe.konaste.repository.nowplaying.NowPlayingRepository
import dev.bauxe.konaste.repository.persistence.history.ScoreHistoryRepository
import dev.bauxe.konaste.repository.score.MemoryBackedScoreRepository
import dev.bauxe.konaste.repository.state.GameStateRepository
import dev.bauxe.konaste.repository.state.MemoryBackedGameStateRepository
import dev.bauxe.konaste.repository.version.VersionRepository
import dev.bauxe.konaste.repository.version.models.VersionConfiguration
import dev.bauxe.konaste.service.ScoreService
import dev.bauxe.konaste.service.SongService
import dev.bauxe.konaste.service.composition.EventListener
import dev.bauxe.konaste.service.composition.EventManager
import dev.bauxe.konaste.service.memory.DecryptionService
import dev.bauxe.konaste.service.memory.GameInfoService
import dev.bauxe.konaste.service.memory.ResultService
import dev.bauxe.konaste.service.memory.readers.ObjectReader
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.polling.GameWindowPoller
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun main(args: Array<String>) {
  EngineMain.main(
      args,
  )
}

fun Application.module(moduleOverrides: Module? = null) {
  install(CORS) {
    anyHost()
    allowHeader(HttpHeaders.ContentType)
  }
  install(
      WebSockets,
  ) {
    pingPeriod = Duration.ofSeconds(15)
    timeout = Duration.ofSeconds(15)
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }
  install(Koin) {
    slf4jLogger()
    modules(
        koinModules(environment, ConfigFactory.load()),
    )
    moduleOverrides?.let { modules(it) }
  }
  install(
      ContentNegotiation,
  ) {
    json(
        Json {
          isLenient = true
          encodeDefaults = true
        },
    )
  }
  install(SwaggerUI) {
    info {
      title = "Konaste API"
      version = "latest"
      description = "API shim between web server and Konaste."
    }
    server {
      url = "http://localhost:4573"
      description = "Konaste Server"
    }
  }

  gameRouting()
  metaRouting()
  scoreRouting()
  songRouting()
  debugRouting()
  routing {
    get(
        "/",
        {
          operationId = "test"
          summary = "Verifies the server is available"
        }) {
          call.respondText(
              "Hello!",
          )
        }
    route("api.json") { openApiSpec() }
    // Create a route for the swagger-ui using the openapi-spec at "/api.json".
    route("swagger") { swaggerUI("/api.json") }
  }
}

@OptIn(ExperimentalSerializationApi::class)
fun koinModules(environment: ApplicationEnvironment, config: Config) = module {
  val logger = KotlinLogging.logger {}
  val enableDumping = System.getenv().getOrElse("ENABLE_DUMPING") { "false" }.equals("true")
  if (enableDumping) {
    logger.info { "Dumping is enabled" }
  }

  val decryptionConfiguration =
      Hocon.decodeFromConfig<DecryptionConfiguration>(config.getConfig("decrypt-api"))
  val versionConfiguration =
      Hocon.decodeFromConfig<VersionConfiguration>(config.getConfig("version-api"))
  singleOf(
      ::ScoreService,
  )
  single { DecryptionRepository(decryptionConfiguration) }
  single { VersionRepository(versionConfiguration) }
  single { ScoreHistoryRepository("scores.db") }
  singleOf(::DecryptionService)
  single { Clock.System } bind Clock::class
  singleOf(::SongService)
  singleOf(::GameInfoService)
  singleOf(::ObjectReader)
  single {
    ResultService(Dispatchers.Default, get(), get(), get(), get(), enableDumping, get())
  } bind ResultService::class bind EventListener::class

  val osName = System.getProperty("os.name").lowercase()
  when {
    "windows" in osName -> {
      single {
        WindowsMemoryClient(
            kernel32Wrapper = get(),
            psapiWrapper = get(),
            clock = get(),
            context = Dispatchers.Default,
            pollingFrequency = 15.seconds)
      } bind MemoryClient::class
      single { WinKernel32Wrapper(get()) } bind Kernel32Wrapper::class
      single { WinPsapiWrapper(get(), get()) } bind PsapiWrapper::class
      single {
        Native.load(
            "kernel32",
            Kernel32::class.java,
        )
      } bind Kernel32::class
      single {
        Native.load(
            "Psapi",
            Psapi::class.java,
        )
      } bind Psapi::class
    }
    "mac" in osName -> {
      single { MacMemoryClient() } bind MemoryClient::class
    }
    else -> {
      logger.info { "Unknown Kernel OS Name: $osName" }
    }
  }
  single {
    VersionResolver(
        get(),
        get(),
        get(),
        false,
        environment.config.propertyOrNull("konaste.version")?.getString())
  } bind VersionResolver::class bind EventListener::class
  single { GameWindowPoller(Dispatchers.Default, 50.milliseconds, get()) } bind
      GameWindowPoller::class bind
      EventListener::class
  single { FileSourceMusicRepository(get()) } bind MusicRepository::class
  single { MemoryBackedNowPlayingRepository(get(), get()) } bind NowPlayingRepository::class
  single { MemoryBackedGameStateRepository(get(), get()) } bind GameStateRepository::class
  single { MemoryBackedScoreRepository(get(), get()) } bind
      MemoryBackedScoreRepository::class bind
      EventListener::class
  single { MemoryBackedItemRepository(get(), get(), get()) } bind ItemRepository::class

  single { EventManager(getAll(EventListener::class)) } bind EventManager::class
}
