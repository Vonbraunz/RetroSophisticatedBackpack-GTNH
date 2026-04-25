package com.cleanroommc.retrosophisticatedbackpacks.backpack

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventory
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.entity.Entity
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary
import kotlin.math.min

object BackpackInventoryHelper {

    fun sortInventory(wrapper: BackpackWrapper) {
        fun compareLists(list1: List<String>, list2: List<String>): Int {
            for (i in 0 until min(list1.size, list2.size)) {
                val cmp = list2[i].compareTo(list1[i])
                if (cmp != 0) return cmp
            }
            return list2.size.compareTo(list1.size)
        }

        // Merge stacks first
        for (i in 0 until wrapper.backpackInventorySize() - 1) {
            if (wrapper.isSlotLocked(i)) continue
            val isMemorized = wrapper.isSlotMemorized(i)
            val baseStack = wrapper.getStackInSlot(i) ?: continue
            val maxSize = baseStack.maxStackSize * wrapper.getTotalStackMultiplier()
            for (j in i + 1 until wrapper.backpackInventorySize()) {
                if (isMemorized != wrapper.isSlotMemorized(j) || wrapper.isSlotLocked(j)) continue
                val stack = wrapper.getStackInSlot(j) ?: continue
                if (!SimpleInventory.canStack(baseStack, stack)) continue
                val diff = min(stack.stackSize, maxSize - baseStack.stackSize)
                if (diff > 0) {
                    baseStack.stackSize += diff
                    stack.stackSize -= diff
                    if (stack.stackSize <= 0) wrapper.backpackItemStackHandler.setStackInSlot(j, null)
                } else if (diff == 0) break
            }
        }

        val inPlaceStacks = mutableListOf<Pair<ItemStack?, Int>>()
        val sorted = mutableListOf<ItemStack?>()

        for (i in 0 until wrapper.backpackInventorySize()) {
            val stack = wrapper.getStackInSlot(i)
            if (wrapper.isSlotMemorized(i) || wrapper.isSlotLocked(i)) {
                inPlaceStacks.add(stack to i)
            } else {
                sorted.add(stack)
            }
        }

        sorted.sortWith { stack1, stack2 ->
            val e1 = stack1 == null || stack1.stackSize <= 0
            val e2 = stack2 == null || stack2.stackSize <= 0
            if (e1 && e2) return@sortWith 0
            else if (e1) return@sortWith 1
            else if (e2) return@sortWith -1

            val item1 = stack1!!.item
            val item2 = stack2!!.item

            when (wrapper.sortType) {
                SortType.BY_NAME ->
                    item2.getItemStackDisplayName(stack2).compareTo(item1.getItemStackDisplayName(stack1))
                SortType.BY_MOD_ID -> {
                    val mod1 = GameRegistry.findUniqueIdentifierFor(item1)?.modId ?: ""
                    val mod2 = GameRegistry.findUniqueIdentifierFor(item2)?.modId ?: ""
                    mod2.compareTo(mod1)
                }
                SortType.BY_COUNT -> stack2.stackSize.compareTo(stack1.stackSize)
                SortType.BY_ORE_DICT -> {
                    val od1 = OreDictionary.getOreIDs(stack1).map(OreDictionary::getOreName)
                    val od2 = OreDictionary.getOreIDs(stack2).map(OreDictionary::getOreName)
                    compareLists(od1, od2)
                }
            }
        }

        for ((stack, i) in inPlaceStacks) {
            sorted.add(i, stack)
        }

        for ((slotIndex, stack) in sorted.withIndex()) {
            wrapper.backpackItemStackHandler.setStackInSlot(slotIndex, stack)
        }
    }

    fun transferPlayerInventoryToBackpack(
        wrapper: BackpackWrapper,
        inventoryPlayer: InventoryPlayer,
        transferMatched: Boolean,
    ) {
        for (i in 9 until inventoryPlayer.mainInventory.size) {
            val slot = inventoryPlayer.mainInventory[i] ?: continue
            if (slot.stackSize <= 0) continue
            if (slot.item is BackpackItem && !wrapper.canNestBackpack()) continue

            var remaining: ItemStack? = slot
            for (j in 0 until wrapper.backpackInventorySize()) {
                remaining = wrapper.backpackItemStackHandler.insertItemToMemorySlots(remaining, false)
                if (transferMatched && wrapper.getStackInSlot(j) == null) continue
                remaining = wrapper.insertItem(j, remaining, false)
                if (remaining == null) break
            }
            inventoryPlayer.mainInventory[i] = remaining
        }
    }

    fun transferBackpackToPlayerInventory(
        wrapper: BackpackWrapper,
        inventoryPlayer: InventoryPlayer,
        transferMatched: Boolean,
    ) {
        for (i in 0 until wrapper.backpackInventorySize()) {
            var remaining: ItemStack? = wrapper.getStackInSlot(i) ?: continue
            for (j in 9 until inventoryPlayer.mainInventory.size) {
                val existing = inventoryPlayer.mainInventory[j]
                if (transferMatched && (existing == null || existing.stackSize <= 0)) continue
                remaining = insertIntoPlayerSlot(inventoryPlayer, j, remaining ?: break)
                if (remaining == null) break
            }
            wrapper.backpackItemStackHandler.setStackInSlot(i, remaining)
        }
    }

    fun attemptDepositOnTileEntity(wrapper: BackpackWrapper, destination: TileEntity): Boolean {
        val inv = destination as? IInventory ?: return false
        return attemptDepositOnInventory(wrapper, inv)
    }

    fun attemptDepositOnEntity(wrapper: BackpackWrapper, destination: Entity): Boolean {
        val inv = destination as? IInventory ?: return false
        return attemptDepositOnInventory(wrapper, inv)
    }

