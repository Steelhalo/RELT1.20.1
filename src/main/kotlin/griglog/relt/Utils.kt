package griglog.relt

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

fun wrapHoverName(stack: ItemStack){
    stack.set(DataComponents.CUSTOM_NAME, Component.literal("*").append(stack.hoverName).append("*"))
}