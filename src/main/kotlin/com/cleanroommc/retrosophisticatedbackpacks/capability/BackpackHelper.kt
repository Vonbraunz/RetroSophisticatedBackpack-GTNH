package com.cleanroommc.retrosophisticatedbackpacks.capability

import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

/** Static accessors replacing the Forge Capability system for BackpackWrapper. */
object BackpackHelper {
    const val BACKPACK_DATA_TAG = "BackpackCapability"
    private const val UUID_TAG = "UUID"

    fun getWrapper(stack: ItemStack?): BackpackWrapper? {
        if (stack == null || stack.stackSize <= 0) return null
        val item = stack.item as? BackpackItem ?: return null
        val nbt = stack.tagCompound
        // Always derive sizes from item type so server and client agree on slot count,
        // regardless of stale sizes that may be stored in old NBT.
        val wrapper = BackpackWrapper({ item.numberOfSlots }, { item.numberOfUpgradeSlots })
        if (nbt != null) {
            val dataTag = nbt.getCompoundTag(BACKPACK_DATA_TAG)
            dataTag.setInteger("BackpackInventorySize", item.numberOfSlots)
            dataTag.setInteger("UpgradeSlotsSize", item.numberOfUpgradeSlots)
            wrapper.deserializeNBT(dataTag)
        }
        return wrapper
    }

    fun saveWrapper(stack: ItemStack, wrapper: BackpackWrapper) {
        var nbt = stack.tagCompound
        if (nbt == null) {
            nbt = NBTTagCompound()
            stack.tagCompound = nbt
        }
        nbt.setTag(BACKPACK_DATA_TAG, wrapper.serializeNBT())
    }
}
