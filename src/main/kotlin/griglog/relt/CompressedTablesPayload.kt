package griglog.relt

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class CompressedTablesPayload(val tables: ByteArray) : CustomPacketPayload{
    companion object {
        val TYPE = CustomPacketPayload.Type<CompressedTablesPayload>(RELT.SEND_LOOT_TABLES)
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, CompressedTablesPayload> =
            StreamCodec.composite(ByteBufCodecs.BYTE_ARRAY, { it.tables }, { CompressedTablesPayload(it) })
    }
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> {
        return TYPE
    }
}
