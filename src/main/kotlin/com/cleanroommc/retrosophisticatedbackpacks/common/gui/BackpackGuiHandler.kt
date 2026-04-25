package com.cleanroommc.retrosophisticatedbackpacks.common.gui

import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackWrapper
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.tileentity.BackpackTileEntity
import cpw.mods.fml.common.network.IGuiHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

object BackpackGuiHandler : IGuiHandler {

    /** GUI opened from a placed BackpackBlock tile entity. */
    const val BACKPACK_TILE_GUI_ID = 0

    /** GUI opened from a backpack item held in the player's inventory. */
    const val BACKPACK_ITEM_GUI_ID = 1

    // ---- Server side -------------------------------------------------------

    override fun getServerGuiElement(
        id: Int, player: EntityPlayer, world: World,
        x: Int, y: Int, z: Int,
    ): Any? = when (id) {
        BACKPACK_TILE_GUI_ID -> {
            val te = world.getTileEntity(x, y, z) as? BackpackTileEntity ?: return null
            BackpackContainer(te.wrapper, null, player)
        }
        BACKPACK_ITEM_GUI_ID -> {
            val slotIndex = x
            val (wrapper, slot) = resolveItemWrapper(player, slotIndex) ?: return null
            object : BackpackContainer(wrapper, slot, player) {
                override fun onContainerClosed(player: EntityPlayer) {
                    super.onContainerClosed(player)
                    val stack = player.inventory.mainInventory[slot] ?: return
                    if (stack.item is BackpackItem) BackpackHelper.saveWrapper(stack, wrapper)
                }
            }
        }
        else -> null
    }

    // ---- Client side -------------------------------------------------------

    override fun getClientGuiElement(
        id: Int, player: EntityPlayer, world: World,
        x: Int, y: Int, z: Int,
    ): Any? = when (id) {
        BACKPACK_TILE_GUI_ID -> {
            val te = world.getTileEntity(x, y, z) as? BackpackTileEntity ?: return null
            RetroSophisticatedBackpacks.proxy?.createBackpackGui(BackpackContainer(te.wrapper, null, player))
        }
        BACKPACK_ITEM_GUI_ID -> {
            val slotIndex = x
            val (wrapper, slot) = resolveItemWrapper(player, slotIndex) ?: return null
            RetroSophisticatedBackpacks.proxy?.createBackpackGui(BackpackContainer(wrapper, slot, player))
        }
        else -> null
    }

    // ---- Helpers -----------------------------------------------------------

    private fun resolveItemWrapper(player: EntityPlayer, slotIndex: Int): Pair<BackpackWrapper, Int>? {
        val stack = player.inventory.mainInventory.getOrNull(slotIndex) ?: return null
        if (stack.item !is BackpackItem) return null
        val wrapper = BackpackHelper.getWrapper(stack) ?: return null
        return wrapper to slotIndex
    }
}
