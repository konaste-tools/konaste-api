package dev.bauxe.konaste.repository

import com.sun.jna.Pointer
import dev.bauxe.konaste.client.MemoryClient
import dev.bauxe.konaste.client.MemoryResult
import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.models.items.Item
import dev.bauxe.konaste.models.items.ItemsArea
import dev.bauxe.konaste.service.composition.EventListener
import dev.bauxe.konaste.service.composition.EventManager
import dev.bauxe.konaste.service.memory.versions.DataReadResult
import dev.bauxe.konaste.service.memory.versions.VersionResolver
import dev.bauxe.konaste.service.polling.GameWindowPoller
import dev.bauxe.konaste.utils.ByteArrayReader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MemoryBackedItemRepository(
    val versionResolver: VersionResolver,
    val memoryClient: MemoryClient,
    gameWindowPoller: GameWindowPoller,
    eventManager: EventManager,
) : ItemRepository, EventListener(eventManager) {
  private var items: List<Item> = listOf()
  private var itemLock = Mutex()

  init {
    gameWindowPoller.addOnStart(GameWindow.UI_MY_ROOM) { itemLock.withLock { items = listOf() } }
  }

  override suspend fun getItems(): List<Item> {
    itemLock.withLock {
      if (!items.isEmpty()) {
        return items
      }
      val itemPointerData =
          when (val result =
              versionResolver
                  .accept { it.getItemsPath() }
                  .readMemory(memoryClient, ItemsArea.Companion)) {
            is DataReadResult.Error<ItemsArea> -> return emptyList()
            is DataReadResult.Ok<ItemsArea> -> result.data
          }
      items =
          (Pointer.nativeValue(itemPointerData.itemArrayStart)..Pointer.nativeValue(
                      itemPointerData.itemArrayEnd) step Item.Companion.size().toLong())
              .mapNotNull {
                when (val result = memoryClient.read(Pointer(it), Item.Companion.size())) {
                  is MemoryResult.KernelError -> null
                  is MemoryResult.Ok -> Item.Companion.fromByteReader(ByteArrayReader(result.data))
                  MemoryResult.ProcessNotFound -> null
                  MemoryResult.ReadViolation -> null
                }
              }
              .plus(Item("dummy", "", 0))
      return items
    }
  }

  override suspend fun hasItem(itemId: String): Boolean {
    return getItems().any { item -> item.id == itemId }
  }

  override fun onLogin() {
    items = listOf()
  }
}
