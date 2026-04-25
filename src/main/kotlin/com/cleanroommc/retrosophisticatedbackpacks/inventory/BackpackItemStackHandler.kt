package com.cleanroommc.retrosophisticatedbackpacks.inventory

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.item.ItemStack
import cpw.mods.fml.common.registry.GameRegistry
import kotlin.math.min

class BackpackItemStackHandler(size: Int, private val wrapper: BackpackWrapper) : ExposedItemStackHandler(size) {

    val memorizedSlotStack: ArrayList<ItemStack?> = ArrayList<ItemStack?>(size).also { list -> repeat(size) { list.add(null) } }
    val memorizedSlotRespectNbtList: MutableList<Boolean> = MutableList(size) { false }
    val sortLockedSlots: MutableList<Boolean> = MutableList(size) { false }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        val regName = GameRegistry.findUniqueIdentifierFor(stack.item)?.toString()
        if (Config.blacklistedItems.contains(regName)) return false
        val memorized = memorizedSlotStack[slot]
        return if (memorized == null) {
            stack.item !is BackpackItem || wrapper.canNestBackpack()
        } else if (memorizedSlotRespectNbtList[slot]) {
            ItemStack.areItemStacksEqual(stack, memorized)
        } else {
            stack.item == memorized.item && ItemStack.areItemStackTagsEqual(stack, memorized)
        }
    }

    override fun getStackLimit(slot: Int, stack: ItemStack): Int {
        val existing = stacks[slot]
        val base = if (existing != null && existing.stackSize > 0) existing.maxStackSize else stack.maxStackSize
        return base * wrapper.getTotalStackMultiplier()
    }

    /** Insert into memorized slots first, then fall back to [slotIndex]. */
    fun prioritizedInsertion(slotIndex: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
        val remaining = insertItemToMemorySlots(stack, simulate)
        return insertItem(slotIndex, remaining, simulate)
    }

    fun insertItemToMemorySlots(stack: ItemStack?, simulate: Boolean): ItemStack? {
        var remaining = stack ?: return null
        for ((slotIndex, memorized) in memorizedSlotStack.withIndex()) {
            if (memorized == null || !canStack(remaining, memorized)) continue
            remaining = insertItem(slotIndex, remaining, simulate) ?: return null
        }
        return remaining
    }

    override fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? {
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

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? {
        if (amount == 0) return null
        validateSlotIndex(slot)

        val stack = stacks[slot]?.takeIf { it.stackSize > 0 } ?: return null
        val slotMax = stack.maxStackSize * wrapper.getTotalStackMultiplier()
        val toExtract = min(amount, slotMax)

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
}
