package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.RestockUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class RestockUpgradeWrapper : BasicUpgradeWrapper<RestockUpgradeItem>(), IRestockUpgrade {
    override val settingsLangKey: String = "gui.restock_settings".asTranslationKey()
    override fun canRestock(stack: ItemStack): Boolean = checkFilter(stack)
}
