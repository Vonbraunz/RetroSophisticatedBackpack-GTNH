package com.cleanroommc.retrosophisticatedbackpacks.item

import com.cleanroommc.retrosophisticatedbackpacks.capability.upgrade.*
import com.cleanroommc.retrosophisticatedbackpacks.config.Config
import net.minecraft.item.Item

@Suppress("UNUSED")
object Items {
    @JvmField
    val ITEMS = mutableListOf<Item>()

    @JvmField
    val BACKPACK_ITEMS = mutableListOf<BackpackItem>()

    // Set by RegistryHandler.registerBlocks() after GameRegistry creates them
    @JvmField
    var backpackLeather: BackpackItem? = null

    @JvmField
    var backpackIron: BackpackItem? = null

    @JvmField
    var backpackGold: BackpackItem? = null

    @JvmField
    var backpackDiamond: BackpackItem? = null

    @JvmField
    var backpackObsidian: BackpackItem? = null

    // Upgrade items — created eagerly; add themselves to ITEMS in their init blocks
    @JvmField
    val upgradeBase = UpgradeBaseItem("upgrade_base")

    @JvmField
    val stackUpgradeTierStarter = StackUpgradeItem("stack_upgrade_starter_tier", Config.stackUpgrade::leatherMultiplier)

    @JvmField
    val exponentialStackUpgrade = ExponentialStackUpgradeItem("exponential_stack_upgrade")

    @JvmField
    val stackUpgradeTier1 = StackUpgradeItem("stack_upgrade_tier_1", Config.stackUpgrade::ironMultiplier)

    @JvmField
    val stackUpgradeTier2 = StackUpgradeItem("stack_upgrade_tier_2", Config.stackUpgrade::goldMultiplier)

    @JvmField
    val stackUpgradeTier3 = StackUpgradeItem("stack_upgrade_tier_3", Config.stackUpgrade::diamondMultiplier)

    @JvmField
    val stackUpgradeTier4 = StackUpgradeItem("stack_upgrade_tier_4", Config.stackUpgrade::obsidianMultiplier)

    @JvmField
    val inceptionUpgrade = InceptionUpgradeItem("inception_upgrade")

    @JvmField
    val pickupUpgrade = PickupUpgradeItem("pickup_upgrade", ::PickupUpgradeWrapper)

    @JvmField
    val advancedPickupUpgrade = PickupUpgradeItem("advanced_pickup_upgrade", ::AdvancedPickupUpgradeWrapper)

    @JvmField
    val feedingUpgrade = FeedingUpgradeItem("feeding_upgrade", ::FeedingUpgradeWrapper)

    @JvmField
    val advancedFeedingUpgrade = FeedingUpgradeItem("advanced_feeding_upgrade", ::AdvancedFeedingUpgradeWrapper)

    @JvmField
    val depositUpgrade = DepositUpgradeItem("deposit_upgrade", ::DepositUpgradeWrapper)

    @JvmField
    val advancedDepositUpgrade = DepositUpgradeItem("advanced_deposit_upgrade", ::AdvancedDepositUpgradeWrapper)

    @JvmField
    val restockUpgrade = RestockUpgradeItem("restock_upgrade", ::RestockUpgradeWrapper)

    @JvmField
    val advancedRestockUpgrade = RestockUpgradeItem("advanced_restock_upgrade", ::AdvancedRestockUpgradeWrapper)

    @JvmField
    val filterUpgrade = FilterUpgradeItem("filter_upgrade", ::FilterUpgradeWrapper)

    @JvmField
    val advancedFilterUpgrade = FilterUpgradeItem("advanced_filter_upgrade", ::AdvancedFilterUpgradeWrapper)
}
