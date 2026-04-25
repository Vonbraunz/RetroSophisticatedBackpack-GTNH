package com.cleanroommc.retrosophisticatedbackpacks.network

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext

typealias INoReplyMessageHandler<REQ> = IRefinedMessageHandler<REQ, IRefinedMessage>

interface IRefinedMessageHandler<REQ, REPLY> : IMessageHandler<REQ, REPLY>
        where REQ : IRefinedMessage, REPLY : IRefinedMessage {
    override fun onMessage(message: REQ, ctx: MessageContext): REPLY?
}
