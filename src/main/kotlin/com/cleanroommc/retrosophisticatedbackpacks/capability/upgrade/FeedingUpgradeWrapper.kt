package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackDataFixer
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventory
import com.cleanroommc.retrosophisticatedbackpacks.item.FeedingUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class FeedingUpgradeWrapper : BasicUpgradeWrapper<FeedingUpgradeItem>(), IFeedingUpgrade {
    override val settingsLangKey: String = "gui.feeding_settings".asTranslationKey()

    override val filterItems: ExposedItemStackHandler = object : ExposedItemStackHandler(9) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean = stack.item is ItemFood
    }

    override fun checkFilter(stack: ItemStack): Boolean = stack.item is ItemFood && super.checkFilter(stack)

    override fun getFoodSlot(handler: SimpleInventory, foodLevel: Int, health: Float, maxHealth: Float): Int {
        for (slot in 0 until handler.slots) {
            val stack = handler.getStackInSlot(slot) ?: continue
            if (!checkFilter(stack)) continue
            val item = stack.item as? ItemFood ?: continue
            if (item.func_150905_g(stack) <= 20 - foodLevel) return slot
        }
        return -1
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        BackpackDataFixer.fixFeedingUpgrade(filterItems)
    }
}
