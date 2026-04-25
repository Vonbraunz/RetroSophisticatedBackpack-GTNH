package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.RestockUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedRestockUpgradeWrapper : AdvancedUpgradeWrapper<RestockUpgradeItem>(), IRestockUpgrade {
    override val settingsLangKey: String = "gui.advanced_restock_settings".asTranslationKey()
    override fun canRestock(stack: ItemStack): Boolean = super.checkFilter(stack)
    override val restocksFromEmptySlots: Boolean get() = true
}
