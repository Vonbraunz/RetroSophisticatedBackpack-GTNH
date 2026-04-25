package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable

sealed interface IDepositUpgrade : ISidelessCapabilityProvider, INBTSerializable<NBTTagCompound> {
    fun canDeposit(stack: ItemStack): Boolean
    /** Basic deposit only stacks into existing matching slots; advanced also fills empty slots. */
    val depositsToEmptySlots: Boolean get() = false
}
