package griglog.relt

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.*
import java.util.*

class SkipConfig {
    val skipEmptyTables: Boolean
    val skipTypes: Set<LootContextParamSet>

    constructor(side: String) {  //no reloading because fuck you
        val config = SimpleConfig.of(RELTMAIN.id + '_' + side).provider {
             """|#These options are present on both client and server sides. Both must be set to false in order to disable skipping
                |skipEmptyTables=true
                |skipBlockLoot=true
                |skipEntityLoot=true
                |skipCommandLoot=true""".trimMargin() }.request()
        skipEmptyTables = config.getOrDefault("skipEmptyTables", true)
        val skipTypes = mutableSetOf<LootContextParamSet>()
        if (config.getOrDefault("skipBlockLoot", true)) skipTypes.add(BLOCK)
        if (config.getOrDefault("skipEntityLoot", true)) skipTypes.add(ENTITY)
        if (config.getOrDefault("skipCommandLoot", true)) skipTypes.add(COMMAND)
        this.skipTypes = Collections.unmodifiableSet(skipTypes)
    }
}