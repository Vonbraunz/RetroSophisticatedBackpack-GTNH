package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack

/** A lazily-resolved slot view that proxies another [SimpleInventory] at call time. */
class DelegatedItemHandler(var delegated: () -> SimpleInventory, var wrappedSlotAmount: Int) {

    val slots: Int get() {
        val d = delegated()
        check(d.size == wrappedSlotAmount) {
            "Mismatched delegated inventory slot count: expected $wrappedSlotAmount, got ${d.size}"
        }
        return wrappedSlotAmount
    }

    fun getStackInSlot(slot: Int): ItemStack? = delegated().getStackInSlot(slot)
    fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? = delegated().insertItem(slot, stack, simulate)
    fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? = delegated().extractItem(slot, amount, simulate)
    fun setStackInSlot(slot: Int, stack: ItemStack?) = delegated().setStackInSlot(slot, stack)
}
