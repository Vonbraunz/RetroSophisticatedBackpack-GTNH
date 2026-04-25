package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventoryAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

open class BackpackContainer(
    val wrapper: BackpackWrapper,
    /** Slot index in the player's inventory that holds the backpack (null if placed as block). */
    private val backpackSlotIndex: Int?,
    player: EntityPlayer,
) : Container() {

    val rowSize: Int = if (wrapper.backpackInventorySize() > 81) 12 else 9
    val colSize: Int = (wrapper.backpackInventorySize() + rowSize - 1) / rowSize

    // Slot ranges for mergeItemStack / transferStackInSlot
    val backpackSlotStart = 0
    val backpackSlotEnd   = wrapper.backpackInventorySize()           // exclusive
    val upgradeSlotStart  = backpackSlotEnd
    val upgradeSlotEnd    = upgradeSlotStart + wrapper.upgradeSlotsSize() // exclusive
    val playerMainStart   = upgradeSlotEnd
    val playerMainEnd     = playerMainStart + 27                      // exclusive
    val hotbarStart       = playerMainEnd
    val hotbarEnd         = hotbarStart + 9                           // exclusive (total = upgradeSlotEnd + 36)

    // How far right the backpack grid starts (upgrade column + gap)
    val backpackOffsetX = UPGRADE_COL_WIDTH + UPGRADE_GAP

    // Horizontal offset to center the 9-slot player inventory under the backpack grid
    val playerXOffset = (rowSize - 9) * SLOT_SIZE / 2

    init {
        val backpackInv = SimpleInventoryAdapter(wrapper.backpackItemStackHandler, "backpack")
        val upgradeInv  = SimpleInventoryAdapter(wrapper.upgradeItemStackHandler, "upgrades")

        // Backpack inventory slots
        for (i in 0 until wrapper.backpackInventorySize()) {
            val col = i % rowSize
            val row = i / rowSize
            addSlotToContainer(object : Slot(backpackInv, i,
                backpackOffsetX + LEFT_PAD + col * SLOT_SIZE,
                TOP_PAD + row * SLOT_SIZE) {
                override fun isItemValid(stack: ItemStack): Boolean {
                    if (stack.item !is BackpackItem) return true
                    if (!wrapper.canNestBackpack()) return false
                    // Prevent inserting the currently-open backpack into itself
                    return BackpackHelper.getWrapper(stack)?.uuid != wrapper.uuid
                }
                override fun getSlotStackLimit(): Int =
                    64 * wrapper.getTotalStackMultiplier()
            })
        }

        // Upgrade slots (left column)
        for (i in 0 until wrapper.upgradeSlotsSize()) {
            addSlotToContainer(object : Slot(upgradeInv, i,
                UPGRADE_SLOT_X,
                TOP_PAD + i * SLOT_SIZE) {
                override fun isItemValid(stack: ItemStack): Boolean =
                    stack.item is UpgradeItem
                override fun getSlotStackLimit(): Int = 1
                override fun canTakeStack(player: EntityPlayer): Boolean {
                    val item = stack?.item ?: return true
                    if (item is ExponentialStackUpgradeItem) return wrapper.canRemoveExponentialStackUpgrade()
                    if (item is StackUpgradeItem) return wrapper.canRemoveStackUpgrade(item.multiplier())
                    return true
                }
            })
        }

        // Player main inventory (9×3) — centered under the backpack grid
        val playerInv = player.inventory
        for (row in 0..2) {
            for (col in 0..8) {
                val slotIndex = 9 + row * 9 + col
                addSlotToContainer(Slot(playerInv, slotIndex,
                    backpackOffsetX + LEFT_PAD + playerXOffset + col * SLOT_SIZE,
                    TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + row * SLOT_SIZE))
            }
        }

        // Player hotbar — centered under the backpack grid
        for (col in 0..8) {
            val isBackpackSlot = backpackSlotIndex == col
            addSlotToContainer(object : Slot(playerInv, col,
                backpackOffsetX + LEFT_PAD + playerXOffset + col * SLOT_SIZE,
                TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + 3 * SLOT_SIZE + HOTBAR_GAP) {
                override fun canTakeStack(player: EntityPlayer): Boolean =
                    !isBackpackSlot || super.canTakeStack(player)
            })
        }
    }

    override fun canInteractWith(player: EntityPlayer): Boolean = true

    /**
     * Override click handling for oversized stacks (> item.maxStackSize).
     * Left-click: pick up 64; right-click: pick up 32. Vanilla breaks on stacks > 64.
     */
    override fun slotClick(slotId: Int, button: Int, mode: Int, player: EntityPlayer): ItemStack? {
        if (mode == 0 && slotId in backpackSlotStart until backpackSlotEnd) {
            val slot = inventorySlots[slotId] as? Slot ?: return super.slotClick(slotId, button, mode, player)
            val slotStack = slot.stack
            val cursor = player.inventory.itemStack
            val oversized = slotStack != null && slotStack.stackSize > slotStack.maxStackSize

            if (button == 0 && cursor != null && slotStack != null) {
                val sameItem = cursor.item == slotStack.item &&
                    (!cursor.hasSubtypes || cursor.itemDamage == slotStack.itemDamage) &&
                    ItemStack.areItemStackTagsEqual(cursor, slotStack)

                if (sameItem && slot.isItemValid(cursor)) {
                    // Merge cursor into slot up to the multiplied limit; vanilla caps at maxStackSize so we handle it.
                    val limit = slotStack.maxStackSize * wrapper.getTotalStackMultiplier()
                    if (slotStack.stackSize < limit) {
                        val toAdd = minOf(cursor.stackSize, limit - slotStack.stackSize)
                        slotStack.stackSize += toAdd
                        slot.onSlotChanged()
                        cursor.stackSize -= toAdd
                        if (cursor.stackSize <= 0) player.inventory.itemStack = null
                    }
                    // Slot is full or items merged — either way, no swap allowed.
                    return slotStack.copy()
                }

                // Different items and slot has an oversized stack: block the swap entirely.
                if (oversized) return null
            }

            // Left/right click with empty cursor on an oversized stack: pick up 64 or 32.
            if (cursor == null && oversized) {
                val toPickUp = if (button == 0) minOf(64, slotStack!!.stackSize) else minOf(32, slotStack!!.stackSize)
                val picked = slot.decrStackSize(toPickUp)
                if (picked != null) {
                    player.inventory.itemStack = picked
                    slot.onPickupFromSlot(player, picked)
                }
                return picked
            }
        }
        return super.slotClick(slotId, button, mode, player)
    }

    /** Vanilla mergeItemStack caps at item.maxStackSize; override to respect backpack stack multiplier. */
    override fun mergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseOrder: Boolean): Boolean {
        val multiplier = wrapper.getTotalStackMultiplier()
        if (multiplier <= 1) return super.mergeItemStack(stack, startIndex, endIndex, reverseOrder)

        var merged = false
        var k = if (reverseOrder) endIndex - 1 else startIndex

        // Phase 1: stack into existing matching slots
        if (stack.isStackable) {
            while (stack.stackSize > 0 && (if (reverseOrder) k >= startIndex else k < endIndex)) {
                val slot = inventorySlots[k] as Slot
                val existing = slot.stack
                if (existing != null && existing.item == stack.item &&
                    (!stack.hasSubtypes || stack.itemDamage == existing.itemDamage) &&
                    ItemStack.areItemStackTagsEqual(stack, existing)) {
                    val limit = if (k in backpackSlotStart until backpackSlotEnd)
                        existing.maxStackSize * multiplier else existing.maxStackSize
                    val total = existing.stackSize + stack.stackSize
                    when {
                        total <= limit -> { stack.stackSize = 0; existing.stackSize = total; slot.onSlotChanged(); merged = true }
                        existing.stackSize < limit -> { stack.stackSize -= limit - existing.stackSize; existing.stackSize = limit; slot.onSlotChanged(); merged = true }
                    }
                }
                k += if (reverseOrder) -1 else 1
            }
        }

        // Phase 2: place into empty slots
        if (stack.stackSize > 0) {
            k = if (reverseOrder) endIndex - 1 else startIndex
            while (if (reverseOrder) k >= startIndex else k < endIndex) {
                val slot = inventorySlots[k] as Slot
                if (slot.stack == null && slot.isItemValid(stack)) {
                    val limit = if (k in backpackSlotStart until backpackSlotEnd)
                        stack.maxStackSize * multiplier else stack.maxStackSize
                    val toPlace = minOf(stack.stackSize, limit)
                    slot.putStack(stack.copy().also { it.stackSize = toPlace })
                    slot.onSlotChanged()
                    stack.stackSize -= toPlace
                    merged = true
                    if (stack.stackSize <= 0) break
                }
                k += if (reverseOrder) -1 else 1
            }
        }

        return merged
    }

    override fun transferStackInSlot(player: EntityPlayer, slotIndex: Int): ItemStack? {
        val slot = inventorySlots[slotIndex] as? Slot ?: return null
        if (!slot.hasStack) return null

        val stack = slot.stack
        val copy = stack.copy()

        when {
            // From backpack → player
            slotIndex in backpackSlotStart until backpackSlotEnd -> {
                if (!mergeItemStack(stack, playerMainStart, hotbarEnd, true)) return null
            }
            // From player → try backpack first, then upgrade
            slotIndex in playerMainStart until hotbarEnd -> {
                val isUpgrade = stack.item is UpgradeItem
                if (isUpgrade) {
                    if (!mergeItemStack(stack, upgradeSlotStart, upgradeSlotEnd, false)) return null
                } else if (stack.item is BackpackItem && !wrapper.canNestBackpack()) {
                    return null
                } else {
                    if (!mergeItemStack(stack, backpackSlotStart, backpackSlotEnd, false)) return null
                }
            }
            // From upgrade slot → player
            slotIndex in upgradeSlotStart until upgradeSlotEnd -> {
                if (!mergeItemStack(stack, playerMainStart, hotbarEnd, true)) return null
            }
            else -> return null
        }

        if (stack.stackSize == 0) slot.putStack(null)
        else slot.onSlotChanged()

        if (stack.stackSize == copy.stackSize) return null
        slot.onPickupFromSlot(player, stack)
        return copy
    }

    override fun onContainerClosed(player: EntityPlayer) {
        super.onContainerClosed(player)
        // Save wrapper state back to item/TE — callers handle this via BackpackHelper if needed
    }

    companion object {
        const val SLOT_SIZE       = 18
        const val TOP_PAD         = 17
        const val LEFT_PAD        = 7
        const val UPGRADE_COL_WIDTH = 22   // width of the upgrade column strip
        const val UPGRADE_GAP       = 2    // gap between upgrade strip and backpack grid
        const val UPGRADE_SLOT_X    = 3    // x of upgrade slots within the GUI
        const val PLAYER_INV_GAP    = 26   // main panel bottom pad (5) + panel gap (4) + inv label area (17)
        const val HOTBAR_GAP        = 4
    }
}
