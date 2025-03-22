package dev.bauxe.konaste.repository

import dev.bauxe.konaste.models.items.Item

interface ItemRepository {
  suspend fun getItems(): List<Item>

  suspend fun hasItem(itemId: String): Boolean
}
