package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.EntityInteractEvent
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent

// Register manually via MinecraftForge.EVENT_BUS.register(EntityEventHandler) in mod init.
object EntityEventHandler {

    @SubscribeEvent
    @JvmStatic
    fun onItemPickup(event: EntityItemPickupEvent) {
        val player = event.entityPlayer
        val entityItem = event.item
        val original = entityItem.entityItem ?: return
        if (original.stackSize <= 0) return

        var remaining: ItemStack? = original.copy()
        remaining = attemptPickup(player.inventory.mainInventory, remaining!!)

        if (remaining == null || remaining.stackSize <= 0) {
            entityItem.setDead()
            event.isCanceled = true
            player.worldObj.playSoundAtEntity(
                player, "random.pop", 0.2f,
                ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
        } else if (remaining.stackSize != original.stackSize) {
            entityItem.setDead()
            event.isCanceled = true
            val world = entityItem.worldObj
            val newEntity = EntityItem(world, entityItem.posX, entityItem.posY, entityItem.posZ, remaining)
            newEntity.delayBeforeCanPickup = 0
            world.spawnEntityInWorld(newEntity)
        }
    }

    private fun attemptPickup(inventoryStacks: Array<ItemStack?>, stack: ItemStack): ItemStack? {
        var remaining: ItemStack? = stack
        for (inventoryStack in inventoryStacks) {
            if (inventoryStack == null || inventoryStack.stackSize <= 0) continue
            if (inventoryStack.item !is BackpackItem) continue
            val wrapper = BackpackHelper.getWrapper(inventoryStack) ?: continue
            if (!wrapper.canPickupItem(remaining ?: return null)) continue
            var slotIndex = 0
            while (remaining != null && slotIndex < wrapper.getSlots()) {
                remaining = wrapper.backpackItemStackHandler.prioritizedInsertion(slotIndex, remaining, false)
                slotIndex++
            }
            if (remaining == null) break
        }
        return remaining
    }

    @SubscribeEvent
    @JvmStatic
    fun onEntityInteract(event: EntityInteractEvent) {
        val player = event.entityPlayer
        val stack = player.getHeldItem() ?: return
        if (stack.stackSize <= 0) return
        val entity = event.target

        if (stack.item is BackpackItem && player.isSneaking) {
            val wrapper = BackpackHelper.getWrapper(stack) ?: return
            var transferred = BackpackInventoryHelper.attemptDepositOnEntity(wrapper, entity)
            transferred = BackpackInventoryHelper.attemptRestockFromEntity(wrapper, entity) || transferred

            if (transferred) {
                player.worldObj.playSoundAtEntity(player, "mob.armor.equip_iron", 0.5f, 0.5f)
                event.isCanceled = true
            }
        }
    }
}
