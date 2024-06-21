package griglog.relt.table_resolving

import griglog.relt.rei_plugin.TableEntryDef
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.*
import net.minecraft.world.level.storage.loot.functions.*


fun LootTable.resolve(): Pair<Collection<ItemLike>, Collection<EntryStack<ResourceLocation>>> {
    val items = hashSetOf<ItemLike>()
    val tables = hashSetOf<EntryStack<ResourceLocation>>()
    for (pool in pools){
        for (entry in pool.entries) {
            resolveEntry(entry, items, tables)
        }
    }
    return Pair(items, tables)
}

fun resolveEntry(entry: LootPoolEntryContainer, items: MutableSet<ItemLike>, tables: MutableSet<EntryStack<ResourceLocation>>){
    when(entry){
        is LootItem -> resolveItem(entry.item.value(), entry.functions)?.let{items.add(it)}
        is NestedLootTable -> entry.contents.map({key -> tables.add(EntryStack.of(TableEntryDef.type, key.location()))},
                                                 {table -> val (i, t) = table.resolve()
                                                     items.addAll(i)
                                                     tables.addAll(t)
                                                 })
        is TagEntry -> BuiltInRegistries.ITEM.getTagOrEmpty(entry.tag).forEach{ item ->
            resolveItem(item.value(), entry.functions)?.let{items.add(it)}
        }
        is CompositeEntryBase -> entry.children.forEach{ resolveEntry(it, items, tables) }
    }
}

fun resolveItem(lootItem: Item, functions: List<LootItemFunction>): ItemLike?{
    if (lootItem == Items.AIR)  //TODO: should compare holders?
        return null //I hope no modder will ever do this but better safe than sorry
    val item = ItemLike(lootItem)
    functions.forEach { function ->
        when (function) {
            is SetComponentsFunction -> item.stack.applyComponents(function.components)
            is EnchantRandomlyFunction -> {
                function.enchantments.ifPresentOrElse({item.enchantRandom(it)}, {item.enchantRandom()})
            }

            is EnchantWithLevelsFunction -> item.enchantWithLevels(function.levels, function.treasure)
            is SetPotionFunction -> item.stack.update(
                DataComponents.POTION_CONTENTS, PotionContents.EMPTY, function.potion, PotionContents::withPotion)
            is ExplorationMapFunction -> item.writeMap()
            //is SetContainerContents
            //is SmeltItemFunction
        }
    }
    return item
}
