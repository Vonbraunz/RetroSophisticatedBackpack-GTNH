package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

sealed interface IMagnetUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    val range: Double
    fun canMagnet(stack: ItemStack): Boolean
}
