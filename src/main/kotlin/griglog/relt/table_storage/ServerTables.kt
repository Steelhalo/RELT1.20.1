package griglog.relt.table_storage

import com.google.gson.JsonObject
import griglog.relt.RELTMAIN
import griglog.relt.entry_points.ReltCommon
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.loot.LootDataType
import net.minecraft.world.level.storage.loot.LootDataType.TABLE
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.*
import org.apache.commons.lang3.ArrayUtils
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

var serverTablesCache: ByteArray = ArrayUtils.EMPTY_BYTE_ARRAY
var serverCacheValid = true
private val skipTypes = setOf(BLOCK, ENTITY, COMMAND)
fun tryUpdateLootTables(server: MinecraftServer) {
    if (serverCacheValid)
        return

    val registries = server.registries();
    val obj = JsonObject()
    val t1 = System.nanoTime()

    server.lootData.getKeys(TABLE).forEach { rl ->
        val table = server.lootData.getLootTable(rl);
        if ((ReltCommon.config.skipEmptyTables && table.pools.isEmpty()) || skipTypes.contains(table.paramSet))
            return@forEach

        val json = LootDataType.TABLE.parser().toJsonTree(table)
        obj.add(rl.toString(), json)
    }

    val str = obj.toString()
    ByteArrayOutputStream().use {
        GZIPOutputStream(it).apply { write(str.toByteArray()); close() }
        serverTablesCache = it.toByteArray()
    }
    RELTMAIN.logger.info("Loot tables compressed: ${serverTablesCache.size} bytes.")
    serverCacheValid = true
}



fun onReloadOrServerStart(){
    serverCacheValid = false
}
