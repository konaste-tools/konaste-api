package dev.bauxe.konaste.models.items

import com.sun.jna.Pointer
import dev.bauxe.konaste.models.ConvertableFromByteReader
import dev.bauxe.konaste.utils.ByteArrayReader
import kotlinx.serialization.Serializable

data class ItemsArea(
    val itemArrayStart: Pointer,
    val itemArrayEnd: Pointer,
) {
  companion object : ConvertableFromByteReader<ItemsArea> {
    override fun size(): Int = 16

    override fun fromByteReader(data: ByteArrayReader): ItemsArea =
        ItemsArea(
            Pointer(data.nextLong()),
            Pointer(data.nextLong()),
        )
  }
}

@Serializable
data class Item(val id: String, val type: String, val flags: Int) {
  companion object : ConvertableFromByteReader<Item> {
    override fun size(): Int = 88

    override fun fromByteReader(data: ByteArrayReader): Item =
        Item(data.nextString(17, "SHIFT-JIS"), data.nextString(77, "SHIFT-JIS"), data.nextInt())
  }
}