    fun attemptDepositOnInventory(wrapper: BackpackWrapper, destination: IInventory): Boolean {
        if (isInventoryFull(destination)) return false
        var transferred = false
        val backpackInventory = wrapper.backpackItemStackHandler

        for (i in 0 until backpackInventory.slots) {
            if (!wrapper.canDeposit(i)) continue
            val stack = wrapper.getStackInSlot(i) ?: continue
            val before = stack.stackSize
            val remaining = insertItemStackedIntoInventory(destination, stack.copy(), false)
            val deposited = before - (remaining?.stackSize ?: 0)
            if (deposited > 0) {
                transferred = true
                wrapper.extractItem(i, deposited, false)
            }
        }
        return transferred
    }

    fun attemptRestockFromTileEntity(wrapper: BackpackWrapper, source: TileEntity): Boolean {
        val inv = source as? IInventory ?: return false
        return attemptRestockFromInventory(wrapper, inv)
    }

    fun attemptRestockFromEntity(wrapper: BackpackWrapper, source: Entity): Boolean {
        val inv = source as? IInventory ?: return false
        return attemptRestockFromInventory(wrapper, inv)
    }

    fun attemptRestockFromInventory(wrapper: BackpackWrapper, source: IInventory): Boolean {
        val backpackInventory = wrapper.backpackItemStackHandler
        if (isFull(backpackInventory)) return false
        var transferred = false

        for (i in 0 until source.sizeInventory) {
            val sourceStack = source.getStackInSlot(i) ?: continue
            if (sourceStack.stackSize <= 0) continue
            if (!wrapper.canRestock(sourceStack)) continue

            val remaining = insertItemStackedIntoBackpack(backpackInventory, sourceStack.copy(), false)
            val moved = sourceStack.stackSize - (remaining?.stackSize ?: 0)
            if (moved > 0) {
                transferred = true
                sourceStack.stackSize -= moved
                if (sourceStack.stackSize <= 0) source.setInventorySlotContents(i, null)
                else source.markDirty()
            }
        }
        return transferred
    }

    private fun insertItemStackedIntoInventory(inv: IInventory, stack: ItemStack, simulate: Boolean): ItemStack? {
        var remaining = stack.copy()
        val invLimit = inv.inventoryStackLimit
        // Pass 1: merge with existing
        for (i in 0 until inv.sizeInventory) {
            val existing = inv.getStackInSlot(i) ?: continue
            if (existing.stackSize <= 0) continue
            if (!inv.isItemValidForSlot(i, remaining)) continue
            if (!SimpleInventory.canStack(existing, remaining)) continue
            val limit = min(invLimit, existing.maxStackSize)
            val space = limit - existing.stackSize
            if (space <= 0) continue
            val toAdd = min(space, remaining.stackSize)
            if (!simulate) { existing.stackSize += toAdd; inv.markDirty() }
            remaining.stackSize -= toAdd
            if (remaining.stackSize <= 0) return null
        }
        // Pass 2: empty slots
        for (i in 0 until inv.sizeInventory) {
            val existing = inv.getStackInSlot(i)
            if (existing != null && existing.stackSize > 0) continue
            if (!inv.isItemValidForSlot(i, remaining)) continue
            val toPlace = min(min(invLimit, remaining.maxStackSize), remaining.stackSize)
            if (!simulate) {
                inv.setInventorySlotContents(i, remaining.copy().also { it.stackSize = toPlace })
                inv.markDirty()
            }
            remaining.stackSize -= toPlace
            if (remaining.stackSize <= 0) return null
        }
        return if (remaining.stackSize <= 0) null else remaining
    }

    private fun insertItemStackedIntoBackpack(backpack: BackpackItemStackHandler, stack: ItemStack, simulate: Boolean): ItemStack? {
        var remaining: ItemStack? = backpack.insertItemToMemorySlots(stack, simulate)
        for (i in 0 until backpack.slots) {
            remaining = backpack.insertItem(i, remaining, simulate)
            if (remaining == null) return null
        }
        return remaining
    }

    private fun insertIntoPlayerSlot(inv: InventoryPlayer, slot: Int, stack: ItemStack): ItemStack? {
        val existing = inv.mainInventory[slot]
        val limit = stack.maxStackSize
        return if (existing != null && existing.stackSize > 0) {
            if (!SimpleInventory.canStack(existing, stack)) stack
            else {
                val space = min(limit, existing.maxStackSize) - existing.stackSize
                if (space <= 0) stack
                else {
                    val toAdd = min(space, stack.stackSize)
                    existing.stackSize += toAdd
                    if (stack.stackSize - toAdd <= 0) null
                    else stack.copy().also { it.stackSize = stack.stackSize - toAdd }
                }
            }
        } else {
            val toPlace = min(limit, stack.stackSize)
            inv.mainInventory[slot] = stack.copy().also { it.stackSize = toPlace }
            if (stack.stackSize - toPlace <= 0) null
            else stack.copy().also { it.stackSize = stack.stackSize - toPlace }
        }
    }

    private fun isFull(handler: BackpackItemStackHandler): Boolean {
        for (i in 0 until handler.slots) {
            val stack = handler.getStackInSlot(i) ?: return false
            if (stack.stackSize < handler.getStackLimit(i, stack)) return false
        }
        return true
    }

    private fun isInventoryFull(inv: IInventory): Boolean {
        for (i in 0 until inv.sizeInventory) {
            val stack = inv.getStackInSlot(i)
            if (stack == null || stack.stackSize < min(inv.inventoryStackLimit, stack.maxStackSize)) return false
        }
        return true
    }
}
