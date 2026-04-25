package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.DepositUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedDepositUpgradeWrapper : AdvancedUpgradeWrapper<DepositUpgradeItem>(), IDepositUpgrade {
    override val settingsLangKey: String = "gui.advanced_deposit_settings".asTranslationKey()
    override fun canDeposit(stack: ItemStack): Boolean = checkFilter(stack)
    override val depositsToEmptySlots: Boolean get() = true
}
