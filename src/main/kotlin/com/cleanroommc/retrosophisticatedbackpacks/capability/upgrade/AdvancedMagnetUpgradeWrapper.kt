package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.MagnetUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class AdvancedMagnetUpgradeWrapper : AdvancedUpgradeWrapper<MagnetUpgradeItem>(), IMagnetUpgrade {
    override val settingsLangKey: String = "gui.magnet_settings".asTranslationKey()
    override val range = 9.0
    override fun canMagnet(stack: ItemStack): Boolean = checkFilter(stack)
}
