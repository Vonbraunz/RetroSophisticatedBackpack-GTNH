package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.PickupUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class PickupUpgradeWrapper : BasicUpgradeWrapper<PickupUpgradeItem>(), IPickupUpgrade {
    override val settingsLangKey: String = "gui.pickup_settings".asTranslationKey()
    override fun canPickup(stack: ItemStack): Boolean = checkFilter(stack)
}
