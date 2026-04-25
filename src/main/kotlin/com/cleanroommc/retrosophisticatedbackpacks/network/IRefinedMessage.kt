package com.cleanroommc.retrosophisticatedbackpacks.network

import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

interface IRefinedMessage : IMessage {
    override fun toBytes(buf: ByteBuf)
    override fun fromBytes(buf: ByteBuf)
}
