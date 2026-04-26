package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.UpgradeWrapper

class VoidUpgradeItem(registryName: String, wrapperFactory: () -> UpgradeWrapper<*>) :
    RankedUpgradeItem(registryName, wrapperFactory)
