package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.StatCollector

class StackUpgradeItem(registryName: String, val multiplier: () -> Int) : UpgradeItem(registryName) {
    @Suppress("UNCHECKED_CAST", "OVERRIDE_DEPRECATION")
    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<*>, advanced: Boolean) {
        val lines = tooltip as MutableList<String>
        val mult = multiplier()
        val L = EnumChatFormatting.GRAY.toString()
        val V = EnumChatFormatting.WHITE.toString()
        lines.add(StatCollector.translateToLocalFormatted("tooltip.stack_upgrade".asTranslationKey(), mult))
        lines.add("${L}Stack limit: ${V}×$mult ${L}(up to ${V}${mult * 64}${L} per slot)")
    }
}
