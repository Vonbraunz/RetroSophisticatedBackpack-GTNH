package com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade

import com.cleanroommc.retrosophisticatedbackpacks.capability.ISidelessCapabilityProvider
import com.cleanroommc.retrosophisticatedbackpacks.inventory.ExposedItemStackHandler
import net.minecraft.item.ItemStack

interface IBasicFilterable : ISidelessCapabilityProvider {
    companion object {
        const val FILTER_ITEMS_TAG = "FilterItems"
        const val FILTER_TYPE_TAG = "FilterType"
    }

    val filterItems: ExposedItemStackHandler
    var filterType: FilterType

    fun checkFilter(stack: ItemStack): Boolean = when (filterType) {
        FilterType.WHITELIST -> {
            val hasFilter = filterItems.inventory.any { it != null && it.stackSize > 0 }
            !hasFilter || filterItems.inventory.any { it != null && it.item == stack.item && ItemStack.areItemStackTagsEqual(it, stack) }
        }
        FilterType.BLACKLIST -> filterItems.inventory.none { it != null && it.item == stack.item && ItemStack.areItemStackTagsEqual(it, stack) }
    }

    enum class FilterType {
        WHITELIST,
        BLACKLIST;
    }

    object Impl : IBasicFilterable {
        override val filterItems: ExposedItemStackHandler
            get() = ExposedItemStackHandler(0)
        override var filterType: FilterType
            get() = FilterType.WHITELIST
            set(_) {}

        override fun checkFilter(itemStack: ItemStack): Boolean = false
    }
}
