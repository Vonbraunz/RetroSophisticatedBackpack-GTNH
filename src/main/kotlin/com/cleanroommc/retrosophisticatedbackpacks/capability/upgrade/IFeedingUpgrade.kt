package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable

sealed interface IFeedingUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {

    /** @return the slot of the food to use in [handler], or -1 if none available */
    fun getFoodSlot(handler: SimpleInventory, foodLevel: Int, health: Float, maxHealth: Float): Int

    /** @return true if the player was fed */
    fun feed(entity: EntityPlayer, handler: SimpleInventory): Boolean {
        if (!entity.canEat(false)) return false

        val slot = getFoodSlot(handler, entity.foodStats.foodLevel, entity.health, entity.maxHealth)
        if (slot > -1) {
            val food = handler.extractItem(slot, Int.MAX_VALUE, false) ?: return false
            val remaining = food.item.onEaten(food, entity.worldObj, entity)
            handler.insertItem(slot, remaining, false)
        }

        return false
    }
}
