package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventory
import com.cleanroommc.retrosophisticatedbackpacks.inventory.SimpleInventoryAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

open class BackpackContainer(
    val wrapper: BackpackWrapper,
    /** Slot index in the player's inventory that holds the backpack (null if placed as block). */
    val backpackSlotIndex: Int?,
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
    val hotbarEnd         = hotbarStart + 9                           // exclusive

    // How far right the backpack grid starts (upgrade column + gap)
    val backpackOffsetX = UPGRADE_COL_WIDTH + UPGRADE_GAP

    // Horizontal offset to center the 9-slot player inventory under the backpack grid
    val playerXOffset = (rowSize - 9) * SLOT_SIZE / 2

    /** Each upgrade slot's pre-allocated filter slot range (always present, one per upgrade position). */
    data class FilterSlotRange(val upgradeSlotIndex: Int, val start: Int, val end: Int)
    val filterSlotRanges: List<FilterSlotRange>

    /** Live deserialized filter wrappers keyed by upgrade slot index. Used by GUI and packets.
     *  Triple: (filterable, upgradeStack, isAdvanced) */
    val filterWrapperCache: MutableMap<Int, Triple<IBasicFilterable, ItemStack, Boolean>>

    /**
     * Thin IInventory adapter whose backing SimpleInventory can be swapped at runtime.
     * Pre-allocated for every upgrade slot so filter container slots exist before any upgrade is placed.
     */
    inner class SwappableFilterInventory : net.minecraft.inventory.IInventory {
        var delegate: SimpleInventory? = null

        override fun getSizeInventory() = MAX_FILTER_SLOTS
        override fun getStackInSlot(slot: Int): ItemStack? =
            delegate?.let { if (slot < it.size) it.getStackInSlot(slot) else null }
        override fun decrStackSize(slot: Int, amount: Int): ItemStack? {
            val d = delegate ?: return null
            if (slot >= d.size) return null
            val stack = d.getStackInSlot(slot) ?: return null
            return if (stack.stackSize <= amount) { d.setStackInSlot(slot, null); stack }
                   else { val split = stack.splitStack(amount); d.onContentsChanged(slot); split }
        }
        override fun getStackInSlotOnClosing(slot: Int): ItemStack? {
            val d = delegate ?: return null
            if (slot >= d.size) return null
            val stack = d.getStackInSlot(slot) ?: return null
            d.setStackInSlot(slot, null)
            return stack
        }
        override fun setInventorySlotContents(slot: Int, stack: ItemStack?) {
            val d = delegate ?: return
            if (slot < d.size) d.setStackInSlot(slot, stack)
        }
        override fun getInventoryName() = "filter"
        override fun hasCustomInventoryName() = false
        override fun getInventoryStackLimit() = 64
        override fun markDirty() {}
        override fun isUseableByPlayer(player: EntityPlayer) = true
        override fun openInventory() {}
        override fun closeInventory() {}
        override fun isItemValidForSlot(slot: Int, stack: ItemStack) = true
    }

    private val swappableFilterInvs: Array<SwappableFilterInventory>

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
                    return BackpackHelper.getWrapper(stack)?.uuid != wrapper.uuid
                }
                override fun getSlotStackLimit(): Int =
                    64 * wrapper.getTotalStackMultiplier()
            })
        }

        // Upgrade slots — override onSlotChanged to refresh the filter cache when upgrades are inserted/removed
        for (i in 0 until wrapper.upgradeSlotsSize()) {
            addSlotToContainer(object : Slot(upgradeInv, i,
                UPGRADE_SLOT_X,
                UPGRADE_TOP_PAD + i * SLOT_SIZE) {
                override fun isItemValid(stack: ItemStack): Boolean =
                    stack.item is UpgradeItem
                override fun getSlotStackLimit(): Int = 1
                override fun canTakeStack(player: EntityPlayer): Boolean {
                    val item = stack?.item ?: return true
                    if (item is ExponentialStackUpgradeItem) return wrapper.canRemoveExponentialStackUpgrade()
                    if (item is StackUpgradeItem) return wrapper.canRemoveStackUpgrade(item.multiplier())
                    return true
                }
                override fun onSlotChanged() {
                    super.onSlotChanged()
                    refreshFilterCache()
                }
            })
        }

        // Player main inventory (9×3)
        val playerInv = player.inventory
        for (row in 0..2) {
            for (col in 0..8) {
                val slotIndex = 9 + row * 9 + col
                addSlotToContainer(Slot(playerInv, slotIndex,
                    backpackOffsetX + LEFT_PAD + playerXOffset + col * SLOT_SIZE,
                    TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + row * SLOT_SIZE))
            }
        }

        // Player hotbar
        for (col in 0..8) {
            val isBackpackSlot = backpackSlotIndex == col
            addSlotToContainer(object : Slot(playerInv, col,
                backpackOffsetX + LEFT_PAD + playerXOffset + col * SLOT_SIZE,
                TOP_PAD + colSize * SLOT_SIZE + PLAYER_INV_GAP + 3 * SLOT_SIZE + HOTBAR_GAP) {
                override fun canTakeStack(player: EntityPlayer): Boolean =
                    !isBackpackSlot || super.canTakeStack(player)
            })
        }

        // Pre-allocate MAX_FILTER_SLOTS container slots for every upgrade position (even empty/non-filter ones).
        // SwappableFilterInventory delegates are set to null until a filter upgrade is placed.
        val filterRanges = mutableListOf<FilterSlotRange>()
        val filterCache  = mutableMapOf<Int, Triple<IBasicFilterable, ItemStack, Boolean>>()
        val swapInvs     = Array(wrapper.upgradeSlotsSize()) { SwappableFilterInventory() }
        for (i in 0 until wrapper.upgradeSlotsSize()) {
            val swapInv    = swapInvs[i]
            val rangeStart = inventorySlots.size
            for (j in 0 until MAX_FILTER_SLOTS) {
                addSlotToContainer(object : Slot(swapInv, j, -1000, -1000) {
                    override fun isItemValid(stack: ItemStack): Boolean {
                        val d = swapInv.delegate ?: return false
                        return j < d.size
                    }
                    override fun onSlotChanged() {
                        super.onSlotChanged()
                        val (filterable, upgradeStack, _) = filterCache[i] ?: return
                        val item = upgradeStack.item as? UpgradeItem ?: return
                        @Suppress("UNCHECKED_CAST")
                        if (filterable is UpgradeWrapper<*>) item.saveWrapper(upgradeStack, filterable)
                    }
                })
            }
            filterRanges.add(FilterSlotRange(i, rangeStart, rangeStart + MAX_FILTER_SLOTS))
        }
        filterSlotRanges = filterRanges
        filterWrapperCache = filterCache
        swappableFilterInvs = swapInvs

        // Populate cache for any filter upgrades already in the upgrade slots
        refreshFilterCache()
    }

    /**
     * Rebuilds filterWrapperCache and updates SwappableFilterInventory delegates from the current
     * upgrade slot contents. Called at init and whenever an upgrade is inserted or removed.
     */
    fun refreshFilterCache() {
        filterWrapperCache.clear()
        for (i in 0 until wrapper.upgradeSlotsSize()) {
            val upgradeStack = wrapper.upgradeItemStackHandler.inventory.getOrNull(i)
            val upgradeItem  = upgradeStack?.item as? UpgradeItem
            val upgradeWrap  = upgradeItem?.getWrapper(upgradeStack!!)
            val advanced     = upgradeWrap as? IAdvancedFilterable
            val filterable   = advanced ?: (upgradeWrap as? IBasicFilterable)

            if (filterable != null && upgradeStack != null) {
                swappableFilterInvs[i].delegate = filterable.filterItems
                filterWrapperCache[i] = Triple(filterable, upgradeStack, advanced != null)
            } else {
                swappableFilterInvs[i].delegate = null
            }
        }
    }

    override fun canInteractWith(player: EntityPlayer): Boolean = true

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
                    val limit = slotStack.maxStackSize * wrapper.getTotalStackMultiplier()
                    if (slotStack.stackSize < limit) {
                        val toAdd = minOf(cursor.stackSize, limit - slotStack.stackSize)
                        slotStack.stackSize += toAdd
                        slot.onSlotChanged()
                        cursor.stackSize -= toAdd
                        if (cursor.stackSize <= 0) player.inventory.itemStack = null
                    }
                    return slotStack.copy()
                }
                if (oversized) return null
            }
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

    override fun mergeItemStack(stack: ItemStack, startIndex: Int, endIndex: Int, reverseOrder: Boolean): Boolean {
        val multiplier = wrapper.getTotalStackMultiplier()
        if (multiplier <= 1) return super.mergeItemStack(stack, startIndex, endIndex, reverseOrder)

        var merged = false
        var k = if (reverseOrder) endIndex - 1 else startIndex

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

        // Check if the slot is a filter upgrade slot
        val filterRange = filterSlotRanges.firstOrNull { slotIndex in it.start until it.end }

        when {
            // From filter panel → player inventory
            filterRange != null -> {
                if (!mergeItemStack(stack, playerMainStart, hotbarEnd, true)) return null
            }
            slotIndex in backpackSlotStart until backpackSlotEnd -> {
                if (!mergeItemStack(stack, playerMainStart, hotbarEnd, true)) return null
            }
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
    }

    companion object {
        const val SLOT_SIZE         = 18
        const val TOP_PAD           = 11
        const val LEFT_PAD          = 7
        const val UPGRADE_COL_WIDTH = 36
        const val UPGRADE_GAP       = 2
        const val UPGRADE_SLOT_X    = 9
        const val PLAYER_INV_GAP    = 16
        const val HOTBAR_GAP        = 4
        const val UPGRADE_TOP_PAD   = 3
        const val MAX_FILTER_SLOTS  = 16  // max filter slots per upgrade (advanced = 16, basic = 9)
    }
}
