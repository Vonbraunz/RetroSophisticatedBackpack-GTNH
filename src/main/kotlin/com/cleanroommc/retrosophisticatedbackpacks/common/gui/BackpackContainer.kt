package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
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

    init {
        val backpackInv = SimpleInventoryAdapter(wrapper.backpackItemStackHandler, "backpack")
        val upgradeInv  = SimpleInventoryAdapter(wrapper.upgradeItemStackHandler, "upgrades")

        // Backpack inventory slots
        for (i in 0 until wrapper.backpackInventorySize()) {
            val col = i % rowSize
            val row = i / rowSize
            addSlotToContainer(Slot(backpackInv, i,
                backpackOffsetX + LEFT_PAD + col * SLOT_SIZE,
                TOP_PAD + row * SLOT_SIZE))
        }

        // Upgrade slots (left column)
        for (i in 0 until wrapper.upgradeSlotsSize()) {
            addSlotToContainer(object : Slot(upgradeInv, i,
                UPGRADE_SLOT_X,
                TOP_PAD + i * SLOT_SIZE) {
                override fun isItemValid(stack: ItemStack): Boolean =
                    stack.item is UpgradeItem
                override fun getSlotStackLimit(): Int = 1
            })
        }

        // Player main inventory (9×3)
        val playerInv = player.inventory
        for (row in 0..2) {
            for (col in 0..8) {
                val slotIndex = 9 + row * 9 + col
                addSlotToContainer(Slot(playerInv, slotIndex,
                    backpackOffsetX + LEFT_PAD + col * SLOT_SIZE,
                    TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + row * SLOT_SIZE))
            }
        }

        // Player hotbar
        for (col in 0..8) {
            val isBackpackSlot = backpackSlotIndex == col
            addSlotToContainer(object : Slot(playerInv, col,
                backpackOffsetX + LEFT_PAD + col * SLOT_SIZE,
                TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + 3 * SLOT_SIZE + HOTBAR_GAP) {
                override fun canTakeStack(player: EntityPlayer): Boolean =
                    !isBackpackSlot || super.canTakeStack(player)
            })
        }
    }

    override fun canInteractWith(player: EntityPlayer): Boolean = true

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
        const val PLAYER_INV_GAP    = 7
        const val HOTBAR_GAP        = 4
    }
}
