package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.Tags
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IMagnetUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.handler.RegistryHandler
import com.cleanroommc.retrosophisticatedbackpacks.util.Utils.asTranslationKey
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
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
        val lines = tooltip as MutableList<String>
        lines.add(StatCollector.translateToLocal("tooltip.${registryNameStr}".asTranslationKey()))

        val wrapper = getWrapper(stack) ?: return

        val L  = EnumChatFormatting.GRAY.toString()
        val V  = EnumChatFormatting.WHITE.toString()
        val ON = EnumChatFormatting.GREEN.toString()
        val OFF = EnumChatFormatting.RED.toString()
        val DIM = EnumChatFormatting.DARK_GRAY.toString()

        if (wrapper is IToggleable) {
            lines.add("${L}Status: ${if (wrapper.enabled) "${ON}Enabled" else "${OFF}Disabled"}")
        }

        if (wrapper is IBasicFilterable) {
            val ft = if (wrapper.filterType == IBasicFilterable.FilterType.WHITELIST) "Whitelist" else "Blacklist"
            lines.add("${L}Filter: ${V}$ft")

            val items = (0 until wrapper.filterItems.size).mapNotNull { wrapper.filterItems.getStackInSlot(it) }
            if (items.isNotEmpty()) {
                val shown = items.take(4).joinToString(", ") { it.displayName }
                val extra = if (items.size > 4) " ${DIM}+${items.size - 4}" else ""
                lines.add("${L}Items: ${V}$shown$extra")
            }
        }

        if (wrapper is IAdvancedFilterable) {
            val mt = when (wrapper.matchType) {
                IAdvancedFilterable.MatchType.ITEM     -> "Item"
                IAdvancedFilterable.MatchType.MOD      -> "Mod"
                IAdvancedFilterable.MatchType.ORE_DICT -> "Ore Dict"
            }
            lines.add("${L}Match: ${V}$mt")
            lines.add("${L}Ignore Durability: ${if (wrapper.ignoreDurability) "${ON}Yes" else "${OFF}No"}")
            lines.add("${L}Ignore NBT: ${if (wrapper.ignoreNBT) "${ON}Yes" else "${OFF}No"}")
        }

        if (wrapper is IFilterUpgrade) {
            val fw = when (wrapper.filterWay) {
                IFilterUpgrade.FilterWayType.IN_OUT -> "In + Out"
                IFilterUpgrade.FilterWayType.IN     -> "In Only"
                IFilterUpgrade.FilterWayType.OUT    -> "Out Only"
            }
            lines.add("${L}Direction: ${V}$fw")
        }

        if (wrapper is AdvancedFeedingUpgradeWrapper) {
            val hunger = wrapper.hungerFeedingStrategy.name
                .lowercase().replaceFirstChar { it.uppercase() }
            val health = wrapper.healthFeedingStrategy.name
                .lowercase().replaceFirstChar { it.uppercase() }
            lines.add("${L}Hunger: ${V}$hunger")
            lines.add("${L}Health: ${V}$health")
        }

        if (wrapper is IMagnetUpgrade) {
            lines.add("${L}Range: ${V}${wrapper.range.toInt()} blocks")
        }
    }
}
