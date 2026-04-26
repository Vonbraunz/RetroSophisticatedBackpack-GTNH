package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.item.VoidUpgradeItem
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.item.ItemStack

class VoidUpgradeWrapper : BasicUpgradeWrapper<VoidUpgradeItem>(), IVoidUpgrade {
    override val settingsLangKey: String = "gui.void_settings".asTranslationKey()
    override fun shouldVoid(stack: ItemStack): Boolean = checkFilter(stack)
}
