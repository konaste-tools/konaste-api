package dev.bauxe.konaste.helpers

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.testing.*
import org.koin.core.module.Module

fun TestApplicationBuilder.partial(module: Module) {
  environment { config = MapApplicationConfig("ktor.environment" to "dev") }
  install(io.ktor.server.websocket.WebSockets)
  install(ContentNegotiation) { json() }
  install(Resources)
}

class PartialTestApplication {}
