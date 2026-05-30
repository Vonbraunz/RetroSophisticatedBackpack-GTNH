package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.backpack.BackpackInventoryHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2SSortPacket : IRefinedMessage {
    override fun toBytes(buf: ByteBuf) {}
    override fun fromBytes(buf: ByteBuf) {}

    class Handler : INoReplyMessageHandler<C2SSortPacket> {
        override fun onMessage(message: C2SSortPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.playerEntity
            val container = player.openContainer as? BackpackContainer ?: return null

            BackpackInventoryHelper.sortInventory(container.wrapper)

            val backpackSlot = container.backpackSlotIndex
            if (backpackSlot != null) {
                val backpackStack = player.inventory.mainInventory.getOrNull(backpackSlot)
                if (backpackStack?.item is BackpackItem) {
                    BackpackHelper.saveWrapper(backpackStack, container.wrapper)
                }
            }

            return null
        }
    }
}
