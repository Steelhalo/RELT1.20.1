package griglog.relt.entry_points

import griglog.relt.CompressedTablesPayload
import griglog.relt.RELT
import griglog.relt.table_storage.onReloadOrServerStart
import griglog.relt.table_storage.serverTablesCache
import griglog.relt.table_storage.tryUpdateLootTables
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.ReloadableServerResources

class ReltCommon : ModInitializer{
    override fun onInitialize() {
        PayloadTypeRegistry.playS2C().register(CompressedTablesPayload.TYPE, CompressedTablesPayload.CODEC);
        ServerLifecycleEvents.SERVER_STARTING.register{ server -> onReloadOrServerStart() }
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register{ server, rm, success -> onReloadOrServerStart() }
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register{ player, joined ->
            tryUpdateLootTables(player.server)
            ServerPlayNetworking.send(player, CompressedTablesPayload(serverTablesCache))
        }
    }
}