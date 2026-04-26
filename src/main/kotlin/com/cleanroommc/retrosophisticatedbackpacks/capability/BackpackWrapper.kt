package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.backpack.SortType
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.inventory.BackpackItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.inventory.UpgradeItemStackHandler
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.ExponentialStackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.InceptionUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.StackUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.BackpackItemStackHelper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable
import java.util.*

class BackpackWrapper(
    var backpackInventorySize: () -> Int = { 27 },
    var upgradeSlotsSize: () -> Int = { 1 },
    var uuid: UUID = UUID.randomUUID(),
) : INBTSerializable<NBTTagCompound> {
    companion object {
        private const val BACKPACK_INVENTORY_TAG = "BackpackInventory"
        private const val UPGRADE_SLOTS_TAG = "UpgradeSlots"
        private const val BACKPACK_INVENTORY_SIZE_TAG = "BackpackInventorySize"
        private const val UPGRADE_SLOTS_SIZE_TAG = "UpgradeSlotsSize"
        private const val MAIN_COLOR_TAG = "MainColor"
        private const val ACCENT_COLOR_TAG = "AccentColor"
        private const val MEMORY_STACK_ITEMS_TAG = "MemoryItems"
        private const val MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT"
        private const val SORT_TYPE_TAG = "SortType"
        private const val LOCKED_SLOTS_TAG = "LockedSlots"
        private const val UUID_TAG = "UUID"

        const val DEFAULT_MAIN_COLOR: Int = -0x339ec6
        const val DEFAULT_ACCENT_COLOR: Int = -0x9dd1e6
    }

    var isCached: Boolean = false
    var backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
    var upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())
    var sortType: SortType = SortType.BY_NAME
    var mainColor = DEFAULT_MAIN_COLOR
    var accentColor = DEFAULT_ACCENT_COLOR

    // ---------- Upgrade dispatch ----------

    /** Gather all upgrade wrappers from the upgrade slots that implement [T]. */
    inline fun <reified T : Any> gatherUpgrades(): List<T> =
        upgradeItemStackHandler.inventory
            .filterNotNull()
            .filter { it.stackSize > 0 }
            .mapNotNull { stack -> (stack.item as? UpgradeItem)?.getWrapper(stack) as? T }

    // ---------- Stack multiplier helpers ----------

    fun isStackedByMultiplication(): Boolean =
        upgradeItemStackHandler.inventory.filterNotNull()
            .map { it.item }.filterIsInstance<ExponentialStackUpgradeItem>().any()

    private fun getStackMultiplyFunction(condition: Boolean): (Int, Int) -> Int =
        if (condition) Int::times else Int::plus

    fun getTotalStackMultiplier(): Int = getTotalStackMultiplier(isStackedByMultiplication())

    fun getTotalStackMultiplier(condition: Boolean): Int {
        val base = if (condition) 1 else 0
        val stackUpgradeItems = upgradeItemStackHandler.inventory.filterNotNull()
            .map { it.item }.filterIsInstance<StackUpgradeItem>()
        val func = getStackMultiplyFunction(condition)
        if (!condition && stackUpgradeItems.isEmpty()) return 1
        return stackUpgradeItems.fold(base) { acc, item -> func(acc, item.multiplier()) }
    }

    fun canAddStackUpgrade(newMultiplier: Int): Boolean {
        return try {
            Math.multiplyExact(getTotalStackMultiplier() * 64, newMultiplier)
            true
        } catch (_: ArithmeticException) { false }
    }

    fun canRemoveStackUpgrade(originalMultiplier: Int): Boolean = canReplaceStackUpgrade(originalMultiplier, 1)

    fun canReplaceStackUpgrade(oldMultiplier: Int, newMultiplier: Int): Boolean {
        val newMult = getTotalStackMultiplier() / oldMultiplier * newMultiplier
        for (stack in backpackItemStackHandler.inventory) {
            if (stack == null || stack.stackSize <= 0) continue
            if (stack.stackSize > stack.maxStackSize * newMult) return false
        }
        return true
    }

    fun canAddExponentialStackUpgrade(): Boolean {
        return try {
            upgradeItemStackHandler.inventory.filterNotNull()
                .map { it.item }.filterIsInstance<StackUpgradeItem>()
                .fold(64) { acc, item -> Math.multiplyExact(acc, item.multiplier()) }
            true
        } catch (_: ArithmeticException) { false }
    }

    fun canRemoveExponentialStackUpgrade(): Boolean {
        val byAddMultiplier = getTotalStackMultiplier(false)
        for (stack in backpackItemStackHandler.inventory) {
            if (stack == null || stack.stackSize <= 0) continue
            if (stack.stackSize > stack.maxStackSize * byAddMultiplier) return false
        }
        return true
    }

    fun canNestBackpack(): Boolean =
        upgradeItemStackHandler.inventory.filterNotNull()
            .map { it.item }.filterIsInstance<InceptionUpgradeItem>().any()

    fun canRemoveInceptionUpgrade(): Boolean =
        !backpackItemStackHandler.inventory.filterNotNull()
            .map { it.item }.filterIsInstance<BackpackItem>().any() ||
                upgradeItemStackHandler.inventory.filterNotNull()
                    .map { it.item }.filterIsInstance<InceptionUpgradeItem>().count() > 1

    // ---------- Upgrade functionality ----------

    fun canPickupItem(stack: ItemStack): Boolean =
        gatherUpgrades<IPickupUpgrade>()
            .any { (it as? IToggleable)?.enabled != false && it.canPickup(stack) }

    fun feed(entity: EntityPlayer, handler: BackpackItemStackHandler): Boolean {
        for (upgrade in gatherUpgrades<IFeedingUpgrade>()) {
            if ((upgrade as? IToggleable)?.enabled == false) continue
            return upgrade.feed(entity, handler)
        }
        return false
    }

    fun canDeposit(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex) ?: return false
        return gatherUpgrades<IDepositUpgrade>()
            .any { (it as? IToggleable)?.enabled != false && it.canDeposit(stack) }
    }

    fun canRestock(stack: ItemStack): Boolean =
        gatherUpgrades<IRestockUpgrade>()
            .any { (it as? IToggleable)?.enabled != false && it.canRestock(stack) }

    fun canInsert(stack: ItemStack): Boolean {
        val filterUpgrades = gatherUpgrades<IFilterUpgrade>().filter { it.enabled }
        return if (filterUpgrades.isEmpty()) true else filterUpgrades.any { it.canInsert(stack) }
    }

    fun canExtract(slotIndex: Int): Boolean {
        val stack = getStackInSlot(slotIndex) ?: return true
        val filterUpgrades = gatherUpgrades<IFilterUpgrade>().filter { it.enabled }
        return if (filterUpgrades.isEmpty()) true else filterUpgrades.any { it.canInsert(stack) }
    }

    // ---------- Slot access ----------

    fun getSlots(): Int = backpackItemStackHandler.slots
    fun getStackInSlot(index: Int): ItemStack? = backpackItemStackHandler.getStackInSlot(index)
    fun insertItem(slot: Int, stack: ItemStack?, simulate: Boolean): ItemStack? = backpackItemStackHandler.insertItem(slot, stack, simulate)
    fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack? = backpackItemStackHandler.extractItem(slot, amount, simulate)

    // ---------- Memory / lock settings ----------

    fun isSlotMemorized(slotIndex: Int): Boolean =
        backpackItemStackHandler.memorizedSlotStack[slotIndex] != null

    fun getMemorizedStack(slotIndex: Int): ItemStack? =
        backpackItemStackHandler.memorizedSlotStack[slotIndex]

    fun setMemoryStack(slotIndex: Int, respectNBT: Boolean) {
        val current = getStackInSlot(slotIndex) ?: return
        val copy = current.copy().also { it.stackSize = 1 }
        backpackItemStackHandler.memorizedSlotStack[slotIndex] = copy
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = respectNBT
    }

    fun unsetMemoryStack(slotIndex: Int) {
        backpackItemStackHandler.memorizedSlotStack[slotIndex] = null
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = false
    }

    fun isMemoryStackRespectNBT(slotIndex: Int): Boolean =
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex]

    fun setMemoryStackRespectNBT(slotIndex: Int, respect: Boolean) {
        backpackItemStackHandler.memorizedSlotRespectNbtList[slotIndex] = respect
    }

    fun isSlotLocked(slotIndex: Int): Boolean = backpackItemStackHandler.sortLockedSlots[slotIndex]
    fun setSlotLocked(slotIndex: Int, locked: Boolean) { backpackItemStackHandler.sortLockedSlots[slotIndex] = locked }

    fun getSortableSlotIndexes(): List<Int> =
        (0 until backpackInventorySize()).filter {
            !backpackItemStackHandler.sortLockedSlots[it] &&
                    backpackItemStackHandler.memorizedSlotStack[it] == null
        }

    fun getDisplayName(): String = "container.backpack".asTranslationKey()

    // ---------- NBT ----------

    override fun serializeNBT(): NBTTagCompound {
        val nbt = NBTTagCompound()
        val backpackNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(backpackNbt, backpackItemStackHandler.inventory)
        nbt.setTag(BACKPACK_INVENTORY_TAG, backpackNbt)
        val upgradesNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(upgradesNbt, upgradeItemStackHandler.inventory)
        nbt.setTag(UPGRADE_SLOTS_TAG, upgradesNbt)
        nbt.setInteger(BACKPACK_INVENTORY_SIZE_TAG, backpackInventorySize())
        nbt.setInteger(UPGRADE_SLOTS_SIZE_TAG, upgradeSlotsSize())
        nbt.setInteger(MAIN_COLOR_TAG, mainColor)
        nbt.setInteger(ACCENT_COLOR_TAG, accentColor)
        val memoryNbt = NBTTagCompound()
        BackpackItemStackHelper.saveAllSlotsExtended(memoryNbt, backpackItemStackHandler.memorizedSlotStack)
        nbt.setTag(MEMORY_STACK_ITEMS_TAG, memoryNbt)
        nbt.setByteArray(
            MEMORY_STACK_RESPECT_NBT_TAG,
            backpackItemStackHandler.memorizedSlotRespectNbtList.map { if (it) 1.toByte() else 0 }.toByteArray()
        )
        nbt.setByte(SORT_TYPE_TAG, sortType.ordinal.toByte())
        nbt.setByteArray(
            LOCKED_SLOTS_TAG,
            backpackItemStackHandler.sortLockedSlots.map { if (it) 1 else 0 }.map(Int::toByte).toByteArray()
        )
        nbt.setString(UUID_TAG, uuid.toString())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        if (nbt.hasKey(BACKPACK_INVENTORY_SIZE_TAG)) backpackInventorySize = { nbt.getInteger(BACKPACK_INVENTORY_SIZE_TAG) }
        if (nbt.hasKey(UPGRADE_SLOTS_SIZE_TAG)) upgradeSlotsSize = { nbt.getInteger(UPGRADE_SLOTS_SIZE_TAG) }
        uuid = if (nbt.hasKey(UUID_TAG)) UUID.fromString(nbt.getString(UUID_TAG)) else UUID.randomUUID()
        backpackItemStackHandler = BackpackItemStackHandler(backpackInventorySize(), this)
        upgradeItemStackHandler = UpgradeItemStackHandler(upgradeSlotsSize())
        mainColor = nbt.getInteger(MAIN_COLOR_TAG)
        accentColor = nbt.getInteger(ACCENT_COLOR_TAG)
        if (nbt.hasKey(BACKPACK_INVENTORY_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(nbt.getCompoundTag(BACKPACK_INVENTORY_TAG), backpackItemStackHandler.inventory)
        if (nbt.hasKey(UPGRADE_SLOTS_TAG))
            BackpackItemStackHelper.loadAllItemsExtended(nbt.getCompoundTag(UPGRADE_SLOTS_TAG), upgradeItemStackHandler.inventory)
        BackpackItemStackHelper.loadAllItemsExtended(nbt.getCompoundTag(MEMORY_STACK_ITEMS_TAG), backpackItemStackHandler.memorizedSlotStack)
        nbt.getByteArray(MEMORY_STACK_RESPECT_NBT_TAG).forEachIndexed { i, b -> setMemoryStackRespectNBT(i, b.toInt() != 0) }
        nbt.getByteArray(LOCKED_SLOTS_TAG).forEachIndexed { i, b -> setSlotLocked(i, b.toInt() != 0) }
        sortType = SortType.entries[nbt.getByte(SORT_TYPE_TAG).toInt()]
    }
}
