package dev.bauxe.konaste.models.memory

class LookupPath(
    val module: String,
    val steps: List<LookupStep>,
    val finalOffset: Long,
)
