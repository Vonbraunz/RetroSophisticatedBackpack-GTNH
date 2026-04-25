package com.cleanroommc.retrosophisticatedbackpacks.client

import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly

object RSBBlockRenderTypes {
    var BACKPACK_RENDER_TYPE: Int = 0
        private set

    @SideOnly(Side.CLIENT)
    fun register() {
        BACKPACK_RENDER_TYPE = RenderingRegistry.getNextAvailableRenderId()
        RenderingRegistry.registerBlockHandler(BackpackBlockRenderer)
    }
}
