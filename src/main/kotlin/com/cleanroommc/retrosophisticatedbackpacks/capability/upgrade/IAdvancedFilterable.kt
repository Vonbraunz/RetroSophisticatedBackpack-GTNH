package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

interface IAdvancedFilterable : IBasicFilterable {
    companion object {
        const val MATCH_TYPE_TAG = "MatchType"
        const val IGNORE_DURABILITY_TAG = "IgnoreDurability"
        const val IGNORE_NBT_TAG = "IgnoreNbt"
        const val ORE_DICT_LIST_TAG = "OreDict"
    }

    var matchType: MatchType
    var oreDictEntries: MutableList<String>
    var ignoreDurability: Boolean
    var ignoreNBT: Boolean

    override fun checkFilter(stack: ItemStack): Boolean = when (matchType) {
        MatchType.ITEM -> matchItem(stack)
        MatchType.MOD -> matchMod(stack)
        MatchType.ORE_DICT -> matchOreDict(stack)
    }

    private fun matchItem(stack: ItemStack): Boolean {
        val filterResult = BooleanArray(16)
        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            if (filterStack == null || filterStack.item != stack.item) continue
            filterResult[i] = matchItemInfo(stack, filterStack)
        }
        return when (filterType) {
            IBasicFilterable.FilterType.WHITELIST -> filterResult.any { it }
            IBasicFilterable.FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchMod(stack: ItemStack): Boolean {
        val stackModId = cpw.mods.fml.common.registry.GameRegistry
            .findUniqueIdentifierFor(stack.item)?.modId ?: return false
        val filterResult = BooleanArray(16)
        for ((i, filterStack) in filterItems.inventory.withIndex()) {
            if (filterStack == null || filterStack.stackSize <= 0) continue
            val filterModId = cpw.mods.fml.common.registry.GameRegistry
                .findUniqueIdentifierFor(filterStack.item)?.modId ?: continue
            filterResult[i] = stackModId == filterModId
        }
        return when (filterType) {
            IBasicFilterable.FilterType.WHITELIST -> filterResult.any { it }
            IBasicFilterable.FilterType.BLACKLIST -> filterResult.none { it }
        }
    }

    private fun matchOreDict(stack: ItemStack): Boolean {
        val stackOreDictionaries = OreDictionary.getOreIDs(stack).map { OreDictionary.getOreName(it) }
        for (oreDictEntry in oreDictEntries) {
            val regex = Regex(oreDictEntry)
            val matchResult = stackOreDictionaries.any { regex.matches(it) }
            if (filterType == IBasicFilterable.FilterType.WHITELIST && matchResult) return true
            if (filterType == IBasicFilterable.FilterType.BLACKLIST && matchResult) return false
        }
        return false
    }

    private fun matchItemInfo(stack: ItemStack, filterStack: ItemStack): Boolean {
        if (filterStack.stackSize <= 0) return false
        var flag = if (ignoreDurability) filterStack.item == stack.item && ItemStack.areItemStackTagsEqual(filterStack, stack)
                   else filterStack.isItemEqual(stack)
        if (!ignoreNBT) flag = flag && filterStack.tagCompound == stack.tagCompound
        return flag
    }

    enum class MatchType { ITEM, MOD, ORE_DICT }

    object Impl : IAdvancedFilterable {
        override val filterItems: ExposedItemStackHandler get() = ExposedItemStackHandler(0)
        override var filterType: IBasicFilterable.FilterType get() = IBasicFilterable.Impl.filterType; set(_) {}
        override fun checkFilter(itemStack: ItemStack): Boolean = false
        override var matchType: MatchType get() = MatchType.ITEM; set(_) {}
        override var oreDictEntries: MutableList<String> get() = mutableListOf(); set(_) {}
        override var ignoreNBT: Boolean get() = false; set(_) {}
        override var ignoreDurability: Boolean get() = false; set(_) {}
    }
}
