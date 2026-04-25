package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.CraftingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector

class CraftingUpgradeItem(registryName: String) : UpgradeItem(registryName, true) {
    override fun createWrapper(): CraftingUpgradeWrapper = CraftingUpgradeWrapper()

    @Suppress("UNCHECKED_CAST", "OVERRIDE_DEPRECATION")
    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<*>, advanced: Boolean) {
        (tooltip as MutableList<String>).add(
            StatCollector.translateToLocal("tooltip.crafting_upgrade".asTranslationKey())
        )
    }
}
