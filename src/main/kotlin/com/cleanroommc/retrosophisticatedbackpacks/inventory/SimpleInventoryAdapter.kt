package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

/**
 * Adapts [SimpleInventory] to the 1.7.10 [net.minecraft.inventory.IInventory] interface
 * so vanilla [net.minecraft.inventory.Container] / [net.minecraft.inventory.Slot] can use it.
 */
class SimpleInventoryAdapter(
    private val delegate: SimpleInventory,
    private val name: String = "backpack",
) : net.minecraft.inventory.IInventory {

    override fun getSizeInventory(): Int = delegate.size

    override fun getStackInSlot(slot: Int): ItemStack? = delegate.getStackInSlot(slot)

    override fun decrStackSize(slot: Int, amount: Int): ItemStack? {
        val stack = delegate.getStackInSlot(slot) ?: return null
        return if (stack.stackSize <= amount) {
            delegate.setStackInSlot(slot, null)
            stack
        } else {
            val split = stack.splitStack(amount)
            delegate.onContentsChanged(slot)
            split
        }
    }

    override fun getStackInSlotOnClosing(slot: Int): ItemStack? {
        val stack = delegate.getStackInSlot(slot) ?: return null
        delegate.setStackInSlot(slot, null)
        return stack
    }

    override fun setInventorySlotContents(slot: Int, stack: ItemStack?) {
        delegate.setStackInSlot(slot, stack)
    }

    override fun getInventoryName(): String = name

    override fun hasCustomInventoryName(): Boolean = false

    override fun getInventoryStackLimit(): Int = 64

    override fun markDirty() {}

    override fun isUseableByPlayer(player: EntityPlayer): Boolean = true

    override fun openInventory() {}

    override fun closeInventory() {}

    override fun isItemValidForSlot(slot: Int, stack: ItemStack): Boolean =
        delegate.isItemValid(slot, stack)
}
