package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.FilterUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

class AdvancedFilterUpgradeWrapper : AdvancedUpgradeWrapper<FilterUpgradeItem>(), IFilterUpgrade {
    override val settingsLangKey: String = "gui.advanced_filter_upgrade".asTranslationKey()
    override var filterWay: IFilterUpgrade.FilterWayType = IFilterUpgrade.FilterWayType.IN_OUT

    override fun canInsert(stack: ItemStack): Boolean =
        filterWay == IFilterUpgrade.FilterWayType.OUT || checkFilter(stack)

    override fun canExtract(stack: ItemStack): Boolean =
        filterWay == IFilterUpgrade.FilterWayType.IN || checkFilter(stack)

    override fun serializeNBT(): NBTTagCompound {
        val nbt = super.serializeNBT()
        nbt.setByte(IFilterUpgrade.FILTER_WAY_TAG, filterWay.ordinal.toByte())
        return nbt
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        filterWay = IFilterUpgrade.FilterWayType.entries[nbt.getByte(IFilterUpgrade.FILTER_WAY_TAG).toInt()]
    }
}
