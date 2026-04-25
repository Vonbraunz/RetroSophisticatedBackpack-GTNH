package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.DepositUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class DepositUpgradeWrapper : BasicUpgradeWrapper<DepositUpgradeItem>(), IDepositUpgrade {
    override val settingsLangKey: String = "gui.deposit_settings".asTranslationKey()
    override fun canDeposit(stack: ItemStack): Boolean = checkFilter(stack)
}
