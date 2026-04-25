package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

sealed interface IPickupUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun canPickup(stack: ItemStack): Boolean
}
