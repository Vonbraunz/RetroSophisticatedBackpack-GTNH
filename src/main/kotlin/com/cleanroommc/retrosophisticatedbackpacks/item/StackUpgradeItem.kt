package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector

class StackUpgradeItem(registryName: String, val multiplier: () -> Int) : UpgradeItem(registryName) {
    @Suppress("UNCHECKED_CAST", "OVERRIDE_DEPRECATION")
    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<*>, advanced: Boolean) {
        (tooltip as MutableList<String>).add(
            StatCollector.translateToLocalFormatted("tooltip.stack_upgrade".asTranslationKey(), multiplier())
        )
    }
}
