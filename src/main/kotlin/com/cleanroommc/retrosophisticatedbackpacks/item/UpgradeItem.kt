package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.StatCollector

abstract class UpgradeItem(val registryNameStr: String, val hasTab: Boolean = false) : ItemBase() {
    init {
        setCreativeTab(RetroSophisticatedBackpacks.CREATIVE_TAB)
        setUnlocalizedName(registryNameStr.asTranslationKey())
        setTextureName("${Tags.MOD_ID}:$registryNameStr")

        Items.ITEMS.add(this)
        RegistryHandler.MODELS.add(this)
    }

    /** Subclasses return a fresh, empty wrapper instance, or null if the item has no wrapper. */
    open fun createWrapper(): UpgradeWrapper<*>? = null

    /** Read the wrapper state from the ItemStack's tag compound, or null if the item has no wrapper. */
    fun getWrapper(stack: ItemStack): UpgradeWrapper<*>? {
        val wrapper = createWrapper() ?: return null
        val nbt = stack.tagCompound
        if (nbt != null) wrapper.deserializeNBT(nbt)
        return wrapper
    }

    /** Persist wrapper state back into the ItemStack's tag compound. */
    fun saveWrapper(stack: ItemStack, wrapper: UpgradeWrapper<*>) {
        stack.tagCompound = wrapper.serializeNBT()
    }

    // 1.7.10 addInformation signature: (ItemStack, EntityPlayer, List, Boolean)
    @Suppress("UNCHECKED_CAST", "OVERRIDE_DEPRECATION")
    override fun addInformation(stack: ItemStack, player: EntityPlayer?, tooltip: MutableList<*>, advanced: Boolean) {
        (tooltip as MutableList<String>).add(
            StatCollector.translateToLocal("tooltip.${registryNameStr}".asTranslationKey())
        )
    }
}
