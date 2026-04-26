package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

sealed interface IVoidUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun shouldVoid(stack: ItemStack): Boolean
}
