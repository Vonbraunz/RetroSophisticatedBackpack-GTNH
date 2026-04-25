package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackGuiHandler
import com.cleanroommc.retrosophisticatedbackpacks.RetroSophisticatedBackpacks
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2SOpenBackpackPacket() : IRefinedMessage {
    private var slotIndex = 0

    constructor(slotIndex: Int) : this() {
        this.slotIndex = slotIndex
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(slotIndex)
    }

    override fun fromBytes(buf: ByteBuf) {
        slotIndex = buf.readInt()
    }

    class Handler : INoReplyMessageHandler<C2SOpenBackpackPacket> {
        override fun onMessage(message: C2SOpenBackpackPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.playerEntity
            val world = player.worldObj
            player.openGui(
                RetroSophisticatedBackpacks.instance,
                BackpackGuiHandler.BACKPACK_ITEM_GUI_ID,
                world, 0, 0, 0
            )
            return null
        }
    }
}
