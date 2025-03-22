package dev.bauxe.konaste.repository.version.models

import kotlinx.serialization.Serializable

@Serializable
data class VersionConfiguration(
    val host: String,
    val port: Int,
    val ssl: Boolean,
)
