package com.cleanroommc.retrosophisticatedbackpacks.integration.nei

import codechicken.nei.api.API

object NEIIntegration {
    fun register() {
        val handler = BackpackUpgradeHandler()
        API.registerRecipeHandler(handler)
        API.registerUsageHandler(handler)
    }
}
