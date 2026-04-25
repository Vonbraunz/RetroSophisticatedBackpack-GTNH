package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import java.util.*

object CapabilityHandler {
    val BACKPACK_INVENTORY_CACHE: HashMap<UUID, BackpackWrapper> = HashMap()

    fun cacheBackpackInventory(backpackWrapper: BackpackWrapper) {
        if (BACKPACK_INVENTORY_CACHE.containsKey(backpackWrapper.uuid)) {
            backpackWrapper.isCached = true
            return
        }
        BACKPACK_INVENTORY_CACHE.put(backpackWrapper.uuid, backpackWrapper)
        backpackWrapper.isCached = true
        RetroSophisticatedBackpacks.LOGGER.info("Backpack ${backpackWrapper.uuid} is cached")
    }

    fun updateBackpackInventory(backpackWrapper: BackpackWrapper) {
        val backpack = BACKPACK_INVENTORY_CACHE[backpackWrapper.uuid] ?: return
        backpack.deserializeNBT(backpackWrapper.serializeNBT())
        RetroSophisticatedBackpacks.LOGGER.info("Backpack ${backpackWrapper.uuid} is updated")
    }
}
