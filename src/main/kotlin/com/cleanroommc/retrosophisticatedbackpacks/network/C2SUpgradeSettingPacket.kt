package com.cleanroommc.retrosophisticatedbackpacks.network

import com.cleanroommc.retrosophisticatedbackpacks.capability.BackpackHelper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.AdvancedFeedingUpgradeWrapper
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IAdvancedFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IBasicFilterable
import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.IFilterUpgrade
import com.cleanroommc.retrosophisticatedbackpacks.common.gui.BackpackContainer
import com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem
import com.cleanroommc.retrosophisticatedbackpacks.item.UpgradeItem
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/** Cycle an upgrade setting on the server.
 *  settingId 0 = cycle filterType      (WHITELIST ↔ BLACKLIST)
 *  settingId 1 = cycle filterWay       (IN_OUT → IN → OUT → IN_OUT)    [filter upgrade only]
 *  settingId 2 = cycle matchType       (ITEM → MOD → ORE_DICT → ITEM)  [advanced only]
 *  settingId 3 = toggle ignoreDurability                                [advanced only]
 *  settingId 4 = toggle ignoreNBT                                       [advanced only]
 *  settingId 5 = cycle hungerFeedingStrategy                            [advanced feeding only]
 *  settingId 6 = cycle healthFeedingStrategy                            [advanced feeding only]
 */
class C2SUpgradeSettingPacket() : IRefinedMessage {
    private var upgradeSlotIndex = 0
    private var settingId = 0

    constructor(upgradeSlotIndex: Int, settingId: Int) : this() {
        this.upgradeSlotIndex = upgradeSlotIndex
        this.settingId = settingId
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(upgradeSlotIndex)
        buf.writeInt(settingId)
    }

    override fun fromBytes(buf: ByteBuf) {
        upgradeSlotIndex = buf.readInt()
        settingId = buf.readInt()
    }

    class Handler : INoReplyMessageHandler<C2SUpgradeSettingPacket> {
        override fun onMessage(message: C2SUpgradeSettingPacket, ctx: MessageContext): IRefinedMessage? {
            val player = ctx.serverHandler.playerEntity
            val container = player.openContainer as? BackpackContainer ?: return null

            val slotIdx = message.upgradeSlotIndex
            if (slotIdx < 0 || slotIdx >= container.wrapper.upgradeSlotsSize()) return null

            val stack = container.wrapper.upgradeItemStackHandler.inventory.getOrNull(slotIdx) ?: return null
            val upgradeItem = stack.item as? UpgradeItem ?: return null
            val wrapper = upgradeItem.getWrapper(stack) ?: return null

            when (message.settingId) {
                0 -> {
                    val filterable = wrapper as? IBasicFilterable ?: return null
                    val types = IBasicFilterable.FilterType.entries
                    filterable.filterType = types[(filterable.filterType.ordinal + 1) % types.size]
                }
                1 -> {
                    val filterUpgrade = wrapper as? IFilterUpgrade ?: return null
                    val ways = IFilterUpgrade.FilterWayType.entries
                    filterUpgrade.filterWay = ways[(filterUpgrade.filterWay.ordinal + 1) % ways.size]
                }
                2 -> {
                    val adv = wrapper as? IAdvancedFilterable ?: return null
                    val types = IAdvancedFilterable.MatchType.entries
                    adv.matchType = types[(adv.matchType.ordinal + 1) % types.size]
                }
                3 -> { (wrapper as? IAdvancedFilterable ?: return null).also { it.ignoreDurability = !it.ignoreDurability } }
                4 -> { (wrapper as? IAdvancedFilterable ?: return null).also { it.ignoreNBT = !it.ignoreNBT } }
                5 -> {
                    val adv = wrapper as? AdvancedFeedingUpgradeWrapper ?: return null
                    val values = AdvancedFeedingUpgradeWrapper.FeedingStrategy.Hunger.entries
                    adv.hungerFeedingStrategy = values[(adv.hungerFeedingStrategy.ordinal + 1) % values.size]
                }
                6 -> {
                    val adv = wrapper as? AdvancedFeedingUpgradeWrapper ?: return null
                    val values = AdvancedFeedingUpgradeWrapper.FeedingStrategy.HEALTH.entries
                    adv.healthFeedingStrategy = values[(adv.healthFeedingStrategy.ordinal + 1) % values.size]
                }
                else -> return null
            }

            upgradeItem.saveWrapper(stack, wrapper)
            container.refreshFilterCache()

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
