package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable

sealed interface IRestockUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun canRestock(stack: ItemStack): Boolean
}
