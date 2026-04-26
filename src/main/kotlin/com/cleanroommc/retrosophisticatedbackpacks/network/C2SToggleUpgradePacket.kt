package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IToggleable
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2SToggleUpgradePacket() : IRefinedMessage {
    private var upgradeSlotIndex = 0

    constructor(upgradeSlotIndex: Int) : this() {
        this.upgradeSlotIndex = upgradeSlotIndex
    }

    override fun toBytes(buf: ByteBuf) { buf.writeInt(upgradeSlotIndex) }
    override fun fromBytes(buf: ByteBuf) { upgradeSlotIndex = buf.readInt() }

    class Handler : INoReplyMessageHandler<C2SToggleUpgradePacket> {
        override fun onMessage(message: C2SToggleUpgradePacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.playerEntity
            val container = player.openContainer as? BackpackContainer ?: return null

            val slotIdx = message.upgradeSlotIndex
            if (slotIdx < 0 || slotIdx >= container.wrapper.upgradeSlotsSize()) return null

            val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(slotIdx) ?: return null
            val upgradeItem = stack.item as? UpgradeItem ?: return null
            val wrapper = upgradeItem.getWrapper(stack) ?: return null
            val toggleable = wrapper as? IToggleable ?: return null

            toggleable.toggle()
            upgradeItem.saveWrapper(stack, wrapper)

            // Persist immediately for item-held backpacks
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
