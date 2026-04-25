package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

// TODO: return real Container/GuiContainer when GUI layer is ported
object BackpackGuiHandler : IGuiHandler {
    const val BACKPACK_TILE_GUI_ID = 0
    const val BACKPACK_ITEM_GUI_ID = 1

    override fun getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? = null

    override fun getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any? = null
}
