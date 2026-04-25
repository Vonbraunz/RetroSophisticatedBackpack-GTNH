package com.cleanroommc.retrosophisticatedbackpacks.handler

import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import cpw.mods.fml.client.event.ConfigChangedEvent
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import com.cleanroommc.retrosophisticatedbackpacks.Tags

// Registered manually in RetroSophisticatedBackpacks.init via MinecraftForge.EVENT_BUS.register
object ConfigHandler {
    @SubscribeEvent
    fun onConfigChange(event: ConfigChangedEvent.OnConfigChangedEvent) {
        if (event.modID == Tags.MOD_ID) {
            Config.syncConfig()
        }
    }
}
