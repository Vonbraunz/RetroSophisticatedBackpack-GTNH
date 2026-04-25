package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper

abstract class RankedUpgradeItem(
    registryName: String,
    private val wrapperFactory: () -> UpgradeWrapper<*>,
) : UpgradeItem(registryName, true) {
    override fun createWrapper(): UpgradeWrapper<*> = wrapperFactory()
}
