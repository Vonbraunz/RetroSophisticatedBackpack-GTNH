package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import com.cleanroommc.retrosophisticatedbackpacks.util.INBTSerializable

sealed interface IFilterUpgrade : ISidelessCapabilityProvider, IToggleable, INBTSerializable<NBTTagCompound> {
    companion object {
        const val FILTER_WAY_TAG = "FilterWay"
    }

    var filterWay: FilterWayType

    fun canInsert(stack: ItemStack): Boolean

    fun canExtract(stack: ItemStack): Boolean

    enum class FilterWayType { IN_OUT, IN, OUT }
}
