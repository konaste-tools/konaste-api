package dev.bauxe.konaste.controller.meta

import dev.bauxe.konaste.service.memory.versions.VersionResolver
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.metaRouting() {
  val versionResolver by inject<VersionResolver>()
  routing {
    route("meta") {
      route("version") { version(versionResolver) }
      route("versions") { versions(versionResolver) }
      route("uptime") {}
    }
  }
}
