package dev.bauxe.konaste.repository.decryption.models

import kotlinx.serialization.Serializable

@Serializable
data class DecryptionConfiguration(
    val host: String,
    val port: Int,
    val ssl: Boolean,
)
