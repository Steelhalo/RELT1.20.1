package griglog.relt

import griglog.relt.RELT.id
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

class CompressedTablesPayload(val tables: ByteArray) : CustomPacketPayload{
    companion object {
        val TYPE = CustomPacketPayload.Type<CompressedTablesPayload>(ResourceLocation(id, "send_loot_tables"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, CompressedTablesPayload> =
            StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, { it.tables }, { CompressedTablesPayload(it) })
    }
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }
}
