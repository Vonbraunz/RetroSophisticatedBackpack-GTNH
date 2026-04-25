package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack

open class ExposedItemStackHandler(size: Int) : SimpleInventory(size) {
    val inventory: ArrayList<ItemStack?> get() = stacks
}
