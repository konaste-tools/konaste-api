package dev.bauxe.konaste.controller.meta

import dev.bauxe.konaste.controller.game.models.VersionResponse
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.memory.versions.VersionResult
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.version(versionResolver: VersionResolver) {
  get({
    operationId = "version"
    summary = "Get active game version"
    description = "Identifies the current running version of Konaste"
    response {
      code(HttpStatusCode.OK) {
        description = "Returns the current version id"
        body<VersionResponse> {
          example("Version Example", { value = VersionResponse("QCV:J:B:A:2024022000") })
        }
      }
      code(HttpStatusCode.NotFound) { description = "Could not find running game version" }
    }
  }) {
    when (val version = versionResolver.getActiveVersion()) {
      VersionResult.NotFound ->
          call.respond(
              HttpStatusCode.NotFound,
          )
      is VersionResult.Ok -> {
        val versionResponse =
            VersionResponse(
                version.version.getVersion(),
            )
        call.respond(
            versionResponse,
        )
      }
    }
  }
}

fun Route.versions(versionResolver: VersionResolver) {
  get({
    operationId = "versions"
    summary = "Get available game versions"
    description = "Lists all suppported game versions. Does not guarantee feature support"
    response {
      code(HttpStatusCode.OK) {
        description = "Returns all game versions"
        body<List<String>> {
          example(
              "Versions response",
              { value = listOf("QCV:J:B:A:2023122003", "QCV:J:B:A:2024022000") })
        }
      }
    }
  }) {
    call.respond(
        versionResolver.getSupportedVersions(),
    )
  }
}
