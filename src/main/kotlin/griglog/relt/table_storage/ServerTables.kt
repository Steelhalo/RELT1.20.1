package griglog.relt.table_storage

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import griglog.relt.RELT
import griglog.relt.entry_points.ReltCommon
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.LootDataType
import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

var serverTablesCache: ByteArray = ArrayUtils.EMPTY_BYTE_ARRAY
var serverCacheValid = true

fun tryUpdateLootTables(server: MinecraftServer) {
    if (serverCacheValid)
        return
    val reloadableRegistries = server.reloadableRegistries()
    val obj = JsonObject()
    val t1 = System.nanoTime()
    reloadableRegistries.getKeys(Registries.LOOT_TABLE).forEach { rl ->
        //todo: is it the correct way to make ResourceKey?
        val table = reloadableRegistries.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, rl))
        if ((ReltCommon.config.skipEmptyTables && table.pools.isEmpty()) || table.paramSet in ReltCommon.config.skipTypes)
            return@forEach
        val regOps = RegistryOps.create(JsonOps.INSTANCE, reloadableRegistries.get())
        LootDataType.TABLE.codec.encodeStart(regOps, table).mapOrElse(
            { json -> obj.add(rl.toString(), json) }, { error -> RELT.logger.error("Failed to serialize loot table $rl: $error") })
    }
    /*
    for (i in 0..50) {
        val test: LootTable = LootTable.lootTable().pool(LootPool.lootPool().also {
            for (item in BuiltInRegistries.ITEM) {
                it.with(LootItem.lootTableItem(item).build())
            }
        }.build()).build()
        obj.add(RELT.id + ":big_test" + i, LootTables.GSON.toJsonTree(test))
    }*/
    val str = obj.toString().toByteArray()
    var t2 = System.nanoTime()
    RELT.logger.info("Collected ${obj.size()} loot tables, ${str.size} bytes in total. Took ${(t2 - t1) / 1000000} ms.")
    val t3 = System.nanoTime()
    ByteArrayOutputStream().use {
        GZIPOutputStream(it).apply { write(str); close() }
        serverTablesCache = it.toByteArray()
    }
    val t4 = System.nanoTime()
    RELT.logger.info("Loot tables compressed to ${serverTablesCache.size} bytes. Took ${(t4 - t3) / 1000000} ms.")
    serverCacheValid = true
}

fun onReloadOrServerStart() {
    serverCacheValid = false
}
