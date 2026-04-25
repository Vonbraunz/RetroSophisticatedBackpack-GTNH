package com.cleanroommc.retrosophisticatedbackpacks.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraftforge.common.util.INBTSerializable

open class SimpleInventory(val size: Int) : INBTSerializable<NBTTagCompound> {

    val stacks: ArrayList<ItemStack?> = ArrayList<ItemStack?>(size).also { list ->
        repeat(size) { list.add(null) }
    }

    val slots: Int get() = size

    fun getStackInSlot(index: Int): ItemStack? {
        validateSlotIndex(index)
        return stacks[index]
    }

    fun setStackInSlot(index: Int, stack: ItemStack?) {
        validateSlotIndex(index)
        stacks[index] = stack
        onContentsChanged(index)
    }

    open fun isItemValid(slot: Int, stack: ItemStack): Boolean = true

    open fun getStackLimit(slot: Int, stack: ItemStack): Int = stack.maxStackSize

    fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
        if (stack == null || stack.stackSize <= 0) return null
        validateSlotIndex(slot)
        if (!isItemValid(slot, stack)) return stack

        val existing = stacks[slot]
        var limit = getStackLimit(slot, stack)

        if (existing != null && existing.stackSize > 0) {
            if (!canStack(stack, existing)) return stack
            limit -= existing.stackSize
        }

        if (limit <= 0) return stack

        val reachedLimit = stack.stackSize > limit

        if (!simulate) {
            if (existing == null || existing.stackSize <= 0) {
                stacks[slot] = if (reachedLimit) copyWithSize(stack, limit) else stack.copy()
            } else {
                existing.stackSize += if (reachedLimit) limit else stack.stackSize
            }
            onContentsChanged(slot)
        }

        return if (reachedLimit) copyWithSize(stack, stack.stackSize - limit) else null
    }

    fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? {
        if (amount <= 0) return null
        validateSlotIndex(slot)

        val stack = stacks[slot]?.takeIf { it.stackSize > 0 } ?: return null
        val toExtract = minOf(amount, stack.maxStackSize)

        return if (stack.stackSize <= toExtract) {
            if (!simulate) {
                stacks[slot] = null
                onContentsChanged(slot)
            }
            stack.copy()
        } else {
            if (!simulate) {
                stacks[slot] = copyWithSize(stack, stack.stackSize - toExtract)
                onContentsChanged(slot)
            }
            copyWithSize(stack, toExtract)
        }
    }

    protected open fun onContentsChanged(slot: Int) {}

    protected fun validateSlotIndex(slot: Int) {
        require(slot in 0 until size) { "Slot $slot not in valid range [0,$size)" }
    }

    override fun serializeNBT(): NBTTagCompound {
        val list = NBTTagList()
        for ((i, stack) in stacks.withIndex()) {
            if (stack != null && stack.stackSize > 0) {
                val tag = NBTTagCompound()
                tag.setByte("Slot", i.toByte())
                stack.writeToNBT(tag)
                list.appendTag(tag)
            }
        }
        return NBTTagCompound().also {
            it.setTag("Items", list)
            it.setInteger("Size", size)
        }
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        val list = nbt.getTagList("Items", 10)
        for (i in 0 until list.tagCount()) {
            val tag = list.getCompoundTagAt(i)
            val slot = tag.getByte("Slot").toInt() and 0xFF
            if (slot < size) stacks[slot] = ItemStack.loadItemStackFromNBT(tag)
        }
    }

    companion object {
        fun canStack(a: ItemStack, b: ItemStack): Boolean =
            a.item == b.item && a.itemDamage == b.itemDamage && ItemStack.areItemStackTagsEqual(a, b)

        fun copyWithSize(stack: ItemStack, size: Int): ItemStack =
            stack.copy().also { it.stackSize = size }
    }
}
