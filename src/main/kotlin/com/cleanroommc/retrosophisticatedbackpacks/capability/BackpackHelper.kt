package com.cleanroommc.retrosophisticatedbackpacks.capability

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

/** Static accessors replacing the Forge Capability system for BackpackWrapper. */
object BackpackHelper {
    private const val BACKPACK_DATA_TAG = "BackpackCapability"

    fun getWrapper(stack: ItemStack?): BackpackWrapper? {
        if (stack == null || stack.stackSize <= 0) return null
        val nbt = stack.tagCompound ?: return BackpackWrapper()
        val dataTag = nbt.getCompoundTag(BACKPACK_DATA_TAG)
        val wrapper = BackpackWrapper()
        wrapper.deserializeNBT(dataTag)
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
